#!/usr/bin/env python

import socket
import struct
from datetime import datetime

MCAST_GRP  = '237.6.6.6'
MCAST_PORT = 5678
FILE       = 'log.txt'

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind((MCAST_GRP, MCAST_PORT))

mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
logfile = open(FILE,"w+")

while True:
    data, addr = sock.recvfrom(64)
    timestamp = datetime.now()
    str_timestamp = str(timestamp)[:-7]
    str_data = str(data.decode('utf-8'))
    logline = str_timestamp + ' : ' + str_data + '\n'
    logfile.write(logline)
    print(str_timestamp, str_data)
