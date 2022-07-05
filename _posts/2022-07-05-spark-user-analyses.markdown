---
layout: post
title:  "Spark User Analyses"
date:   2022-07-05 17:26:05 +0800
categories: bigdata
tags:
    - Bigdata
    - Spark
---

## 1. Requirement

Analysis of the distribution of users by province, age group and sex, based on user registration data from a website.

The sample of desensitised data looks like:   
```json
{"AGE":48,"BIRTHDAY":"1973","BPLACE":"吉林省吉林市磐石县","IDTYPE":"01","RNAME":"崔**","SEX":"女"}
{"AGE":45,"BIRTHDAY":"1976","BPLACE":"四川省重庆市綦江县","IDTYPE":"01","RNAME":"朱**","SEX":"男"}
{"AGE":26,"BIRTHDAY":"1995","BPLACE":"浙江省杭州市富阳市","IDTYPE":"01","RNAME":"骆**","SEX":"男"}
```

_The full file and project code are provided at the end of this article_.

## 2. Developemnt

### 2.1 pom.xml
Create a maven project, and load scala/spark/hadoop/maven-compiler-plugin in pom.xml:
```xml
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <scala.version>2.11.8</scala.version>
        <spark.version>2.3.0</spark.version>
        <hadoop.version>2.6.5</hadoop.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.11</artifactId>
            <version>${spark.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-sql_2.11</artifactId>
            <version>${spark.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>${hadoop.version}</version>
        </dependency>
    </dependencies>
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <source>1.8</source>
                        <target>1.8</target>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>    
```

### 2.2 Program
Create directory src/main/scala, and set it as "Sources" in [_**Project Structure > Module**_] page.  

Create a package and an object UserReport under this package. 

#### 2.2.1 Get province
we need to extract the province from address, there we need to do it by registering a UDF.
In your development, you better debug the UDF first to ensure its bug free.
```shell
scala> def getProvince(addr: String):String = {
     |       try {
     |         val p = "^(.*省|.*自治区|.*市).*".r
     |         val p(province) = addr
     |         province
     |       }catch{
     |         case _ => ""
     |       }
     |     }
<console>:32: warning: This catches all Throwables. If this is really intended, use `case _ : Throwable` to clear this warning.
               case _ => ""
                    ^
getProvince: (addr: String)String

scala> getProvince("广西壮族自治区南宁地区横县")
res63: String = 广西壮族自治区

scala> getProvince("广东省汕头市潮州市")
res64: String = 广东省

scala> getProvince("浙江省温州市永嘉县")
res65: String = 浙江省

scala> getProvince("上海市黄浦区打浦路")
res66: String = 上海市
```

#### 2.2.2 Get age group
Based on a user's age, we want get age range such as 30s/40s/50s, also we need a UDF to do this:
```shell
scala> def getAgeGroup (age: Int) = {
     |       try {
     |         val g = (age/10) * 10
     |         if (g == 0){
     |           "1-9"
     |         } else {
     |           g.toString + "s"
     |         }
     |       } catch{
     |         case _ => ""
     |       }
     |     }
<console>:36: warning: This catches all Throwables. If this is really intended, use `case _ : Throwable` to clear this warning.
               case _ => ""
                    ^
getAgeGroup: (age: Int)String

scala> getAgeGroup(5)
res72: String = 1-9

scala> getAgeGroup(9)
res73: String = 1-9

scala> getAgeGroup(33)
res67: String = 30s

scala> getAgeGroup(49)
res69: String = 40s

scala> getAgeGroup(56)
res70: String = 50s

scala> getAgeGroup(102)
res71: String = 100s
```

#### 2.2.3 SQL analyses
put the following code in UserReport.scala:
```java
package org.yukun

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{udf, when}

object UserReport {
  def main(args: Array[String]): Unit = {
    println("hello user report");
    if (args.length < 1) {
      println(
        """
          |Usage: org.yukun.UserReport <data-path>
          |""".stripMargin)
      System.exit(0)
    }

    val Array(dataPath) = args
    val conf = new SparkConf()
    conf.setAppName("User Report")
    conf.setMaster("local")
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val df = spark.read.json(dataPath)
    df.createOrReplaceTempView("persons")

    val getProvince = udf((addr: String) => {
      try {
        val p = "^(.*省|.*自治区|.*市).*".r
        val p(province) = addr
        province
      }catch{
        case _ => ""
      }
    })
    val getAgeGroup = udf((age: Int) => {
      try {
        val g = (age/10) * 10
        if (g == 0){
          "1-9"
        } else {
          g.toString + "s"
        }
      } catch{
        case _ => ""
      }
    })
    spark.udf.register("getProvince", getProvince)
    spark.udf.register("getAgeGroup", getAgeGroup)
    spark.sql("select getProvince(BPLACE) as province, count(*) as total from persons group by province order by total desc")
      .repartition(1).write.json("./report/province")

    spark.sql("select SEX, count(*) as total from persons group by SEX order by total desc")
      .repartition(1).write.json("./report/sex")

    spark.sql("select getAgeGroup(AGE) as age_group, count(*) as total from persons group by age_group order by age_group")
      .repartition(1).write.json("./report/age-group")

    spark.stop
  }
}

```

