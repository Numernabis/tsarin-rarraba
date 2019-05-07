module Banking
{
  enum Currency { PLN, EUR, CHF };
  enum AccountType { STANDARD, PREMIUM };

  exception UnsupportedCurrencyException
  {
    string reason;
  };

  struct Account
  {
    AccountType type;
    string password;
  }

  struct Person
  {
    string name;
    string surname;
    string pesel;
  }

  struct Credit
  {
    Currency nativeCurrency;
    float nativeCurrencyCost;
    Currency foreignCurrency;
    float foreignCurrencyCost;
  }

  interface AccountStandard
  {
    float getBalance();
  };

  interface AccountPremium extends AccountStandard
  {
    Credit takeCredit(Currency currency, float amount, int days) throws UnsupportedCurrencyException;
  };

  interface Bank
  {
    Account createAccount(Person person, float declaredIncome);
    AccountStandard* signIn(string pesel);
  };
};