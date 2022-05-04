---
layout: post
title:  "Sqoop usage"
date:   2022-05-02 17:45:23 +0800
categories: bigdata
tags:
    - Bigdata 
    - Sqoop
---
# Overview

Sqoop 是Apache开源软件，用于在HDFS/Hive和关系型数据库之间双向传输数据。

```mermaid
graph TD;
DB[Mysql/Oracle] -->|Import| Hadoop[HDFS/Hive];
Hadoop --> |Export| DB;
```

## 工作机制
Translate the import/export command into map reduce tasks.

## 版本比较
Sqoop有1/2两个版本，差异比较大，不兼容。

各自特点：

Sqoop1(>=1.44)：命令行方式不容易编辑，容易出错；紧耦合；安全机制不够完善，密码直接暴露于命令行上；部署简单。

Sqoop2(>=1.99)：引入server，集中化管理connector；多种交互机制（除了cli，还有java/rest api，webui）；引入基于角色的安全机制；缺点是部署复杂。

## 示例

### Import from Mysql to HDFS

```shell
$ sqoop import \
--connect jdbc:mysql://mysql-server-host:3306/testdb?zeroDateTimeBehavior=CONVERT_TO_NULL \
--username USER --password PASSWORD \
--table users \
--fields-terminated-by "\t" \
--target-dir /user/root/testdb/users

22/05/04 17:38:49 INFO mapreduce.Job: The url to track the job: http://localhost:8080/
22/05/04 17:38:49 INFO mapreduce.Job: Running job: job_local1361328796_0001
...
22/05/04 17:38:51 INFO mapred.Task: Task 'attempt_local1361328796_0001_m_000000_0' done.
...
22/05/04 17:38:51 INFO mapred.Task: Task 'attempt_local1361328796_0001_m_000001_0' done.
...
22/05/04 17:38:52 INFO mapred.Task: Task 'attempt_local1361328796_0001_m_000002_0' done.
...
22/05/04 17:38:52 INFO mapred.Task: Task 'attempt_local1361328796_0001_m_000003_0' done.
...
22/05/04 17:38:52 INFO mapreduce.ImportJobBase: Transferred 10.6484 KB in 5.7357 seconds (1.8565 KB/sec)
22/05/04 17:38:52 INFO mapreduce.ImportJobBase: Retrieved 31 records.
```
### Import from Mysql to Hive directly
```shell
$ sqoop import \
--connect jdbc:mysql://mysql-server-host:3306/testdb?zeroDateTimeBehavior=CONVERT_TO_NULL \
--username USER --password PASSWORD \
--table users \
--fields-terminated-by "\t" \
--hive-import  \
--hive-database HIVE_DB_NAME \
--hive-table HIVE_users 

22/05/04 18:14:34 INFO mapreduce.Job: The url to track the job: http://localhost:8080/
22/05/04 18:14:34 INFO mapreduce.Job: Running job: job_local961761218_0001
22/05/04 18:14:35 INFO mapred.Task: Task 'attempt_local961761218_0001_m_000000_0' done.
...
22/05/04 18:14:35 INFO mapred.Task: Task 'attempt_local961761218_0001_m_000001_0' done.
...
22/05/04 18:14:36 INFO mapred.Task: Task 'attempt_local961761218_0001_m_000002_0' done.
...
22/05/04 18:14:36 INFO mapred.Task: Task 'attempt_local961761218_0001_m_000003_0' done.
...
22/05/04 18:14:37 INFO mapreduce.ImportJobBase: Transferred 10.6484 KB in 5.1004 seconds (2.0878 KB/sec)
22/05/04 18:14:37 INFO mapreduce.ImportJobBase: Retrieved 31 records.
```

### Export from HDFS to Mysql
```shell
$ sqoop export \
--connect jdbc:mysql://mysql-server-host:3306/testdb?zeroDateTimeBehavior=CONVERT_TO_NULL \
--username USER --password PASSWORD \
--table users \
--fields-terminated-by "\t" \
--export-dir /user/root/testdb/users

...
22/05/04 17:40:00 INFO mapreduce.Job: The url to track the job: http://localhost:8080/
22/05/04 17:40:00 INFO mapreduce.Job: Running job: job_local2068004457_0001
...
22/05/04 17:40:00 INFO mapred.Task: Task 'attempt_local2068004457_0001_m_000000_0' done.
...
22/05/04 17:40:01 INFO mapred.Task: Task 'attempt_local2068004457_0001_m_000001_0' done.
...
22/05/04 17:40:01 INFO mapred.Task: Task 'attempt_local2068004457_0001_m_000002_0' done.
...
22/05/04 17:40:02 INFO mapred.Task: Task 'attempt_local2068004457_0001_m_000003_0' done.
...
22/05/04 17:40:02 INFO mapreduce.ExportJobBase: Transferred 17.123 KB in 3.9558 seconds (4.3286 KB/sec)
22/05/04 17:40:02 INFO mapreduce.ExportJobBase: Exported 31 records.
```
