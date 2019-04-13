#ifndef COMMON_H
#define COMMON_H

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <sys/un.h>
#include <sys/epoll.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>

#define MAX_NAME 20
#define MAX_MSG  100

#define MESSAGE 1
#define LOGIN   2
#define LOGOUT  3

#define LOCALHOST "127.0.0.1"
#define MCAST_IPAD "237.6.6.6"
#define MCAST_PORT 5678

#define RED "\e[1;31m"
#define GRE "\e[1;32m"
#define YEL "\e[1;33m"
#define RST "\e[0m"

typedef struct Token {
    int type;
    int is_free;
    char dest[MAX_NAME];
    char src[MAX_NAME];
    char msg[MAX_MSG];
    /* optional */
    in_port_t old_port;
    in_port_t new_port;
    in_addr_t new_address;
} Token;

#endif
