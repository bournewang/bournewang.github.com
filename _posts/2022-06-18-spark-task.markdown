---
layout: post
title:  "Spark Tasks"
date:   2022-06-20 17:31:06 +0800
categories: bigdata
tags:
    - Bigdata
    - Spark
---

## Task 1
A bookstore selling the following number of books per day in the last N days, 
calculate the average daily sales of each book.

Day 1: ("spark",2), ("hadoop",6),  
Day 2: ("hadoop",4),("spark",6),  
...  

```shell
scala> val rdd = sc.parallelize(Array(("spark",2),("hadoop",6),("hadoop",4),("spark",6)))
rdd: org.apache.spark.rdd.RDD[(String, Int)] = ParallelCollectionRDD[0] at parallelize at <console>:23

scala> rdd.mapValues(x=>(x,1)).
     | reduceByKey((x,y)=>(x._1+y._1, x._2+y._2)).
     | mapValues(x=>x._1/x._2).
     | foreach(println)
(spark,4)
(hadoop,5)

```

## Task 2
A list of accounts and balance stored in a file like: 
```text
hadoop@apache          200
hive@apache            550
yarn@apache            580
hive@apache            159
hadoop@apache          300
hive@apache            258
hadoop@apache          150
yarn@apache            560
yarn@apache            260
```
Aggregate the list by account, sort by account and then by balance.
```shell
scala> val rdd = sc.textFile("/data/balance.txt")
rdd: org.apache.spark.rdd.RDD[String] = /data/balance.txt MapPartitionsRDD[1] at textFile at <console>:23

scala> rdd.
     |   map(x => x.replaceAll("\\s+"," ").split(" ")).
     |   map(x => (x(0),x(1))).
     |   repartition(1).
     |   groupByKey().
     |   mapValues(x => x.toList.sortBy(x=>x)).
     |   sortByKey().
     |   foreach(println)
(hadoop@apache,List(150, 200, 300))
(hive@apache,List(159, 258, 550))
(yarn@apache,List(260, 560, 580))
```

## Task 3
Columns in log file means order_id, user_id, payment amount, and product_id. 
The content of the log file looks like:
```text
100,4287,226,233
101,6562,489,124
102,1124,33,17
103,3267,159,179
104,4569,57,125
105,1438,37,116
```
Calculate the top N payment amount.
```shell
scala> val rdd = sc.textFile("/data/order.log")
rdd: org.apache.spark.rdd.RDD[String] = /data/order.log MapPartitionsRDD[1] at textFile at <console>:23

scala> rdd.
  map(x=>(x.split(',')(2).toInt,1)).
  sortByKey(false).
  top(3).
  foreach(x=>println(x._1))
489
226
159
```

