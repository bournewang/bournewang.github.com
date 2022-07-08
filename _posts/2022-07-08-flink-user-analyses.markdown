---
layout: post
title:  "Flink Realtime User Analyses"
date:   2022-07-08 23:23:30 +0800
categories: bigdata
tags:
    - Bigdata
    - Flink
---
## 1. Requirement

Analysis of the distribution of users by province, age group and sex, based on user realtime registration data from a website.

The sample of desensitised data looks like:
```json
{"AGE":48,"BIRTHDAY":"1973","BPLACE":"吉林省吉林市磐石县","IDTYPE":"01","RNAME":"崔**","SEX":"女"}
{"AGE":45,"BIRTHDAY":"1976","BPLACE":"四川省重庆市綦江县","IDTYPE":"01","RNAME":"朱**","SEX":"男"}
{"AGE":26,"BIRTHDAY":"1995","BPLACE":"浙江省杭州市富阳市","IDTYPE":"01","RNAME":"骆**","SEX":"男"}
```

_The full file and project code are provided at the end of this article_.

## 2. Developemnt

### 2.1 pom.xml
Create a maven project, and load scala/flink/maven-compiler-plugin in pom.xml:
```xml
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <flink.version>1.12.7</flink.version>
        <scala.version>2.11.8</scala.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-scala_2.11</artifactId>
            <version>${flink.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-streaming-scala_2.11</artifactId>
            <version>${flink.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-clients_2.11</artifactId>
            <version>${flink.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>org.json4s</groupId>
            <artifactId>json4s-core_2.13</artifactId>
            <version>3.7.0-M11</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.78</version>
        </dependency>
    </dependencies>
```

### 2.2 Program
Create directory src/main/scala, and set it as "Sources" in [_**Project Structure > Module**_] page.

Create a package and an object UserReport under this package. 

#### 2.2.1 Data Source
Create an object named "UserSource", which will read data from a json file continuously to simulate a realtime data feed.  
```scala
object UserSource extends SourceFunction[String]{
  override
  def run(sourceContext: SourceFunction.SourceContext[String]): Unit = {
    try {
      val input = "./data/person_info_1000.json"
      val fp = new File(input)
      val reader = new BufferedReader(new FileReader(fp));
      var line: String = reader.readLine();
      var i = 0;
      while (line != null) {
//        println("+++++ " + i +", " + line)
        i+=1
        sourceContext.collect(line);
        Thread.sleep(20);
        line = reader.readLine();
      }
    } catch{
      case e:Exception => e.printStackTrace()
      case _ => ""
    }
  }

  override def cancel(): Unit = {

  }

  def main(args: Array[String]): Unit = {
    UserSource.run(null)
  }
}

```

#### 2.2.2 UserModel
Create a class named "UserModel" to hold mapping of each line input data, in which we create methods to extract province from address, and age group from age.
```scala
class UserModel(na: String, se: String, ag: String, bd: String, bp: String) {
  var name:String = na
  var sex:String = se
  var age:String = ag
  var birthday:String = bd
  var birth_place:String = bp
  var province: String = getProvince(bp)
  var age_group: String = getAgeGroup(ag)

  def getProvince(addr: String): String = {
    try {
      val p = "^(.*省|.*自治区|.*市).*".r
      val p(province) = addr
      province
    }catch{
      case _ => ""
    }
  }

  def getAgeGroup(ag: String): String = {
    try {
      val a = (ag.toDouble.toInt / 10) * 10
      if (a == 0) {
        "0-9"
      } else {
        a.toString + "s"
      }
    }catch{
      case _ => ""
    }
  }

  override def toString = s"UserModel(name=$name, sex=$sex, age=$age, age_group=$age_group, birthday=$birthday, birth_place=$birth_place, province=$province)"
}
```

#### 2.2.3 UserMapFunction
Ceate a map class named "UserMapFunction" to create UserModel from a line data:
```scala
class UserMapFunction extends RichMapFunction[String, UserModel] {
  override def map(in: String): UserModel = {
//    println("raw data: " + in)
    if (StringUtils.isNullOrWhitespaceOnly(in)) {
      return null
    }
    try {
      val jsonS = JSON.parseFull(in)
      jsonS match {
        case Some(obj: Map[String, String]) => {
          val u = new UserModel(obj.getOrElse("RNAME", ""),
            obj.getOrElse("SEX", ""),
            String.valueOf(obj.getOrElse("AGE", "")),
            String.valueOf(obj.getOrElse("BIRTHDAY", "")),
            obj.getOrElse("BPLACE", "")
          )
          u
        }
        case _ => null
      }
    }catch{
      case _ => null
    }
  }
}

```

