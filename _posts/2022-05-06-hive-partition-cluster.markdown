---
layout: post
title:  "Partition and Cluster in Hive"
date:   2022-05-06 19:22:54 +0800
categories: bigdata
tags:
    - Bigdata
    - Hive
---

# Hive的分区与分桶

分区与分桶都是提高查询效率的机制。

分区：按某一字断统一归类，并存储在不同的位置，一个分区对应一个HDFS一个子目录。分区的字段可以不在表中。

分桶：用于优化查询而设计，指定桶的个数和分桶依据，Hive就可以将数据分桶存储。
- 分桶的实质是将数据分成不同的文件。桶数和Reduce（集群中节点）的个数相同；同一个区中的数据会大致均匀分布在不同的桶中（通过hash算法）。
- 作用：每个节点处理一个桶，并行处理，从而提高查询、采样、join效率；
- 原理：把hive单个数据文件，拆分成均匀大小的数据；

我们来看下：

## 1. 创建分区、分桶表格

```shell
hive> create table score(
    >  stu_id bigint,
    >  stu_name string,
    >  score int)
    >  partitioned by (pt string)
    >  clustered by (stu_id) into 3 buckets
    >  row format delimited fields terminated by '\t';
OK
Time taken: 0.03 seconds
hive> 
```

## 2. 导入数据
```shell
hive> load data local inpath '/root/scores.txt'
    > overwrite into table score partition (pt='2022-05-06');
Query ID = root_20220506180830_9dabf8ed-1eea-4ace-8e06-2986cbfc13f0
Total jobs = 2
...
MapReduce Jobs Launched: 
Stage-Stage-1:  HDFS Read: 9353896 HDFS Write: 36990 SUCCESS
Stage-Stage-3:  HDFS Read: 4676948 HDFS Write: 36470 SUCCESS
Total MapReduce CPU Time Spent: 0 msec
OK
Time taken: 26.872 seconds
```

## 3. 查看数据存储
```shell
[root@hadoop001 ~]# hdfs dfs -lsr /user/hive/warehouse/school.db
lsr: DEPRECATED: Please use 'ls -R' instead.
drwxr-xr-x   - root supergroup          0 2022-05-06 18:08 /user/hive/warehouse/school.db/score
drwxr-xr-x   - root supergroup          0 2022-05-06 18:08 /user/hive/warehouse/school.db/score/pt=2022-05-06
-rw-r--r--   3 root supergroup       5980 2022-05-06 18:08 /user/hive/warehouse/school.db/score/pt=2022-05-06/000000_0
-rw-r--r--   3 root supergroup       5724 2022-05-06 18:08 /user/hive/warehouse/school.db/score/pt=2022-05-06/000001_0
-rw-r--r--   3 root supergroup       6000 2022-05-06 18:08 /user/hive/warehouse/school.db/score/pt=2022-05-06/000002_0
[root@hadoop001 ~]# hdfs dfs -cat /user/hive/warehouse/school.db/score/pt=2022-05-06/000000_0 |wc
    338     676    5980
[root@hadoop001 ~]# hdfs dfs -cat /user/hive/warehouse/school.db/score/pt=2022-05-06/000001_0 |wc
    324     648    5724
[root@hadoop001 ~]# hdfs dfs -cat /user/hive/warehouse/school.db/score/pt=2022-05-06/000002_0 |wc
    338     676    6000
```

我们看到数据存在"pt=2022-05-06"的目录内,且近似均匀的分布在三个文件（桶）中。
