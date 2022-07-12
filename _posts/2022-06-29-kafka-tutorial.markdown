---
layout: post
title:  "Kafka Tutorial"
date:   2022-06-29 16:01:09 +0800
categories: bigdata
tags:
    - Bigdata
    - Kafka
---

## 1. Install
Download Kafka from [Kafka.apache.org](https://kafka.apache.org/downloads){:target="_blank"}, and extract to /opt/module.
```shell
[root@hadoop001 software]# tar xvf kafka_2.11-2.4.1.tgz  -C /opt/module/
```
## 2. Configuration
Go to $KAFKA_HOME/conf/ directory, change the following options in server.properties:
```text
broker.id=0
listeners=PLAINTEXT://hadoop001:9092
log.dirs=/opt/module/kafka_2.11-2.4.1/data
zookeeper.connect=hadoop001:2181,hadoop002:2181,hadoop003:2181
```

Copy kafka folder to hadoop002/003
```shell
[root@hadoop001 module]# scp -r kafka_2.13-2.4.1/ hadoop002:`pwd`
[root@hadoop001 module]# scp -r kafka_2.13-2.4.1/ hadoop003:`pwd`
```

Change the broker.id/listeners in server.properties:  
**hadoop002**:
```text
broker.id=1
listeners=PLAINTEXT://hadoop002:9092
```

**hadoop003**:
```text
broker.id=2
listeners=PLAINTEXT://hadoop003:9092
```

Set environment:
```shell
[root@hadoop001 kafka_2.11-2.4.1]# echo export KAFKA_HOME=`pwd` > /etc/profile
[root@hadoop001 kafka_2.11-2.4.1]# echo export PATH=$PATH:$KAFKA_HOME/bin > /etc/profile
```

## 3. Start Server
```shell
[root@hadoop001 kafka_2.11-2.4.1]# source /etc/profile
[root@hadoop001 kafka_2.11-2.4.1]# kafka-server-start.sh config/server.properties 2>&1 &
[1] 399641
[root@hadoop001 kafka_2.11-2.4.1]# [2022-06-29 15:10:02,391] INFO Registered kafka:type=kafka.Log4jController MBean (kafka.utils.Log4jControllerRegistration$)
[2022-06-29 15:10:02,825] INFO Registered signal handlers for TERM, INT, HUP (org.apache.kafka.common.utils.LoggingSignalHandler)
[2022-06-29 15:10:02,825] INFO starting (kafka.server.KafkaServer)
[2022-06-29 15:10:02,831] INFO Connecting to zookeeper on hadoop001:2181,hadoop002:2181,hadoop003:2181 (kafka.server.KafkaServer)
[2022-06-29 15:10:02,856] INFO [ZooKeeperClient Kafka server] Initializing a new session to hadoop001:2181,hadoop002:2181,hadoop003:2181. (kafka.zookeeper.ZooKeeperClient)
...
[2022-06-29 15:10:04,185] INFO Kafka version: 2.4.1 (org.apache.kafka.common.utils.AppInfoParser)
[2022-06-29 15:10:04,185] INFO Kafka commitId: c57222ae8cd7866b (org.apache.kafka.common.utils.AppInfoParser)
[2022-06-29 15:10:04,185] INFO Kafka startTimeMs: 1656486604184 (org.apache.kafka.common.utils.AppInfoParser)
[2022-06-29 15:10:04,186] INFO [KafkaServer id=0] started (kafka.server.KafkaServer)

[root@hadoop001 kafka_2.11-2.4.1]# jps
198018 NameNode
198545 JournalNode
136912 QuorumPeerMain
403792 Jps
198219 DataNode
198153 NodeManager
198765 DFSZKFailoverController
399641 Kafka
```

## 4. Commands
### 4.1 Create Topic
```shell
[root@hadoop001 ~]# kafka-topics.sh --create --bootstrap-server hadoop001:9092 --topic topic1
[2022-06-29 15:37:17,323] INFO Creating topic topic1 with configuration {} and initial partition assignment Map(1 -> ArrayBuffer(0), 0 -> ArrayBuffer(0)) (kafka.zk.AdminZkClient)
[2022-06-29 15:37:17,350] INFO [ReplicaFetcherManager on broker 0] Removed fetcher for partitions Set(topic1-0, topic1-1) (kafka.server.ReplicaFetcherManager)
[2022-06-29 15:37:17,355] INFO [Log partition=topic1-0, dir=/opt/module/kafka_2.11-2.4.1/logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2022-06-29 15:37:17,355] INFO [Log partition=topic1-0, dir=/opt/module/kafka_2.11-2.4.1/logs] Completed load of log with 1 segments, log start offset 0 and log end offset 0 in 1 ms (kafka.log.Log)
[2022-06-29 15:37:17,356] INFO Created log for partition topic1-0 in /opt/module/kafka_2.11-2.4.1/logs/topic1-0 with properties {compression.type -> producer, message.downconversion.enable -> true, min.insync.replicas -> 1, segment.jitter.ms -> 0, cleanup.policy -> [delete], flush.ms -> 1000, segment.bytes -> 1073741824, retention.ms -> 604800000, flush.messages -> 10000, message.format.version -> 2.4-IV1, file.delete.delay.ms -> 60000, max.compaction.lag.ms -> 9223372036854775807, max.message.bytes -> 1000012, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, min.cleanable.dirty.ratio -> 0.5, index.interval.bytes -> 4096, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2022-06-29 15:37:17,357] INFO [Partition topic1-0 broker=0] No checkpointed highwatermark is found for partition topic1-0 (kafka.cluster.Partition)
[2022-06-29 15:37:17,357] INFO [Partition topic1-0 broker=0] Log loaded for partition topic1-0 with initial high watermark 0 (kafka.cluster.Partition)
[2022-06-29 15:37:17,357] INFO [Partition topic1-0 broker=0] topic1-0 starts at leader epoch 0 from offset 0 with high watermark 0. Previous leader epoch was -1. (kafka.cluster.Partition)
[2022-06-29 15:37:17,366] INFO [Log partition=topic1-1, dir=/opt/module/kafka_2.11-2.4.1/logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2022-06-29 15:37:17,366] INFO [Log partition=topic1-1, dir=/opt/module/kafka_2.11-2.4.1/logs] Completed load of log with 1 segments, log start offset 0 and log end offset 0 in 1 ms (kafka.log.Log)
[2022-06-29 15:37:17,367] INFO Created log for partition topic1-1 in /opt/module/kafka_2.11-2.4.1/logs/topic1-1 with properties {compression.type -> producer, message.downconversion.enable -> true, min.insync.replicas -> 1, segment.jitter.ms -> 0, cleanup.policy -> [delete], flush.ms -> 1000, segment.bytes -> 1073741824, retention.ms -> 604800000, flush.messages -> 10000, message.format.version -> 2.4-IV1, file.delete.delay.ms -> 60000, max.compaction.lag.ms -> 9223372036854775807, max.message.bytes -> 1000012, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, min.cleanable.dirty.ratio -> 0.5, index.interval.bytes -> 4096, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2022-06-29 15:37:17,367] INFO [Partition topic1-1 broker=0] No checkpointed highwatermark is found for partition topic1-1 (kafka.cluster.Partition)
[2022-06-29 15:37:17,367] INFO [Partition topic1-1 broker=0] Log loaded for partition topic1-1 with initial high watermark 0 (kafka.cluster.Partition)
[2022-06-29 15:37:17,367] INFO [Partition topic1-1 broker=0] topic1-1 starts at leader epoch 0 from offset 0 with high watermark 0. Previous leader epoch was -1. (kafka.cluster.Partition)
```

### 4.2 List Topic 
```shell
[root@hadoop001 ~]# kafka-topics.sh --list --bootstrap-server hadoop001:9092
topic1
```


### 4.3 Produce messages
```shell
[root@hadoop001 ~]# kafka-console-producer.sh --broker-list hadoop001:9092 --topic topic1
```

### 4.4 Consume messages
Open another terminal, input kafka consumer command:
```shell
[root@hadoop001 ~]# kafka-console-consumer.sh --bootstrap-server hadoop001:9092 --topic topic1
```
Go back to previous producer window, type some words like:
```text
hello world
today is a good day
```
You can see the consumer window receive the same message as well.
![kafka producer consumer](/post_img/kafka-producer-consumer.jpg)

### 4.5 Describe a topic
Show the details of a topic:
```shell
[root@hadoop001 ~]# kafka-topics.sh --describe --bootstrap-server hadoop001:9092 --topic topic1
Topic: topic1	PartitionCount: 2	ReplicationFactor: 1	Configs: flush.ms=1000,segment.bytes=1073741824,flush.messages=10000
	Topic: topic1	Partition: 0	Leader: 0	Replicas: 0	Isr: 0
	Topic: topic1	Partition: 1	Leader: 0	Replicas: 0	Isr: 0
```

### 4.6 Increase partitions of a topic 
```shell
[root@hadoop001 ~]# kafka-topics.sh --zookeeper hadoop001:2181 --alter --topic topic1 --partitions 4
WARNING: If partitions are increased for a topic that has a key, the partition logic or ordering of the messages will be affected
Adding partitions succeeded!
```
Let's verify it by describe command: 
```shell
[root@hadoop001 ~]# kafka-topics.sh --describe --bootstrap-server hadoop001:9092 --topic topic1
Topic: topic1	PartitionCount: 4	ReplicationFactor: 1	Configs: flush.ms=1000,segment.bytes=1073741824,flush.messages=10000
	Topic: topic1	Partition: 0	Leader: 0	Replicas: 0	Isr: 0
	Topic: topic1	Partition: 1	Leader: 0	Replicas: 0	Isr: 0
	Topic: topic1	Partition: 2	Leader: 0	Replicas: 0	Isr: 0
	Topic: topic1	Partition: 3	Leader: 0	Replicas: 0	Isr: 0
```

### 4.7 Delete a topic 
```shell
[root@hadoop001 ~]# kafka-topics.sh --zookeeper hadoop001:2181 --delete --topic topic1
Topic topic1 is marked for deletion.
Note: This will have no impact if delete.topic.enable is not set to true.
[root@hadoop001 ~]# kafka-topics.sh --list --zookeeper hadoop001:2181
__consumer_offsets
topic1
```
**By default, the delete command only marked a topic instead of real DELETE it**, which will take effect when you restart kafka service next time.

If you want to delete it immediately, set '_**delete.topic.enable=true**_' in server.properties.  
```text
delete.topic.enable=true
```
