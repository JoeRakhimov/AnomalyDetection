#1. Download docker, set RAM to 4GB from 2GB in docker settings

#2. Get elasticsearch docker image
docker pull docker.elastic.co/elasticsearch/elasticsearch:7.10.1

#3. start elasticsearch container with this command
docker run -d --rm -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name elasticsearch docker.elastic.co/elasticsearch/elasticsearch:7.10.1

#4. Get kibana docker image
docker pull docker.elastic.co/kibana/kibana:7.10.1

#5 start kibana container with this command, --link elasticsearch will connect it to the container named elasticsearch:
docker run -d --rm -p 5601:5601 -e "discovery.type=single-node" --name kibana --link elasticsearch docker.elastic.co/kibana/kibana:7.10.1

#Kibana will be available on: localhost:5601

#6 Create index "balance" in elasticsearch:
curl -X PUT "localhost:9200/balance?pretty"

#7 Create index "filtered_balance" in elasticsearch:
curl -X PUT "localhost:9200/filtered_balance?pretty"

#8 View index, or go to localhost:9200 in a browser
curl localhost:9200/balance/_search?pretty

#9 in intellij change port to 9200, and https to http: new HttpHost(hostname, 9200, "http"))
#elasticsearch.properties:
hostname=127.0.0.1
username=elastic
password=changeme


This part is not working completely yet:
docker exec -i -t -u root $(docker ps | grep docker_kafka | cut -d' ' -f1) /bin/bash