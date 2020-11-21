# This file contains instructions to setup environment on PC
# Same instruction in video format: https://youtu.be/PhDzCsgMvf0

# A. 'kafka-connect' MODULE SETUP
# 1. modify 'balance_hist_anon.csv': rename first column to 'id'
# 2. rename 'balance_hist_anon.csv' to 'balance.csv'
# 3. copy it to 'kafka-connect/data' folder

# B. ZOOKEEPER
# 1. navigate to kafka folder. For example:
cd /Users/joe/kafka_2.13-2.6.0
# 2. start zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# C. KAFKA SERVER
# 1. navigate to kafka folder. For example:
cd /Users/joe/kafka_2.13-2.6.0
# 2. start server
kafka-server-start config/server.properties

# E. KAFKA CONNECT
# 1. navigate to 'kafka-connect' folder
cd /path/to/project/AnomalyDetection/kafka-connect
# 2. create stream from csv file
connect-standalone connect-standalone.properties balance.properties

# D. CONSUMER
# 1. start consumer
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic balance_topic --from-beginning






