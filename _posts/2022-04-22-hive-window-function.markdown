---
layout: post
title:  "Hive Windowing and Analytics Function - Part 1"
date:   2022-04-21 17:29:38 +0800
categories: bigdata
tags:
    - Bigdata
    - Hive
---
# Hive Windowing and Analytics Function - Part 1

Hive provides powerful window functions on current frame.

In this chapter, I will demostrate how these functions work: 
* first_value()
* last_value()
* sum()
* count()

For example, we have orders table, 

```shell
> describe orders;
col_name	        data_type
order_id            	bigint
user_id             	int
order_amount        	decimal(16,4)
create_time         	string
pt                  	string
```

and I want to get these order info for each user with sql: 
* First and last order info(create_time/amount) 
* Minimum and Maximum amount of order and create_time
* Total order amount and count

```shell
select distinct(user_id),
--     first order
    first_value(create_time)  over(partition by user_id order by create_time) as first_order_time,
    first_value(order_amount) over(partition by user_id order by create_time) as first_order_amount,
--     last order
    last_value(create_time)  over(partition by user_id order by create_time) as last_order_time,
    last_value(order_amount) over(partition by user_id order by create_time) as last_order_amount,
--     min amount
    first_value(create_time)  over(partition by user_id order by order_amount ) as min_order_time,
    first_value(order_amount) over(partition by user_id order by order_amount ) as min_order_amount,
--     max amount
    first_value(create_time)  over(partition by user_id order by order_amount desc) as max_order_time,
    first_value(order_amount) over(partition by user_id order by order_amount desc) as max_order_amount,
--     total amount/count
    sum(order_amount) over(partition by user_id) as total_order_amount,
    count() over(partition by user_id) as total_order_count
    from orders;
```

