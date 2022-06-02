---
layout: post
title:  "Spark RDD - PySpark Word Count"
date:   2022-06-01 19:32:35 +0800
categories: language
tags:
    - Bigdata
    - Spark
    - PySpark
---

```python
from pyspark import SparkContext
```


```python
sc = SparkContext("local","PySpark Word Count Exmaple")
```

    /usr/local/lib/python3.6/site-packages/pyspark/context.py:238: FutureWarning: Python 3.6 support is deprecated in Spark 3.2.
      FutureWarning



```python
tf = sc.textFile("/root/news1.txt", 3)
```


```python
tf.collect()
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




```python
tf1 = tf.flatMap(lambda line: line.split(" ")).filter(lambda w: w)
```


```python
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




```python
tf2 = tf1.map(lambda w: (w,1))
```


```python
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




```python
tf3 = tf2.reduceByKey(lambda a,b:a+b).filter(lambda x:x[1] > 1)
```


```python
tf3.take(10)
```




    [('official', 2),
     ('Cole', 2),
     ('seen', 2),
     ('the', 4),
     ('according', 3),
     ('to', 6),
     ('murder', 2),
     ('escaped', 2),
     ('Casey', 2),
     ('and', 3)]




```python
tf4 = tf3.sortBy(lambda x: x[1], False)
```


```python
tf4.take(10)
```




    [('to', 6),
     ('the', 4),
     ('White,', 4),
     ('according', 3),
     ('and', 3),
     ('on', 3),
     ('a', 3),
     ('official', 2),
     ('Cole', 2),
     ('seen', 2)]




```python
sc.stop()
```
