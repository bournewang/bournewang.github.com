---
layout: post
title:  "Hbase Notes"
date:   2022-07-15 16:01:09 +0800
categories: bigdata
tags: [Bigdata, HBase]
---


## HBase
### HMaster

**What's the function of HMaster?**  
HMaster is responsible for region assignment as well as DDL (create, delete tables) operations.
There are two main responsibilities of HMaster:
* Coordinating the region servers, including assigns regions on startup, recovery or load balancing, and re-assigns regions.
* Manage the tables, including creating, updating, deleting tables in HBase.

![Hbase](/post_img/hbase.jpg)

### Data split
![Data split](/post_img/hbase-data-split.jpg)
