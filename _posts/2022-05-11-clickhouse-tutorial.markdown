---
layout: post
title:  "Clickhouse实战"
date:   2022-05-11 22:28:40 +0800
categories: bigdata
tags:
    - Bigdata
    - Clickhouse
---

# Clickhouse Overview
Clickhouse是优秀的列式数据库，主要用于OLAP领域。  

特点:  
* 不同于OLTP需要大量的CRUD，OLAP一般只对数据做读（R）操作；
* 无需事物，低一致性要求；
* 查询通常涉及大量的行，少量的列，较小的结果集；

根据以上特点，列式数据库要从底层优化。

列式数据库优点  
* 行存储模式下，不相关的列在查询时也被读出，读取数据量大于需要的数据量。列存储时，只需要读取相关的列即可；
* 同一列中数据类型一致，压缩效果明显。列存有着高达十倍的压缩比，节省存储空间；
* 更高的压缩比，数据量更小，减少了读取时间。 
* 高压缩比还意味着系统缓存效果更好。
* 有序存储，建表时按照某列排序；


## 安装
```shell
[root@hadoop002 ~]# yum install yum-utils
[root@hadoop002 ~]# rpm --import https://repo.clickhouse.com/CLICKHOUSE-KEY.GPG
[root@hadoop002 ~]# yum-config-manager --add-repo https://repo.clickhouse.com/rpm/stable/x86_64
[root@hadoop002 ~]# yum install clickhouse-server clickhouse-client
```
## 配置
编辑/etc/clickhouse-server/config.xml，添加listen_host,使所有地址都可访问。
```shell
<listen_host>0.0.0.0</listen_host>
```
clickhouse默认使用9000端口，跟hdfs的rpc端口冲突，需要将9000更改一下，这里统一替换为9010；

## 启动服务
```shell
[root@hadoop002 ~]# systemctl start clickhouse-server.service
[root@hadoop002 ~]# systemctl enable clickhouse-server.service
```

## 登录客户端
```shell
[root@hadoop002 ~]# clickhouse-client -h hadoop002 -m --port 9010
ClickHouse client version 22.2.2.1.
Connecting to hadoop002:9010 as user default.
Connected to ClickHouse server version 22.2.2 revision 54455.

hadoop002 :)
```
## 创建数据库
```shell
hadoop002 :) create database if not exists db1;

CREATE DATABASE IF NOT EXISTS db1

Query id: 1c7a6b12-98c8-4b84-a049-0a0afdcae635

Ok.

0 rows in set. Elapsed: 0.002 sec.

hadoop002 :)
```

## 创建表格
```shell
hadoop002 :) create table if not exists books(id Int32, name String, author String, pub_year Int32)
             engine=MergeTree order by id;

CREATE TABLE IF NOT EXISTS books
(
    `id` Int32,
    `name` String,
    `author` String,
    `pub_year` Int32
)
ENGINE = MergeTree
ORDER BY id

Query id: 78ce95ea-d5e5-4582-80d7-2907ee1181f8

Ok.

0 rows in set. Elapsed: 0.004 sec.
```

## 插入数据
注意：<font color="red">字串要用单引号</font>
```shell
hadoop002 :) insert into books (id, name, author, pub_year) values(1,"How to learn Python", "Mike", 2015);

INSERT INTO books (id, name, author, pub_year) FORMAT Values

Query id: c8cd0d67-01b3-42a7-ac21-251ce6c3f459

Connecting to database db1 at hadoop002:9010 as user default.
Connected to ClickHouse server version 22.2.2 revision 54455.

Exception on client:
Code: 47. DB::Exception: Missing columns: 'How to learn Python' while processing query: '`How to learn Python`', required columns: 'How to learn Python' 'How to learn Python': While executing ValuesBlockInputFormat: data for INSERT was parsed from query. (UNKNOWN_IDENTIFIER)

hadoop002 :) insert into books (id, name, author, pub_year) values(1,'How to learn Python', 'Mike', 2015);

INSERT INTO books (id, name, author, pub_year) FORMAT Values

Query id: 31ff4118-9993-4f82-8d60-41d1ffb29d38

Connecting to database db1 at hadoop002:9010 as user default.
Connected to ClickHouse server version 22.2.2 revision 54455.

Ok.

1 rows in set. Elapsed: 0.002 sec.

```

