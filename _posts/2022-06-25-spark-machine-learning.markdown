---
layout: post
title:  "Spark Machine Learning"
date:   2022-06-25 18:45:17 +0800
categories: bigdata
tags:
    - Bigdata
    - Spark
    - Spark ML 
    - Machine Learning
---

# 1 Core concept
* **Transformer**, implement transform() method which can transform a dataframe to another dataframe with more columns;
* **Estimator**, implement fit() method which manipulate a dataframe and produce a transformer/model.
* **Pipeline**, connect multiple transformer and estimator to a work flow.



# 2 Task
## 2.1 predict whether a line contains word "spark"

```shell
[root@hadoop001 ~]# spark-shell
Spark context Web UI available at http://hadoop001:4040
Spark context available as 'sc' (master = local[*], app id = local-1656150455596).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  `_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.4.5
      /_/

Using Scala version 2.11.12 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_231)
Type in expressions to have them evaluated.
Type :help for more information.

scala>

scala> import spark.implicits._
import spark.implicits._

scala> import org.apache.spark.ml.feature._
import org.apache.spark.ml.feature._

scala> import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.classification.LogisticRegression

scala> import org.apache.spark.ml.{Pipeline,PipelineModel}
import org.apache.spark.ml.{Pipeline, PipelineModel}

scala> import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.linalg.Vector

scala> import org.apache.spark.sql.Row
import org.apache.spark.sql.Row

scala> val training = spark.createDataFrame(Seq(
    | (0L, "a b c d e spark", 1.0), 
    | (1L, "b d", 0.0), 
    | (2L, "spark f g h", 1.0),
    | (3L, "hadoop mapreduce", 0.0)
    | )).toDF("id", "text", "label")
training: org.apache.spark.sql.DataFrame = [id: bigint, text: string ... 1 more field]


scala> training.printSchema
root
 |-- id: long (nullable = false)
 |-- text: string (nullable = true)
 |-- label: double (nullable = false)


scala> training.show
+---+----------------+-----+
| id|            text|label|
+---+----------------+-----+
|  0| a b c d e spark|  1.0|
|  1|             b d|  0.0|
|  2|     spark f g h|  1.0|
|  3|hadoop mapreduce|  0.0|
+---+----------------+-----+


scala> val tokenizer = new Tokenizer().
    | setInputCol("text").
    | setOutputCol("words")
tokenizer: org.apache.spark.ml.feature.Tokenizer = tok_c274e39d1269

scala> val hashingTF = new HashingTF().
     | setNumFeatures(1000).
     | setInputCol(tokenizer.getOutputCol).
     | setOutputCol("features")
hashingTF: org.apache.spark.ml.feature.HashingTF = hashingTF_4b69551b45e1

scala> val lr = new LogisticRegression().
     | setMaxIter(10).
     | setRegParam(0.01)
lr: org.apache.spark.ml.classification.LogisticRegression = logreg_fe387b0c61e0

scala> val pipeline = new Pipeline().
     | setStages(Array(tokenizer, hashingTF, lr))
pipeline: org.apache.spark.ml.Pipeline = pipeline_079a563f3f95

scala> val model = pipeline.fit(training)
model: org.apache.spark.ml.PipelineModel = pipeline_079a563f3f95

scala> val test = spark.createDataFrame(Seq(
     | (4L, "spark i j k"),
     | (5L, "l m n"),
     | (6L, "spark a"),
     | (7L, "apache hadoop")
     | )).toDF("id", "text")
test: org.apache.spark.sql.DataFrame = [id: bigint, text: string]

scala> model.transform(test).
     | select("id", "text", "probability", "prediction").
     | collect().
     | foreach{case Row(id:Long, text:String, prob:Vector, prediction:Double) => println(s"($id, $text) --> prob=$prob, prediction=$prediction")}
(4, spark i j k) --> prob=[0.540643354485232,0.45935664551476796], prediction=0.0
(5, l m n) --> prob=[0.9334382627383527,0.06656173726164716], prediction=0.0
(6, spark a) --> prob=[0.1504143004807332,0.8495856995192668], prediction=1.0
(7, apache hadoop) --> prob=[0.9768636139518375,0.02313638604816238], prediction=0.0

```
We can see the model failed to predict 4L "spark i j k".

## 2.2 training with a larger dataset
Add more lines in training data set, and mark the lines contain "spark" with 1.0 .
```shell
scala> val training1 = spark.createDataFrame(Seq(
    | (0L, "a b c d e spark", 1.0), 
    | (1L, "b d", 0.0), 
    | (2L, "spark f g h", 1.0),
    | (3L, "hadoop mapreduce", 0.0)
    | (4L, "hello spark", 1.0)
    | (5L, "spark is a good framework", 1.0)
    | (6L, "many developers in the world are using spark, which is a good tool!", 1.0)
    | (7L, "spark contains 4 components", 1.0)
    | (8L, "big data platform hadoop", 0.0)
    | (9L, "someone like flink", 0.0)
    | (10L, "Hdfs is a great invention", 0.0)
    | )).toDF("id", "text", "label")
    
scala> training1.show
+---+--------------------+-----+
| id|                text|label|
+---+--------------------+-----+
|  0|     a b c d e spark|  1.0|
|  1|                 b d|  0.0|
|  2|         spark f g h|  1.0|
|  3|    hadoop mapreduce|  0.0|
|  4|         hello spark|  1.0|
|  5|spark is a good f...|  1.0|
|  6|many developers i...|  1.0|
|  7|spark contains 4 ...|  1.0|
|  8|big data platform...|  0.0|
|  9|  someone like flink|  0.0|
| 10|Hdfs is a great i...|  0.0|
+---+--------------------+-----+

scala> val model1 = pipeline.fit(training1)
model1: org.apache.spark.ml.PipelineModel = pipeline_079a563f3f95

scala> model1.transform(test).
     |       select("id", "text", "probability", "prediction").
     |       collect().
     |       foreach{case Row(id:Long, text:String, prob:Vector, prediction:Double) => println(s"($id, $text) --> prob=$prob, prediction=$prediction")}
(4, spark i j k) --> prob=[0.18786259772300723,0.8121374022769927], prediction=1.0
(5, l m n) --> prob=[0.7648742169322029,0.23512578306779716], prediction=0.0
(6, spark a) --> prob=[0.09369154387609983,0.9063084561239001], prediction=1.0
(7, apache hadoop) --> prob=[0.9276280847186241,0.07237191528137597], prediction=0.0
       
```

this time, the model1 perfectly predict the test data.
