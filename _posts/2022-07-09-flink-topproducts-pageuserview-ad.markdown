---
layout: post
title:  "Flink Tasks: Top Hot Products/Page View"
date:   2022-07-09 23:23:30 +0800
categories: bigdata
tags:
    - Bigdata
    - Flink
---
## 1. Top Hot Products

Get top hot products according log continuously. The log has such columns: userId,productId,categoryId,behavior,timeStamp,sessionId, and the sample data looks like: 

```text
1021615,4355712,15138396,P,1601688552,913d5742-0fd8-46db-bb26-8cf0d09d90da
2178695,12498888,2595117,P,1601688552,3d29d1e2-d514-4991-bd79-9a866a10ad97
4306505,17843562,462120,P,1601688552,2dd496f4-7615-40f5-9d9e-bd5eab113519
```

### 1.1 Program
Create directory src/main/scala, and set it as "Sources" in [_**Project Structure > Module**_] page.

Create a case class named "UserBehavior" to define data model for each line:
```scala
package org.yukun

case class UserBehavior(
                         userId: Long,
                         productId: Long,
                         categoryId: Long,
                         behavior: String,
                         timeStamp: Long,
                         sessionId: String
                       )
```

Create a class named "Utils" to parse line convert to an UserBehavior:
```scala
package org.yukun

object Utils {
  def string2UserBehavior(line:String):UserBehavior ={
    val fields = line.split(",")
    UserBehavior(
      fields(0).trim.toLong,
      fields(1).trim.toLong,
      fields(2).trim.toLong,
      fields(3).trim,
      fields(4).trim.toLong,
      fields(5).trim
    )
  }
}
```

Create a class to define EventTimeExtractor:
```scala
package org.yukun

import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.watermark.Watermark

class EventTimeExtractor extends AssignerWithPeriodicWatermarks[UserBehavior] {
  var currentMaxEventTime = 0L;
  val maxOutOfOrderness = 10;
  override def getCurrentWatermark: Watermark = {
    new Watermark((currentMaxEventTime - maxOutOfOrderness) * 1000)
  }

  override def extractTimestamp(t: UserBehavior, l: Long): Long = {
    val timeStamp = t.timeStamp * 1000
    currentMaxEventTime = Math.max(timeStamp, currentMaxEventTime)
    timeStamp
  }
}
```

Create an object named "HotProduct":

```scala
package org.yukun

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPeriodicWatermarks, KeyedProcessFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import java.sql.Timestamp
import scala.collection.mutable.ListBuffer

object HotProduct {

  case class ProductViewCount(productId: Long, windowEnd:Long, count: Long)

  def main(args: Array[String]): Unit = {
    println("hello world!")
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.readTextFile("./data/data1.csv")
      .map(Utils.string2UserBehavior(_))
      .assignTimestampsAndWatermarks(new EventTimeExtractor)
      .filter(_.behavior == "P")
      .keyBy(_.productId)
      .timeWindow(Time.hours(1), Time.minutes(5))
      .aggregate(new CountProduct, new WindowResult)
      .keyBy(_.windowEnd)
      .process(new TopHotProduct(3))
      .print

    env.execute("Hot Products ...")
  }

  class TopHotProduct(topN: Int) extends KeyedProcessFunction[Long, ProductViewCount, String] {
    private var productState:ListState[ProductViewCount] = _

    override def open(parameters: Configuration): Unit = {
      productState = getRuntimeContext.getListState(
        new ListStateDescriptor[ProductViewCount](
          "product-state",
          classOf[ProductViewCount]
        )
      )
    }

    override def processElement(i: ProductViewCount,
                                context: KeyedProcessFunction[Long, ProductViewCount, String]#Context,
                                collector: Collector[String]): Unit = {
      productState.add(i)
      context.timerService().registerEventTimeTimer(i.windowEnd + 1)
    }

    override def onTimer(timestamp: Long,
                         ctx: KeyedProcessFunction[Long, ProductViewCount, String]#OnTimerContext,
                         out: Collector[String]): Unit = {
      val allProduct:ListBuffer[ProductViewCount] = new ListBuffer[ProductViewCount]
      val iterable = productState.get.iterator()
      while (iterable.hasNext) {
        allProduct += iterable.next()
      }

      val sortedProducts = allProduct.sortWith(_.count > _.count).take(topN)
      productState.clear()

      val sb = new StringBuilder
      sb.append("Time: " + (new Timestamp(timestamp - 1)) + "\n")
      sortedProducts.foreach(product => {
        sb.append("product Id: "+product.productId +", access: " + product.count + "\n")
      })

      sb.append("===================")
      out.collect(sb.toString())
    }
  }
  class CountProduct extends AggregateFunction[UserBehavior, Long, Long] {
    override def createAccumulator(): Long = 0L

    override def add(in: UserBehavior, acc: Long): Long = acc + 1

    override def getResult(acc: Long): Long = acc

    override def merge(acc: Long, acc1: Long): Long = acc + acc1
  }

  class WindowResult extends WindowFunction[Long, ProductViewCount, Long,TimeWindow] {
    override def apply(key: Long,
                       window: TimeWindow,
                       input: Iterable[Long],
                       out: Collector[ProductViewCount]): Unit = {
      out.collect(ProductViewCount(key, window.getEnd, input.iterator.next()))
    }
  }

}

```

