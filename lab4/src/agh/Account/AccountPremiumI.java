package agh.Account;

import Banking.*;
import com.zeroc.Ice.Current;
import agh.Bank.BankI;

public class AccountPremiumI extends AccountStandardI implements AccountPremium {

    public AccountPremiumI(Person person, String password, float declaredIncome, BankI bank) {
        super(person, password, declaredIncome, bank);
    }

    @Override
    public Credit takeCredit(Currency currency, float amount, int days, Current current) throws UnsupportedCurrencyException {
        Float rate = bank.getExchangeRateOf(currency);
        if (!bank.getCurrencies().contains(currency)) {
            System.out.println("Unsupported currency");
            throw new UnsupportedCurrencyException(currency + " currency is not supported in this bank");
        }
        float amountPerDay = amount / days;
        float amountForeign = rate * amountPerDay;
        this.setBalance(amount);
        return new Credit(
                bank.getNativeCurrency(),
                amountPerDay + BankI.creditPercentage * amountPerDay,
                currency,
                amountForeign + BankI.creditPercentage * amountForeign);
    }
}
