---
layout: post
title:  "创建DataWorks工作空间无法选中EMR引擎"
date:   2022-04-03 17:15:43 +0800
categories: bigdata
tags:
  - Bigdata
  - DataWorks
  - E-MapReduce
---

# 创建DataWorks工作空间无法选中EMR引擎

创建DataWorks工作空间的时候，无法选中E-MapReduce引擎（已经在同区提前购买，并完成了初始化），如图：
![EMR](/img/workspace_cannot_select_emr.png "Workspace cannot select EMR")

以下是所做尝试：
* 切换到北京区，购买集群，创建空间，问题依旧；
* 在已经创建的空间里绑定EMR集群，因为没有资源组，无法绑定成功。
* 购买资源组，等了半天不见创建出实例；跟客服沟通了一小时，竟然说我弄错区了，要退款重新买；退款前，我自己弄清楚了，要手工创建资源组，并关联订单。<font style="color: green;">//并不是所有的实例都是自动创建的。</font>
* 资源组绑定了EMR集群。工作空间里可以绑定了；但是创建工作空间时仍然无法选择EMR引擎。<font style="color: green;">//难道只能创建后再修改？</font>