### 1.3 Run

```shell
Time: 2020-10-03 09:30:00.0
product Id: 10601868, access: 2
product Id: 27358782, access: 2
product Id: 18916440, access: 2
===================
Time: 2020-10-03 09:35:00.0
product Id: 28427400, access: 3
product Id: 1325508, access: 3
product Id: 5191422, access: 3
===================
Time: 2020-10-03 09:40:00.0
product Id: 21123024, access: 6
product Id: 14188074, access: 5
product Id: 3104010, access: 5

```

## 2. Page View
Compute page view every 10 seconds according previous log data.

### 2.1 Program
Create a class named "PageView", and put the following code, in which we use classes such as EventTimeExtractor/Utils defined early.

```scala
package org.yukun

import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time

object PageView {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    env.readTextFile("./data/data1.csv")
      .map(Utils.string2UserBehavior(_))
      .assignTimestampsAndWatermarks(new EventTimeExtractor)
      .filter(_.behavior == "P")
      .map(data => ("P", 1))
      .timeWindowAll(Time.seconds(10))
      .sum(1)
      .print()

    env.execute("Page View")
  }
}
```

### 2.2 Run
Run the program, we get the following output every 10 seconds:
```shell
(P,130)
(P,146)
(P,140)
(P,139)
(P,126)
(P,141)
(P,129)
(P,37)
```


## 3. Top N Hot Page

### 3.1 Requirement
Get top 3 hot pages of the last 10 minutes every 5 seconds, sameple log:
```text
83.149.9.123 - - 17/05/2020:10:05:03 +0000 GET /presentations/logstash-kafkamonitor-2020/images/kibana-search.png
83.149.9.123 - - 17/05/2020:10:05:43 +0000 GET /presentations/logstash-kafkamonitor-2020/images/kibana-dashboard3.png
83.149.9.123 - - 17/05/2020:10:05:47 +0000 GET /presentations/logstash-kafkamonitor-2020/plugin/highlight/highlight.js
```

### 3.2 Program
Create a class named "HotPage"

