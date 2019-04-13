#Define variables
CC= gcc
CFLAGS= -Wall -std=gnu99 -lpthread
.PHONY: clean

#Default rule
all: client_tcp client_udp

client_tcp: client_tcp.c
	$(CC) -o $@ $< $(CFLAGS)

client_udp: client_udp.c
	$(CC) -o $@ $< $(CFLAGS)

clean:
	rm -f *.o
#---------------------------------------
