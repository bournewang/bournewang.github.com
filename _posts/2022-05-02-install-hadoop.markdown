---
layout: post
title:  "Install Hadoop and related components automatically"
date:   2022-05-02 17:23:18 +0800
categories: bigdata
tags:
    - Bigdata
    - Hadoop
---
# Install Hadoop and related components automatically

Install and configure hadoop is tedious, so I build a script to do this process automatically.

The git project can be found here [install hadoop](https://github.com/bournewang/install_hadoop){:target="_blank"}.

## 1. Configure Hosts
Let's assume you have 3 hosts  hadoop001/hadoop002/hadoop003, and configured access each other with ssh key(withput entering password).

If not, do as following steps.

Make sure you can access them with your private key(you can login to them directly with command "ssh root@hadoop001").

Download or clone project on your computer, get into the project folder, and then execute:
```shell
./config-hosts-access.sh "hadoop001 hadoop002 hadoop003"
```
or use the IP instead:
```shell
./config-hosts-access.sh "10.10.10.1 10.10.10.2 10.10.10.3"
```
this script will generate ssh key in each host, and append all the public keys in "/root/.ssh/authorized_keys" for each hosts.

Verify
```shell
[root@hadoop001 opt]# ssh hadoop002
Last login: Mon May  2 12:40:55 2022 from 183.194.183.43

Welcome to Alibaba Cloud Elastic Compute Service !

[root@hadoop002 ~]# exit
logout
Connection to hadoop002 closed.
[root@hadoop001 opt]# ssh hadoop003

Welcome to Alibaba Cloud Elastic Compute Service !

Activate the web console with: systemctl enable --now cockpit.socket

Last login: Mon May  2 12:41:43 2022 from 183.194.183.43
[root@hadoop003 ~]# exit
logout
Connection to hadoop003 closed.
[root@hadoop001 opt]#
```

## 2. Prepare packages
Download or clone the project in all 3 hosts;
Run _./download.sh_ to download packages,
download jdk-8u231-linux-x64.tar.gz(from oracle java official website) manually and save to software folder;
then, you should get these files in _software_ folder:
```shell
bigdata git:(master) ✗ ll software
total 3064776
-rw-r--r--  1 wangxiaopei  staff   67938106 Jul  6  2020 apache-flume-1.9.0-bin.tar.gz
-rw-r--r--  1 wangxiaopei  staff  278813748 Feb 20 13:04 apache-hive-3.1.2-bin.tar.gz
-rw-r--r--@ 1 wangxiaopei  staff    9623007 Feb 18 20:58 apache-zookeeper-3.5.9-bin.tar.gz
-rw-r--r--  1 wangxiaopei  staff  333553760 Dec 15 08:51 flink-1.12.7-bin-scala_2.11.tgz
-rw-r--r--  1 wangxiaopei  staff  214092195 Feb 20 13:03 hadoop-2.7.3.tar.gz
-rw-r--r--  1 wangxiaopei  staff  118178630 Jul 16  2021 hbase-1.7.1-bin.tar.gz
-rw-r--r--  1 wangxiaopei  staff  194151339 Feb 20 13:03 jdk-8u231-linux-x64.tar.gz
-rwxr-xr-x  1 wangxiaopei  staff   70159813 Mar 10 23:16 kafka_2.11-2.4.1.tgz
-rw-r--r--@ 1 wangxiaopei  staff    2036609 Jun  4  2020 mysql-connector-java-8.0.11.jar
-rw-r--r--  1 wangxiaopei  staff   29114457 Nov 10  2017 scala-2.11.12.tgz
-rwxr-xr-x  1 wangxiaopei  staff  232530699 Mar  2 12:32 spark-2.4.5-bin-hadoop2.7.tgz
-rw-r--r--@ 1 wangxiaopei  staff   17953604 Feb 18 18:01 sqoop-1.4.7.bin__hadoop-2.6.0.tar.gz
```

copy packages to other hosts;
```shell
[root@hadoop001 bigdata]# scp -r software hadoop002:`pwd`
spark-2.4.5-bin-hadoop2.7.tgz                                        100%  222MB 191.0MB/s   00:01
flink-1.12.7-bin-scala_2.11.tgz                                      100%  318MB 173.8MB/s   00:01
apache-hive-3.1.2-bin.tar.gz                                         100%  266MB 122.6MB/s   00:02
apache-flume-1.9.0-bin.tar.gz                                        100%   65MB 129.3MB/s   00:00
mysql-connector-java-8.0.11.jar                                      100% 1989KB 119.7MB/s   00:00
sqoop-1.4.7.bin__hadoop-2.6.0.tar.gz                                 100%   17MB  28.0MB/s   00:00
apache-zookeeper-3.5.9-bin.tar.gz                                    100% 9397KB 169.4MB/s   00:00
hadoop-2.7.3.tar.gz                                                  100%  204MB 143.5MB/s   00:01
jdk-8u231-linux-x64.tar.gz                                           100%  185MB 107.2MB/s   00:01
hbase-1.7.1-bin.tar.gz                                               100%  113MB 112.7MB/s   00:01
kafka_2.11-2.4.1.tgz                                                 100%   67MB 109.7MB/s   00:00
scala-2.11.12.tgz                                                    100%   28MB 103.5MB/s   00:00

[root@hadoop001 bigdata]# scp -r software hadoop003:`pwd`
spark-2.4.5-bin-hadoop2.7.tgz                                        100%  222MB 191.0MB/s   00:01
flink-1.12.7-bin-scala_2.11.tgz                                      100%  318MB 173.8MB/s   00:01
apache-hive-3.1.2-bin.tar.gz                                         100%  266MB 122.6MB/s   00:02
apache-flume-1.9.0-bin.tar.gz                                        100%   65MB 129.3MB/s   00:00
mysql-connector-java-8.0.11.jar                                      100% 1989KB 119.7MB/s   00:00
sqoop-1.4.7.bin__hadoop-2.6.0.tar.gz                                 100%   17MB  28.0MB/s   00:00
apache-zookeeper-3.5.9-bin.tar.gz                                    100% 9397KB 169.4MB/s   00:00
hadoop-2.7.3.tar.gz                                                  100%  204MB 143.5MB/s   00:01
jdk-8u231-linux-x64.tar.gz                                           100%  185MB 107.2MB/s   00:01
hbase-1.7.1-bin.tar.gz                                               100%  113MB 112.7MB/s   00:01
kafka_2.11-2.4.1.tgz                                                 100%   67MB 109.7MB/s   00:00
scala-2.11.12.tgz                                                    100%   28MB 103.5MB/s   00:00

```
## 3. Install

This step will install packages into /opt/module, and add environment variables to /etc/profile;

run "./install.sh 1" on hadoop001;
```shell
[root@hadoop001 bigdata]# ./install.sh 1
....
[root@hadoop001 bigdata]# cd /opt
[root@hadoop001 opt]# ll
total 84
-rwxr-xr-x  1 root root   265 May  2 10:32 clear.sh
-rwxr-xr-x  1 root root   256 May  2 10:32 init-hive.sh
drwxr-xr-x 12 root root  4096 May  2 10:32 module
-rw-r--r--  1 root root   314 May  2 10:32 start-zookeeper.sh
-rw-r--r--  1 root root 62306 May  2 12:45 start.log
-rwxr-xr-x  1 root root  1252 May  2 10:32 start.sh
[root@hadoop001 opt]# ll module/
total 40
drwxr-xr-x  7 root root 4096 May  2 10:32 apache-flume-1.9.0-bin
drwxr-xr-x 10 root root 4096 May  2 10:32 apache-hive-3.1.2-bin
drwxr-xr-x  8 root root 4096 May  2 12:07 apache-zookeeper-3.5.9-bin
drwxr-xr-x 11 root root 4096 May  2 12:41 hadoop-2.7.3
drwxr-xr-x  7 root root 4096 May  2 10:32 hbase-1.7.1
drwxr-xr-x  7   10  143 4096 Oct  5  2019 jdk1.8.0_231
drwxr-xr-x  6 root root 4096 Mar  3  2020 kafka_2.11-2.4.1
drwxrwxr-x  6 1001 1001 4096 Nov 10  2017 scala-2.11.12
drwxr-xr-x 13 1000 1000 4096 Feb  3  2020 spark-2.4.5-bin-hadoop2.7
drwxr-xr-x  9 1000 1000 4096 Dec 19  2017 sqoop-1.4.7.bin__hadoop-2.6.0
```
run "./install.sh 2" on hadoop002;

run "./install.sh 3" on hadoop003;


## 4. Start Service
let the environment variables go effect.
go to /opt, run "./start.sh" on hadoop001 to start all services on three hosts;
```shell
[root@hadoop001 opt]# cat /etc/profile
unset i
unset -f pathmunge
export JAVA_HOME=/opt/module/jdk1.8.0_231
export HADOOP_HOME=/opt/module/hadoop-2.7.3
export HADOOP_COMMON_LIB_NATIVE_DIR=/opt/module/hadoop-2.7.3/lib/native
export HADOOP_OPTS="-Djava.library.path=/opt/module/hadoop-2.7.3/lib"
export ZOOKEEPER_HOME=/opt/module/apache-zookeeper-3.5.9-bin
export HIVE_HOME=/opt/module/apache-hive-3.1.2-bin
export FLUME_HOME=/opt/module/apache-flume-1.9.0-bin
export SQOOP_HOME=/opt/module/sqoop-1.4.7.bin__hadoop-2.6.0
export SCALA_HOME=/opt/module/scala-2.11.12
export SPARK_HOME=/opt/module/spark-2.4.5-bin-hadoop2.7
export HBASE_HOME=/opt/module/hbase-1.7.1
export KAFKA_HOME=/opt/module/kafka_2.11-2.4.1
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/root/bin:/opt/module/jdk1.8.0_231/bin:/opt/module/hadoop-2.7.3/bin:/opt/module/hadoop-2.7.3/sbin:/opt/module/apache-zookeeper-3.5.9-bin/bin:/opt/module/apache-hive-3.1.2-bin/bin:/opt/module/apache-flume-1.9.0-bin/bin:/opt/module/sqoop-1.4.7.bin__hadoop-2.6.0/bin:/opt/module/scala-2.11.12/bin:/opt/module/spark-2.4.5-bin-hadoop2.7/bin:/opt/module/spark-2.4.5-bin-hadoop2.7/sbin:/opt/module/hbase-1.7.1/bin:/opt/module/kafka_2.11-2.4.1/bin

[root@hadoop001 opt]# source /etc/profile
[root@hadoop001 opt]# ./start.sh
...
[root@hadoop001 opt]# jps
13459 ResourceManager
15078 DataNode
14966 NameNode
14472 NodeManager
15196 DFSZKFailoverController
12988 QuorumPeerMain
20685 Jps
14815 JournalNode
```

## 5. Uninstall
go to /opt, and run "clear.sh"; repeat the command in the other hosts.
```shell
[root@hadoop001 opt]# ./clear.sh
```
