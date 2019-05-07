package agh.Bank;

import Banking.Currency;
import ExchangeRateService.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceClient extends Thread {

    private final ManagedChannel channel;
    private final CurrencyGrpc.CurrencyBlockingStub currBlockingStub;
    private BankI bank;

    public ServiceClient(String host, int port, BankI bank) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
        currBlockingStub = CurrencyGrpc.newBlockingStub(channel);
        this.bank = bank;
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            downloadCurrentExchangeRate();
        } finally {
            try {
                shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadCurrentExchangeRate() {
        List<Integer> foreignCurrencies = new LinkedList<>();
        Currency bankNativeCurrency = bank.getNativeCurrency();
        for (Currency currency : bank.getCurrencies()) {
            if (currency.value() != bankNativeCurrency.value()) {
                foreignCurrencies.add(currency.value());
            }
        }
        CurrencyOuterClass.SubscribedCurrencies request =
                CurrencyOuterClass.SubscribedCurrencies
                        .newBuilder()
                        .setNativeCurrencyValue(bankNativeCurrency.value())
                        .addAllForeignCurrenciesValue(foreignCurrencies)
                        .build();
        Iterator<CurrencyOuterClass.CurrencyValue> currencies;
        try {
            currencies = currBlockingStub.downloadCurrencies(request);
            while (currencies.hasNext()) {
                CurrencyOuterClass.CurrencyValue currencyValue = currencies.next();
                Currency currency = Currency.valueOf(currencyValue.getCurrency().getNumber());
                float cost = currencyValue.getValue();
//                System.out.println("currency type: " + currency);
//                System.out.println("value: " + cost);
                bank.setCurrencyExchangeRate(currency, cost);
            }
        } catch (StatusRuntimeException ex) {
            ex.printStackTrace();
        }
    }
}