#### 2.2.4 UserProcessFunction
In Process function class, we define MapState for provinces/sex/ageGroup, and add 1 for each province/sex/ageGroup, and finally output the sorted province/sex/ageGroup list as a string.
```scala
class UserProcessFunction extends ProcessAllWindowFunction[UserModel, String, TimeWindow] {
  var provincesState:MapState[String, Int] = null
  var sexState:MapState[String, Int] = null
  var ageState:MapState[String, Int] = null

  override def open(parameters: Configuration): Unit = {
    val provinceDesc = new MapStateDescriptor[String, Int]("provincesState", classOf[String], classOf[Int])
    val sexDesc = new MapStateDescriptor[String, Int]("sexState", classOf[String], classOf[Int])
    val ageDesc = new MapStateDescriptor[String, Int]("ageState", classOf[String], classOf[Int])

    provincesState = getRuntimeContext.getMapState(provinceDesc)
    sexState = getRuntimeContext.getMapState(sexDesc)
    ageState = getRuntimeContext.getMapState(ageDesc)
  }

  override def process(context: Context, elements: Iterable[UserModel], out: Collector[String]): Unit = {
    var user:UserModel = null
    for ( user <- elements ) {
//      println(user.toString)
      if (user != null) {
        val province = user.province
        if (!StringUtil.isNullOrEmpty(province)) {
          provincesState.put(province, provincesState.get(province)+1)
        }

        val sex = user.sex
        if (!StringUtil.isNullOrEmpty(sex)) {
          sexState.put(sex, sexState.get(sex)+1)
        }

        val age_group = user.age_group
        if (!StringUtil.isNullOrEmpty(age_group)) {
          ageState.put(age_group, ageState.get(age_group)+1)
        }
      }
    }

    println("----------")
    var output:String = ""
    output += getStateString(provincesState) + "\n"
    output += getStateString(sexState) + "\n"
    output += getStateString(ageState)
    out.collect(output)
  }

  def getStateString(state: MapState[String, Int]): String = {
    if (!state.isEmpty) {
      val iterator = state.entries().iterator()
      var map = new mutable.HashMap [String, Int]
      while (iterator.hasNext) {
        val item = iterator.next()
        map += (item.getKey -> item.getValue)
      }
      map.toList.sortBy(-_._2).toString() //.take(10)
    }else{
      ""
    }
  }
}

```

#### 2.2.5 UserReport
In the main program, we specify map and process function and set window time to 2 seconds.
```scala
object UserReport {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val stream = env.addSource(UserSource).setParallelism(1)
    var result = stream
      .map(new UserMapFunction())
      .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(2)))
      .process(new UserProcessFunction())

      result.print
    env.execute("Realtime User Analyse ...")
  }
}
```


