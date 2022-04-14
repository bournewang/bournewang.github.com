---
layout: post
title:  "Max Compute - 搭建互联网在线运营分析平台"
date:   2022-04-11 12:54:56 +0800
categories: bigdata
tags:
- Bigdata
- MaxCompute
- Tablestore
---

## 1. Aliyun Services involved
1. Tablestore
2. MaxCompute
3. DataWorks
4. QuickBI

## 2. Prepare data
Create Tablestore instance and generate fake user data. step by step document is [here](https://help.aliyun.com/document_detail/122438.html).

## 3. Create tables (in Dataworks)
first, create a business process in Dataworks;
create tables:
- create an external table ots_user_trace_log(use ODPS SQL node, to sync data from table store);
- create table ods_user_trace_log;
- create table dw_user_trace_log;
- create table rpt_user_trace_log;

Add resource jar/file, which will be used to translate ip address to region(province/city).

## 4. Work Flow Design
In DataStudio, add a virtual node and 3 ODPS SQL node to business process; and link them like:
start -> ods_user_trace_log -> dw_user_trace_log -> rpt_user_trace_log; 

The logical work flow looks like:  

```mermaid
graph TD;
A2[Start] -->|ODPS ods_user_trace_log| T1[fa:fa-table ods_user_trace_log];
T1 --> |ODPS dw_user_trace_log| T2[fa:fa-table dw_user_trace_log];
T2 --> |ODPS rpt_user_trace_log| T3[fa:fa-table rpt_user_trace_log];
```

save the work process, submit and run.

verify data in a temp query builder. 
```
select * from rpt_user_trace_log where dt=20220413;
```

## 5. Present in QuickBI


This practice was based on [云原生大数据计算服务 MaxCompute > 产品教程 > 搭建互联网在线运营分析平台](https://help.aliyun.com/document_detail/122337.html)

