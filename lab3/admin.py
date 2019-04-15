#!/usr/bin/env python
import pika
import sys
from threading import Thread

exchange_1 = 'examination'
exchange_2 = 'announcement'

def log(msg):
    print('[{}] {}'.format(tech_name, msg))

def receive():
    connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
    channel = connection.channel()

    def callback(channel, method, properties, body):
        if method.routing_key == '{}'.format(admin_name) and body == 'exit':
            channel.stop_consuming()
        else:
            log('{} : {}'.format(method.routing_key, body))
    
    channel.exchange_declare(exchange=exchange_name,
                             exchange_type='topic')        
         
    result = channel.queue_declare('', exclusive=True)
    queue_name = result.method.queue

    channel.queue_bind(exchange = exchange_1,
                       queue = queue_name,
                       routing_key = '*.result.*')
    channel.queue_bind(exchange = exchange_1,
                       queue = queue_name,
                       routing_key = '*.order.*')
                       
    channel.queue_bind(exchange = exchange_1,
                       queue = queue_name,
                       routing_key = '{}'.format(admin_name))
                      
    channel.basic_consume(
            queue = queue_name,
            auto_ack = True,
            on_message_callback = callback)
            
    channel.start_consuming()

# -----------------------------------------------------------------

if len(sys.argv) < 2:
    exit('admin.py NAME')

admin_name = sys.argv[1]

receive_thread = Thread(target=receive)
receive_thread.start()

connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
channel = connection.channel()

while True:
    try:
        input = raw_input('> ')
        if input == 'exit':
            raise KeyboardInterrupt
        channel.basic_publish(
            exchange = exchange_2,
            routing_key = 'info',
            body = '{} : {}'.format(admin_name, input))
        log('Publish info : {}'.format(input))
    except KeyboardInterrupt:
        print('\n')
        channel.basic_publish(
            exchange = exchange_1,
            routing_key = '{}'.format(admin_name),
            body = 'exit',
            properties = pika.BasicProperties(
                delivery_mode = 2
            ))
        connection.close()
        exit()