#!/usr/bin/env python
import pika
import sys
from termcolor import colored
from threading import Thread

exchange_1 = "examination"
exchange_2 = "announcement"
procedures = ("knee", "hip", "elbow")
uicolor = "magenta"

def log(msg):
    print(colored("[{}]".format(doc_name), uicolor) + " {}".format(msg))

def setup_queue(exchange, name, key, callback):
    channel.queue_declare(
        queue = name)
    
    channel.queue_bind(
        exchange = exchange,
        queue = name,
        routing_key = key)
    
    channel.basic_consume(
        queue = name,
        auto_ack = True,
        on_message_callback = callback)

def setup_receiver():
    # examination exchange
    channel.exchange_declare(
        exchange = exchange_1,
        exchange_type = "topic")

    queue_name = "doc-{}".format(doc_name)
    key = "{}.result.*".format(doc_name)
    setup_queue(exchange_1, queue_name, key, info_callback)

    # announcement exchange
    channel.exchange_declare(
        exchange = exchange_2,
        exchange_type = "fanout")

    result = channel.queue_declare('', exclusive = True)
    queue_name = result.method.queue

    channel.queue_bind(
        exchange = exchange_2,
        queue = queue_name,
        routing_key = "info")
            
    channel.basic_consume(
        queue = queue_name,
        auto_ack = True,
        on_message_callback = info_callback)
    
    # consuming on another thread       
    consume_thread = Thread(target = channel.start_consuming)
    consume_thread.start()

def info_callback(channel, method, properties, body):
    body = body.decode()
    log(body)
        
# -----------------------------------------------------------------

if len(sys.argv) < 2:
    exit("doc.py NAME")

doc_name = sys.argv[1]

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host = "localhost"))
channel = connection.channel()

setup_receiver()
print(colored("DOCTOR {} started working".format(doc_name), uicolor))
log("waiting for orders: PATIENT-PROCEDURE")

while True:
    order = input()
    if order == "exit":
        connection.close()
        exit()
    if "-" not in order:
        log("bad order format")
        continue        

    patient, proc = order.split("-")
    if proc in procedures:
        channel.basic_publish(
            exchange = exchange_1,
            routing_key = "{}.order.{}".format(doc_name, proc),
            body = patient,
            properties = pika.BasicProperties(
                delivery_mode = 2
            ))
        log("examination order: {}, {} procedure".format(patient, proc))
    else:
        log("no such procedure")

# -----------------------------------------------------------------