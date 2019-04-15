#!/usr/bin/env python
import pika
import sys
from threading import Thread

exchange_1 = 'examination'
exchange_2 = 'announcement'
procedures = ('knee', 'hip', 'elbow')

def log(msg):
    print('[{}] {}'.format(doc_name, msg))

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

def receive():
    connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
    channel = connection.channel()

    def info_callback(channel, method, properties, body):
        log(body)

    def result_callback(channel, method, properties, body):
        if method.routing_key == '{0}.result.{0}'.format(doc_name) and body == 'exit':
            channel.basic_ack(delivery_tag = method.delivery_tag)
            channel.stop_consuming()
        else:
            log(body)
            channel.basic_ack(delivery_tag = method.delivery_tag)

    # examination exchange
	channel.exchange_declare(
	    exchange = exchange_1,
	    exchange_type = 'topic')

    queue_name = 'doc-{}'.format(doc_name)
    key = '{}.result.*'.format(doc_name)
    setup_queue(exchange_1, queue_name, key, result_callback)

	# announcement exchange
	channel.exchange_declare(
	    exchange = exchange_2,
	    exchange_type = 'fanout')

	queue_name = 'info-{}'.format(doc_name)
	key = 'info'
	setup_queue(exchange_2, queue_name, key, info_callback)
            
    channel.start_consuming()

# -----------------------------------------------------------------

if len(sys.argv) < 2:
    exit('doc.py NAME')

doc_name = sys.argv[1]

receive_thread = Thread(target=receive)
receive_thread.start()

connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
channel = connection.channel()


log('Waiting for orders: PATIENT:PROCEDURE')
while True:
    try:
        input = raw_input('> ')
        if input == 'exit':
            raise KeyboardInterrupt
        try:
            patient, proc = input.split(':')
            if proc in procedures:
                channel.basic_publish(
                    exchange = exchange_name,
                    routing_key = '{}.order.{}'.format(dr_name, proc),
                    body = patient,
                    properties = pika.BasicProperties(
                        delivery_mode = 2
                    ))
                log('Order: {}, {} procedure'.format(patient, proc))
        except ValueError:
            pass
    except KeyboardInterrupt:
        print('\n')
        channel.basic_publish(
            exchange = exchange_name,
            routing_key = '{0}.result.{0}'.format(dr_name),
            body = 'exit',
            properties = pika.BasicProperties(
                delivery_mode = 2
            ))
        exit()