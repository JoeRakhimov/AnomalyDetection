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

# D. KAFKA CONNECT: BALANCE DATA
# 1. modify these properties in balance.properties (do not commit it while you are making pull request)
input.path=/Users/joe/Documents/ANDROID/idea/AnomalyDetection/kafka-connect/data
finished.path=/Users/joe/Documents/ANDROID/idea/AnomalyDetection/kafka-connect/processed
error.path=/Users/joe/Documents/ANDROID/idea/AnomalyDetection/kafka-connect/error
# 2. navigate to 'kafka-connect' folder
cd /Users/joe/Documents/ANDROID/idea/AnomalyDetection/kafka-connect
# 3. create stream from csv file
connect-standalone connect-standalone.properties balance.properties

# E. CONSUMER
# 1. start consumer
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic balance_topic --from-beginning

# F. Get Twitter API credentials
# 1. Go to https://developer.twitter.com/en/docs/twitter-api
# 2. Sign up and create new app
# 3. Get credentials and place them in twitter.properties file

# G. KAFKA CONNECT: TWITTER
# 1. create new topic: twitter_status_connect
kafka-topics --zookeeper 127.0.0.1:2181 --create --topic twitter_status_connect --partitions 3 --replication-factor 1
# 2. create new topic: twitter_deletes_connect
kafka-topics --zookeeper 127.0.0.1:2181 --create --topic twitter_deletes_connect --partitions 3 --replication-factor 1
# 3. navigate to 'kafka-connect' folder
cd /Users/joe/Documents/ANDROID/idea/AnomalyDetection/kafka-connect
# 4. create stream from Twitter API
connect-standalone connect-standalone.properties twitter.properties

# H. CONSUMER
# 1. start consumer
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic twitter_status_connect







