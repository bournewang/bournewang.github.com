---
layout: post
title:  "Hive Windowing and Analytics Function - Part 2 实操"
date:   2022-05-13 17:29:38 +0800
categories: bigdata
tags:
    - Bigdata
    - Hive
---

# Hive Windowing and Analytics Function - Part 2 实操

## 1. Hive的增强函数可分为以下几部分：
### 1.1 窗口函数：

| 名称               | 说明           |
|------------------|--------------|
| lead(col, n)     | col列往后/下第N个值 |
| lag(col,n)       | col列往前/上第N个值 |
| first_value(col) | col列第一行的值    |
| last_value(col)  | col列最后一行的值   |

### 1.2 Over从句
语法： 
OVER (

[ PARTITION BY COLUMN ]

[ORDER BY COLUMN asc/desc]

(ROWS | RANGE)  between xx and yy
)
#### 1.2.1 _聚合函数_
* count
* sum
* min
* max
* avg

#### 1.2.2 分区排序 _partition/order by_

#### 1.2.3 窗口从句

对当前的partition来说，

| data row  | range               |
|-----------|---------------------|
| first row | unbounded preceding |
| - - - - - | ...                 |
| - - - - - | m preceding         |
| - - - - - | ...                 |
| - - - - - | **current row**     |
| - - - - - | ...                 |
| - - - - - | n following         |
| - - - - - | ...                 |
| last row  | unbounded following |

例如:
```shell
over (partition by city_code order by create_time rows between 3 preceding and 1 following)
over (partition by city_code order by create_time rows between unbounded preceding and 1 following)
over (partition by city_code order by create_time range between unbounded preceding and unbounded following)
```

### 1.3 Analytics functions
* RANK
* ROW_NUMBER
* DENSE_RANK
* CUME_DIST
* PERCENT_RANK
* NTILE

### 1.4 Distinct
```shell
COUNT(DISTINCT a) OVER (PARTITION BY c)
```

### 1.5 Aggregate 
```shell
SELECT rank() OVER (ORDER BY sum(b))
FROM T
GROUP BY a;
```
## 2. 实操
### 2.1 按月汇总用户访问量和截止该月份的总计访问量
我们有如下的用户访问数据  

| user_id | visit_date | visit_count |
|-----|-----------|---|
| u01 | 2017/1/21 | 5 |  
| u02 | 2017/1/23 | 6 |  
| u03 | 2017/1/22 | 8 |  
| u04 | 2017/1/20 | 3 |  
| u01 | 2017/1/23 | 6 |  
| u01 | 2017/2/21 | 8 |  
| U02 | 2017/1/23 | 6 |  
| U01 | 2017/2/22 | 4 |  

要求使用SQL统计出每个用户的累积访问次数，如下表所示：  

| 用户id |   月份 | 小计 | 累积|
|-----|---------|----|-----|
| u01 | 2017-01 | 11 | 11 |  
| u01 | 2017-02 | 12 | 23 |  
| u02 | 2017-01 | 12 | 12 |  
| u03 | 2017-01 | 8 | 8 |  
| u04 | 2017-01 | 3 | 3 |  

#### 2.1.1 首先按月份显示访问次数
```shell
0: jdbc:hive2://0.0.0.0:10000> select user_id,from_unixtime(unix_timestamp(visit_date, 'yyyy/MM/dd'), 'yyyy-MM') as visit_month, visit_count from test1;
...
INFO  : OK
+----------+--------------+--------------+
| user_id  | visit_month  | visit_count  |
+----------+--------------+--------------+
| u01      | 2017-01      | 5            |
| u02      | 2017-01      | 6            |
| u03      | 2017-01      | 8            |
| u04      | 2017-01      | 3            |
| u01      | 2017-01      | 6            |
| u01      | 2017-02      | 8            |
| u02      | 2017-01      | 6            |
| u01      | 2017-02      | 4            |
+----------+--------------+--------------+
8 rows selected (0.143 seconds)

```
#### 2.1.2 需要按用户、月份汇总出访问量：
```shell
0: jdbc:hive2://0.0.0.0:10000> select user_id, visit_month, sum(visit_count) as month_visits from (
. . . . . . . . . . . . . . .>    select user_id,from_unixtime(unix_timestamp(visit_date, 'yyyy/MM/dd'), 'yyyy-MM') as visit_month, visit_count from test1
. . . . . . . . . . . . . . .> ) t1 group by user_id,visit_month;
...
INFO  : OK
+----------+--------------+---------------+
| user_id  | visit_month  | month_visits  |
+----------+--------------+---------------+
| u01      | 2017-01      | 11            |
| u01      | 2017-02      | 12            |
| u02      | 2017-01      | 12            |
| u03      | 2017-01      | 8             |
| u04      | 2017-01      | 3             |
+----------+--------------+---------------+
5 rows selected (11.301 seconds)
```

