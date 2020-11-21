import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StreamsFilterBalance {

    private static JsonParser jsonParser = new JsonParser();
    private static Map<String, Double> balanceMap = new HashMap<>();

    public static void main(String[] args) {

        // create properties
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "balance-streams");
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

        // create a topology
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // input topic
        KStream<String, String> inputTopic = streamsBuilder.stream("balance_topic");
        KStream<String, String> filteredStream = inputTopic.filter(
                (k, jsonValue) -> {

                    JsonObject payload = jsonParser.parse(jsonValue)
                            .getAsJsonObject()
                            .get("payload")
                            .getAsJsonObject();
                    String giroNumber = payload.get("GIRONUMBER").getAsString();
                    double amount = Double.parseDouble(payload.get("AMOUNT").getAsString());
                    String currency = payload.get("CURRENCY").getAsString();

                    if (balanceMap.containsKey(giroNumber)) {
                        double previousAmount = balanceMap.get(giroNumber);
                        double difference = amount - previousAmount;
                        double anomalyLimit = getAnomalyLimit(currency);
                        boolean anomalyFound = (-1 * difference) > anomalyLimit;
                        balanceMap.put(giroNumber, amount);
                        return anomalyFound;
                    } else {
                        balanceMap.put(giroNumber, amount);
                        return false;
                    }

                }
        );
        filteredStream.to("filtered_balance_topic");

        // build topology
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);

        // start our streams application
        kafkaStreams.start();

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

}
