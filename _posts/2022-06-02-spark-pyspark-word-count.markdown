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
sc = SparkContext("local","PySpark Word Count")
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
tf1.collect()
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
     'to',
     'retire',
     'before',
     'murder',
     'suspect',
     'escaped',
     'Casey',
     'Cole',
     'White,',
     '38',
     'and',
     'Vicky',
     'White,',
     '56,',
     'were',
     'last',
     'seen',
     'Friday',
     'morning',
     'By',
     'Michael',
     'Ruiz',
     '|',
     'Fox',
     'News',
     'An',
     'escaped',
     'Alabama',
     'murder',
     'suspect',
     'is',
     'on',
     'the',
     'run,',
     'possibly',
     'armed',
     'and',
     'dangerous,',
     'as',
     'a',
     'top',
     'official',
     'at',
     'the',
     'jail',
     'who',
     'colleagues',
     'expected',
     'to',
     'retire',
     'has',
     'been',
     'linked',
     'to',
     'the',
     'jailbreak,',
     'according',
     'to',
     'the',
     'local',
     'sheriff.',
     'Casey',
     'Cole',
     'White,',
     '38',
     'and',
     'Vicky',
     'White,',
     '56,',
     'were',
     'last',
     'seen',
     'Friday',
     'morning',
     'on',
     'surveillance',
     'video',
     'ditching',
     'a',
     'marked',
     'vehicle',
     'at',
     'a',
     'parking',
     'lot',
     'on',
     'Florence,',
     'Alabama,',
     'about',
     '70',
     'miles',
     'west',
     'of',
     'Huntsville,',
     'according',
     'to',
     'authorities.',
     'The',
     'two',
     'are',
     'not',
     'related,',
     'according',
     'to',
     'investigators.']




```python
tf2 = tf1.map(lambda w: (w,1))
```


```python
tf2.collect()
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
     ('to', 1),
     ('retire', 1),
     ('before', 1),
     ('murder', 1),
     ('suspect', 1),
     ('escaped', 1),
     ('Casey', 1),
     ('Cole', 1),
     ('White,', 1),
     ('38', 1),
     ('and', 1),
     ('Vicky', 1),
     ('White,', 1),
     ('56,', 1),
     ('were', 1),
     ('last', 1),
     ('seen', 1),
     ('Friday', 1),
     ('morning', 1),
     ('By', 1),
     ('Michael', 1),
     ('Ruiz', 1),
     ('|', 1),
     ('Fox', 1),
     ('News', 1),
     ('An', 1),
     ('escaped', 1),
     ('Alabama', 1),
     ('murder', 1),
     ('suspect', 1),
     ('is', 1),
     ('on', 1),
     ('the', 1),
     ('run,', 1),
     ('possibly', 1),
     ('armed', 1),
     ('and', 1),
     ('dangerous,', 1),
     ('as', 1),
     ('a', 1),
     ('top', 1),
     ('official', 1),
     ('at', 1),
     ('the', 1),
     ('jail', 1),
     ('who', 1),
     ('colleagues', 1),
     ('expected', 1),
     ('to', 1),
     ('retire', 1),
     ('has', 1),
     ('been', 1),
     ('linked', 1),
     ('to', 1),
     ('the', 1),
     ('jailbreak,', 1),
     ('according', 1),
     ('to', 1),
     ('the', 1),
     ('local', 1),
     ('sheriff.', 1),
     ('Casey', 1),
     ('Cole', 1),
     ('White,', 1),
     ('38', 1),
     ('and', 1),
     ('Vicky', 1),
     ('White,', 1),
     ('56,', 1),
     ('were', 1),
     ('last', 1),
     ('seen', 1),
     ('Friday', 1),
     ('morning', 1),
     ('on', 1),
     ('surveillance', 1),
     ('video', 1),
     ('ditching', 1),
     ('a', 1),
     ('marked', 1),
     ('vehicle', 1),
     ('at', 1),
     ('a', 1),
     ('parking', 1),
     ('lot', 1),
     ('on', 1),
     ('Florence,', 1),
     ('Alabama,', 1),
     ('about', 1),
     ('70', 1),
     ('miles', 1),
     ('west', 1),
     ('of', 1),
     ('Huntsville,', 1),
     ('according', 1),
     ('to', 1),
     ('authorities.', 1),
     ('The', 1),
     ('two', 1),
     ('are', 1),
     ('not', 1),
     ('related,', 1),
     ('according', 1),
     ('to', 1),
     ('investigators.', 1)]




```python
tf3 = tf2.reduceByKey(lambda a,b:a+b).filter(lambda x:x[1] > 1)
```


```python
tf3.collect()
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
     ('and', 3),
     ('last', 2),
     ('Alabama', 2),
     ('expected', 2),
     ('retire', 2),
     ('suspect', 2),
     ('White,', 4),
     ('38', 2),
     ('Vicky', 2),
     ('56,', 2),
     ('were', 2),
     ('Friday', 2),
     ('morning', 2),
     ('on', 3),
     ('a', 3),
     ('at', 2)]




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

```
