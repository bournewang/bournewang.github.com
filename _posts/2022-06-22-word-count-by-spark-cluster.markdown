---
layout: post
title:  "Word counting by spark cluster"
date:   2022-06-22 17:50:20 +0800
categories: bigdata
tags:
    - Bigdata
    - Spark
---

# 1 Create project
## 1.1 pom.xml
Create a maven project in IDEA, and put the following content in pom.xml:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.demo</groupId>
    <artifactId>demo1</artifactId>
    <version>1.0-SNAPSHOT</version>
    <inceptionYear>2008</inceptionYear>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <log4j-core.version>2.17.2</log4j-core.version>
        <spark.version>2.4.5</spark.version>
        <scala.version>2.11.12</scala.version>
    </properties>

    <dependencies>

        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.12</artifactId>
            <version>${spark.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>${log4j-core.version}</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>net.alchim31.maven</groupId>
                <artifactId>scala-maven-plugin</artifactId>
                <version>3.4.6</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
    <repositories>
        <repository>
            <id>scala-tools.org</id>
            <name>Scala-Tools Maven2 Repository</name>
            <url>http://scala-tools.org/repo-releases</url>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>scala-tools.org</id>
            <name>Scala-Tools Maven2 Repository</name>
            <url>http://scala-tools.org/repo-releases</url>
        </pluginRepository>
    </pluginRepositories>
</project>
```

## 1.2 Set scala source directory
Create "src/main/scala" directory, and set it to "Sources" in "Project Structure > Module" page.

## 1.3 Create Task
New a package in "src/main/scala" named "org.demo", under which [New] an Object named "WordCount". 
Put the following code in this file, 
this will count the news.txt and output words which emerged more than 10 times to hdfs:
```scala
package org.demo

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Word Count").setMaster("yarn")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    val rdd = sc.textFile("/dir1/news.txt")
    rdd.flatMap(_.split(" ")).
      map(x => (x, 1)).
      reduceByKey((v1, v2) => v1 + v2).
      filter(x => x._1 != "").
      sortBy(x => x._2, false).
      filter(x => x._2 > 10).
      saveAsTextFile("/dir1/news-count")
    sc.stop()
  }
}
 
