#!/usr/bin/env bash

cd $HOME/opt/kafka_2.11-0.9.0.0

bin/zookeeper-server-start.sh config/zookeeper.properties

bin/kafka-server-start.sh config/server.properties

 
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic names
bin/kafka-topics.sh --list --zookeeper localhost:2181


 ./bin/kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic names
