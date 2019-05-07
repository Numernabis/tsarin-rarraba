package agh.Bank;

import Banking.*;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.LocalException;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Client {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Bad number of arguments");
            return;
        }
        int status = 0;
        String password;
        AccountType type;
        Communicator communicator = null;
        int bankId;
        try {
            bankId = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.err.println("Incorrect arguments");
            return;
        }

        try {
            communicator = Util.initialize(args);
            ObjectPrx base = communicator.stringToProxy(
                    "bank/bank" + bankId + ":tcp -h localhost -p 1000" + bankId + " :udp -h localhost -p 1000" + bankId);
            BankPrx bank = BankPrx.checkedCast(base);
            if (bank == null) throw new Error("Invalid proxy");
            String line = null;
            String[] words;
            Map<String, String> passwordContext = new HashMap<>();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            AccountPremiumPrx accountPremium = null;
            AccountStandardPrx accountStandard = null;
            do {
                try {
                    System.out.print("==> ");
                    System.out.flush();
                    line = in.readLine();
                    if (line == null) {
                        break;
                    } else {
                        words = line.split("\\s+");
                    }
                    switch (words[0]) {
                        case "create": {
                            if (words.length < 5) {
                                System.out.println("Please provide: name surname pesel income");
                                continue;
                            }
                            Person person = new Person(words[1], words[2], words[3]);
                            float declaredIncome = Float.parseFloat(words[4]);
                            Account account = bank.createAccount(person, declaredIncome);

                            if (account.password.equals("")) {
                                System.out.println("There is already account with this pesel");
                            } else {
                                password = account.password;
                                type = account.type;
                                System.out.println("Created " + type + " account. Password: " + password);
                                //passwordContext.put("password", password);
                            }
                            break;
                        }
                        case "login": {
                            if (words.length < 3) {
                                System.out.println("Please provide: pesel password");
                                continue;
                            }
                            passwordContext.put("password", words[2]);
                            AccountStandardPrx account = bank.ice_context(passwordContext).signIn(words[1]);

                            if (AccountPremiumPrx.checkedCast(account) != null) {
                                System.out.println("Welcome " + words[1] + " (premium)");
                                accountPremium = AccountPremiumPrx.checkedCast(account);
                                accountStandard = null;
                            } else if (account != null) {
                                System.out.println("Welcome " + words[1] + " (standard)");
                                accountStandard = account;
                                accountPremium = null;
                            } else {
                                System.out.println("No account with this pesel / wrong password");
                            }
                            break;
                        }
                        case "balance":
                            if (accountPremium != null) {
                                System.out.println(accountPremium.getBalance());
                            } else {
                                System.out.println(accountStandard.getBalance());
                            }
                            break;
                        case "credit":
                            if (accountPremium == null) {
                                System.out.println("Credit is available only for premium accounts");
                                continue;
                            }
                            if (words.length < 4) {
                                System.out.println("Please provide: currency amount days");
                                continue;
                            }
                            Currency currency = Currency.valueOf(words[1]);
                            float amount = Float.parseFloat(words[2]);
                            int days = Integer.parseInt(words[3]);
                            try {
                                Credit credit = accountPremium.takeCredit(currency, amount, days);
                                System.out.println("Credit cost (per day): " + credit.nativeCurrencyCost + " " +
                                        credit.nativeCurrency + " / " + credit.foreignCurrencyCost + " " + currency);
                            } catch (UnsupportedCurrencyException ex) {
                                System.err.println(ex);
                            }
                            break;
                        default:
                            System.err.println("Bad method. Available: create, login, balance, credit");
                            break;
                    }

                } catch (java.io.IOException ex) {
                    System.err.println(ex);
                }
            }
            while (!line.equals("x"));

        } catch (LocalException e) {
            e.printStackTrace();
            status = 1;
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
