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
    private static Map<String, Account> accounts = new HashMap<>();

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

                    Account account = Account.createNewAccountFromJson(payload);
                    String key = account.getGiroNumber();
                    boolean anomalyFound = false;

                    if (accounts.containsKey(key)) {
                        Account oldAccount = accounts.get(key);
                        anomalyFound = account.searchAnomalyInCurrentAccountTimeStamp() ||
                                       Account.searchAnomalyInBalance(account,oldAccount) ||
                                       Account.searchAnomalyInAccountSequenceByDate(account,oldAccount) ||
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

}