```scala
package org.yukun

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPeriodicWatermarks, KeyedProcessFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import java.sql.Timestamp
import scala.collection.mutable.ListBuffer

case class ApacheLogEvent(
                   ip: String,
                   userId: String,
                   eventTime: Long,
                   method: String,
                   url: String
                   )

case class UrlViewCount(
                       url:String,
                       windowEnd: Long,
                       count: Long
                       )
object HotPage {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    env.readTextFile("./data/access.log")
      .map(Utils.string2ApacheLogEvent(_))
      .assignTimestampsAndWatermarks(new HotPageEventTimeExtractor)
      .keyBy(_.url)
      .timeWindow(Time.minutes(10), Time.seconds(5))
      .aggregate(new PageCountAgg, new PageWindowResult)
      .keyBy(_.windowEnd)
      .process(new TopNHotPage(3))
      .print()

    env.execute("Hot Page Count")
  }

  class HotPageEventTimeExtractor extends AssignerWithPeriodicWatermarks[ApacheLogEvent]{

    var currentMaxEventTime = 0L
    val maxOufOfOrderness = 10000
    override def getCurrentWatermark: Watermark = {
      new Watermark(currentMaxEventTime - maxOufOfOrderness)
    }

    override def extractTimestamp(element: ApacheLogEvent, previousElementTimestamp: Long): Long = {
      val timestamp = element.eventTime
      currentMaxEventTime = Math.max(element.eventTime, currentMaxEventTime)
      timestamp;
    }
  }

  class PageCountAgg extends AggregateFunction[ApacheLogEvent, Long, Long] {
    override def createAccumulator(): Long = 0L

    override def add(in: ApacheLogEvent, acc: Long): Long = acc + 1

    override def getResult(acc: Long): Long = acc

    override def merge(acc: Long, acc1: Long): Long = acc + acc1
  }

  class PageWindowResult extends WindowFunction[Long, UrlViewCount, String, TimeWindow] {
    override def apply(key: String,
                       window: TimeWindow,
                       input: Iterable[Long],
                       out: Collector[UrlViewCount]): Unit = {
      out.collect(UrlViewCount(key, window.getEnd, input.iterator.next()))
    }
  }

  class TopNHotPage(topN:Int) extends KeyedProcessFunction[Long, UrlViewCount, String] {
    lazy val urlState: MapState[String, Long] =
      getRuntimeContext.getMapState(new MapStateDescriptor[String, Long](
        "url-state", classOf[String], classOf[Long]
      ))

    override def processElement(i: UrlViewCount,
                                context: KeyedProcessFunction[Long, UrlViewCount, String]#Context,
                                collector: Collector[String]): Unit = {
      urlState.put(i.url, i.count)
      context.timerService().registerEventTimeTimer(i.windowEnd + 1)
    }

    override def onTimer(timestamp: Long,
                         ctx: KeyedProcessFunction[Long, UrlViewCount, String]#OnTimerContext,
                         out: Collector[String]): Unit = {
      val allUrlViews:ListBuffer[(String, Long)] = new ListBuffer[(String, Long)]
      val iter = urlState.entries().iterator()
      while (iter.hasNext) {
        val entry = iter.next()
        allUrlViews += ((entry.getKey, entry.getValue))
      }

      urlState.clear()

      val sortedUrlView = allUrlViews.sortWith(_._2 > _._2).take(topN)

      val result = new StringBuilder
      result.append("Time: "+(new Timestamp(timestamp-1))+"\n")
      sortedUrlView.foreach(view => {
        result.append("URL: "+view._1 +" Access: " +view._2 +"\n")
      })
      result.append("------------")
      out.collect(result.toString())
    }
  }
}

```

### 3.2 Run
Run the program, we get the following output every 10 seconds:
```shell
Time: 2020-05-20 06:15:25.0
URL: /style2.css Access: 9
URL: /reset.css Access: 9
URL: /images/jordan-80.png Access: 9
------------
Time: 2020-05-20 06:15:30.0
URL: /images/jordan-80.png Access: 9
URL: /style2.css Access: 8
URL: /favicon.ico Access: 8
------------
Time: 2020-05-20 06:15:35.0
URL: /images/jordan-80.png Access: 9
URL: /favicon.ico Access: 7
URL: /style2.css Access: 6
------------
```


## 4. Download
Project code and data download:  
[Flink Tasks](/other/code/FlinkScala.tar.gz){:target="_blank"}
