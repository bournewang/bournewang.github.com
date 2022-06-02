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
**find the top ranking words used in an article**

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

## 2. Load text file
```shell
scala> val tf = sc.textFile("/dir1/news.txt")
f: org.apache.spark.rdd.RDD[String] = /dir1/news.txt MapPartitionsRDD[15] at textFile at <console>:24
```

## 3. Split every row and put all the words into a list
## 4. Set number 1 to every single word
```shell
// get each word
scala> val f1 = f.flatMap(x => x.split(" ")).map(x=>(x,1))
f1: org.apache.spark.rdd.RDD[(String, Int)] = MapPartitionsRDD[17] at map at <console>:25
```

## 5. Aggregate the second column for the same word(key)
```shell
// get the word count
scala> val f2 = f1.reduceByKey((a,b) => a+b)
f2: org.apache.spark.rdd.RDD[(String, Int)] = ShuffledRDD[18] at reduceByKey at <console>:27

scala> f2.collect
res23: Array[(String, Int)] = Array((someone,1), (Corrections),1), (call,4), (inmate,5), (arrested,1), (transferred,1), (afternoon,1), (Mateo,1), (Chesa,2), (behind,1), (happen.",1), (been,8), (begins,1), (DEAD,1), (clip,1), (over,1), (Girl.",1), (Nightingale-Bamford,11), (any,1), (CLICK,3), (grab,1), (instead,1), (offering,2), (ex-con,1), (month,1), (Seth,1), (million,1), (tips,3), (shootings,1), (are,12), (parts,1), (2015,1), (?WE?RE,1), (times,1), (Girl,,2), (into,,1), (TEEN,1), (safely,1), (our,1), (go,,1), (plotted,1), (going,1), (1988.,1), (Ziegesar,,1), (them,2), (conference,1), (assistant,1), (planned,1), (US,2), (GIRL,2), (retiring,,1), (According,3), (dealing,1), (viral,1), (long,1), (HERE,3), (train,1), (McDermid),1), (goods.,1), (evaluation,1), (even,1), (Girl,1), (Connie,1)...
```

## 6. Sort by the second column(word numbers) descendingly
```shell
// sort by word count desc
scala> val f4 = f3.filter(x => !x._1.isEmpty).sortBy(x => x._2, false)
f4: org.apache.spark.rdd.RDD[(String, Int)] = MapPartitionsRDD[52] at sortBy at <console>:25

// get the top 10 words
scala> f4.take(10)
res45: Array[(String, Int)] = Array((the,72), (to,63), (a,51), (of,50), (in,40), (on,33), (and,32), (for,26), (at,24), (her,23))

scala> f4.take(10).foreach(println)
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
