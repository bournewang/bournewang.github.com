---
layout: post
title:  "Hive Windowing and Analytics Function - Part 3 实操"
date:   2022-05-14 19:14:11 +0800
categories: bigdata
tags:
    - Bigdata
    - Hive
---

# Hive Windowing and Analytics Function - Part 3 实操

## 1.1 订单表统计 

已知一个表orders，有如下字段:dt(date)，order_id，user_id，amount。  
数据样例:2021-01-01,10029028,1000003251,33.57。

### 1.1.1 给出 2021年每个月的订单数、用户数、总成交金额
思路：  
先根据日期计算月份，再根据月份分组统计：
```shell
0: jdbc:hive2://0.0.0.0:10000> select month, 
. . . . . . . . . . . . . . .> count(order_id) as order_count, 
. . . . . . . . . . . . . . .> count(distinct(user_id)) as user_count, 
. . . . . . . . . . . . . . .> sum(amount) as order_amount 
. . . . . . . . . . . . . . .> from ( select order_id, user_id, amount, dt, 
. . . . . . . . . . . . . . .>   from_unixtime(unix_timestamp(dt, 'yyyy-MM-dd'),'yyyy-MM') as month from orders
. . . . . . . . . . . . . . .> ) t1 group by month;
...
INFO  : OK
INFO  : Concurrency mode is disabled, not creating a lock manager
+----------+--------------+-------------+---------------+
|  month   | order_count  | user_count  | order_amount  |
+----------+--------------+-------------+---------------+
| 2018-11  | 1            | 1           | 234.00        |
| 2021-01  | 3            | 2           | 100.71        |
| 2021-02  | 3            | 2           | 100.71        |
| 2021-11  | 1            | 1           | 234.00        |
+----------+--------------+-------------+---------------+
4 rows selected (12.162 seconds)
```

### 1.1.2 给出2021年11月的新客数(指在11月才有第一笔订单)
思路：
* 1，根据日期转换月份；
* 2，按照用户分区，并按日期排序；
* 3，分区内第一行月份是2021-11的用户即为新用户；
```shell
0: jdbc:hive2://0.0.0.0:10000> select user_id, month, dt,rank from (
. . . . . . . . . . . . . . .>    select user_id, dt,month, 
. . . . . . . . . . . . . . .>      row_number() over(partition by user_id order by dt asc) rank from (
. . . . . . . . . . . . . . .>      select order_id, user_id, amount, dt, from_unixtime(unix_timestamp(`dt`, 'yyyy-MM-dd'),'yyyy-MM') as month from orders
. . . . . . . . . . . . . . .>    )t1
. . . . . . . . . . . . . . .>  )t2 where rank=1 and month='2021-11';
INFO  : OK
INFO  : Concurrency mode is disabled, not creating a lock manager
+------------+----------+-------------+-------+
|  user_id   |  month   |     dt      | rank  |
+------------+----------+-------------+-------+
| 100003253  | 2021-11  | 2021-11-02  | 1     |
+------------+----------+-------------+-------+
1 row selected (12.425 seconds)
```

## 1.2 用户年龄段电影偏好统计 
需求：  
有一个5000万的用户文件(字段：user_id，name，age)  
一个2亿记录的用户看电影的记录文件(字段：user_id，url)  
根据年龄段观看电影的次数进行排序？  

思路：  
1、表1中根据年龄计算年龄段；  
2、表2中计算相同用户的观看次数；  
3、join前两步结果，得到年龄段、观看次数；  
4、再次合并相同年龄段的观看次数、排序；  

**注意：要先计算相同用户的观看次数再做join，大幅度降低结果积的大小。**

```shell
0: jdbc:hive2://0.0.0.0:10000> select t1.age_phase, sum(t2.cnt) as count from 
. . . . . . . . . . . . . . .>  (select user_id, case 
. . . . . . . . . . . . . . .>    when age < 10 then '<10' 
. . . . . . . . . . . . . . .>    when age < 20 then '11-20' 
. . . . . . . . . . . . . . .>    when age < 30 then '21-30' 
. . . . . . . . . . . . . . .>    when age < 40 then '31-40' 
. . . . . . . . . . . . . . .>    when age < 50 then '41-50' 
. . . . . . . . . . . . . . .>    else '>50' 
. . . . . . . . . . . . . . .>    end as age_phase from test4user) t1 
. . . . . . . . . . . . . . .>  join 
. . . . . . . . . . . . . . .>    (select user_id, count(url) as cnt from test4log group by user_id) t2 
. . . . . . . . . . . . . . .>  on t1.user_id=t2.user_id   
. . . . . . . . . . . . . . .>  group by age_phase order by count desc;
...
INFO  : OK
+---------------+--------+
| t1.age_phase  | count  |
+---------------+--------+
| 11-20         | 22     |
| 21-30         | 7      |
| 41-50         | 4      |
| 31-40         | 3      |
| >50           | 2      |
+---------------+--------+

```

## 1.3 用户首次购买的金额
需求：  
请用sql写出所有用户中在今年10月份第一次购买商品的金额，  
表orders1字段:  
userid，money，paymenttime(格式：2021-10-01)，orderid  

思路：  
1、先将付款日期转换为月份；  
2、窗口函数以用户分区、按支付日期排序；  
3、过滤出10月份的第一个订单
```shell
0: jdbc:hive2://0.0.0.0:10000> select * from (
. . . . . . . . . . . . . . .>    select *, row_number() over(partition by userid order by paymenttime asc) rank from ( 
. . . . . . . . . . . . . . .>        select *, substr(paymenttime, 0,7) as payment_month from orders1
. . . . . . . . . . . . . . .>     ) t1
. . . . . . . . . . . . . . .>  )t2 where rank=1 and payment_month='2021-10';
...
INFO  : OK
+------------+-----------+-----------------+-------------+-------------------+----------+
| t2.userid  | t2.money  | t2.paymenttime  | t2.orderid  | t2.payment_month  | t2.rank  |
+------------+-----------+-----------------+-------------+-------------------+----------+
| 001        | 100.00    | 2021-10-01      | 123         | 2021-10           | 1        |
| 002        | 500.00    | 2021-10-01      | 125         | 2021-10           | 1        |
+------------+-----------+-----------------+-------------+-------------------+----------+
2 rows selected (13.927 seconds)

```