#### 2.1.3 用窗口函数累加截止某月份的总计访问量
因为窗口函数默认范围就是从开头到当前行，刚好是累加的范围，不需要加rows between限定。
```shell
0: jdbc:hive2://0.0.0.0:10000> select user_id, visit_month, month_visits, sum(month_visits) over(partition by user_id order by visit_month asc) total_visits
. . . . . . . . . . . . . . .> from ( 
. . . . . . . . . . . . . . .>     select user_id, visit_month, sum(visit_count) as month_visits from (
. . . . . . . . . . . . . . .>         select user_id,from_unixtime(unix_timestamp(visit_date, 'yyyy/MM/dd'), 'yyyy-MM') as visit_month, visit_count from test1
. . . . . . . . . . . . . . .>     ) t1 group by user_id,visit_month
. . . . . . . . . . . . . . .> ) t2;
...
INFO  : OK
+----------+--------------+---------------+---------------+
| user_id  | visit_month  | month_visits  | total_visits  |
+----------+--------------+---------------+---------------+
| u01      | 2017-01      | 11            | 11            |
| u01      | 2017-02      | 12            | 23            |
| u02      | 2017-01      | 12            | 12            |
| u03      | 2017-01      | 8             | 8             |
| u04      | 2017-01      | 3             | 3             |
+----------+--------------+---------------+---------------+
5 rows selected (11.095 seconds)

```

### 2.2 店铺UV统计
-- 有50W个京东店铺，每个顾客访客访问任何一个店铺的任何一个商品时都会产生一条访问日志，  
-- 访问日志存储的表名为Visit，访客的用户id为user_id，被访问的店铺名称为shop，数据如下：  

--                 u1  a  
--                 u2  b  
--                 u1  b  
--                 u1  a  
--                 u3  c  
--                 u4  b  
--                 u1  a  
--                 u2  c  
--                 u5  b  
--                 u4  b  
--                 u6  c  
--                 u2  c  
--                 u1  b  
--                 u2  a  
--                 u2  a  
--                 u3  a  
--                 u5  a  
--                 u5  a  
--                 u5  a  
-- 请统计：  
-- (1)每个店铺的UV（访客数）  
-- (2)每个店铺访问次数top3的访客信息。输出店铺名称、访客id、访问次数  

#### 2.2.1 每个店铺的UV（访客数）
```shell
0: jdbc:hive2://0.0.0.0:10000> select shop, count(user_id) count from visit group by shop;
...
+-------+--------+
| shop  | count  |
+-------+--------+
| a     | 9      |
| b     | 6      |
| c     | 4      |
+-------+--------+
3 rows selected (10.679 seconds)

```

#### 2.2.2 每个店铺访问次数top3的访客信息。输出店铺名称、访客id、访问次数
先按照店铺、用户统计访问次数
```shell
0: jdbc:hive2://0.0.0.0:10000> select shop, user_id, count(user_id) count from visit group by shop, user_id;
...
INFO  : OK
+-------+----------+--------+
| shop  | user_id  | count  |
+-------+----------+--------+
| a     | u1       | 3      |
| a     | u2       | 2      |
| a     | u3       | 1      |
| a     | u5       | 3      |
| b     | u1       | 2      |
| b     | u2       | 1      |
| b     | u4       | 2      |
| b     | u5       | 1      |
| c     | u2       | 2      |
| c     | u3       | 1      |
| c     | u6       | 1      |
+-------+----------+--------+
11 rows selected (22.809 seconds)
0: jdbc:hive2://0.0.0.0:10000>
```

再以shop做分区并按访问量排序
```shell
0: jdbc:hive2://0.0.0.0:10000> select shop, user_id, count,
. . . . . . . . . . . . . . .> row_number() over(partition by shop order by count desc ) rank
. . . . . . . . . . . . . . .> from (
. . . . . . . . . . . . . . .>   select shop, user_id, count(user_id) count from visit group by shop, user_id
. . . . . . . . . . . . . . .> ) t1;
... 
INFO  : OK
+-------+----------+--------+-------+
| shop  | user_id  | count  | rank  |
+-------+----------+--------+-------+
| a     | u5       | 3      | 1     |
| a     | u1       | 3      | 2     |
| a     | u2       | 2      | 3     |
| a     | u3       | 1      | 4     |
| b     | u4       | 2      | 1     |
| b     | u1       | 2      | 2     |
| b     | u5       | 1      | 3     |
| b     | u2       | 1      | 4     |
| c     | u2       | 2      | 1     |
| c     | u6       | 1      | 2     |
| c     | u3       | 1      | 3     |
+-------+----------+--------+-------+
11 rows selected (26.162 seconds)
```

最后筛选出排序的rank<=3即可

```shell
0: jdbc:hive2://0.0.0.0:10000> select * from (
. . . . . . . . . . . . . . .>     select shop, user_id, count,
. . . . . . . . . . . . . . .>     row_number() over(partition by shop order by count desc ) rank
. . . . . . . . . . . . . . .>     from (
. . . . . . . . . . . . . . .>         select shop, user_id, count(user_id) count from visit group by shop, user_id
. . . . . . . . . . . . . . .>     ) t1
. . . . . . . . . . . . . . .> ) t2 where rank <=3;
...
INFO  : OK

+----------+-------------+-----------+----------+
| t2.shop  | t2.user_id  | t2.count  | t2.rank  |
+----------+-------------+-----------+----------+
| a        | u5          | 3         | 1        |
| a        | u1          | 3         | 2        |
| a        | u2          | 2         | 3        |
| b        | u4          | 2         | 1        |
| b        | u1          | 2         | 2        |
| b        | u5          | 1         | 3        |
| c        | u2          | 2         | 1        |
| c        | u6          | 1         | 2        |
| c        | u3          | 1         | 3        |
+----------+-------------+-----------+----------+

9 rows selected (25.355 seconds)

```
