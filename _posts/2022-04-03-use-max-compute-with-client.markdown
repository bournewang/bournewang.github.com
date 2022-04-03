---
layout: post
title:  "通过客户端使用MaxCompute"
date:   2022-04-03 17:15:43 +0800
categories: bigdata
tags:
    - Bigdata
    - MaxCompute
---

## 1. Install client tools
Please see [准备环境及安装工具](https://help.aliyun.com/document_detail/252783.htm?spm=a2c4g.11186623.0.0.56eeb13a0ZEnht#task-2074973){:target="_blank"}

## 2. Login
fill you access_id/key/project_name in conf/odps_config.ini;
then launch bin/odpscmd, it's successful when you see:
```shell
(base) ➜  maxcompute ./bin/odpscmd
          __                         __
 ___  ___/ /___   ___ ____ __ _  ___/ /
/ _ \/ _  // _ \ (_-</ __//  ' \/ _  /
\___/\_,_// .__//___/\__//_/_/_/\_,_/
         /_/
Aliyun ODPS Command Line Tool
Version 0.37.5-public
@Copyright 2020 Alibaba Cloud Computing Co., Ltd. All rights reserved.
Connecting to http://service.cn-shanghai.maxcompute.aliyun.com/api, project: default_project_6C87D05
Project timezone: Asia/Shanghai
Connected!
```

## 3. Create table
### a. download sample data
[banking.txt](https://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/cn/shujia/0.2.00/assets/pic/data-develop/banking.txt?spm=a2c4g.11186623.0.0.5bd4125d30UnyI&file=banking.txt)<br/>
[banking_nocreditcard.csv](https://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/assets/attach/170609/cn_zh/1621396708253/banking_nocreditcard.csv?spm=a2c4g.11186623.0.0.5bd4125d30UnyI&file=banking_nocreditcard.csv)<br/>
[banking_uncreditcard.csv](https://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/assets/attach/170609/cn_zh/1621396728129/banking_uncreditcard.csv?spm=a2c4g.11186623.0.0.5bd4125d30UnyI&file=banking_uncreditcard.csv)<br/>
[banking_yescreditcard.csv](https://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/assets/attach/170609/cn_zh/1621396745372/banking_yescreditcard.csv?spm=a2c4g.11186623.0.0.5bd4125d30UnyI&file=banking_yescreditcard.csv)

### b. create table

create non-partition table bank_data:
```shell
create table if not exists bank_data
(
 age             BIGINT comment '年龄',
 job             STRING comment '工作类型',
 marital         STRING comment '婚否',
 education       STRING comment '教育程度',
 credit          STRING comment '是否有信用卡',
 housing         STRING comment '是否有房贷',
 loan            STRING comment '是否有贷款',
 contact         STRING comment '联系方式',
 month           STRING comment '月份',
 day_of_week     STRING comment '星期几',
 duration        STRING comment '持续时间',
 campaign        BIGINT comment '本次活动联系的次数',
 pdays           DOUBLE comment '与上一次联系的时间间隔',
 previous        DOUBLE comment '之前与客户联系的次数',
 poutcome        STRING comment '之前市场活动的结果',
 emp_var_rate    DOUBLE comment '就业变化速率',
 cons_price_idx  DOUBLE comment '消费者物价指数',
 cons_conf_idx   DOUBLE comment '消费者信心指数',
 euribor3m       DOUBLE comment '欧元存款利率',
 nr_employed     DOUBLE comment '职工人数',
 fixed_deposit   BIGINT comment '是否有定期存款'
);
```

create partition table bank_data_pt
```shell
create table if not exists bank_data_pt
(
 age             BIGINT comment '年龄',
 job             STRING comment '工作类型',
 marital         STRING comment '婚否',
 education       STRING comment '教育程度',
 housing         STRING comment '是否有房贷',
 loan            STRING comment '是否有贷款',
 contact         STRING comment '联系方式',
 month           STRING comment '月份',
 day_of_week     STRING comment '星期几',
 duration        STRING comment '持续时间',
 campaign        BIGINT comment '本次活动联系的次数',
 pdays           DOUBLE comment '与上一次联系的时间间隔',
 previous        DOUBLE comment '之前与客户联系的次数',
 poutcome        STRING comment '之前市场活动的结果',
 emp_var_rate    DOUBLE comment '就业变化速率',
 cons_price_idx  DOUBLE comment '消费者物价指数',
 cons_conf_idx   DOUBLE comment '消费者信心指数',
 euribor3m       DOUBLE comment '欧元存款利率',
 nr_employed     DOUBLE comment '职工人数',
 fixed_deposit   BIGINT comment '是否有定期存款'
)partitioned by (credit STRING comment '是否有信用卡');

alter table bank_data_pt add if not exists partition (credit='yes') partition (credit='no') partition (credit='unknown');
```

create result_table1:
```shell
create table if not exists result_table1
(
 education   STRING comment '教育程度',
 num         BIGINT comment '人数'
);
```

create table result_table2:
```shell
create table if not exists result_table2
(
 education   STRING comment '教育程度',
 num         BIGINT comment '人数',
 credit      STRING comment '是否有信用卡'
);
```

then, run "show tables" to confirm your table;
```shell
show tables;
```

run "desc table_name" to check the table;
```shell
--查看bank_data表结构。
desc bank_data;
--查看bank_data_pt表结构。
desc bank_data_pt;
--查看bank_data_pt的分区。
show partitions bank_data_pt;
--查看result_table1表结构。
desc result_table1;
--查看result_table2表结构。
desc result_table2;
```

get partitions of a partition table:
```shell
show partitions bank_data_pt;
```

## 4. Import Data
use "tunnel" command to upload the data, let's say you download the sample banking data in "~/Downloads";
```shell
tunnel upload ~/Downloads/banking.txt bank_data;
tunnel upload ~/banking_yescreditcard.csv bank_data_pt/credit="yes";
tunnel upload ~/banking_uncreditcard.csv bank_data_pt/credit="unknown";
tunnel upload ~/banking_nocreditcard.csv bank_data_pt/credit="no";
```
you'll see "OK" if success.

let's confirm the result:
```shell
select count(age) as num1 from bank_data;
select count(age) as num2 from bank_data_pt where credit="yes";
select count(age) as num3 from bank_data_pt where credit="unknown";
select count(age) as num4 from bank_data_pt where credit="no";
```
you'll see numbers replied.

## 5. SQL and Compute
get the single people number who has housing and group by education and put the result into "result_table":
```shell
--查询非分区表bank_data中各个学历下的贷款买房的单身人士数量并将查询结果写入result_table1。
insert overwrite table result_table1
select education, count(marital) as num
from bank_data
where housing = 'yes' and marital = 'single'
group by education;

--查询分区表bank_data_pt中各个学历下的贷款买房的单身人士数量并将查询结果写入result_table2。
set odps.sql.allow.fullscan=true;
insert overwrite table result_table2 
select education, count(marital) as num, credit 
from bank_data_pt 
where housing = 'yes' and marital = 'single'
group by education, credit;
```

CAUTION: partition table will not allow full table scan unless you set fullscan to true:
```shell
set odps.sql.allow.fullscan=true;
```

## 6. Download the result
```shell
tunnel download result_table1 ~/Downloads/result_table1.csv;
tunnel download result_table2 ~/Downloads/result_table2.csv;
```
you'll see OK if success.  open the downloaded file:
```shell
(base) ➜  Downloads cat result_table1.csv
basic.4y,227
basic.6y,172
basic.9y,709
high.school,1641
illiterate,1
professional.course,785
university.degree,2399
unknown,257
(base) ➜  Downloads cat result_table2.csv
basic.4y,164,no
basic.4y,63,unknown
basic.6y,104,no
basic.6y,68,unknown
basic.9y,547,no
basic.9y,162,unknown
high.school,1469,no
high.school,172,unknown
illiterate,1,unknown
professional.course,721,no
professional.course,64,unknown
university.degree,2203,no
university.degree,196,unknown
unknown,206,no
unknown,51,unknown
```

This practise based on Aliyun document [云原生大数据计算服务 MaxCompute > 快速入门 > 通过MaxCompute客户端使用MaxCompute](https://help.aliyun.com/document_detail/252785.html){:target="_blank"}.
