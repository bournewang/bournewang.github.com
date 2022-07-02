---
layout: post
title:  "Flink Realtime word counting"
date:   2022-07-02 17:26:05 +0800
categories: bigdata
tags:
    - Bigdata
    - Flink
---

## Word Count 
Create a maven project, add org.apache.flink dependency in pom.xml.   
the full file can be downloaded at the end of this article.

### Task 1
Create a package named "com.example", and then create a Class named "WordCount" under the package.
```java
package com.example;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class WordCount {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> stream = env.socketTextStream("localhost",1234);
        SingleOutputStreamOperator<Tuple2<String, Integer>> result =
                stream.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String line, Collector<Tuple2<String, Integer>> out) throws Exception {
                        String[] fields = line.split(",");
                        for (String word: fields) {
                            out.collect(new Tuple2<String, Integer>(word, 1));
                        }
                    }
                }).keyBy(0).sum(1);

        result.print();
        try {
            env.execute("wordcount ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

This example will open a socket connection to localhost on port 1234, and do computing as soon as receive messages from the connection.  
Open a terminal and run "nc -l 1234" to open a incoming connection, run program, and then type some words in the "_nc_" window: 
```shell
(base) ➜  nc -l 1234
I love flink
```
once I hit the [Enter] key to send the message, I got the flatMap result from Intellij IDEA run window:
```shell
6> (love,1)
7> (flink,1)
3> (I,1)
```
Type more message in _nc_ window:
```shell
(base) ➜  nc -l 1234
I love flink
I love spark too
```
We got the result from running window:
```shell
5> (too,1)
6> (love,2)
3> (I,2)
1> (spark,1)
```


### Task 2
Create a class named WordCount2 in the same package.  
This time, we use a class WordAndOne in the FlapMapFunction, which attribute can be used in _keyBy_ and _sum_ operator. 
```shell
public class WordCount2 {
    public static void main(String[] args) {
        //...
        SingleOutputStreamOperator<WordAndOne> result = stream.flatMap(new FlatMapFunction<String, WordAndOne>() {
            @Override
            public void flatMap(String s, Collector<WordAndOne> collector) throws Exception {
                String[] words = s.split(" ");
                for (String word: words) {
                    collector.collect(new WordAndOne(word, 1));
                }
            }
        }).keyBy("word").sum("count");
        // ...
    }

    public static class WordAndOne{
        private String word;
        private Integer count;

        public WordAndOne(){}
        public WordAndOne(String word, Integer count) {
            this.word = word;
            this.count = count;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getWord() {
            return word;
        }

        public Integer getCount() {
            return count;
        }

        @Override
        public String toString() {
            return "WordAndOne{" +
                    "word='" + word + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

}
```
Similarly, when we type the following messages in _nc_ window afte running: 
```shell
(base) ➜  ~ nc -l 1234
hello flink
hello spark
I love flink
```
We got the result perfectly:
```shell
7> WordAndOne{word='flink', count=1}
3> WordAndOne{word='hello', count=1}

1> WordAndOne{word='spark', count=1}
3> WordAndOne{word='hello', count=2}

3> WordAndOne{word='I', count=1}
7> WordAndOne{word='flink', count=2}
6> WordAndOne{word='love', count=1}
```


### Task 3
Create a new class WordCount3, this time, we move the main task logical into to class named StringSplitTask:
```java
    public static class StringSplitTask implements FlatMapFunction<String, WordAndOne>{
        @Override
        public void flatMap(String s, Collector<WordAndOne> collector) throws Exception {
            String[] words = s.split(" ");
            for (String word: words) {
                collector.collect(new WordAndOne(word, 1));
            }
        }
    }
```
and the main program simplified to just one line:
```java
SingleOutputStreamOperator<WordAndOne> result =
        stream.flatMap(new StringSplitTask()).keyBy("word").sum("count");
```


### Task 4
Change the stream source to a host and port specified by parameter to make the program more flexible:
```java
import org.apache.flink.api.java.utils.ParameterTool;

ParameterTool paras = ParameterTool.fromArgs(args);
DataStreamSource<String> stream = env.socketTextStream(paras.get("host"), paras.getInt("port"));
```

the code can be downloaded here:  
[pom.xml](/other/code/flinktasks/pom.xml){:target="_blank"}  
[WordCount](/other/code/flinktasks/src/main/java/com/example/WordCount.java){:target="_blank"}  
[WordCount2](/other/code/flinktasks/src/main/java/com/example/WordCount2.java){:target="_blank"}  
[WordCount3](/other/code/flinktasks/src/main/java/com/example/WordCount3.java){:target="_blank"}  
[WordCount4](/other/code/flinktasks/src/main/java/com/example/WordCount4.java){:target="_blank"}  
