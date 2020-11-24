import com.google.gson.JsonObject;

import java.util.Objects;

public class Account {

    private final String giroNumber;
    private double balance;
    private final String currency;

    public Account(String giroNumber, String currency, double balance) {
        this.giroNumber = giroNumber;
        this.currency = currency;
        this.balance = balance;
    }

    public void refresh(Account newAccount) {
        if (giroNumber.equals(newAccount.giroNumber)) {
            balance = newAccount.balance;
        }
    }

    static Account createNewAccountFromJson(JsonObject json) {
        String giroNumber = json.get("GIRONUMBER").getAsString();
        double amount = Double.parseDouble(json.get("AMOUNT").getAsString());
        String currency = json.get("CURRENCY").getAsString();
        return new Account(giroNumber,currency,amount);
    }

    static boolean searchAnomalyInBalance(Account newAccount, Account oldAccount) {
        double previousAmount = oldAccount.balance;
        double difference = newAccount.balance - previousAmount;
        double anomalyLimit = Account.getAnomalyLimit(newAccount.currency);
        boolean anomalyFound = (-1 * difference) > anomalyLimit;
        return anomalyFound;
    }

    public String getGiroNumber() {
        return giroNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    private static double getAnomalyLimit(String currency) {
        switch (currency) {
            case "HUF":
                return 300_000;
            case "USD":
                return 1_000;
            case "EUR":
                return 1_000;
            case "GBP":
                return 1_000;
            case "RON":
                return 4_000;
            case "PLN":
                return 4_000;
            case "CZK":
                return 20_000;
            case "RSD":
                return 100_000;
            case "BGN":
                return 1_000;
            case "HRK":
                return 6_000;
            case "SEK":
                return 8_000;
            default:
                return 1_000;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return giroNumber.equals(account.giroNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(giroNumber);
    }
}
