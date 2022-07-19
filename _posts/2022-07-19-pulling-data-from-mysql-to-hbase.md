---
layout: post
title:  "Pulling Data From Mysql to HBase"
date:   2022-07-19 18:11:15 +0800
categories: bigdata
tags: [Bigdata, Flin, DataPulling]
---

## Overview

In data warehouse, every once in a while, for example, a day, we need to pull all amount of data from MySQL to HBase.
Today, we create a Flink java project to do this pull work.  

In this experiment, we pull a table named goods, which has the following columns.
```shell
MariaDB [mall]> describe goods;
+------------+--------------+------+-----+---------+-------+
| Field      | Type         | Null | Key | Default | Extra |
+------------+--------------+------+-----+---------+-------+
| goodsId    | int(11)      | YES  |     | NULL    |       |
| goodsName  | varchar(64)  | YES  |     | NULL    |       |
| goodsPrice | decimal(6,2) | YES  |     | NULL    |       |
| goodsStock | int(11)      | YES  |     | NULL    |       |
| goodsViews | int(11)      | YES  |     | NULL    |       |
+------------+--------------+------+-----+---------+-------+
```

## Create a Maven project

Load the following dependencies in the pom.xml, the full file can be found at the end of this article.
* flink-java
* flink-streaming-java_2.11
* flink-scala_2.11
* flink-runtime-web_2.11
* flink-cep-scala_2.11
* flink-streaming-scala_2.11
* flink-connector-kafka_2.11
* flink-connector-redis_2.11
* flink-hadoop-compatibility_2.11
* flink-hbase_2.11
* protobuf-java
* flink-json
* flink-jdbc_2.11
* flink-table_2.11
* flink-shaded-hadoop2
* mysql-connector-java
* fastjson
* lombok
* protobuf-java
* canal.client
* slf4j-log4j12
* log4j 

## Program

### Define the Config class
```java
import java.io.Serializable;

public class Config implements Serializable {
    public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
    public static final String DB_URL = "jdbc:mysql://you-mysql-host:3306/mall?useUnicode=true&characterEncoding=utf8";
    public static final String USERNAME = "*****";  // use your username
    public static final String PASSWORD = "*****";  // use your password
    public static final int BATCH_SIZE = 2;
}

```

Replace the DB_URL/USERNAME/PASSWORD values with yours.

### Define connector and source

Define a JDBC connector and create a data source from the connector .
```java
        JDBCInputFormat.JDBCInputFormatBuilder jdbc = JDBCInputFormat.buildJDBCInputFormat()
                .setDrivername(Config.DRIVER_CLASS)
                .setDBUrl(Config.DB_URL)
                .setUsername(Config.USERNAME)
                .setPassword(Config.PASSWORD)
                .setQuery("select * from mall.goods")
                .setRowTypeInfo(ROW_TYPE_INFO);
                
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();                
        DataSource<Row> source = env.createInput(jdbc.finish());                
```

### Create a HBase job from config 

```java
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "hadoop001");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("zookeeper.znode.parent", "/hbase");
        conf.set(TableOutputFormat.OUTPUT_TABLE, "mall:goods");
        conf.set("mapreduce.output.fileoutputformat.outputdir", "/tmp");

        Job job = Job.getInstance(conf);
```

### Put record into HBase for every row data

Define a function to deal with MySQL records:  
```java
public static DataSet<Tuple2<Text, Mutation>> converyMysqlToHBase(DataSet<Row> ds) {
        return ds.map(new RichMapFunction<Row, Tuple2<Text, Mutation>>() {
            private transient Tuple2<Text, Mutation> resultTp;
            private byte[] cf = "info".getBytes(ConfigConstants.DEFAULT_CHARSET);

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                resultTp = new Tuple2<>();
            }

            @Override
            public Tuple2<Text, Mutation> map(Row row) throws Exception {
                resultTp.f0 = new Text(row.getField(0).toString());
                Put put = new Put(row.getField(0).toString().getBytes(ConfigConstants.DEFAULT_CHARSET));
                if (row.getField(1) != null) {
                    put.addColumn(cf, Bytes.toBytes("goodsName"), Bytes.toBytes(row.getField(1).toString()));
                }
                put.addColumn(cf, Bytes.toBytes("goodsPrice"), Bytes.toBytes(row.getField(2).toString()));
                put.addColumn(cf, Bytes.toBytes("goodsStock"), Bytes.toBytes(row.getField(3).toString()));
                put.addColumn(cf, Bytes.toBytes("goodsViews"), Bytes.toBytes(row.getField(4).toString()));

                resultTp.f1 = put;
                return resultTp;
            }
        });
    }
```

### Start job
```java
        DataSet<Tuple2<Text, Mutation>> hbaseResult = converyMysqlToHBase(source);
        hbaseResult.output(new HadoopOutputFormat<Text, Mutation>(new TableOutputFormat(), job));
        env.execute("Full Pull Mysql to HBase");
```

### Run
Before run the application, we should make sure the namespace "mall" and table "goods" be created in HBase.

```shell
hbase(main):008:0* create_namespace 'mall'
0 row(s) in 0.8850 seconds

hbase(main):009:0> create 'mall:goods', 'info'
0 row(s) in 1.3090 seconds
```
Then run the application.  
Check the HBase table after running successfully:  
```shell
hbase(main):010:0> scan 'mall:goods'
ROW                            COLUMN+CELL
 1                             column=info:goodsName, timestamp=1658233279152, value=Apple iPhone X
 1                             column=info:goodsPrice, timestamp=1658233279152, value=6399.00
 1                             column=info:goodsStock, timestamp=1658233279152, value=10000
 1                             column=info:goodsViews, timestamp=1658233279152, value=34567
 2                             column=info:goodsName, timestamp=1658233279152, value=Vivo iQ100
 2                             column=info:goodsPrice, timestamp=1658233279152, value=3599.00
 2                             column=info:goodsStock, timestamp=1658233279152, value=20000
 2                             column=info:goodsViews, timestamp=1658233279152, value=3433
 3                             column=info:goodsName, timestamp=1658233279152, value=Apple Watch 3
 3                             column=info:goodsPrice, timestamp=1658233279152, value=2598.00
 3                             column=info:goodsStock, timestamp=1658233279152, value=2000
 3                             column=info:goodsViews, timestamp=1658233279152, value=34221
 4                             column=info:goodsName, timestamp=1658233279152, value=iPad 5
 4                             column=info:goodsPrice, timestamp=1658233279152, value=3988.00
 4                             column=info:goodsStock, timestamp=1658233279152, value=10000
 4                             column=info:goodsViews, timestamp=1658233279152, value=12293
4 row(s) in 0.0410 seconds
```

## Download
Project code download:  
[Pull Data From MySQL to HBase](/other/code/pull-data.tar.gz){:target="_blank"}
