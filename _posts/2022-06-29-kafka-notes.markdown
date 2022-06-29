---
layout: post
title:  "Kafka Notes"
date:   2022-06-29 11:51:13 +0800
categories: bigdata
tags:
    - Bigdata
    - Kafka
---

## Overall
Kafka起源于LinkedIn公司，用于对各业务系统的基础指标（内存/CPU/磁盘/网络等）和应用指标数据进行分析，自定义开发系统实现逐渐不能满足。
随着数据增长，业务需求复杂度提高，自定义开发问题越来越多。
逐渐进化成一个技能满足实时处理，又支持水平扩展的消息系统-Kafka。
是一个发布-订阅式的队列消息系统，使用scala语言编写，非常适合离线、在线消息消费。消息存储在磁盘上，并在集群内复制，防止数据丢失。

## Advantages
* 可靠性：分布式的，分区复制容错；
* 可扩展：轻松扩展；
* 耐用性：消息会尽可能快速的保存在磁盘上
* 性能：对发布和订阅都有高吞吐量，即使对TB级的消息，也有稳定的性能；
* 速度：非常快

## 应用场景
- 指标分析
- 日志聚合：从多服务器收集日志，以标准的格式提供给多消费者。
- 流处理：配合spark/storm/flink流处理框架使用。

## 基本概念
Broker：即一个Kafka进程，通常一个服务器节点部署一个实例（Broker）
Producer: 消息的产生者，Producer将消息记录发送到Kafka集群指定的主题（Topic）中进行存储；
Consumer: 消息的使用者，Consumer从Kafka集群指定的主题（Topic）中读取消息记录；
Topic主题：通过主题来区分不同业务类型的消息。
Partition：每个主题可以有一个或多个分区（提供数据冗余、可靠性、高性能）
- 多个分区并发读取，提高吞吐量；
- 分区内部消息是有序的，每个消息有一个偏移量（Offset）；
- 一个代理节点内部可管理多个分区。
  Replication副本：主题创建时指定副本数（默认1）
  建议副本数量：
  集群节点数量>=3时，副本=3;
  节点小于3时，副本=节点数；

```mermaid
graph LR;
Producer[Producer: DB/Web/... Logs] -->|Push| Broker[Broker: Kafka];
Broker -->|Pull| Consumer[Consumer: Hadoop/Spark/ElasticSearch/...] ;
```

￼
通过topic/partition/offset 来定位一条消息。

ACKS=ALL，至少1次
至多一次：避免数据重复；
精准一次


**同步写入**

```mermaid
graph LR;
Client -->|1 Push| Buffer;
Buffer -->|2 Write to | Partitions;
Buffer -->|3 Return| Client;
```

**异步写入**
```mermaid
graph LR;
Client -->|1 Push| Buffer;
Buffer -->|3 Write to | Partitions;
Buffer -->|2 Return| Client;
```


**Consumer Group**, 多个消费者组成一个消费组。
读取消息时，每个Consumer从不同的分区上并行获取消息，提高吞吐量。
消费组中的消费者数量不能大于分区数
一个组中是同业务消费者？


**存储**
一个topic包含多个分区，一个分区为一个目录，分区命名规则topic+序号（从0开始）
每个分区相当于一个超大的文件被均匀分割成若干大小相等的片段（Segment），每个片段的消息数量不一定相等。过期的数据会被删除，提高磁盘利用率。
如：  
00000000015.index //索引文件，不对每条消息建立索引，采用稀疏存储，每隔一定字节的数据建立一条索引  
00000000015.log   //数据文件  

## Flume和Kafka比较

| Flume               | Kafka          |
|---------------------|----------------|
| Cloudera开发          | 	LinkedIn开发    |
| 适合多个生产者，但下游消费者不多；强汇聚 |  适合下游消费者众多；强发散 |
| 数据安全性要求不高	          | 安全性较高          |
| 适合与Hadoop生态圈对接      ||


## CAP理论
- **Consistency**：一致性，更新操作后，各节点数据完全一致；在高并发时容易出问题。  
- **Availability**：可用性，read and write always succeed.  
- **Partition tolerance**：分区容错性, the system continues to operate despite arbitrary message loss or failure of part of the system. 即部分节点故障后，仍能对外提供满足一致性可用性的服务。采用多副本来保障。  

CA（without P），不允许分区，可保证强一致性和可用性，如Mysql/Oracle。
CP（without A），在强一致性的要求下，分区容错性会导致数据在各分区同步时间很长。很多传统数据库就是这样，如NoSQL/MongoDB/HBase/Redis。
AP（without C），高可用并允许分区，则需要放弃一致性。节点之间可能失去联系，为了实现高可用，节点只能用本地数据，导致不一致。（Coach DB）

**Kafka满足的是CA**，Partition tolerance是通过一定的机制尽量的保证容错。

**Kafka使用ISR同步策略**（In-Sync Replicas同步副本）列表，决定哪些副本分区是可用的，因素有：

replica.lag.time.max.ms=10000     副本分区与主分区心跳时间延迟
replica.lag.max.messages=4000    副本分区与主分区消息同步最大差

Producer请求被认为完成时的确认值：
request.required.acks=0
0:不等待broker确认，最快
1:leader已经收到数据确认，副本异步拉取消息，比较折中；
-1: ISR中所有副本都反悔确认消息

min.insync.replicas=1 ,
至少有1个Replica返回成功，否则producer异常。