```

## 1.4 Compile and build the jar
```shell
(base) ➜  demo1 mvn package -P demo1
[INFO] Scanning for projects...
[INFO]
[INFO] ---------------------------< org.demo:demo1 >---------------------------
[INFO] Building demo1 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ demo1 ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] Copying 0 resource
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ demo1 ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- scala-maven-plugin:3.4.6:compile (default) @ demo1 ---
[INFO] /Users/wangxiaopei/work/demo1/src/main/java:-1: info: compiling
[INFO] /Users/wangxiaopei/work/demo1/src/main/scala:-1: info: compiling
[INFO] Compiling 4 source files to /Users/wangxiaopei/work/demo1/target/classes at 1655889428418
[INFO] prepare-compile in 0 s
[INFO] compile in 3 s
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ demo1 ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /Users/wangxiaopei/work/demo1/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ demo1 ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- scala-maven-plugin:3.4.6:testCompile (default) @ demo1 ---
[INFO] No sources to compile
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ demo1 ---
[INFO] No tests to run.
[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ demo1 ---
[INFO] Building jar: /Users/wangxiaopei/work/demo1/target/demo1-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  5.098 s
[INFO] Finished at: 2022-06-22T17:17:11+08:00
[INFO] ------------------------------------------------------------------------
```

# 2. Run the task 
## 2.1 Send jar to cluster 
```shell
(base) ➜  demo1 scp target/demo1-1.0-SNAPSHOT.jar root@hadoop001:~
demo1-1.0-SNAPSHOT.jar                                                                                         100%   22KB   1.1MB/s   00:00
```
## 2.2 Submit to cluster
```shell
(base) ➜  demo1 ssh root@hadoop001
Last login: Wed Jun 22 17:16:14 2022 from 101.87.93.63
-bash: warning: setlocale: LC_CTYPE: cannot change locale (UTF-8): No such file or directory
[root@hadoop001 ~]#
[root@hadoop001 ~]# spark-submit \
  --master yarn \
  --deploy-mode cluster \
  --class org.demo.WordCount \
  --name hadoop001 \
  demo1-1.0-SNAPSHOT.jar
22/06/22 17:17:38 INFO yarn.Client: Requesting a new application from cluster with 3 NodeManagers
22/06/22 17:17:38 INFO yarn.Client: Verifying our application has not requested more than the maximum memory capability of the cluster (8192 MB per container)
22/06/22 17:17:38 INFO yarn.Client: Will allocate AM container, with 1408 MB memory including 384 MB overhead
22/06/22 17:17:38 INFO yarn.Client: Setting up container launch context for our AM
22/06/22 17:17:38 INFO yarn.Client: Setting up the launch environment for our AM container
22/06/22 17:17:38 INFO yarn.Client: Preparing resources for our AM container
22/06/22 17:17:38 WARN yarn.Client: Neither spark.yarn.jars nor spark.yarn.archive is set, falling back to uploading libraries under SPARK_HOME.
22/06/22 17:17:44 INFO yarn.Client: Uploading resource file:/tmp/spark-02dd2738-4f38-438c-8da0-2111081e66ee/__spark_libs__1362423651026350313.zip -> hdfs://ns/user/root/.sparkStaging/application_1655878482992_0007/__spark_libs__1362423651026350313.zip
22/06/22 17:18:02 INFO yarn.Client: Uploading resource file:/root/demo1-1.0-SNAPSHOT.jar -> hdfs://ns/user/root/.sparkStaging/application_1655878482992_0007/demo1-1.0-SNAPSHOT.jar
22/06/22 17:18:03 INFO yarn.Client: Uploading resource file:/tmp/spark-02dd2738-4f38-438c-8da0-2111081e66ee/__spark_conf__3289509193272400464.zip -> hdfs://ns/user/root/.sparkStaging/application_1655878482992_0007/__spark_conf__.zip
22/06/22 17:18:04 INFO spark.SecurityManager: Changing view acls to: root
22/06/22 17:18:04 INFO spark.SecurityManager: Changing modify acls to: root
22/06/22 17:18:04 INFO spark.SecurityManager: Changing view acls groups to:
22/06/22 17:18:04 INFO spark.SecurityManager: Changing modify acls groups to:
22/06/22 17:18:04 INFO spark.SecurityManager: SecurityManager: authentication disabled; ui acls disabled; users  with view permissions: Set(root); groups with view permissions: Set(); users  with modify permissions: Set(root); groups with modify permissions: Set()
22/06/22 17:18:07 INFO yarn.Client: Submitting application application_1655878482992_0007 to ResourceManager
22/06/22 17:18:08 INFO impl.YarnClientImpl: Submitted application application_1655878482992_0007
22/06/22 17:18:09 INFO yarn.Client: Application report for application_1655878482992_0007 (state: ACCEPTED)
22/06/22 17:18:09 INFO yarn.Client:
	 client token: N/A
	 diagnostics: N/A
	 ApplicationMaster host: N/A
	 ApplicationMaster RPC port: -1
	 queue: default
	 start time: 1655889487798
	 final status: UNDEFINED
	 tracking URL: http://hadoop003:8088/proxy/application_1655878482992_0007/
	 user: root
22/06/22 17:18:10 INFO yarn.Client: Application report for application_1655878482992_0007 (state: ACCEPTED)
22/06/22 17:18:11 INFO yarn.Client: Application report for application_1655878482992_0007 (state: ACCEPTED)
22/06/22 17:18:12 INFO yarn.Client: Application report for application_1655878482992_0007 (state: ACCEPTED)
...
22/06/22 17:25:00 INFO yarn.Client: Application report for application_1655878482992_0007 (state: ACCEPTED)
22/06/22 17:25:01 INFO yarn.Client: Application report for application_1655878482992_0007 (state: ACCEPTED)
22/06/22 17:25:02 INFO yarn.Client: Application report for application_1655878482992_0007 (state: RUNNING)
22/06/22 17:25:02 INFO yarn.Client:
	 client token: N/A
	 diagnostics: N/A
	 ApplicationMaster host: hadoop002
	 ApplicationMaster RPC port: 46755
	 queue: default
	 start time: 1655889487798
	 final status: UNDEFINED
	 tracking URL: http://hadoop003:8088/proxy/application_1655878482992_0007/
	 user: root
22/06/22 17:25:03 INFO yarn.Client: Application report for application_1655878482992_0007 (state: RUNNING)
22/06/22 17:25:04 INFO yarn.Client: Application report for application_1655878482992_0007 (state: RUNNING)
...
22/06/22 17:25:24 INFO yarn.Client: Application report for application_1655878482992_0007 (state: RUNNING)
22/06/22 17:25:25 INFO yarn.Client: Application report for application_1655878482992_0007 (state: RUNNING)
22/06/22 17:25:26 INFO yarn.Client: Application report for application_1655878482992_0007 (state: FINISHED)
22/06/22 17:25:26 INFO yarn.Client:
	 client token: N/A
	 diagnostics: N/A
	 ApplicationMaster host: hadoop002
	 ApplicationMaster RPC port: 46755
	 queue: default
	 start time: 1655889487798
	 final status: SUCCEEDED
	 tracking URL: http://hadoop003:8088/proxy/application_1655878482992_0007/
	 user: root
22/06/22 17:25:26 INFO util.ShutdownHookManager: Shutdown hook called
22/06/22 17:25:26 INFO util.ShutdownHookManager: Deleting directory /tmp/spark-02dd2738-4f38-438c-8da0-2111081e66ee
22/06/22 17:25:26 INFO util.ShutdownHookManager: Deleting directory /tmp/spark-be973890-974c-4df5-84f1-17c15a084935  
```

## 2.3 Check the output
```shell
[root@hadoop001 ~]# hdfs dfs -ls -R /dir1
drwxr-xr-x   - root supergroup          0 2022-06-22 17:25 /dir1/news-count
-rw-r--r--   3 root supergroup          0 2022-06-22 17:25 /dir1/news-count/_SUCCESS
-rw-r--r--   3 root supergroup        242 2022-06-22 17:25 /dir1/news-count/part-00000
-rw-r--r--   3 root supergroup          0 2022-06-22 17:25 /dir1/news-count/part-00001
-rw-r--r--   3 root supergroup      13882 2022-06-01 18:28 /dir1/news.txt
[root@hadoop001 ~]# hdfs dfs -cat /dir1/news-count/part-00000
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
(is,19)
(she,18)
(an,17)
(from,17)
(San,16)
(The,14)
(has,13)
(with,13)
(was,12)
(are,12)
(that,11)
(Casey,11)
(Nightingale-Bamford,11)
(as,11)
(Francisco,11)
```