## 3. Run
Build and run the program, we got the realtime analyses data:
```shell
----------
4> List((湖南省,9), (安徽省,9), (江苏省,9), (四川省,8), (浙江省,6), (吉林省,4), (河南省,4), (辽宁省,4), (湖北省,4), (甘肃省,4), (广东省,3), (江西省,3), (黑龙江省,3), (陕西省,2), (河北省,2), (福建省,2), (山西省,2), (上海市,1), (重庆市,1), (青海省,1), (宁夏回族自治区,1), (广西壮族自治区,1), (贵州省,1), (新疆维吾尔自治区,1), (山东省,1), (湖北省省,1), (内蒙古自治区,1))
List((男,53), (女,36))
List((40s,17), (30s,16), (50s,16), (20s,15), (10s,8), (60s,6), (0-9,3), (70s,2), (110s,2), (90s,2), (80s,1), (120s,1))
----------
5> List((安徽省,20), (江苏省,16), (湖南省,15), (浙江省,13), (四川省,12), (河南省,10), (湖北省,9), (甘肃省,9), (吉林省,8), (辽宁省,7), (广东省,6), (黑龙江省,6), (江西省,5), (山东省,5), (河北省,4), (广西壮族自治区,4), (上海市,3), (陕西省,3), (贵州省,3), (福建省,3), (重庆市,2), (湖北省省,2), (山西省,2), (内蒙古自治区,2), (北京市,1), (青海省,1), (宁夏回族自治区,1), (新疆维吾尔自治区,1), (天津市,1))
List((男,103), (女,72))
List((40s,34), (30s,33), (20s,30), (50s,29), (60s,16), (10s,14), (0-9,4), (70s,4), (110s,4), (90s,3), (80s,2), (120s,2))
----------
6> List((安徽省,28), (江苏省,26), (浙江省,24), (湖南省,19), (四川省,17), (河南省,16), (辽宁省,15), (湖北省,15), (甘肃省,13), (吉林省,10), (广东省,10), (黑龙江省,9), (山东省,8), (河北省,7), (江西省,7), (福建省,6), (广西壮族自治区,5), (贵州省,5), (上海市,4), (陕西省,4), (内蒙古自治区,4), (重庆市,3), (山西省,3), (湖北省省,2), (北京市,1), (青海省,1), (宁夏回族自治区,1), (新疆维吾尔自治区,1), (天津市,1))
List((男,154), (女,112))
List((30s,52), (40s,45), (20s,44), (50s,39), (60s,27), (10s,24), (0-9,9), (70s,7), (120s,5), (80s,5), (110s,4), (90s,4), (100s,1))
----------
7> List((江苏省,34), (安徽省,33), (湖南省,29), (浙江省,29), (四川省,27), (河南省,21), (辽宁省,20), (湖北省,19), (甘肃省,14), (广东省,13), (黑龙江省,12), (吉林省,11), (江西省,11), (福建省,10), (河北省,9), (山东省,9), (广西壮族自治区,8), (贵州省,7), (上海市,6), (内蒙古自治区,6), (山西省,5), (重庆市,4), (陕西省,4), (湖北省省,2), (北京市,1), (青海省,1), (宁夏回族自治区,1), (新疆维吾尔自治区,1), (天津市,1))
List((男,195), (女,154))
List((30s,62), (20s,59), (50s,59), (40s,53), (60s,38), (10s,29), (0-9,14), (70s,13), (80s,6), (110s,5), (120s,5), (90s,4), (100s,2))
----------
8> List((江苏省,44), (安徽省,43), (四川省,38), (浙江省,36), (湖南省,34), (河南省,26), (辽宁省,25), (湖北省,23), (广东省,17), (江西省,16), (甘肃省,16), (黑龙江省,15), (福建省,13), (吉林省,12), (河北省,11), (山东省,11), (广西壮族自治区,10), (上海市,9), (贵州省,8), (内蒙古自治区,8), (山西省,6), (重庆市,5), (陕西省,4), (湖北省省,3), (北京市,1), (内蒙古巴彦淖尔盟临河市,1), (青海省,1), (宁夏回族自治区,1), (新疆维吾尔自治区,1), (天津市,1))
List((男,238), (女,202))
List((50s,83), (20s,76), (30s,73), (40s,63), (60s,44), (10s,37), (0-9,20), (70s,17), (110s,7), (120s,6), (80s,6), (90s,4), (100s,4))
----------
1> List((江苏省,55), (安徽省,51), (四川省,46), (浙江省,43), (湖南省,39), (河南省,32), (辽宁省,29), (湖北省,25), (广东省,20), (黑龙江省,20), (江西省,19), (甘肃省,18), (福建省,15), (上海市,13), (吉林省,13), (河北省,13), (山东省,13), (广西壮族自治区,11), (贵州省,9), (内蒙古自治区,8), (山西省,7), (重庆市,6), (陕西省,5), (湖北省省,4), (青海省,3), (内蒙古巴彦淖尔盟临河市,2), (天津市,2), (广西桂林市,1), (北京市,1), (宁夏回族自治区,1), (新疆维吾尔自治区,1), (云南省,1))
List((男,287), (女,240))
List((50s,97), (30s,89), (20s,89), (40s,80), (60s,52), (10s,42), (70s,23), (0-9,21), (110s,8), (80s,8), (90s,7), (120s,7), (100s,4))
----------
2> List((江苏省,66), (安徽省,58), (四川省,57), (浙江省,49), (湖南省,45), (河南省,38), (辽宁省,33), (湖北省,28), (广东省,23), (甘肃省,23), (江西省,22), (黑龙江省,22), (上海市,17), (福建省,17), (山东省,16), (河北省,15), (吉林省,14), (广西壮族自治区,12), (贵州省,9), (山西省,9), (内蒙古自治区,8), (重庆市,7), (陕西省,6), (青海省,4), (湖北省省,4), (内蒙古巴彦淖尔盟临河市,3), (云南省,2), (天津市,2), (广西桂林市,1), (北京市,1), (宁夏回族自治区,1), (新疆维吾尔自治区,1))
List((男,327), (女,287))
List((50s,112), (30s,105), (20s,102), (40s,96), (60s,58), (10s,47), (70s,29), (0-9,24), (120s,10), (110s,9), (90s,9), (80s,8), (100s,5))
----------
3> List((江苏省,76), (安徽省,69), (四川省,63), (浙江省,52), (湖南省,50), (河南省,43), (辽宁省,37), (湖北省,33), (广东省,26), (江西省,25), (甘肃省,25), (黑龙江省,24), (山东省,22), (上海市,20), (福建省,19), (吉林省,15), (河北省,15), (广西壮族自治区,14), (山西省,11), (重庆市,9), (贵州省,9), (陕西省,8), (内蒙古自治区,8), (湖北省省,5), (内蒙古巴彦淖尔盟临河市,4), (青海省,4), (云南省,3), (北京市,2), (天津市,2), (广西桂林市,1), (宁夏回族自治区,1), (新疆维吾尔自治区,1))
List((男,365), (女,332))
List((50s,128), (30s,121), (20s,114), (40s,113), (60s,68), (10s,50), (70s,31), (0-9,27), (110s,10), (90s,10), (120s,10), (80s,10), (100s,5))
----------
4> List((江苏省,83), (安徽省,80), (四川省,73), (湖南省,57), (浙江省,55), (河南省,49), (辽宁省,41), (湖北省,36), (广东省,30), (江西省,28), (山东省,26), (甘肃省,26), (上海市,25), (黑龙江省,25), (福建省,21), (吉林省,17), (广西壮族自治区,16), (河北省,15), (山西省,13), (重庆市,10), (陕西省,9), (贵州省,9), (内蒙古自治区,9), (湖北省省,6), (云南省,6), (内蒙古巴彦淖尔盟临河市,5), (青海省,4), (天津市,3), (北京市,2), (宁夏回族自治区,2), (新疆维吾尔自治区,2), (广西桂林市,1))
List((男,407), (女,378))
List((30s,145), (50s,140), (40s,131), (20s,124), (60s,73), (10s,59), (70s,33), (0-9,27), (120s,13), (110s,12), (80s,12), (90s,11), (100s,5))
----------
5> List((安徽省,90), (江苏省,90), (四川省,85), (湖南省,63), (浙江省,62), (河南省,54), (辽宁省,45), (湖北省,40), (广东省,32), (山东省,32), (江西省,31), (上海市,29), (甘肃省,28), (黑龙江省,25), (福建省,24), (吉林省,18), (河北省,18), (广西壮族自治区,17), (山西省,15), (重庆市,10), (陕西省,10), (内蒙古自治区,10), (贵州省,9), (云南省,7), (内蒙古巴彦淖尔盟临河市,6), (湖北省省,6), (青海省,4), (天津市,4), (新疆维吾尔自治区,3), (北京市,2), (宁夏回族自治区,2), (广西桂林市,1))
List((男,454), (女,419))
List((30s,165), (50s,152), (40s,147), (20s,138), (60s,80), (10s,67), (70s,37), (0-9,29), (120s,14), (110s,13), (90s,13), (80s,12), (100s,6))
----------
6> List((安徽省,100), (江苏省,99), (四川省,94), (湖南省,69), (浙江省,68), (河南省,61), (辽宁省,47), (湖北省,44), (山东省,39), (广东省,35), (江西省,35), (上海市,33), (甘肃省,30), (福建省,26), (黑龙江省,25), (河北省,20), (吉林省,19), (广西壮族自治区,19), (山西省,17), (陕西省,11), (重庆市,10), (贵州省,10), (内蒙古自治区,10), (云南省,9), (内蒙古巴彦淖尔盟临河市,6), (湖北省省,6), (青海省,4), (天津市,4), (新疆维吾尔自治区,3), (北京市,2), (宁夏回族自治区,2), (广西桂林市,1))
List((男,502), (女,458))
List((30s,179), (50s,168), (40s,163), (20s,144), (60s,87), (10s,78), (70s,42), (0-9,32), (80s,17), (110s,16), (120s,15), (90s,13), (100s,6))
```

## 4. Downlaod

Project code and data download:  
[Flink Realtime User Analyses](/other/code/FlinkUserReport.tar.gz){:target="_blank"}

