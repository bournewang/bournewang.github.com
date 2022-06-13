---
layout: post
title:  "Sqoop2 Practice"
date:   2022-06-11 15:19:00 +0800
categories: bigdata
tags:
    - Bigdata
    - Sqoop2
---

## 1 Install and configuration
### 1.1 extract the tar ball to /opt/module, set SQOOP_HOME in /etc/profile
```shell
[root@hadoop001 sqoop-1.99.7-bin-hadoop200]# echo export SQOOP_HOME=`pwd`
export SQOOP_HOME=/opt/module/sqoop-1.99.7-bin-hadoop200
[root@hadoop001 sqoop-1.99.7-bin-hadoop200]# echo export SQOOP_HOME=`pwd` >> /etc/profile
[root@hadoop001 sqoop-1.99.7-bin-hadoop200]# echo export PATH=\$PATH:\$SQOOP_HOME/bin >> /etc/profile
```
### 1.2 configure the sqoop-env.sh
put HADOOP_HOME in conf/sqoop-env.sh
```shell
[root@hadoop001 sqoop-1.99.7-bin-hadoop200]# echo export HADOOP_HOME=$HADOOP_HOME >  conf/sqoop-env.sh
```
create "_lib_" dir, put mysql-connector-java-8.0.11.jar into it, and export SQOOP_SERVER_EXTRA_LIB:
```shell
[root@hadoop001 sqoop-1.99.7-bin-hadoop200]# mkdir lib/
[root@hadoop001 sqoop-1.99.7-bin-hadoop200]# cd lib/
[root@hadoop001 lib]# cp /opt/module/apache-hive-3.1.2-bin/lib/mysql-connector-java-8.0.11.jar .
[root@hadoop001 lib]# echo export SQOOP_SERVER_EXTRA_LIB=`pwd`
export SQOOP_SERVER_EXTRA_LIB=/opt/module/sqoop-1.99.7-bin-hadoop200/lib
[root@hadoop001 lib]# echo export SQOOP_SERVER_EXTRA_LIB=`pwd` >> ../conf/sqoop-env.sh
[root@hadoop001 lib]# cd ..
[root@hadoop001 sqoop-1.99.7-bin-hadoop200]# cat conf/sqoop-env.sh
export HADOOP_HOME=/opt/module/hadoop-2.7.3
export SQOOP_SERVER_EXTRA_LIB=/opt/module/sqoop-1.99.7-bin-hadoop200/lib
```

### 1.3 configure hadoop
```shell
[root@hadoop001 module]# cd hadoop-2.7.3/
[root@hadoop001 hadoop-2.7.3]# cd etc/hadoop/
[root@hadoop001 hadoop]# pwd
/opt/module/hadoop-2.7.3/etc/hadoop
```
add following hadoop.proxyuser.sqoop2 property in etc/hadoop/core-site.xml: 
```xml
<property>
  <name>hadoop.proxyuser.sqoop2.hosts</name>
  <value>*</value>
</property>
<property>
  <name>hadoop.proxyuser.sqoop2.groups</name>
  <value>*</value>
</property>
```
and update "_allowed.system.users=sqoop2_" in container-executor.cfg: 
```shell
[root@hadoop001 hadoop]# cat container-executor.cfg
yarn.nodemanager.linux-container-executor.group=#configured value of yarn.nodemanager.linux-container-executor.group
banned.users=#comma separated list of users who can not run applications
min.user.id=1000#Prevent other super-users
allowed.system.users=sqoop2##comma separated list of system users who CAN run applications
```

## 2 Repository Initialization
got " _sealing violation: package org.apache.derby.impl.services.locks is sealed_" while run "_sqoop2-tool upgrade_", which was caused by derby version conflict in hive and sqoop.  
the solution is simple, remove the derby jar in $SQOOP_HOME/server/lib, and copy the derby jar from $HIVE_HOME/lib to $SQOOP_HOME/server/lib.  
```shell
[root@hadoop001 module]# sqoop2-tool upgrade
Setting conf dir: /opt/module/sqoop-1.99.7-bin-hadoop200/bin/../conf
Sqoop home directory: /opt/module/sqoop-1.99.7-bin-hadoop200
Sqoop tool executor:
	Version: 1.99.7
	Revision: 435d5e61b922a32d7bce567fe5fb1a9c0d9b1bbb
	Compiled on Tue Jul 19 16:08:27 PDT 2016 by abefine
Running tool: class org.apache.sqoop.tools.tool.UpgradeTool
0    [main] INFO  org.apache.sqoop.core.PropertiesConfigurationProvider  - Starting config file poller thread
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/module/hadoop-2.7.3/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/module/apache-hive-3.1.2-bin/lib/log4j-slf4j-impl-2.10.0.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
java.lang.SecurityException: sealing violation: package org.apache.derby.impl.services.locks is sealed
	at java.net.URLClassLoader.getAndVerifyPackage(URLClassLoader.java:400)
	at java.net.URLClassLoader.definePackageInternal(URLClassLoader.java:420)
	at java.net.URLClassLoader.defineClass(URLClassLoader.java:452)
```