## 3. Run
Click [**_Run -> Edit Configurations_**], in the _**Program arguments**_ field, input data path " ./data/person_info_1000.json".
Run the program, we get the result in _report_ directory:
```shell
(base) ➜  SparkUserReport find report 
report
report/sex
report/sex/._SUCCESS.crc
report/sex/part-00000-9e393740-8e74-4ad8-be54-17cc795bcf5d-c000.json
report/sex/_SUCCESS
report/sex/.part-00000-9e393740-8e74-4ad8-be54-17cc795bcf5d-c000.json.crc
report/province
report/province/part-00000-6decd3c2-1a58-476f-870a-08e136875bb3-c000.json
report/province/._SUCCESS.crc
report/province/.part-00000-6decd3c2-1a58-476f-870a-08e136875bb3-c000.json.crc
report/province/_SUCCESS
report/age-group
report/age-group/._SUCCESS.crc
report/age-group/_SUCCESS
report/age-group/.part-00000-b91f9ff5-f13c-4b19-b808-ec5bf10e2bac-c000.json.crc
report/age-group/part-00000-b91f9ff5-f13c-4b19-b808-ec5bf10e2bac-c000.json

(base) ➜  SparkUserReport cat report/sex/part-00000-9e393740-8e74-4ad8-be54-17cc795bcf5d-c000.json 
{"SEX":"男","total":523}
{"SEX":"女","total":470}
{"total":7}
(base) ➜  SparkUserReport cat report/province/part-00000-6decd3c2-1a58-476f-870a-08e136875bb3-c000.json 
{"province":"江苏省","total":104}
{"province":"安徽省","total":100}
{"province":"四川省","total":97}
{"province":"湖南省","total":71}
{"province":"浙江省","total":69}
{"province":"河南省","total":62}
{"province":"辽宁省","total":49}
{"province":"湖北省","total":48}
{"province":"山东省","total":43}
{"province":"广东省","total":36}
{"province":"江西省","total":36}
{"province":"上海市","total":33}
{"province":"甘肃省","total":32}
{"province":"福建省","total":26}
{"province":"黑龙江省","total":26}
{"province":"河北省","total":21}
{"province":"广西壮族自治区","total":20}
{"province":"山西省","total":19}
{"province":"吉林省","total":19}
{"province":"陕西省","total":12}
{"province":"重庆市","total":10}
{"province":"内蒙古自治区","total":10}
{"province":"贵州省","total":10}
{"province":"","total":9}
{"province":"云南省","total":9}
{"province":"湖北省省","total":6}
{"province":"内蒙古巴彦淖尔盟临河市","total":6}
{"province":"青海省","total":5}
{"province":"天津市","total":4}
{"province":"新疆维吾尔自治区","total":3}
{"province":"北京市","total":2}
{"province":"宁夏回族自治区","total":2}
{"province":"广西桂林市","total":1}
(base) ➜  SparkUserReport cat report/age-group/part-00000-b91f9ff5-f13c-4b19-b808-ec5bf10e2bac-c000.json
{"total":7}
{"age_group":"1-9","total":32}
{"age_group":"100s","total":7}
{"age_group":"10s","total":81}
{"age_group":"110s","total":17}
{"age_group":"120s","total":15}
{"age_group":"20s","total":152}
{"age_group":"30s","total":184}
{"age_group":"40s","total":167}
{"age_group":"50s","total":173}
{"age_group":"60s","total":93}
{"age_group":"70s","total":43}
{"age_group":"80s","total":18}
{"age_group":"90s","total":11}
```

Because some addresses do not strictly use province and city format, the parsed province looks a bit strange, in production environment, we need to clean the data first.

## 4. Download
Project code and data download:  
[Spark User Profile](/other/code/SparkUserReport.tar.gz){:target="_blank"}
