import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Account {

    private final String giroNumber;
    private double balance;
    private double balanceInEur;
    private final String currency;
    private long valid_from;
    private long valid_to;

    private double averageOfExpenses; //- negative change in balance
    private int numberOfExpenses;

    private Account(String giroNumber, String currency, double balance, double balanceInEur, String valid_from, String valid_to) {
        this.giroNumber = giroNumber;
        this.currency = currency;
        this.balance = balance;
        this.balanceInEur = balanceInEur;
        this.valid_from = CustomDateFormatter.getTimeStampFromDateInSeconds(valid_from);
        this.valid_to = CustomDateFormatter.getTimeStampFromDateInSeconds(valid_to);

        averageOfExpenses = 0;
        numberOfExpenses = 0;
    }

    private void refreshAverageOfExpenses(double newExpenseValue) {
        averageOfExpenses += newExpenseValue;
        numberOfExpenses += 1;
        averageOfExpenses = averageOfExpenses / numberOfExpenses;
    }

    public void refresh(Account newAccount) {
        if (giroNumber.equals(newAccount.giroNumber)) {
            balance = newAccount.balance;
            valid_from = newAccount.valid_from;
            valid_to = newAccount.valid_to;
            refreshAverageOfExpenses(newAccount.balance - balance);
        }
    }

    static Account createNewAccountFromJson(JsonObject json) {
        String giroNumber = json.get("GIRONUMBER").getAsString();
        double amount = Double.parseDouble(json.get("AMOUNT").getAsString());
        double amountInEur = Double.parseDouble(json.get("AMOUNT_EUR").getAsString());
        String currency = json.get("CURRENCY").getAsString();
        String valid_from_date = json.get("VALID_FROM").getAsString();
        String valid_to_date = json.get("VALID_TO").getAsString();
        return new Account(giroNumber,currency,amount, amountInEur, valid_from_date, valid_to_date);
    }

    public boolean searchPatternInExpenses(Account newAccount) {
        boolean anomaly = false;
        double difference = newAccount.balance - balance;
        if (difference < 0) { // only for withdraws
            double deviation = Math.pow(difference - Math.abs(averageOfExpenses),2);
            if (deviation > Math.abs(averageOfExpenses) + 0.5 * getAnomalyLimit(currency)) anomaly = true; // Predefined threshold
        }
        return anomaly;
    }

    public static boolean zscoreOutlier(Account account, Double lastWindowMean, Double lastWindowStdDev, Integer thresh) {
        boolean anomaly = false;
        double zscore = (account.balanceInEur - lastWindowMean)/lastWindowStdDev;
        //System.out.println(zscore);
        if (Math.abs(zscore) > thresh) { // Predefined threshold
            anomaly = true;
        }
        return anomaly;
    }

    public static boolean searchAnomalyInBalance(Account newAccount, Account oldAccount) {
//        double previousAmount = oldAccount.balance;
        double previousAmount = oldAccount.balanceInEur;
//        double difference = newAccount.balance - previousAmount;
        double difference = newAccount.balanceInEur - previousAmount;
//        double anomalyLimit = Account.getAnomalyLimit(newAccount.currency);
        double anomalyLimit = Account.getAnomalyLimit("EUR");
        boolean anomalyFound = (-1 * difference) > anomalyLimit;
        return anomalyFound;
    }

    public static boolean searchAnomalyInAccountSequenceByDate(Account newAccount, Account oldAccount) {
        long timeDifference = newAccount.valid_from - oldAccount.valid_to;
        if (timeDifference < 0 || timeDifference > CustomDateFormatter.ONE_YEAR) return true;
        return false;
    }

    public boolean searchAnomalyInCurrentAccountTimeStamp() {
        boolean anomalyFound = false;
        if (valid_from < 0 || valid_to < 0) anomalyFound = true;
        long time_slot = getDifferenceInTime();
        if (time_slot < 0 || time_slot > CustomDateFormatter.ONE_DAY) anomalyFound = true;
        return anomalyFound;
    }

    private long getDifferenceInTime() {
        return valid_to - valid_from;
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

    public long getValid_from() {
        return valid_from;
    }

    public long getValid_to() {
        return valid_to;
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
