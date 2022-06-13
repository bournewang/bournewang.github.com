---
layout: post
title:  "Datawarehouse Model Design"
date:   2022-04-17 18:50:36 +0800
categories: bigdata
tags:
    - Bigdata 
    - Model Design
---

Model design process seems like combine original tables with proper redundancy.
For example, we have 3 tables in database: brands/corps/shops,
![img.png](/img/original_tables.png)

Just put these fields together, and add table name as prefix to avoid conflict, we'll get the final DWD table as follow:
![img_1.png](/img/dwd_shop.png)
