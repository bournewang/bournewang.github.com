---
layout: post
title:  "Build Realtime Data Flow With Canal"
subtitle: "Build a pipeline from MySQL to Kafka"
date:   2022-07-16 21:06:15 +0800
categories: bigdata
tags: [Bigdata, MySQL, Canal, Kafka]
---

## Overview
Canal is a realtime database syncing tool developed and maintained by Alibaba.   
Canal worked as a MySQL slave, which communicates with master using the replication protocol.

```mermaid
graph LR;
MySQL -->|push binary log| Canal[Canal: Pretend to be a MySQL slave];
Canal -->|dump| MySQL;
Canal -->|Push| MQ[Kafka/RocketMQ/RabbitMQ/...] ;
```

![Canal work flow](/post_img/canal-work-flow.jpg)

**Plan**:

| Node      | Service |
|-----------|---------|
| hadoop001 | MySQL |
| hadoop002 | Canal/Kafka server |
| hadoop003 | Kafka consumer |

### 1. Installation Canal
Download canal 1.1.5 from its [official website](https://alibaba.github.io/canal/){:target="blank"}.

Extract it to /opt/module/canal:
```shell
[root@hadoop002 ~]# mkdir /opt/module/canal/
[root@hadoop002 ~]# tar xvf canal.deployer-1.1.5.tar.gz -C /opt/module/canal/
```

<font style="background: yellow;">Notice: the latest version is 1.1.6, which keeps occurring the following error, I am sure I don't have a BASE table in my DB</font>
```shell
com.alibaba.otter.canal.parse.exception.CanalParseException:  \
java.io.IOException: ErrorPacket [errorNumber=1146, fieldCount=-1, \ 
message=Table 'mall.BASE TABLE' doesn't exist, sqlState=42S02, sqlStateMarker=#]
```

### 2. Start Zookeeper in all cluster nodes
```shell
[root@hadoop001 ~]# zkServer.sh start
[root@hadoop001 ~]# jps
26711 Jps
24999 QuorumPeerMain
```
```shell
[root@hadoop002 ~]# zkServer.sh start
[root@hadoop002 ~]# jps
17846 Jps
15979 QuorumPeerMain
```
```shell
[root@hadoop003 ~]# zkServer.sh start
[root@hadoop003 ~]# jps
29364 QuorumPeerMain
29178 Jps
```

### 3. Start MySQL as Master in Hadoop001
Install MySQL or MariaDB of 5.x version in node hadoop001. In CentOS 7.x, default is MariaDB, so directly install it with yum:
```shell
[root@hadoop001 ~]# yum install -y mariadb-server
```
Add following optinos in my.cnf
```shell
[root@hadoop001 ~]# cat /etc/my.cnf
[mysqld]
log-bin=/var/log/mysql/master-bin
binlog-format=ROW
server_id=1
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
sync_binlog=1
```

Start service:
```shell
[root@hadoop001 ~]# systemctl start mariadb
```  

Add a user **_canal_** to do the replication, password is also "canal" which is the default value in Canal's config.  

```shell
[root@hadoop001 ~]# mysql
Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 563
Server version: 5.5.68-MariaDB MariaDB Server

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
MariaDB [(none)]> grant replication client,replication slave on *.* to 'canal'@'%' identified by 'canal';
Query OK, 0 rows affected (0.00 sec)
MariaDB [(none)]> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```

### 4. Start Kafka server in Hadoop002
I already [installed Kafka](/bigdata/2022/06/29/kafka-tutorial/){:target="_blank"} in the cluster, start the service directly: 
```shell
[root@hadoop002 kafka_2.11-2.4.1]# ./bin/kafka-server-start.sh -daemon config/server.properties
[root@hadoop002 kafka_2.11-2.4.1]# jps
17462 Jps
19592 Kafka
15979 QuorumPeerMain
```
### 5. Configure Canal
Edit config file:
```shell
[root@hadoop002 ~]# cd /opt/module/canal/conf/
[root@hadoop002 conf]# vim example/instance.properties
```
Change the following options, keep other options as default:
```shell
canal.instance.mysql.slaveId=2
# position info
canal.instance.master.address=hadoop001:3306
```
Edit _**canal.properties**_:
* Open option "_canal.instance.parser.parallelThreadSize = 16_"; 
* Change serverMode to kafka;
* Set bootstrap server of kafka;

```shell
canal.instance.parser.parallelThreadSize = 16
...
# tcp, kafka, rocketMQ, rabbitMQ
canal.serverMode = kafka
...
kafka.bootstrap.servers = hadoop002:9092
```

Start Canal service:
```shell
[root@hadoop002 canal]# ./bin/startup.sh
cd to /opt/module/canal/bin for workaround relative path
LOG CONFIGURATION : /opt/module/canal/bin/../conf/logback.xml
canal conf : /opt/module/canal/bin/../conf/canal.properties
CLASSPATH :/opt/module/canal/bin/../conf:/opt/module/canal/bin/../lib/zookeeper-3.4.5.jar:/opt/module/canal/bin/../lib/zkclient-0.10.jar:/opt/module/canal/bin/../lib/spring-tx-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/spring-orm-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/spring-jdbc-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/spring-jcl-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/spring-expression-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/spring-core-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/spring-context-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/spring-beans-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/spring-aop-5.0.5.RELEASE.jar:/opt/module/canal/bin/../lib/slf4j-api-1.7.12.jar:/opt/module/canal/bin/../lib/simpleclient_pushgateway-0.4.0.jar:/opt/module/canal/bin/../lib/simpleclient_httpserver-0.4.0.jar:/opt/module/canal/bin/../lib/simpleclient_hotspot-0.4.0.jar:/opt/module/canal/bin/../lib/simpleclient_common-0.4.0.jar:/opt/module/canal/bin/../lib/simpleclient-0.4.0.jar:/opt/module/canal/bin/../lib/protobuf-java-3.6.1.jar:/opt/module/canal/bin/../lib/oro-2.0.8.jar:/opt/module/canal/bin/../lib/netty-all-4.1.6.Final.jar:/opt/module/canal/bin/../lib/netty-3.2.2.Final.jar:/opt/module/canal/bin/../lib/mysql-connector-java-5.1.48.jar:/opt/module/canal/bin/../lib/mybatis-spring-2.0.4.jar:/opt/module/canal/bin/../lib/mybatis-3.5.4.jar:/opt/module/canal/bin/../lib/logback-core-1.1.3.jar:/opt/module/canal/bin/../lib/logback-classic-1.1.3.jar:/opt/module/canal/bin/../lib/jsr305-3.0.2.jar:/opt/module/canal/bin/../lib/joda-time-2.9.4.jar:/opt/module/canal/bin/../lib/jctools-core-2.1.2.jar:/opt/module/canal/bin/../lib/jcl-over-slf4j-1.7.12.jar:/opt/module/canal/bin/../lib/javax.annotation-api-1.3.2.jar:/opt/module/canal/bin/../lib/j2objc-annotations-1.1.jar:/opt/module/canal/bin/../lib/httpcore-4.4.3.jar:/opt/module/canal/bin/../lib/httpclient-4.5.1.jar:/opt/module/canal/bin/../lib/h2-1.4.196.jar:/opt/module/canal/bin/../lib/guava-22.0.jar:/opt/module/canal/bin/../lib/fastjson-1.2.58.sec06.jar:/opt/module/canal/bin/../lib/error_prone_annotations-2.0.18.jar:/opt/module/canal/bin/../lib/druid-1.2.6.jar:/opt/module/canal/bin/../lib/disruptor-3.4.2.jar:/opt/module/canal/bin/../lib/connector.core-1.1.5.jar:/opt/module/canal/bin/../lib/commons-logging-1.2.jar:/opt/module/canal/bin/../lib/commons-lang3-3.7.jar:/opt/module/canal/bin/../lib/commons-lang-2.6.jar:/opt/module/canal/bin/../lib/commons-io-2.4.jar:/opt/module/canal/bin/../lib/commons-compress-1.9.jar:/opt/module/canal/bin/../lib/commons-codec-1.9.jar:/opt/module/canal/bin/../lib/commons-beanutils-1.8.2.jar:/opt/module/canal/bin/../lib/canal.store-1.1.5.jar:/opt/module/canal/bin/../lib/canal.sink-1.1.5.jar:/opt/module/canal/bin/../lib/canal.server-1.1.5.jar:/opt/module/canal/bin/../lib/canal.protocol-1.1.5.jar:/opt/module/canal/bin/../lib/canal.prometheus-1.1.5.jar:/opt/module/canal/bin/../lib/canal.parse.driver-1.1.5.jar:/opt/module/canal/bin/../lib/canal.parse.dbsync-1.1.5.jar:/opt/module/canal/bin/../lib/canal.parse-1.1.5.jar:/opt/module/canal/bin/../lib/canal.meta-1.1.5.jar:/opt/module/canal/bin/../lib/canal.instance.spring-1.1.5.jar:/opt/module/canal/bin/../lib/canal.instance.manager-1.1.5.jar:/opt/module/canal/bin/../lib/canal.instance.core-1.1.5.jar:/opt/module/canal/bin/../lib/canal.filter-1.1.5.jar:/opt/module/canal/bin/../lib/canal.deployer-1.1.5.jar:/opt/module/canal/bin/../lib/canal.common-1.1.5.jar:/opt/module/canal/bin/../lib/aviator-2.2.1.jar:/opt/module/canal/bin/../lib/animal-sniffer-annotations-1.14.jar:
cd to /opt/module/canal for continue
[root@hadoop002 canal]# jps
19970 Jps
19815 CanalLauncher
19592 Kafka
15979 QuorumPeerMain
[root@hadoop002 canal]# tail -f logs/canal/canal.log
2022-07-16 20:40:13.193 [Thread-6] INFO  com.alibaba.otter.canal.deployer.CanalController - ## stop the canal server[172.17.0.3(172.17.0.3):11111]
2022-07-16 20:40:13.200 [Thread-6] INFO  com.alibaba.otter.canal.deployer.CanalStarter - ## canal server is down.
2022-07-16 20:40:23.167 [main] INFO  com.alibaba.otter.canal.deployer.CanalLauncher - ## set default uncaught exception handler
2022-07-16 20:40:23.215 [main] INFO  com.alibaba.otter.canal.deployer.CanalLauncher - ## load canal configurations
2022-07-16 20:40:23.352 [main] WARN  org.apache.kafka.clients.producer.ProducerConfig - The configuration 'kerberos.enable' was supplied but isn't a known config.
2022-07-16 20:40:23.354 [main] WARN  org.apache.kafka.clients.producer.ProducerConfig - The configuration 'kerberos.krb5.file' was supplied but isn't a known config.
2022-07-16 20:40:23.354 [main] WARN  org.apache.kafka.clients.producer.ProducerConfig - The configuration 'kerberos.jaas.file' was supplied but isn't a known config.
2022-07-16 20:40:23.356 [main] INFO  com.alibaba.otter.canal.deployer.CanalStarter - ## start the canal server.
2022-07-16 20:40:23.406 [main] INFO  com.alibaba.otter.canal.deployer.CanalController - ## start the canal server[172.17.0.3(172.17.0.3):11111]
2022-07-16 20:40:25.003 [main] INFO  com.alibaba.otter.canal.deployer.CanalStarter - ## the canal server is running now ......

[root@hadoop002 canal]# tail -f logs/example/example.log
2022-07-16 20:40:12.198 [Thread-6] INFO  c.a.otter.canal.instance.core.AbstractCanalInstance - stop CannalInstance for null-example
2022-07-16 20:40:13.192 [Thread-6] INFO  c.a.otter.canal.instance.core.AbstractCanalInstance - stop successful....
2022-07-16 20:40:24.779 [main] INFO  c.a.otter.canal.instance.spring.CanalInstanceWithSpring - start CannalInstance for 1-example
2022-07-16 20:40:24.799 [main] WARN  c.a.o.canal.parse.inbound.mysql.dbsync.LogEventConvert - --> init table filter : ^.*\..*$
2022-07-16 20:40:24.799 [main] WARN  c.a.o.canal.parse.inbound.mysql.dbsync.LogEventConvert - --> init table black filter : ^mysql\.slave_.*$
2022-07-16 20:40:24.998 [main] INFO  c.a.otter.canal.instance.core.AbstractCanalInstance - start successful....
2022-07-16 20:40:25.044 [destination = example , address = hadoop001/172.17.0.16:3306 , EventParser] WARN  c.a.o.c.p.inbound.mysql.rds.RdsBinlogEventParserProxy - ---> begin to find start position, it will be long time for reset or first position
2022-07-16 20:40:25.069 [destination = example , address = hadoop001/172.17.0.16:3306 , EventParser] WARN  c.a.o.c.p.inbound.mysql.rds.RdsBinlogEventParserProxy - prepare to find start position just last position
 {"identity":{"slaveId":-1,"sourceAddress":{"address":"hadoop001","port":3306}},"postion":{"gtid":"","included":false,"journalName":"master-bin.000006","position":4747,"serverId":1,"timestamp":1657967227000}}
2022-07-16 20:40:25.528 [destination = example , address = hadoop001/172.17.0.16:3306 , EventParser] WARN  c.a.o.c.p.inbound.mysql.rds.RdsBinlogEventParserProxy - ---> \
find start position successfully, EntryPosition[included=false,journalName=master-bin.000006,position=4747,serverId=1,gtid=,timestamp=1657967227000] cost : 472ms , \
the next step is binlog dump
```

The Cancel service start successfully.

### 6. Start Kafka Consumer in Hadoop003
```shell
[root@hadoop003 ~]# kafka-console-consumer.sh --bootstrap-server hadoop002:9092 --topic example
```

In Hadoop001, we insert a row to books table:
```shell
MariaDB [mall]> insert into books values(7, "Web Development");
Query OK, 1 row affected (0.00 sec)
```

Immediately we get the data change in Kafka consumer window:
```shell
[root@hadoop003 ~]# kafka-console-consumer.sh --bootstrap-server hadoop002:9092 --topic example
{"data":[{"id":"7","name":"Web Development"}],"database":"mall","es":1657975530000,"id":2,"isDdl":false,"mysqlType":{"id":"int(11)","name":"varchar(45)"},"old":null,"pkNames":["id"],"sql":"","sqlType":{"id":4,"name":12},"table":"books","ts":1657975530937,"type":"INSERT"}
```

Great! Everything seems going on well.
