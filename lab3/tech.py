#!/usr/bin/env python
import pika
import sys
import time

exchange_1 = 'examination'
exchange_2 = 'announcement'
procedures = ('knee', 'hip', 'elbow')

def log(msg):
    print('[{}] {}'.format(tech_name, msg))

def setup_queue(exchange, name, key, callback):
    channel.queue_declare(
        queue = name)
        #durable = True)
    
    channel.queue_bind(
        exchange = exchange,
        queue = name,
        routing_key = key)
    
    channel.basic_consume(
        queue = name,
        on_message_callback = callback)

def order_callback(channel, method, properties, body):
    doctor, _, proc = method.routing_key.split('.')
    patient = body
    duration = len(body)
    
    log('{} : {}, {} procedure'.format(doctor, patient, proc))
    time.sleep(duration)
    log('{}, {} procedure - done'.format(patient, proc))
    channel.basic_ack(delivery_tag = method.delivery_tag)
    
    channel.basic_publish(
        exchange = exchange_1,
        routing_key = '{}.result.{}'.format(doctor, tech_name),
        body = '{} : {}, {} procedure - done in {} seconds'.format(tech_name, patient, proc, duration),
        properties = pika.BasicProperties(
            delivery_mode = 2
        ))

def info_callback(channel, method, properties, body):
    log(body)

# -----------------------------------------------------------------

if len(sys.argv) < 3:
    exit('tech.py NAME PROC1 [PROC2]')

tech_name = sys.argv[1]
tech_procs = []

for arg in sys.argv[2:4]:
    if arg in procedures:
        tech_procs.append(arg)
    else:
        exit('Unknown med. procedure {}'.format(arg))

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host = 'localhost'))

channel = connection.channel()

# examination exchange
channel.exchange_declare(
    exchange = exchange_1,
    exchange_type = 'topic')

for proc in tech_procs:
    queue_name = 'techs-{}'.format(proc)
    key = '*.order.{}'.format(proc)
    setup_queue(exchange_1, queue_name, key, order_callback)
    channel.basic_qos(prefetch_count = 1)

# announcement exchange
channel.exchange_declare(
    exchange = exchange_2,
    exchange_type = 'fanout')

queue_name = 'info-{}'.format(tech_name)
key = 'info'
setup_queue(exchange_2, queue_name, key, info_callback)

# ----------------------------------------------------------------
log('Waiting for orders...')
try:
    channel.start_consuming()
except KeyboardInterrupt:
    channel.stop_consuming()
    print('\n')
    exit()
