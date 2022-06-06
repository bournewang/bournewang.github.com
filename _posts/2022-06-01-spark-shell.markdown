---
layout: post
title:  "Spark RDD - Spark Shell Word Count"
date:   2022-06-01 19:32:35 +0800
categories: language
tags:
    - Bigdata
    - Spark
---

# Spark RDD - Spark Shell Word Count
**find the top 10 ranking words used in an article**

## 1. upload news file to hdfs
```shell
[root@hadoop001 ~]# hdfs dfs -put news.txt /dir1
[root@hadoop001 ~]# hdfs dfs -ls /dir1
-rw-r--r--   3 root supergroup      13882 2022-06-01 18:28 /dir1/news.txt
```

## 2. Start Spark Shell
```shell
[root@hadoop001 ~]# spark-shell
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/module/spark-2.4.5-bin-hadoop2.7/jars/slf4j-log4j12-1.7.16.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/module/hadoop-2.7.3/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]
22/06/01 19:35:58 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
Spark context Web UI available at http://hadoop001:4040
Spark context available as 'sc' (master = local[*], app id = local-1654083402486).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  ''_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.4.5
      /_/

Using Scala version 2.11.12 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_231)
Type in expressions to have them evaluated.
Type :help for more information.

```

## 3. Load text file
```shell
scala> val tf = sc.textFile("/dir1/news.txt")
f: org.apache.spark.rdd.RDD[String] = /dir1/news.txt MapPartitionsRDD[15] at textFile at <console>:24
```

## 4. transform the RDD and made some actions to get the top 10 ranking words
```shell
scala> tf.flatMap(line =>line.split(" ")).
     | filter(x=>x.nonEmpty).
     | map(x=>(x,1)).
     | reduceByKey((a,b)=>a+b).
     | sortBy(x=>x._2, false).
     | take(10).
     | foreach(println)
(the,72)
(to,63)
(a,51)
(of,50)
(in,40)
(on,33)
(and,32)
(for,26)
(at,24)
(her,23)
```


|method| description                                               |
|-----|-----------------------------------------------------------|
|flatMap(line =>line.split(" "))| split each row by space and put all the words in an array |
|filter(x=>x.nonEmpty)| root out emepty element                                   |
|map(x=>(x,1))| give each word an initial number - 1                      |
|reduceByKey((a,b) => a+b)| aggregate the numbers for every same word(key)            |
|sortBy(x=>x._2, false)| sort by number(2nd column) descendingly                   |
|take(10)| take the top 10 element                                   |
|foreach(println)| print each element                                        |
