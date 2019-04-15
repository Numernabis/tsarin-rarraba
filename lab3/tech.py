#!/usr/bin/env python
import pika
import sys
import time
import random
from termcolor import colored

exchange_1 = "examination"
exchange_2 = "announcement"
procedures = ("knee", "hip", "elbow")
uicolor = "blue"

def log(msg):
    print(colored("[{}]".format(tech_name), uicolor) + " {}".format(msg))

def setup_queue(exchange, name, key, callback):
    channel.queue_declare(
        queue = name)
    
    channel.queue_bind(
        exchange = exchange,
        queue = name,
        routing_key = key)
    
    channel.basic_consume(
        queue = name,
        auto_ack = False,
        on_message_callback = callback)

def setup_receiver():
    # examination exchange
    channel.exchange_declare(
        exchange = exchange_1,
        exchange_type = "topic")

    for proc in tech_procs:
        queue_name = "techs-{}".format(proc)
        key = "*.order.{}".format(proc)
        setup_queue(exchange_1, queue_name, key, order_callback)

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

def order_callback(channel, method, properties, body):
    body = body.decode()
    doctor, _, proc = method.routing_key.split(".")
    patient = body
    duration = len(body)
    result = duration * random.randint(0,9)
    
    log("from {} : [{}, {}]".format(doctor, patient, proc))
    time.sleep(duration)
    log("to {} : [{}, {}] - [result: {}]".format(doctor, patient, proc, result))
    channel.basic_ack(delivery_tag = method.delivery_tag)
    
    channel.basic_publish(
        exchange = exchange_1,
        routing_key = "{}.result.{}".format(doctor, tech_name),
        body = "from {} : [{}, {}] - [result: {}]".format(tech_name, patient, proc, result),
        properties = pika.BasicProperties(
            delivery_mode = 2
        ))

def info_callback(channel, method, properties, body):
    body = body.decode()
    log(body)

# -----------------------------------------------------------------

if len(sys.argv) < 3:
    exit("tech.py NAME PROC1 [PROC2]")

tech_name = sys.argv[1]
tech_procs = []

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host = "localhost"))
channel = connection.channel()

for arg in sys.argv[2:4]:
    if arg in procedures:
        tech_procs.append(arg)
    else:
        exit("unknown procedure {}".format(arg))

setup_receiver()
print(colored("TECHNICIAN {} started working".format(tech_name), uicolor))
log("waiting for examination orders")

try:
    channel.start_consuming()
except KeyboardInterrupt:
    channel.stop_consuming()
    exit()

# -----------------------------------------------------------------