## 新增列
```shell
hadoop002 :) alter table books add column category Nullable(String);

ALTER TABLE books
    ADD COLUMN `category` Nullable(String)

Query id: 4c4eec89-c7ef-4d86-bcf5-3cbfc53791f2

Ok.

0 rows in set. Elapsed: 0.003 sec.
hadoop002 :) describe books;

DESCRIBE TABLE books

Query id: b5cf4c2f-d1d6-4846-a932-7b8b62070573

┌─name─────┬─type─────────────┬─default_type─┬─default_expression─┬─comment─┬─codec_expression─┬─ttl_expression─┐
│ id       │ Int32            │              │                    │         │                  │                │
│ name     │ String           │              │                    │         │                  │                │
│ author   │ String           │              │                    │         │                  │                │
│ pub_year │ Int32            │              │                    │         │                  │                │
│ category │ Nullable(String) │              │                    │         │                  │                │
└──────────┴──────────────────┴──────────────┴────────────────────┴─────────┴──────────────────┴────────────────┘

5 rows in set. Elapsed: 0.001 sec.
```

## 复制表

```shell
hadoop002 :) create table books1 as books;

CREATE TABLE books1 AS books

Query id: ff473ee4-89d7-41dc-9c21-98dab15df462

Ok.

0 rows in set. Elapsed: 0.004 sec.

hadoop002 :) insert into books1 select * from books;

INSERT INTO books1 SELECT *
FROM books

Query id: 7dc19e20-0075-4e2a-b4be-95678cfc89a8

Ok.

0 rows in set. Elapsed: 0.002 sec.

hadoop002 :) select * from books1;

SELECT *
FROM books1

Query id: 670b34a4-009b-485c-863d-58f1b0c54542

┌─id─┬─name────────────────┬─author──┬─pub_year─┬─category──┐
│  1 │ How to learn Python │ Mike    │     2015 │ ᴺᵁᴸᴸ      │
│  2 │ Inside Java         │ Orilly  │     2013 │ Programme │
│  3 │ Inside Linux        │ Orilly  │     2016 │ Programme │
│  4 │ Mastering Nginx     │ Charles │     2012 │ Server    │
└────┴─────────────────────┴─────────┴──────────┴───────────┘

4 rows in set. Elapsed: 0.002 sec.
```

## 重命名表
```shell
hadoop002 :) rename table books1 to books_copy;

RENAME TABLE books1 TO books_copy

Query id: 7182e842-e510-411b-a2e4-f87e1f20d9d4

Ok.

0 rows in set. Elapsed: 0.001 sec. 

hadoop002 :) show tables;

SHOW TABLES

Query id: 2672dc02-ee03-4709-9b5f-5b6a4fe942ae

┌─name───────┐
│ books      │
│ books_copy │
└────────────┘

2 rows in set. Elapsed: 0.002 sec. 
```

## 删除表
```shell
hadoop002 :) drop table books_copy;

DROP TABLE books_copy

Query id: d9be7ef2-bf5d-4f7a-9da0-b134c31ed45f

Ok.

0 rows in set. Elapsed: 0.001 sec. 

hadoop002 :) show tables;

SHOW TABLES

Query id: 5aed16f0-6f04-4b06-9a2e-27d011483786

┌─name──┐
│ books │
└───────┘

1 rows in set. Elapsed: 0.002 sec. 
```

## 删除数据库
```shell
hadoop002 :) drop database db2;

DROP DATABASE db2

Query id: f5429ee1-3384-49d7-8999-7df921d1b07d

Ok.

0 rows in set. Elapsed: 0.001 sec. 


```
