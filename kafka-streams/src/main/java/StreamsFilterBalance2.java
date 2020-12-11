import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StreamsFilterBalance2 {

    private static JsonParser jsonParser = new JsonParser();
    private static Map<String, Account> accounts = new HashMap<>();
    private static Map<String, HashMap<String, Double>> exchangeRates = new HashMap<>();
    private static String exchangeRateCsvFilePath = "/Users/joe/Documents/ANDROID/idea/AnomalyDetection/kafka-connect/exchange.csv";

    public static void main(String[] args) throws IOException {

        prepareExchangeRates();

        // create properties
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "balance-streams-"+System.currentTimeMillis());
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");

        // create a topology
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // input topic
        KStream<String, String> inputTopic = streamsBuilder.stream("balance_topic");

        KStream<String, String> updatedStream = inputTopic.mapValues(new ValueMapper<String, String>() {
            @Override
            public String apply(String jsonValue) {
                JsonObject jsonObject = jsonParser.parse(jsonValue).getAsJsonObject();
                String currency = jsonObject.get("payload").getAsJsonObject().get("CURRENCY").getAsString();
                if (!currency.equals("EUR")) {
                    String validFromAsString = jsonObject.get("payload").getAsJsonObject().get("VALID_FROM").getAsString();
                    Long validFrom = CustomDateFormatter.getTimeStampFromDateInSeconds(validFromAsString);
                    Date date = new Date(validFrom * 1000);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    int tries_count = 0;
                    while (!exchangeRates.keySet().contains(sdf.format(date))) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.add(Calendar.DATE, -1);
                        date = calendar.getTime();
                        tries_count++;
                        if (tries_count == 30) {
                            break;
                        }
                    }
                    if (exchangeRates.keySet().contains(sdf.format(date))) {
                        Double rate = exchangeRates.get(sdf.format(date)).get(currency);
                        Double amount = Double.parseDouble(jsonObject.get("payload").getAsJsonObject().get("AMOUNT").getAsString());
                        amount = amount / rate;
                        jsonObject.get("payload").getAsJsonObject().addProperty("AMOUNT", amount);
                        jsonObject.get("payload").getAsJsonObject().addProperty("CURRENCY", "EUR");
                    }
                }
                String result = new Gson().toJson(jsonObject);
                return result;
            }
        });

        KStream<String, String> filteredStream = updatedStream.filter(
                (k, jsonValue) -> {

                    JsonObject payload = jsonParser.parse(jsonValue)
                            .getAsJsonObject()
                            .get("payload")
                            .getAsJsonObject();

                    Account account = Account.createNewAccountFromJson(payload);
                    String key = account.getGiroNumber();
                    boolean anomalyFound = false;

                    if (accounts.containsKey(key)) {
                        Account oldAccount = accounts.get(key);
                        anomalyFound = account.searchAnomalyInCurrentAccountTimeStamp() ||
                                Account.searchAnomalyInBalance(account, oldAccount) ||
                                Account.searchAnomalyInAccountSequenceByDate(account, oldAccount) ||
                                oldAccount.searchPatternInExpenses(account);
                        oldAccount.refresh(account);
                    } else {
                        accounts.put(key, account);
                    }
                    return anomalyFound;

                }
        );
        filteredStream.to("filtered_balance_topic");

        // build topology
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);

        // start our streams application
        kafkaStreams.start();

    }

    private static void prepareExchangeRates() throws IOException {
        exchangeRates = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(exchangeRateCsvFilePath))) {
            String line;
            String[] keys = null;
            boolean firstLine = false;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (keys == null) {
                    keys = values;
                    continue;
                } else {
                    String date = values[0];
                    HashMap<String, Double> rates = new HashMap();
                    for (int i = 1; i < values.length; i++) {
                        rates.put(keys[i], Double.parseDouble(values[i]));
                    }
                    exchangeRates.put(date, rates);
                }
            }
        }
    }

}
