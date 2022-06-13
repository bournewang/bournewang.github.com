---
layout: post
title:  "Spark RDD - PySpark Word Count"
date:   2022-06-01 19:32:35 +0800
categories: bigdata
tags:
    - Bigdata
    - Spark
    - PySpark
---

# Spark RDD - PySpark Word Count
## 1. Prepare spark context
```python
from pyspark import SparkContext
sc = SparkContext("local","PySpark Word Count Exmaple")
```
    /usr/local/lib/python3.6/site-packages/pyspark/context.py:238: FutureWarning: Python 3.6 support is deprecated in Spark 3.2.
      FutureWarning


## 2. Read text file
```python
tf = sc.textFile("/root/news.txt", 3)
tf.take(10)
```

    ['',
     'Missing Alabama prison official had sold home, was expected to retire before murder suspect escaped',
     'Casey Cole White, 38 and Vicky White, 56, were last seen Friday morning',
     'By Michael Ruiz | Fox News',
     '',
     'An escaped Alabama murder suspect is on the run, possibly armed and dangerous, as a top official at the jail who colleagues expected to retire has been linked to the jailbreak, according to the local sheriff.',
     '',
     'Casey Cole White, 38 and Vicky White, 56, were last seen Friday morning on surveillance video ditching a marked vehicle at a parking lot on Florence, Alabama, about 70 miles west of Huntsville, according to authorities.',
     '',
     'The two are not related, according to investigators.']


## 3. Split every row and put all the words into a list


```python
tf1 = tf.flatMap(lambda line: line.split(" ")).filter(lambda w: w)
tf1.take(10)
```
    ['Missing',
     'Alabama',
     'prison',
     'official',
     'had',
     'sold',
     'home,',
     'was',
     'expected',
     'to']


## 4. Set number 1 to every single word
```python
tf2 = tf1.map(lambda w: (w,1))
tf2.take(10)
```
    [('Missing', 1),
     ('Alabama', 1),
     ('prison', 1),
     ('official', 1),
     ('had', 1),
     ('sold', 1),
     ('home,', 1),
     ('was', 1),
     ('expected', 1),
     ('to', 1)]


## 5. Aggregate the second column for the same word(key)
```python
tf3 = tf2.reduceByKey(lambda a,b:a+b)
tf3.take(10)
```

    [('Missing', 2),
     ('prison', 5),
     ('official', 3),
     ('sold', 2),
     ('before', 6),
     ('Cole', 6),
     ('seen', 6),
     ('Ruiz', 2),
     ('Fox', 10),
     ('the', 72)]


## 6. Sort by the second column(word numbers) descendingly
```python
tf4 = tf3.sortBy(lambda x: x[1], False)
tf4.take(10)
```

    [('the', 72),
     ('to', 63),
     ('a', 51),
     ('of', 50),
     ('in', 40),
     ('on', 33),
     ('and', 32),
     ('for', 26),
     ('at', 24),
     ('her', 23)]


# Combine all these steps in one sentence
```python
tf.flatMap(lambda line: line.split(" "))\
.filter(lambda x:x)\
.map(lambda w:(w,1))\
.reduceByKey(lambda a,b:a+b)\
.sortBy(lambda x:x[1], False)\
.take(10)
```

    [('the', 72),
     ('to', 63),
     ('a', 51),
     ('of', 50),
     ('in', 40),
     ('on', 33),
     ('and', 32),
     ('for', 26),
     ('at', 24),
     ('her', 23)]

```python
sc.stop()
```
