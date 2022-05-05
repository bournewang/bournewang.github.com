---
layout: post
title:  "Word Count - find the top ranking words used in an article "
date:   2022-05-03 19:37:44 +0800
categories: bigdata
tags:
    - Bigdata
    - MapReduce
---

# 1. Aim
Create a MapReduce Task by Jar to find the top ranking words in articles.

# 2. Java Development
## 2.1 create a maven project
Create a maven project named '_WordCount_'.

## 2.2 add dependency
in pom.xml, add dependency and reload in maven settings, wait for installing dependency library.
```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>2.7.3</version>
        </dependency>
    </dependencies>
```

## 2.3 add mapper class
Create a package named '_com.yukun_' under 'src/main/java', then create a class named '_WordCountMapper_', add following code:
```java
package com.yukun;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] split = line.split(" ");
        for (String word: split) {
            context.write(new Text(word), new LongWritable(1));
        }
    }
}
```

## 2.4 add reducer class
Create a class named '_WordCountReducer_', add following code:
```java
package com.yukun;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        long count = 0;
        for (LongWritable value: values) {
            count += value.get();
        }
        context.write(key, new LongWritable(count));
    }
}
```

## 2.5 add driver
Create a class named '_JobServerDriver_', add following code:
```java
package com.yukun;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

public class JobServerDriver {
    public static void main(String[] args) throws Exception{
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://hadoop001:9000");
        Job job = Job.getInstance();
        if (args.length > 1) {
            FileSystem.get(conf).delete(new Path(args[1]));
        }

        job.setJobName("JobServerDriver");
        job.setJarByClass(JobServerDriver.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);
        job.waitForCompletion(true);
    }
}
```

## 2.6 build a jar file
* Add Artifacts (Open File > Project Structure > Project Settings > Artifacts > Create -> Jar -> From modules with dependencies).
* Build > Build Artifacts > Build, this action will create a jar under out/artifacts/WordCount_jar

# 3. Run on Hadoop
Copy WordCount.jar to hadoop001.
## 3.1 prepare data
Get 3 news articles from a website, you can download the files at the end of this article. 

upload them to hadoop001, and combine them to a file.

```shell
[root@hadoop001 ~]# wc news1 news2 news3
   72   910  5632 news1
   47   591  3800 news2
   62   737  4450 news3
  181  2238 13882 total
[root@hadoop001 ~]# cat news1 news2 news3 > news  
[root@hadoop001 ~]# wc news
  181  2238 13882 news
```

## 3.2 upload to HDFS
```shell
[root@hadoop001 ~]# hdfs dfs -put news* /
[root@hadoop001 ~]# hdfs dfs -ls /
Found 20 items
-rw-r--r--   3 root supergroup      13882 2022-05-05 11:54 /news
-rw-r--r--   3 root supergroup       5632 2022-05-03 18:05 /news1
-rw-r--r--   3 root supergroup       3800 2022-05-03 18:05 /news2
-rw-r--r--   3 root supergroup       4450 2022-05-03 18:05 /news3
```

## 3.3 Do the word counting
```shell
[root@hadoop001 ~]# hadoop jar WordCount.jar com.yukun.JobServerDriver /news1 /news1-count
[root@hadoop001 ~]# hadoop jar WordCount.jar com.yukun.JobServerDriver /news2 /news2-count
[root@hadoop001 ~]# hadoop jar WordCount.jar com.yukun.JobServerDriver /news3 /news3-count
[root@hadoop001 ~]# hadoop jar WordCount.jar com.yukun.JobServerDriver /news /news-count

[root@hadoop001 ~]# hdfs dfs -ls -R /
-rw-r--r--   3 root supergroup      13882 2022-05-05 11:54 /news
drwxr-xr-x   - root supergroup          0 2022-05-05 11:55 /news-count
-rw-r--r--   3 root supergroup          0 2022-05-05 11:55 /news-count/_SUCCESS
-rw-r--r--   3 root supergroup       8791 2022-05-05 11:55 /news-count/part-r-00000
-rw-r--r--   3 root supergroup       5632 2022-05-03 18:05 /news1
drwxr-xr-x   - root supergroup          0 2022-05-05 11:51 /news1-count
-rw-r--r--   3 root supergroup          0 2022-05-05 11:51 /news1-count/_SUCCESS
-rw-r--r--   3 root supergroup       3855 2022-05-05 11:51 /news1-count/part-r-00000
-rw-r--r--   3 root supergroup       3800 2022-05-03 18:05 /news2
drwxr-xr-x   - root supergroup          0 2022-05-05 11:51 /news2-count
-rw-r--r--   3 root supergroup          0 2022-05-05 11:51 /news2-count/_SUCCESS
-rw-r--r--   3 root supergroup       2645 2022-05-05 11:51 /news2-count/part-r-00000
-rw-r--r--   3 root supergroup       4450 2022-05-03 18:05 /news3
drwxr-xr-x   - root supergroup          0 2022-05-05 11:51 /news3-count
-rw-r--r--   3 root supergroup          0 2022-05-05 11:51 /news3-count/_SUCCESS
-rw-r--r--   3 root supergroup       3374 2022-05-05 11:51 /news3-count/part-r-00000
```

