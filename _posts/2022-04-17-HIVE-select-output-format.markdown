---
layout: post
title:  "HIVE select output without header"
date:   2022-04-17 17:49:05 +0800
categories: bigdata
tags:
  - Bigdata
  - Hive
---

# Problem: Hive select output without column name
```shell
hive> select * from ods_crm_cdel_hold_da  limit 10;
OK
1	20001	7767	496	1	2021-11-23 11:25:27.0	2021-12-08 11:25:27.0	20220417
2	20002	9068	112	1	2021-05-28 19:39:23.0	2021-07-23 19:39:23.0	20220417
3	20003	8501	456	1	2021-08-19 19:31:28.0	2021-09-24 19:31:28.0	20220417
4	20004	10348	64	1	2021-05-09 18:39:11.0	2021-04-30 18:39:11.0	20220417
```

To solve this issus, set print header to true: 
```shell
hive> set hive.cli.print.header=true;
hive> select * from ods_crm_cdel_hold_da  limit 10;
OK
ods_crm_cdel_hold_da.id	ods_crm_cdel_hold_da.cdel_id	ods_crm_cdel_hold_da.holder_id	ods_crm_cdel_hold_da.shop_code	ods_crm_cdel_hold_da.status	ods_crm_cdel_hold_da.create_time	ods_crm_cdel_hold_da.update_time	ods_crm_cdel_hold_da.pt
1	20001	7767	496	1	2021-11-23 11:25:27.0	2021-12-08 11:25:27.0	20220417
2	20002	9068	112	1	2021-05-28 19:39:23.0	2021-07-23 19:39:23.0	20220417
3	20003	8501	456	1	2021-08-19 19:31:28.0	2021-09-24 19:31:28.0	20220417
4	20004	10348	64	1	2021-05-09 18:39:11.0	2021-04-30 18:39:11.0	20220417
```

Seems working, but why also output table name with it?
We need another setting, set unique column names to false:
```shell
hive> set hive.resultset.use.unique.column.names=false;
hive> select * from ods_crm_cdel_hold_da  limit 10;
OK
id	cdel_id	holder_id	shop_code	status	create_time	update_time	pt
1	20001	7767	496	1	2021-11-23 11:25:27.0	2021-12-08 11:25:27.0	20220417
2	20002	9068	112	1	2021-05-28 19:39:23.0	2021-07-23 19:39:23.0	20220417
3	20003	8501	456	1	2021-08-19 19:31:28.0	2021-09-24 19:31:28.0	20220417
4	20004	10348	64	1	2021-05-09 18:39:11.0	2021-04-30 18:39:11.0	20220417
5	20005	9939	11	1	2021-09-04 19:15:26.0	2021-09-09 19:15:26.0	20220417
```
Good, everything seems working right.
Let's exit hive shell, re-enter it, and execute query again:
```shell
hive> select * from ods_crm_cdel_hold_da limit 10;
OK
1	20001	7767	496	1	2021-11-23 11:25:27.0	2021-12-08 11:25:27.0	20220417
2	20002	9068	112	1	2021-05-28 19:39:23.0	2021-07-23 19:39:23.0	20220417
3	20003	8501	456	1	2021-08-19 19:31:28.0	2021-09-24 19:31:28.0	20220417
4	20004	10348	64	1	2021-05-09 18:39:11.0	2021-04-30 18:39:11.0	20220417
```

WOW, nothing changed, let fix it in $HIVE_HOME/conf/hive-site.xml:
```xml
    <property>
        <name>hive.resultset.use.unique.column.names</name>
        <value>false</value>
    </property>
...
    <property>
        <name>hive.cli.print.header</name>
        <value>true</value>
    </property>
```

restart hive and check it again:
```shell
hive> select * from ods_crm_cdel_hold_da limit 10;
OK
id	cdel_id	holder_id	shop_code	status	create_time	update_time	pt
1	20001	7767	496	1	2021-11-23 11:25:27.0	2021-12-08 11:25:27.0	20220417
2	20002	9068	112	1	2021-05-28 19:39:23.0	2021-07-23 19:39:23.0	20220417
3	20003	8501	456	1	2021-08-19 19:31:28.0	2021-09-24 19:31:28.0	20220417
4	20004	10348	64	1	2021-05-09 18:39:11.0	2021-04-30 18:39:11.0	20220417
5	20005	9939	11	1	2021-09-04 19:15:26.0	2021-09-09 19:15:26.0	20220417
```
Great, problem solved.
