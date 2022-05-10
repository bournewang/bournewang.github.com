---
layout: post
title:  "Hive beeline连接配置"
date:   2022-05-10 12:46:55 +0800
categories: bigdata
tags:
    - Bigdata 
    - Hive
---
# Hive Beeline连接配置

## 1. Hive-cli和Beeline区别
* hive-cli只能一次一个连接，不能并行处理多个连接；
* hive-cli直接通过metaserver访问元数据，没有权限控制；
* hiveserver2提供权限控制；
* beeline连接hiveserver2，实现多用户、有权限控制的访问；

## 2. 配置
### 2.1 hadoop配置
core-site.xml中添加:
```xml
<property>
    <name>hadoop.proxyuser.root.hosts</name>
    <value>*</value>
</property>
<property>
    <name>hadoop.proxyuser.root.groups</name>
    <value>*</value>
</property>
```

hdfs-site.xml中添加:
```xml
<property>
    <name>dfs.webhdfs.enabled</name>
    <value>true</value>
</property>
```
重启hdfs和yarn。

### 2.2 hive配置
hive-site.xml中修改hiveserver2主机：
```xml
  <property>
    <name>hive.server2.thrift.bind.host</name>
    <value>hadoop001</value>
  </property>
```
重启hive meta， 启动hiveserver2.

连接beeline报错,查看log（/tmp/root/hive.log）, 找到错误：<font color='red'>java.lang.NoClassDefFoundError: org/apache/tez/dag/api/SessionNotRunning</font>

hive-site.xml中打开高可用设置再重启：
```xml
<property>
    <name>hive.server2.active.passive.ha.enable</name>
    <value>true</value>
</property>
```

beeline登录报错<font color='red'>User: root is not allowed to impersonate root</font>， 还需要修改：
```xml
<property>
    <name>hive.server2.enable.doAs</name>
    <value>false</value>
</property>
```
重启hiveserver2，登录成功，
```shell
[root@hadoop001 conf]# beeline
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/module/apache-hive-3.1.2-bin/lib/log4j-slf4j-impl-2.10.0.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/module/hadoop-2.7.3/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
Beeline version 3.1.2 by Apache Hive
beeline> !connect jdbc:hive2://0.0.0.0:10000
Connecting to jdbc:hive2://0.0.0.0:10000
Enter username for jdbc:hive2://0.0.0.0:10000: root
Enter password for jdbc:hive2://0.0.0.0:10000:
Connected to: Apache Hive (version 3.1.2)
Driver: Hive JDBC (version 3.1.2)
Transaction isolation: TRANSACTION_REPEATABLE_READ
0: jdbc:hive2://0.0.0.0:10000> show databases;
OK
INFO  : Compiling command(queryId=root_20220510123418_0395d946-17df-49f0-bd4b-ff444d97f5d5): show databases
INFO  : Concurrency mode is disabled, not creating a lock manager
INFO  : Semantic Analysis Completed (retrial = false)
INFO  : Returning Hive schema: Schema(fieldSchemas:[FieldSchema(name:database_name, type:string, comment:from deserializer)], properties:null)
INFO  : Completed compiling command(queryId=root_20220510123418_0395d946-17df-49f0-bd4b-ff444d97f5d5); Time taken: 0.007 seconds
INFO  : Concurrency mode is disabled, not creating a lock manager
INFO  : Executing command(queryId=root_20220510123418_0395d946-17df-49f0-bd4b-ff444d97f5d5): show databases
INFO  : Starting task [Stage-0:DDL] in serial mode
INFO  : Completed executing command(queryId=root_20220510123418_0395d946-17df-49f0-bd4b-ff444d97f5d5); Time taken: 0.014 seconds
INFO  : OK
INFO  : Concurrency mode is disabled, not creating a lock manager
+----------------+
| database_name  |
+----------------+
| db1            |
| default        |
+----------------+
2 rows selected (0.116 seconds)
0: jdbc:hive2://0.0.0.0:10000> use db1;
OK
INFO  : Compiling command(queryId=root_20220510123421_d1ae673d-ad05-4ab5-9b44-8985d9fa41ed): use db1
INFO  : Concurrency mode is disabled, not creating a lock manager
INFO  : Semantic Analysis Completed (retrial = false)
INFO  : Returning Hive schema: Schema(fieldSchemas:null, properties:null)
INFO  : Completed compiling command(queryId=root_20220510123421_d1ae673d-ad05-4ab5-9b44-8985d9fa41ed); Time taken: 0.008 seconds
INFO  : Concurrency mode is disabled, not creating a lock manager
INFO  : Executing command(queryId=root_20220510123421_d1ae673d-ad05-4ab5-9b44-8985d9fa41ed): use db1
INFO  : Starting task [Stage-0:DDL] in serial mode
INFO  : Completed executing command(queryId=root_20220510123421_d1ae673d-ad05-4ab5-9b44-8985d9fa41ed); Time taken: 0.01 seconds
INFO  : OK
INFO  : Concurrency mode is disabled, not creating a lock manager
No rows affected (0.036 seconds)
0: jdbc:hive2://0.0.0.0:10000> show tables;
OK
INFO  : Compiling command(queryId=root_20220510123423_628ca5d2-02fe-470b-8e26-a076168b60b7): show tables
INFO  : Concurrency mode is disabled, not creating a lock manager
INFO  : Semantic Analysis Completed (retrial = false)
INFO  : Returning Hive schema: Schema(fieldSchemas:[FieldSchema(name:tab_name, type:string, comment:from deserializer)], properties:null)
INFO  : Completed compiling command(queryId=root_20220510123423_628ca5d2-02fe-470b-8e26-a076168b60b7); Time taken: 0.01 seconds
INFO  : Concurrency mode is disabled, not creating a lock manager
INFO  : Executing command(queryId=root_20220510123423_628ca5d2-02fe-470b-8e26-a076168b60b7): show tables
INFO  : Starting task [Stage-0:DDL] in serial mode
INFO  : Completed executing command(queryId=root_20220510123423_628ca5d2-02fe-470b-8e26-a076168b60b7); Time taken: 0.015 seconds
INFO  : OK
INFO  : Concurrency mode is disabled, not creating a lock manager
+-----------+
| tab_name  |
+-----------+
| tb1       |
+-----------+
1 row selected (0.041 seconds)
```
