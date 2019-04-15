#!/usr/bin/env python
import pika
import sys
from termcolor import colored
from threading import Thread

exchange_1 = "examination"
exchange_2 = "announcement"
uicolor = "cyan"

def log(msg):
    print(colored("[{}]".format(admin_name), uicolor) + " {}".format(msg))

def setup_receiver():    
    # examination exchange
    channel.exchange_declare(
        exchange = exchange_1,
        exchange_type = "topic")       
         
    result = channel.queue_declare("", exclusive = True)
    queue_name = result.method.queue

    channel.queue_bind(
        exchange = exchange_1,
        queue = queue_name,
        routing_key = "*.result.*")

    channel.queue_bind(
        exchange = exchange_1,
        queue = queue_name,
        routing_key = "*.order.*")
                      
    channel.basic_consume(
        queue = queue_name,
        auto_ack = True,
        on_message_callback = log_callback)

    # announcement exchange
    channel.exchange_declare(
        exchange = exchange_2,
        exchange_type = "fanout")
    
    # consuming on another thread    
    consume_thread = Thread(target = channel.start_consuming)
    consume_thread.start()

def log_callback(channel, method, properties, body):
    body = body.decode()
    log("{} : {}".format(method.routing_key, body))
    #channel.basic_ack(delivery_tag = method.delivery_tag)

# -----------------------------------------------------------------

if len(sys.argv) < 2:
    exit("admin.py NAME")

admin_name = sys.argv[1]

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host = "localhost"))
channel = connection.channel()

setup_receiver()
print(colored("ADMIN {} started working".format(admin_name), uicolor))
log("waiting for logs or message to broadcast")

while True:
    msg = input()
    if msg == "exit":
        connection.close()
        exit()
    channel.basic_publish(
        exchange = exchange_2,
        routing_key = "info",
        body = "{} : {}".format(admin_name, msg))
    log("announcement : {}".format(msg))

# -----------------------------------------------------------------