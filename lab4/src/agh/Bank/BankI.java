package agh.Bank;

import Banking.*;
import com.zeroc.Ice.*;
import agh.Account.*;

import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BankI implements Bank {

    private int id;
    private ObjectAdapter adapter;
    private Currency nativeCurrency;
    private final Map<Currency, Float> supportedCurrencies;
    private Map<AccountStandardI, AccountStandardPrx> accounts = new HashMap<>();
    private static final float thresholdForPremium = 500f;
    public static final float creditPercentage = 0.05f;

    public BankI(int id, ObjectAdapter adapter, Currency nativeCurrency, Map<Currency, Float> supportedCurrencies) {
        this.id = id;
        this.adapter = adapter;
        this.nativeCurrency = nativeCurrency;
        this.supportedCurrencies = supportedCurrencies;
        ServiceClient client = new ServiceClient("localhost", 50051, this);
        client.start();
    }

    public Currency getNativeCurrency() {
        return nativeCurrency;
    }

    public Set<Currency> getCurrencies() {
        return supportedCurrencies.keySet();
    }

    public Float getExchangeRateOf(Currency currency) {
        return supportedCurrencies.get(currency);
    }

    public void setCurrencyExchangeRate(Currency currency, float cost) {
        synchronized (supportedCurrencies) {
            supportedCurrencies.replace(currency, cost);
        }
    }

    @Override
    public Account createAccount(Person person, float declaredIncome, Current current) {
        AccountStandardI accountI;
        AccountStandardPrx accountPrx;
        Account account;
        int randomInt = (int) (Math.random() * 50 + 1);
        String password = String.valueOf(randomInt);

        if (adapter.find(new Identity(person.pesel, "standard")) != null ||
                adapter.find(new Identity(person.pesel, "premium")) != null) {
            System.out.println("There is already account with this pesel");
            return null;
        }
        if (declaredIncome > thresholdForPremium) {
            accountI = new AccountPremiumI(person, password, declaredIncome, this);
            System.out.println("Created premium account");
            accountPrx = AccountPremiumPrx.checkedCast(adapter.add(accountI, new Identity(person.pesel, "premium")));
            account = new Account(AccountType.PREMIUM, password);
        } else {
            accountI = new AccountStandardI(person, password, declaredIncome, this);
            System.out.println("Created standard account");
            accountPrx = AccountStandardPrx.checkedCast(adapter.add(accountI, new Identity(person.pesel, "standard")));
            account = new Account(AccountType.STANDARD, password);
        }
        accounts.put(accountI, accountPrx);
        return account;
    }

    @Override
    public AccountStandardPrx signIn(String pesel, Current current) {
        String password = current.ctx.get("password");
        for (AccountStandardI account : accounts.keySet()) {
            if (account.getPesel().equals(pesel) && account.getPassword().equals(password)) {
                System.out.println("Current client: " + pesel + " " + password);
                return accounts.get(account);
            }
        }
        System.out.println("No matching account");
        return null;
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Bad number of arguments");
            return;
        }
        int status = 0;
        int id;
        Communicator communicator = null;
        Currency nativeCurrency;
        Map<Currency, Float> supportedCurrencies = new HashMap<>();
        try {
            id = Integer.parseInt(args[0]);
            nativeCurrency = Currency.valueOf(args[1]);
            for (int i = 1; i < args.length; i++) {
                supportedCurrencies.put(Currency.valueOf(args[i]), 1.0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Incorrect arguments");
            return;
        }

        try {
            communicator = Util.initialize();
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "Adapter" + id, "tcp -h localhost -p 1000" + id + ":udp -h localhost -p 1000" + id);
            BankI bank = new BankI(id, adapter, nativeCurrency, supportedCurrencies);
            adapter.add(bank, new Identity("bank" + id, "bank"));
            adapter.activate();
            System.out.print("BANK " + id + " | currencies: ");
            for (Currency currency : supportedCurrencies.keySet()) {
                System.out.print(currency + " ");
            }
            System.out.println("| native: " + nativeCurrency);
            System.out.println("Entering event processing loop...");
            communicator.waitForShutdown();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;
        }
        if (communicator != null) {
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
    }
}
