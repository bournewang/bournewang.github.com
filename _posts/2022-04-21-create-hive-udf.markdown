---
layout: post
title:  "Create Hive UDF with IntelliJ"
date:   2022-04-21 17:29:38 +0800
categories: bigdata
tags:
    - Bigdata 
    - Hive
    - UDF
---

# 1. Create a maven project
* Don't choose any template from the first dialog.
* Enter project name, such as "yukun-udf"; 
* Add your identifier to group id like "org.yukun".

# 2. Add dependency
* Create "lib" folder under project root folder, copy _hive-exec-3.1.2.jar_ from $HIVE_HOME/lib/ to here. 
* Add lib to librarie(Open File > Project Structure > Project Settings > Libraries).
* Add code in pom.xml:
```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.hive</groupId>
            <artifactId>hive-exec</artifactId>
            <version>1.2.1</version>
        </dependency>
    </dependencies>
```

# 3. Complete UDF
Create a Java class on src/main/java folder, enter name: org.yukun.YouClassName, here I create a class "org.yukun.AddFunc"; 

Open this _AddFunc_ class, import UDF class, and add your business logic in function "_evaluate_" :
```java 
package org.yukun;
import org.apache.hadoop.hive.ql.exec.UDF;

public class AddFunc extends UDF{
    public int evaluate (int a, int b) {
        return a+b;
    }

    public int evaluate (int a, int b, int c) {
        return a+b+c;
    }
}
```

# 4. Build jar
* Add Artifacts (Open File > Project Structure > Project Settings > Artifacts > Create -> Jar -> From modules with dependencies).
* Build > Build Artifacts > Build, this action will create a jar under out/artifacts/yukun_udf_jar

# 5. Test
* Copy jar to hadoop001
* Add jar resource and register temporary UDF;
```shell
$ hive
hive> add jar /opt/udf/yukun-udf.jar;
Added [/opt/udf/yukun-udf.jar] to class path
Added resources: [/opt/udf/yukun-udf.jar]
hive> create temporary function addfc as 'org.yukun.AddFunc';
OK
hive> select addfc(1,2);
OK
_c0
3
Time taken: 2.017 seconds, Fetched: 1 row(s)
hive> select addfc(1,2,3);
OK
_c0
6
```

# 6. Create permanent UDF
* put jar into hdfs
```shell
$ hdfs dfs -mkdir -p /user/hive/func/lib
$ hdfs dfs -put /opt/udf/yukun-udf.jar /user/hive/func/lib
$ hdfs dfs -ls /user/hive/func/lib
Found 1 items
-rw-r--r--   3 root supergroup        662 2022-04-21 17:16 /user/hive/func/lib/yukun-udf.jar
```

* register UDF
```shell
$ hive
hive> create function addfc as 'org.yukun.AddFunc' using jar 'hdfs://hadoop001:9000/user/hive/func/lib/yukun-udf.jar';
Added [/opt/module/apache-hive-3.1.2-bin/tmp/e243ba39-fd2e-4acd-85b7-5915f604026c_resources/yukun-udf.jar] to class path
Added resources: [hdfs://hadoop001:9000/user/hive/func/lib/yukun-udf.jar]
OK
hive> select addfc(3,4);
OK
_c0
7
Time taken: 1.994 seconds, Fetched: 1 row(s)
hive> select addfc(3,4,5);
OK
_c0
12
Time taken: 0.083 seconds, Fetched: 1 row(s)
```
Great!
