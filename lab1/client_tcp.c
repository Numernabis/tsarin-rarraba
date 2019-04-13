#include "common.h"

char* client_name;
int port_to_listen;
in_addr_t neighbour_address;
int neighbour_port;
int first_token;

int epoll_fd;
int in_socket;
int out_socket;
int log_socket;
Token token;

char msg_text[100];
char msg_dest[20];
int msg_waits;
int has_token;
int after_logout;

/* -------------------------------------------------------------------------- */
void init_tcp_in_socket() {
    in_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (in_socket == -1) {
        printf(YEL"in_socket error\n"RST);
        exit(1);
    }

    struct sockaddr_in addr;
    bzero(&addr, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    addr.sin_port = htons(port_to_listen);

    int b = bind(in_socket, (const struct sockaddr*) &addr, sizeof(addr));
    if (b == -1) {
        printf(YEL"bind error\n"RST);
        exit(1);
    }
    listen(in_socket, 64);

    printf("[%s]: init_tcp_in_socket success\n", client_name);
}

void init_tcp_out_socket() {
    if (out_socket != 0) {
        shutdown(out_socket, SHUT_RDWR);
        close(out_socket);
    }

    out_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (out_socket == -1) {
        printf(YEL"socket error\n"RST);
        exit(1);
    }

    struct sockaddr_in addr;
    bzero(&addr, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = inet_addr(LOCALHOST);
    addr.sin_port = htons(neighbour_port);

    int c = connect(out_socket, (const struct sockaddr*) &addr, sizeof(addr));
    if (c == -1) {
        printf(YEL"connect error\n"RST);
        exit(1);
    }

    printf("[%s]: init_tcp_out_socket success\n", client_name);
}

void init_log_socket() {
    log_socket = socket(AF_INET, SOCK_DGRAM, 0);
    if (log_socket == -1) {
        printf(YEL"log_socket error\n"RST);
        exit(1);
    }
}

void init_epoll_monitor() {
    epoll_fd = epoll_create1(0);

    struct epoll_event epoll_event1;
    epoll_event1.events = EPOLLIN | EPOLLPRI;
    epoll_event1.data.fd = -in_socket;
    epoll_ctl(epoll_fd, EPOLL_CTL_ADD, in_socket, &epoll_event1);

    printf("[%s]: init_epoll_monitor success\n", client_name);
}

void register_socket(int socket) {
    int new_client = accept(socket, NULL, NULL);

    struct epoll_event epoll_event1;
    epoll_event1.events = EPOLLIN | EPOLLPRI;
    epoll_event1.data.fd = new_client;
    epoll_ctl(epoll_fd, EPOLL_CTL_ADD, new_client, &epoll_event1);

    printf("[%s]: register_socket(%d) success\n", client_name, socket);
}

void remove_socket(int socket) {
    epoll_ctl(epoll_fd, EPOLL_CTL_DEL, socket, NULL);
    shutdown(socket, SHUT_RDWR);
    close(socket);
    printf("[%s]: remove_socket(%d) success\n", client_name, socket);
}
/* -------------------------------------------------------------------------- */
void handle_token(int);
void input_from_cmd();
void send_message();
void put_message();
void handle_message();
void handle_loginout(char*);
void handle_free();
void forward_token();
void clear_token();
void send_log(char*, int);
void handle_sigint(int);
void clean_up(void);

/* -------------------------------------------------------------------------- */
int main(int argc, char** argv) {
    if (argc != 6) {
        printf(YEL"Invalid program call. Proper arguments as follows:\n");
        printf("./client_tcp  client_name  port_to_listen  ");
        printf("neighbour_address  neighbour_port  has_token\n"RST);
        return 2;
    }

    /* parse arguments */
    client_name = argv[1];
    port_to_listen = (int) strtoul(argv[2], NULL, 0);
    inet_aton(argv[3], (struct in_addr*) &neighbour_address);
    //neighbour_address = argv[3];
    neighbour_port = (int) strtoul(argv[4], NULL, 0);
    first_token = (int) strtoul(argv[5], NULL, 0);

    /* init sockets */
    init_tcp_in_socket();
    init_tcp_out_socket();
    init_log_socket();
    init_epoll_monitor();

    /* signals */
    signal(SIGINT, handle_sigint); //ctrl+c
    signal(SIGTSTP, input_from_cmd); //ctrl+z
    atexit(clean_up);

    /* login client */
    clear_token();
    strcpy(token.src, client_name);
    token.type = LOGIN;
    token.old_port = neighbour_port;
    token.new_port = port_to_listen;
    //inet_aton(LOCALHOST, &token.new_address);
    token.new_address = inet_addr(LOCALHOST);
    write(out_socket, &token, sizeof(token));
    printf("[%s]: LOGIN send\n", client_name);

    /* ---------------------------------------------------------------------- */
    struct epoll_event event;
    while (1) {
        epoll_wait(epoll_fd, &event, 1, -1);

        if (event.data.fd < 0) {
            register_socket(-event.data.fd);
        } else {
            handle_token(event.data.fd);
        }

        if (first_token || after_logout) {
            strcpy(msg_dest, "kx");
            strcpy(msg_text, "xx");
            send_message();
            first_token = 0;
            after_logout = 0;
        }
    }
    /* ---------------------------------------------------------------------- */
    return 0;
}
/* -------------------------------------------------------------------------- */
void handle_token(int socket) {
    ssize_t bytes_read = read(socket, &token, sizeof(token));
    if (bytes_read != sizeof(token)) {
        remove_socket(socket);
        return;
    }
    //log
    send_log(client_name, strlen(client_name));
    has_token = 1;
    sleep(1);

    switch (token.type) {
        case MESSAGE:
            handle_message();
            break;
        case LOGIN:
            handle_loginout("in");
            break;
        case LOGOUT:
            handle_loginout("out");
            break;
        default:
            handle_free();
            break;
    }
    if (has_token) forward_token();
}

void input_from_cmd() {
    if (msg_waits) {
        printf(YEL"there is already a message waiting to send\n"RST);
        return;
    }
    memset(msg_dest, 0, 20);
    memset(msg_text, 0, 100);

    printf("\nenter destination ID: \n");
    fgets(msg_dest, 20, stdin);
    msg_dest[strlen(msg_dest) - 1] = '\0';
    printf("enter message text: \n");
    fgets(msg_text, 100, stdin);
    msg_text[strlen(msg_text) - 1] = '\0';

    msg_waits = 1;
    printf("ok. your message waits to be send.\n");
}

void send_message() {
    clear_token();
    put_message();
    printf(GRE"start msg send to %s\n"RST, token.dest);
    forward_token();
}

void put_message() {
    strcpy(token.src, client_name);
    strcpy(token.dest, msg_dest);
    strcpy(token.msg, msg_text);
    token.type = MESSAGE;
    token.is_free = 0;
    msg_waits = 0;
}

void handle_message() {
    if (strcmp(token.dest, client_name) == 0) {
        printf(GRE"msg received from %s:  %s\n"RST, token.src, token.msg);
        clear_token();
        token.is_free = 1;
    }
    if (strcmp(token.src, client_name) == 0) {
        printf(GRE"msg not received. host unreachable\n"RST);
        clear_token();
        token.is_free = 1;
    }
    handle_free();
}

void handle_loginout(char* action) {
    if (token.old_port == neighbour_port) {
        printf(GRE"log%s received from %s\n", action, token.src);
        printf("changing my neighbour_port to %d\n"RST, token.new_port);
        neighbour_port = token.new_port;
        //inet_ntop(AF_INET, &token.new_address, neighbour_address, 20);
        neighbour_address = token.new_address;
        has_token = 0;
        clear_token();
        init_tcp_out_socket();
        if (strcmp(action, "out") == 0) {
            after_logout = 1;
        }
    }
}

void handle_free() {
    if (token.is_free && msg_waits) {
        put_message();
        printf(GRE"msg send to %s\n"RST, token.dest);
    }
}

void forward_token() {
    ssize_t bytes_send = write(out_socket, &token, sizeof(token));
    if (bytes_send != sizeof(token)) {
        printf(YEL"write error\n"RST);
    }
}

void clear_token() {
    memset(token.msg, 0, 100);
    memset(token.dest, 0, 20);
    memset(token.src, 0, 20);
    token.type = 0;
    token.is_free = 0;
    token.old_port = 0;
    token.new_port = 0;
    token.new_address = 0;
}

void send_log(char* message, int size) {
    struct sockaddr_in addr;
    bzero(&addr, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = inet_addr(MCAST_IPAD);
    addr.sin_port = htons(MCAST_PORT);

    int st = sendto(log_socket, message, size, 0, (struct sockaddr*) &addr, sizeof(addr));
    if (st < 0) {
        printf(YEL"sendto error\n"RST);
        exit(1);
    }
}
/* -------------------------------------------------------------------------- */
void handle_sigint(int signum) {
    printf(RED"\nReceived SIG=%d -- closing\n"RST, signum);
    exit(EXIT_SUCCESS);
}

void clean_up(void) {
    /* logout client */
    clear_token();
    strcpy(token.src, client_name);
    token.type = LOGOUT;
    token.old_port = port_to_listen;
    token.new_port = neighbour_port;
    //inet_aton(neighbour_address, &token.new_address);
    token.new_address = neighbour_address;
    write(out_socket, &token, sizeof(token));
    printf("[%s]: LOGOUT send\n", client_name);

    close(epoll_fd);
    shutdown(in_socket, SHUT_RDWR);
    close(in_socket);
    shutdown(out_socket, SHUT_RDWR);
    close(out_socket);
    shutdown(log_socket, SHUT_RDWR);
    close(log_socket);
}
/* -------------------------------------------------------------------------- */
