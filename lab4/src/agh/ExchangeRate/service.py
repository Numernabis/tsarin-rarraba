import currency_pb2_grpc
import currency_pb2
import grpc
from concurrent import futures
import time
import random


class CurrencyServicer(currency_pb2_grpc.CurrencyServicer):
    def DownloadCurrencies(self, request, context):
        exchange_rate = {}
        currencies = request.foreignCurrencies
        currencies.append(request.nativeCurrency)
        for currency in currencies:
            if currency == request.nativeCurrency:
                exchange_rate[currency] = 1.0
            else:
                exchange_rate[currency] = random.uniform(1.0, 4.0)
            value = exchange_rate[currency]
            print(currency, value)
            yield currency_pb2.CurrencyValue(currency=currency, value=value)

        while True:
            time.sleep(5)
            random_currency = random.choice(currencies)
            delta = random.uniform(-0.5, 0.5)
            exchange_rate[random_currency] += delta
            if random_currency != request.nativeCurrency:
                value = exchange_rate[random_currency]
                print(random_currency, value)
                yield currency_pb2.CurrencyValue(currency=random_currency, value=value)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    currency_pb2_grpc.add_CurrencyServicer_to_server(
        CurrencyServicer(), server)
    server.add_insecure_port('[::1]:50051')
    server.start()
    print("server started")
    try:
        while True:
            time.sleep(10)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    serve()
