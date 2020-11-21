# This file contains instructions to setup environment on PC

# 1. create topic
kafka-topics --zookeeper 127.0.0.1:2181 --topic filtered_balance_topic --create --partitions 3 --replication-factor 1
# 2. start consumer
joe$ kafka-console-consumer --bootstrap-server 1270.1:9092 --topic filtered_balance_topic --from-beginning
# 3. run StreamsFilterBalance.main()

