---
layout: post
title:  "Hadoop Warnings"
date:   2022-04-11 12:54:56 +0800
categories: bigdata
tags:
  - Bigdata
  - Hadoop
  - Debug 
---

# Hadoop Warnings
### After installing Hadoop and related coponents, when I run the hdfs command, I got warning:

```shell
[root@hadoop001 ~]# hdfs dfs -ls /
Java HotSpot(TM) Server VM warning: You have loaded library /opt/module/hadoop-2.10.1/lib/native/libhadoop.so.1.0.0 which might have disabled stack guard. The VM will try to fix the stack guard now.
It's highly recommended that you fix the library with 'execstack -c <libfile>', or link it with '-z noexecstack'.
22/04/11 12:49:41 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
```
add variables to /etc/profile to resolve this:

```shell
echo export HADOOP_COMMON_LIB_NATIVE_DIR=\${HADOOP_HOME}/lib/native >> /etc/profile 
echo export HADOOP_OPTS="-Djava.library.path=\$HADOOP_HOME/lib" >> /etc/profile
source /etc/profile
```

check the command again: 
```shell
[root@hadoop001 ~]# hdfs dfs -ls /
22/04/11 12:52:22 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
```

good, just ignore that.