solution: 
```shell
[root@hadoop001 module]# cp apache-hive-3.1.2-bin/lib/derby-10.14.1.0.jar sqoop-1.99.7-bin-hadoop200/server/lib/
[root@hadoop001 module]# cd sqoop-1.99.7-bin-hadoop200/server/lib
[root@hadoop001 lib]# mv derby-10.8.2.2.jar derby-10.8.2.2.jar.bk
```
run upgrade again, this time it's successed.  

verify:
```shell
[root@hadoop001 ~]# sqoop2-tool verify
Setting conf dir: /opt/module/sqoop-1.99.7-bin-hadoop200/bin/../conf
Sqoop home directory: /opt/module/sqoop-1.99.7-bin-hadoop200
Sqoop tool executor:
	Version: 1.99.7
	Revision: 435d5e61b922a32d7bce567fe5fb1a9c0d9b1bbb
	Compiled on Tue Jul 19 16:08:27 PDT 2016 by abefine
Running tool: class org.apache.sqoop.tools.tool.VerifyTool
0    [main] INFO  org.apache.sqoop.core.SqoopServer  - Initializing Sqoop server.
5    [main] INFO  org.apache.sqoop.core.PropertiesConfigurationProvider  - Starting config file poller thread
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/module/hadoop-2.7.3/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/module/apache-hive-3.1.2-bin/lib/log4j-slf4j-impl-2.10.0.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
Verification was successful.
Tool class org.apache.sqoop.tools.tool.VerifyTool has finished correctly.
```

## 3 Start sqoop2 server
```shell
[root@hadoop001 module]# sqoop2-server start
Setting conf dir: /opt/module/sqoop-1.99.7-bin-hadoop200/bin/../conf
Sqoop home directory: /opt/module/sqoop-1.99.7-bin-hadoop200
Starting the Sqoop2 server...
0    [main] INFO  org.apache.sqoop.core.SqoopServer  - Initializing Sqoop server.
7    [main] INFO  org.apache.sqoop.core.PropertiesConfigurationProvider  - Starting config file poller thread
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/module/hadoop-2.7.3/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/module/apache-hive-3.1.2-bin/lib/log4j-slf4j-impl-2.10.0.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
Sqoop2 server started.
[root@hadoop001 module]# Exception in thread "main" java.lang.RuntimeException: Failure in server initialization
	at org.apache.sqoop.core.SqoopServer.initialize(SqoopServer.java:68)
	at org.apache.sqoop.server.SqoopJettyServer.<init>(SqoopJettyServer.java:67)
	at org.apache.sqoop.server.SqoopJettyServer.main(SqoopJettyServer.java:177)
Caused by: org.apache.sqoop.common.SqoopException: MAPREDUCE_0002:Failure on submission engine initialization - 
Invalid Hadoop configuration directory (not a directory or permission issues): /etc/hadoop/conf/
	at org.apache.sqoop.submission.mapreduce.MapreduceSubmissionEngine.initialize(MapreduceSubmissionEngine.java:97)
	at org.apache.sqoop.driver.JobManager.initialize(JobManager.java:257)
	at org.apache.sqoop.core.SqoopServer.initialize(SqoopServer.java:64)
	... 2 more
```

fix mapreduce.configuration.directory in _$SQOOP_HOME/conf/sqoop.properties_ to _$HADOOP_HOME/etc/hadoop_.  
It should looks like this after fix:
```shell
[root@hadoop001 conf]# grep mapreduce.configuration sqoop.properties
#org.apache.sqoop.submission.engine.mapreduce.configuration.directory=/etc/hadoop/conf/
org.apache.sqoop.submission.engine.mapreduce.configuration.directory=/opt/module/hadoop-2.7.3/etc/hadoop
```

