---
layout: page
title: 我的简历
description: "My Resume"
header-img: "img/tag-bg.jpg"
permalink: /resume-zh
---

## 简介

自2005年获得学士学位以来，我一直从事软件开发和管理工作。我在互联网和企业应用程序开发领域有超过10年的经验。
我从今年初开始转向大数据方向，师从中国著名大数据专家[**侯胜文**老师](https://mvp.aliyun.com/mvp/detail/394 ){:target="_blank"}，他曾是阿里巴巴的[**MVP**](https://mvp.aliyun.com/mvp/detail/394 ){:target="_blank"}和腾讯的[**TVP**](https://cloud.tencent.com/tvp/member/462){:target="_blank"}，现在是电信天一云的首席技术专家。

## 工作经历
### **2022-01~现在 大数据**

<style>a{text-decoration: underline;}</style>
**Hadoop/HDFS/Hive**
* 建设大数据集群;
* [使用UDF实现复杂的操作](/bigdata/2022/04/21/create-hive-udf/){:target="_blank"};
* [使用窗口功能按月汇总用户访问量和截至当月的总访问量](/bigdata/2022/05/13/hive-windowing-function-2/){:target="_blank"};
* [使用窗口功能汇总每个门店前N名用户的访问量](/bigdata/2022/05/13/hive-windowing-function-2/){:target="_blank"};
* 为房屋中介设计离线数据仓库（ODS/DWD/DWS/ADS）;

**Spark**
* [使用Spark Shell做单词统计](/bigdata/2022/06/01/spark-shell/){:target="_blank"};
* [使用集群模式做单词统计](/bigdata/2022/06/22/word-count-by-spark-cluster/){:target="_blank"};
* [计算图书的每日平均销量](/bigdata/2022/06/20/spark-task/#task-1){:target="_blank"};
* [对账户按账户名和余额排序](/bigdata/2022/06/20/spark-task/#task-2){:target="_blank"};
* [获取前N名的支付金额](/bigdata/2022/06/20/spark-task/#task-3){:target="_blank"};

**Clickhouse**
* [实验：Brown University Benchmark](/bigdata/2022/06/08/clickhouse-brown-university-benchmark/){:target="_blank"};

**Flume**
* [传输Nginx log转存到HDFS](/db/2022/06/13/flume-practice/#receive-nginx-logs-and-transcribe-to-hdfs){:target="_blank"};
* [使用Flume获取Nginx log给Kafka消费](/bigdata/2022/04/25/kafka-collect-nginx-log-through-flume/){:target="_blank"};

**Sqoop 1/2**
* [从MySQL导入数据到HDFS和Hive表](/bigdata/2022/05/04/sqoop-usage/){:target="_blank"};
* [从HDFS导出数据到MySQL](/bigdata/2022/05/04/sqoop-usage/#export-from-hdfs-to-mysql){:target="_blank"};
* [Sqoop2：从MySQL导入数据到HDFS](/bigdata/2022/06/11/sqoop2-practice/){:target="_blank"};

**Canal/Kafka**
* [实时获取MySQL数据变化到Kafka](/bigdata/2022/07/16/build-realtime-data-flow/){:target="_blank"};

**Flink**
* [实时获取前N名热销商品](/bigdata/2022/07/09/flink-topproducts-pageuserview-ad/#1-top-hot-products){:target="_blank"};
* [实时获取Page View和前N名页面](/bigdata/2022/07/09/flink-topproducts-pageuserview-ad/#2-product-page-view){:target="_blank"};
* [实时分析注册用户的省份、年龄段、性别分布](/bigdata/2022/07/08/flink-user-analyses/){:target="_blank"};
* [实时单词统计](/bigdata/2022/07/02/flink-realtime-word-counting/){:target="_blank"};
* [从Mysql拉取数据到HBase](/bigdata/2022/07/19/pulling-data-from-mysql-to-hbase/){:target="_blank"};
* [Flink CEP - 预警登录异常用户](/bigdata/2022/07/25/flink-cep/){:target="_blank"};
* [Flink CDC - 从MySQL实时同步数据](/bigdata/2022/07/29/flink-cdc/){:target="_blank"};

### 2012~2021 互联网

* 2019/1-Now 绍兴随手智能科技有限公司  **CTO & 技术合伙人**  
  我以技术合伙人的身份参与创办了一家公司。我们向纺织行业的工厂提供SaaS服务（包括库存管理、订单管理等）。在接下来的2-3年里，我们已经开发了近300个客户。
  职责： 
  * 与客户沟通，找到他们的需求；
  * 系统架构、适当语言和工具的选择；
  * 模型设计；分配任务，并监控进度。
  * 全栈开发；  
  技术栈：**Linux-Nginx-MySQL-Redis-PHP/Laravel-VUE/Nova**


* 2016/3-2016/9  弈汇数据技术有限公司  **技术经理**    
  职责：与客户沟通，了解客户需求；系统架构；    
  技术栈： **Linux Apache/Nginx MySQL PHP Ruby/Rails**


* 2014/3-2016/2 眼璞网络科技有限公司  **高级PHP工程师/技术经理**  
  这是一家初创公司，最初我们在眼镜领域创建了一个垂直电子商务平台。之后，我们为眼镜行业搭建了一个培训平台，为全国的眼镜商提供服务。  
  职责：系统架构；向团队成员分配任务；与市场部协调；编程；  
  技术栈： **Linux Apache/Nginx MySQL PHP Ruby/Rails**


* 2012/8-2014/2 Nextec in Otto Group  **中级PHP工程师**  
  我们是德国OTTO集团的研发部门，我们为该集团进入中国的业务开发了电子商务系统。
  职责：需求分析；编程和调试；作为主要开发者交付电子商务系统；   
  技术栈： **Linux Apache/Nginx MySQL PHP**

* 2011/3-2012/8 新大陆翼码科技有限公司  **初级PHP工程师**     
  该公司主要提供QRCode支付解决方案。
  职责：数据库模型设计；需求分析；编程、调试和维护项目代码；   
  技术栈： **Linux Apache/Nginx MySQL PHP**

### 2005~2011 C & Linux 工程师

* 2008/7-2011/3 [百咨信息科技（上海）有限公司](http://linpus.com/pages/page_index_en){:target="_blank"}    **高级C工程师**   
  我们将Linpus Linux交付给电脑供应商，如宏碁和华硕，它们安装在电脑上，一起销售一空。
  职责：在linux环境下编程、调试和发布；将任务分配给我的团队成员。   
  技术栈： **Linux C/GTK Shell**

* 2005/7-2008/5 [东软股份大连分公司](https://www.neusoft.com/){:target="_blank"}   **C工程师**   
  我所在的部门是一家汽车软件供应商，从事嵌入式软件开发，我们的主要开发环境是C语言。我们也是中国第一家通过CMMI5认证的公司。
  职责：需求分析、设计、编码；  
  技术栈： **C** 