## 3.4 Check the result
show only a part of the content
```shell
[root@hadoop001 ~]# hdfs dfs -cat /news3-count/part-r-00000
	40
...
A	4
ACTIVIST	1
AND	1
APP	1
AREA	1
AS	1
AUTOPSY	1
About	1
According	2
Another	1
Attorney	1
BAY	1
Boudin	2
Boudin,	1
CLICK	1
CONSUME	1
COVID-19	1
CRIME	1
CVS	2
California,	1
Chesa	2
Chronicle	1
Committee	1
County	1
DA	1
DEAD	1
DETAILS	1
DISTRICT	1
DISTURBING	1
DRUGS	1
Digital	1
Digital's	1
District	1
Experts	2
FIGHTING,'	1
FOR	1
FOUND	1
FOX	1
FRANCISCO	2
Federation	1
Fox	4
Francisco	11
...
```

## 3.5 Get the result and retrieve to local computer
```shell
[root@hadoop001 ~]# hdfs dfs -get /news-count/part-r-00000 news-count
[root@hadoop001 ~]# hdfs dfs -get /news1-count/part-r-00000 news1-count
[root@hadoop001 ~]# hdfs dfs -get /news2-count/part-r-00000 news2-count
[root@hadoop001 ~]# hdfs dfs -get /news3-count/part-r-00000 news3-count
[root@hadoop001 ~]# ll
total 38612
-rw-r--r--  1 root root  39424732 May  3 17:40 WordCount.jar
-rw-r--r--  1 root root     13882 May  5 11:59 news
-rw-r--r--  1 root root      8791 May  5 12:09 news-count
-rw-r--r--  1 root root      5632 May  3 18:05 news1
-rw-r--r--  1 root root      3855 May  3 18:06 news1-count
-rw-r--r--  1 root root      3800 May  3 18:05 news2
-rw-r--r--  1 root root      2645 May  3 18:06 news2-count
-rw-r--r--  1 root root      4450 May  3 18:05 news3
-rw-r--r--  1 root root      3374 May  3 18:06 news3-count
[root@hadoop001 ~]# exit
logout
Connection to bj-hadoop001 closed.
(base) ➜  ~ cd tmp
(base) ➜  tmp scp root@bj-hadoop001:~/news-count .
news-count                                                                                                                                                100% 8791   280.2KB/s   00:00
(base) ➜  tmp scp root@bj-hadoop001:~/news1-count .
news1-count                                                                                                                                               100% 3855   124.2KB/s   00:00
(base) ➜  tmp scp root@bj-hadoop001:~/news2-count .
news2-count                                                                                                                                               100% 2645    67.4KB/s   00:00
(base) ➜  tmp scp root@bj-hadoop001:~/news3-count .
news3-count                                                                                                                                               100% 3374   101.0KB/s   00:00
(base) ➜  tmp ll
total 296
-rw-r--r--   1 wangxiaopei  staff   8791 May  5 12:10 news-count
-rw-r--r--   1 wangxiaopei  staff   3855 May  5 12:10 news1-count
-rw-r--r--   1 wangxiaopei  staff   2645 May  5 12:10 news2-count
-rw-r--r--   1 wangxiaopei  staff   3374 May  5 12:10 news3-count
```

## 3.6 Analysing in Excel
Open the count files in Excel, sort the second(count) column in excel, combine the top 10 words from each file, finally, we can get the result:

|rank| news |count| news1 |count| news2   |count| news3 | count |
| --- |-----| --- |-------| --- |-----| --- |-----|-------|
|1| the    |**72**| to    |34| her        |19| of  | 25    |
|2| to     |**63**| the   |33| the        |15| the | 24    |
|3| a      |**51**| of    |21| on         |14| to  | 21    |
|4| of     |**50**| a     |20| in         |13| in  | 18    |
|5| in     |**40**| and   |17| she        |13| has | 8|
|6| on     |**33**| at    |11| Nightingale-Bamford |11| on  | 8|
|7| and    |**32**| on    |11| for        |11| for | 7|
|8| for    |**26**| an    |9| Jezlieanne |10| from | 6|
|9| at     |**24**| in    |9| home       |8| items | 6|
|10| her    |**23**| is    |9| to         |8| out | 6|


Files used in this experiment:

[news1](/other/news1.txt "news1"){:target='_blank'} 

[news2](/other/news2.txt "news2"){:target='_blank'}

[news3](/other/news3.txt "news3"){:target='_blank'}

[news-count](/other/news-count.txt "news-count"){:target='_blank'}

[news1-count](/other/news1-count.txt "news1-count"){:target='_blank'}

[news2-count](/other/news2-count.txt "news2-count"){:target='_blank'}

[news3-count](/other/news3-count.txt "news3-count"){:target='_blank'}