Restart sqoop2 server
```shell
[root@hadoop001 module]# sqoop2-server start
Setting conf dir: /opt/module/sqoop-1.99.7-bin-hadoop200/bin/../conf
Sqoop home directory: /opt/module/sqoop-1.99.7-bin-hadoop200
Starting the Sqoop2 server...
0    [main] INFO  org.apache.sqoop.core.SqoopServer  - Initializing Sqoop server.
4    [main] INFO  org.apache.sqoop.core.PropertiesConfigurationProvider  - Starting config file poller thread
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/module/hadoop-2.7.3/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/module/apache-hive-3.1.2-bin/lib/log4j-slf4j-impl-2.10.0.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
Sqoop2 server started.
```

## 4 Create links
```shell
[root@hadoop001 module]# sqoop2-shell
Setting conf dir: /opt/module/sqoop-1.99.7-bin-hadoop200/bin/../conf
Sqoop home directory: /opt/module/sqoop-1.99.7-bin-hadoop200
Sqoop Shell: Type 'help' or '\h' for help.
sqoop:000> create link -c generic-jdbc-connector
Creating link for connector with name generic-jdbc-connector
Please fill following values to create new link object
Name: gt

Database connection

Driver class: com.mysql.jdbc.Driver
Connection String: jdbc:mysql://rm-2ze9z87x15p0qa7bgfo.mysql.rds.aliyuncs.com:3306/hdel
Username: gt1206
Password: **********
Fetch Size: 100000
Connection Properties:
There are currently 0 values in the map:
entry# protocol=tcp
There are currently 1 values in the map:
protocol = tcp
entry#

SQL Dialect

Identifier enclose:
New link was successfully created with validation status OK and name gt
```
Caution: 
**for the "Identifier enclose:" section, you should put a space before hit [Enter] key.**


## 5 Create a job
```shell
sqoop:000> show link
+---------+------------------------+---------+
|  Name   |     Connector Name     | Enabled |
+---------+------------------------+---------+
| gt      | generic-jdbc-connector | true    |
| hdfs-gt | hdfs-connector         | true    |
| gt_hdfs | hdfs-connector         | true    |
+---------+------------------------+---------+
sqoop:000> create job -f gt -t gt_hdfs
Creating job for links with from name gt and to name gt_hdfs
Please fill following values to create new job object
Name: mysql2hdfs

Database source

Schema name: hdel
Table name: city
SQL statement:
Column names:
There are currently 0 values in the list:
element#
Partition column:
Partition column nullable:
Boundary query:

Incremental read

Check column:
Last value:

Target configuration

Override null value:
Null value:
File format:
  0 : TEXT_FILE
  1 : SEQUENCE_FILE
  2 : PARQUET_FILE
Choose: 0
Compression codec:
  0 : NONE
  1 : DEFAULT
  2 : DEFLATE
  3 : GZIP
  4 : BZIP2
  5 : LZO
  6 : LZ4
  7 : SNAPPY
  8 : CUSTOM
Choose: 0
Custom codec:
Output directory: /hdel/city
Append mode:

Throttling resources

Extractors:
Loaders:

Classpath configuration

Extra mapper jars:
There are currently 0 values in the list:
element#
New job was successfully created with validation status OK  and name mysql2hdfs
sqoop:000> show job
+----+---------------+-----------------------------+--------------------------+---------+
| Id |     Name      |       From Connector        |       To Connector       | Enabled |
+----+---------------+-----------------------------+--------------------------+---------+
| 1  | mysql-to-hdfs | gt (generic-jdbc-connector) | hdfs-gt (hdfs-connector) | true    |
| 2  | mysql2hdfs    | gt (generic-jdbc-connector) | gt_hdfs (hdfs-connector) | true    |
+----+---------------+-----------------------------+--------------------------+---------+
```

## 6 Start the Job
```shell
sqoop:000> start job -n mysql2hdfs
...
org.apache.sqoop.common.SqoopException: GENERIC_HDFS_CONNECTOR_0007:Invalid input/output directory - Unexpected exception
        at org.apache.sqoop.connector.hdfs.HdfsToInitializer.initialize(HdfsToInitializer.java:85)
        at org.apache.sqoop.connector.hdfs.HdfsToInitializer.initialize(HdfsToInitializer.java:37)
        at org.apache.sqoop.driver.JobManager$1.call(JobManager.java:520)
        at org.apache.sqoop.driver.JobManager$1.call(JobManager.java:517)
        at org.apache.sqoop.utils.ClassUtils.executeWithClassLoader(ClassUtils.java:281)
```
I checked the output directory again, I am sure the /hdel/city is exist and empty.  
After googled on internet, I changed _**hadoop.proxyuser**_ to root, restart dfs/yarn, and the job.
This time, the job executed success.

