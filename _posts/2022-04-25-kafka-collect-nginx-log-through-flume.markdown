---
layout: post
title:  "Kafka collect nginx log through flume"
date:   2022-04-25 22:22:20 +0800
categories: bigdata
tags:
    - Bigdata 
    - Kafka
    - Flume
---

## 1. Create topic in Kafka

```shell
$ kafka-topics.sh --create \
--zookeeper hadoop001:2181 \
--replication-factor 2 \
--partitions 3 \ 
--topic gt_flume_data
```

## 2. Create flume configure file
Specify the nginx log as source, and kafka as sink:
```shell
[apache-flume-1.8.0-bin]$ vim conf/kafka-flume.conf
a1.sources = r1
a1.sinks = k1
a1.channels = c1
# Describe/configure the source
a1.sources.r1.type = exec
a1.sources.r1.command = tail -F /var/log/nginx/access.log
# Describe the sink
#a1.sinks.k1.type = logger
a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.topic = gt_flume_data
a1.sinks.k1.brokerList = hadoop001:9092,hadoop002:9092,hadoop003:9092
a1.sinks.k1.requiredAcks = 1
a1.sinks.k1.batchSize = 20
# Use a channel which buffers events in memory
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100
# Bind the source and sink to the channel
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

## 3. Start Flume

```shell
[apache-flume-1.8.0-bin] $ ./bin/flume-ng agent -c conf/ \
-f conf/kafka-flume.conf \
-n a1 -Dflume.root.logger=INFO,console 
```

## 4. Consume nginx log in Kafka
```shell
$ kafka-console-consumer.sh --topic gt_flume_data --bootstrap-server hadoop001:9092
101.87.89.250 - - [25/Apr/2022:23:11:36 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36" "-"
101.87.89.250 - - [25/Apr/2022:23:11:38 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36" "-"
101.87.89.250 - - [25/Apr/2022:23:12:34 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36" "-"
193.46.255.49 - - [25/Apr/2022:22:39:43 +0800] "GET / HTTP/1.1" 200 4833 "-" "libwww-perl/6.62" "-"
```

