package agh.Account;

import Banking.AccountStandard;
import Banking.Person;
import com.zeroc.Ice.Current;
import agh.Bank.BankI;

public class AccountStandardI implements AccountStandard {

    protected Person person;
    protected String password;
    protected float declaredIncome;
    protected float balance = 0.0f;
    protected BankI bank;

    public AccountStandardI(Person person, String password, float declaredIncome, BankI bank) {
        this.person = person;
        this.password = password;
        this.declaredIncome = declaredIncome;
        this.bank = bank;
    }

    @Override
    public float getBalance(Current current) {
        return this.balance;
    }

    public void setBalance(float income) {
        this.balance += income;
    }

    public String getPesel() {
        return this.person.pesel;
    }

    public String getPassword() {
        return this.password;
    }
}