```shell
[root@hadoop001 hadoop]# cat core-site.xml
<?xml version='1.0' encoding='UTF-8'?>
<?xml-stylesheet type='text/xsl' href='configuration.xsl'?>
<configuration>
	<property><name>fs.defaultFS</name><value>hdfs://ns</value></property>
	<property><name>hadoop.tmp.dir</name><value>/opt/module/hadoop-2.7.3/data</value></property>
	<property><name>ha.zookeeper.quorum</name><value>hadoop001:2181,hadoop002:2181,hadoop003:2181</value></property>
	<property>
		<name>hadoop.proxyuser.root.hosts</name>
		<value>*</value>
	</property>
	<property>
		<name>hadoop.proxyuser.root.groups</name>
		<value>*</value>
	</property>
</configuration>
```

```shell

sqoop:000> start job -n mysql2hdfs
Submission details
Job Name: mysql2hdfs
Server URL: http://localhost:12000/sqoop/
Created by: root
Creation date: 2022-06-11 12:11:42 CST
Lastly updated by: root
External ID: job_local311058820_0002
	http://localhost:8080/
2022-06-11 12:11:48 CST: SUCCEEDED
```

Check the HDFS folder:

```shell
[root@hadoop001 ~]# hdfs dfs -ls /hdel/city
22/06/11 13:00:38 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Found 10 items
-rw-r--r--   3 root supergroup      16467 2022-06-11 12:11 /hdel/city/12becf42-112d-4c48-b7dd-436a093b4949.txt
-rw-r--r--   3 root supergroup         90 2022-06-11 12:11 /hdel/city/1d09cf24-78f9-480c-a2b7-f9216969c606.txt
-rw-r--r--   3 root supergroup      16881 2022-06-11 12:11 /hdel/city/61e034eb-9324-47e6-90f2-2bc4d0898837.txt
-rw-r--r--   3 root supergroup       2985 2022-06-11 12:11 /hdel/city/78327b6d-675d-481f-a016-4aa97a9be7ae.txt
-rw-r--r--   3 root supergroup      20418 2022-06-11 12:11 /hdel/city/89cef32e-6d7a-413e-a892-e493e5ff23bf.txt
-rw-r--r--   3 root supergroup      12324 2022-06-11 12:11 /hdel/city/b7732139-7942-401c-804a-d1de2506b118.txt
-rw-r--r--   3 root supergroup      28272 2022-06-11 12:11 /hdel/city/b7773286-1d1b-43d3-9d6a-bff0626b857f.txt
-rw-r--r--   3 root supergroup       5055 2022-06-11 12:11 /hdel/city/c0603053-598c-43ea-8ed7-4e5355600d11.txt
-rw-r--r--   3 root supergroup      22098 2022-06-11 12:11 /hdel/city/da51b909-33f0-455b-9fca-5c25a070ae36.txt
-rw-r--r--   3 root supergroup         33 2022-06-11 12:11 /hdel/city/dd47ce6d-c7bb-4a52-b27d-42e75b19cb87.txt
[root@hadoop001 ~]# hdfs dfs -tail /hdel/city/12becf42-112d-4c48-b7dd-436a093b4949.txt
22/06/11 13:00:50 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
县',654000,NULL,NULL
654024,'巩留县',654000,NULL,NULL
654025,'新源县',654000,NULL,NULL
654026,'昭苏县',654000,NULL,NULL
654027,'特克斯县',654000,NULL,NULL
654028,'尼勒克县',654000,NULL,NULL
654200,'塔城地区',650000,0,NULL
654201,'塔城市',654200,NULL,NULL
654202,'乌苏市',654200,NULL,NULL
654221,'额敏县',654200,NULL,NULL
654223,'沙湾县',654200,NULL,NULL
654224,'托里县',654200,NULL,NULL
654225,'裕民县',654200,NULL,NULL
654226,'和布克赛尔蒙古自治县',654200,NULL,NULL
654300,'阿勒泰地区',650000,0,NULL
654301,'阿勒泰市',654300,NULL,NULL
654321,'布尔津县',654300,NULL,NULL
654322,'富蕴县',654300,NULL,NULL
654323,'福海县',654300,NULL,NULL
654324,'哈巴河县',654300,NULL,NULL
654325,'青河县',654300,NULL,NULL
654326,'吉木乃县',654300,NULL,NULL
659000,'省直辖行政单位',650000,0,NULL
659001,'石河子市',659000,NULL,NULL
659002,'阿拉尔市',659000,NULL,NULL
659003,'图木舒克市',659000,NULL,NULL
659004,'五家渠市',659000,NULL,NULL
```