---
layout: post
title:  "Hbase Practice"
date:   2022-05-12 22:19:45 +0800
categories: bigdata
tags: [Bigdata, HBase]
---
# Hbase Practice
## 介绍
HBase是开源、分布式、版本化的非关系型（列式）数据库，由Hdfs提供存储、MapReduce进行并行计算，是Google Bigtable的一个开源实现。

非关系型，不要求数据之间有严格的关系，甚至允许同一列的不同功行存储不同类型的数据。

优势：
* 典型的NoSQL
* 容量大， 单表可存储百亿行、百万列，在横向和纵向两个维度插入数据，有很高的弹性。
* 采用LSM树作为内部存储结构（周期性的将小文件合并成大文件，减少对磁盘的访问）
* 列存储，面向列的存储和权限控制
* 稀疏性，关系型DB中，每一个字段类型是事先定义好的，占用固定的存储空间，即使空值也需要占用存储。Hbase中为空的列不占用存储空间。
* 扩展性强，根据表region大小进行分区，存储在集群不同的节点上。热扩展，无需停止现有服务。
* 高可靠性，WAL（Write-Ahead-Log)预写日志将数据操作（CRUD）记录下来，Replication机制根据日志操作来做数据同步。

进程说明：
HMaster
- 管理对表的CRUD
- 管理RegionServer的负载均衡，调度Region（分配和移除）
- 处理RegionServer的故障转移

RegionServer
- 处理分配的region
- 处理客户端请求
- 刷新缓存到hdfs
- 处理region分片
- 执行压缩

**概念**
* Column Family：列族，一组相关的列组成，有相同的名称前缀，类似一个子表；列族是分开存储的；
* Column Qualifier，列标识；
* Cell：单元格，row key + column family + column qualifier 确定一个cell，一个cell保存着同一份数据的多个版本（默认维护3个版本），
* Timestamp：根据时间戳来获取特定版本的数据，默认获取最新的。

## 连接 HMaster
```shell
[root@hadoop001 ~]# hbase shell
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/module/hbase-1.7.1/lib/slf4j-log4j12-1.7.25.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/module/hadoop-2.7.3/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]
2022-05-12 17:41:43,952 WARN  [main] util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
HBase Shell
Use "help" to get list of supported commands.
Use "exit" to quit this interactive shell.
Version 1.7.1, r2d9273667e418e7023f9104a830cdcb8233b6f25, Fri Jul 16 00:20:26 PDT 2021
```

## 创建表 - create
注意：
* <font color="red">跟(hive)sql不同，除了关键字，表明、列族要加引号;</font>
* <font color="red">大小写敏感，如NAME不能写作name;</font>
* <font color="red">命令行结尾不需要/不能添加分号';'</font>

```shell
hbase(main):002:0> create 'Students','StuInfo', 'Grades'
0 row(s) in 2.5090 seconds

=> Hbase::Table - Students

hbase(main):025:0* exists 'Students'
Table Students does exist
0 row(s) in 0.0060 seconds

hbase(main):026:0> describe 'Students'
Table Students is ENABLED
Students
COLUMN FAMILIES DESCRIPTION
{NAME => 'Grades', BLOOMFILTER => 'ROW', VERSIONS => '1', IN_MEMORY => 'false', KEEP_DE
LETED_CELLS => 'FALSE', DATA_BLOCK_ENCODING => 'NONE', TTL => 'FOREVER', COMPRESSION =>
 'NONE', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE => '65536', REPLICATION_S
COPE => '0'}
{NAME => 'StuInfo', BLOOMFILTER => 'ROW', VERSIONS => '1', IN_MEMORY => 'false', KEEP_D
ELETED_CELLS => 'FALSE', DATA_BLOCK_ENCODING => 'NONE', TTL => 'FOREVER', COMPRESSION =
> 'NONE', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE => '65536', REPLICATION_
SCOPE => '0'}
2 row(s) in 0.0260 seconds

hbase(main):021:0> create 'Students2', {NAME=>'StuInfo',VERSIONS=>3}, {NAME=>'Grades', BLOCKCACHE=>true}
0 row(s) in 1.2410 seconds

=> Hbase::Table - Students2
```

VERSION => 3,表示Cell内的数据可以保存3个版本
BLOCKCACHE => true, 表示读取数据是允许缓存。

## 修改表 - alter
```shell
hbase(main):028:0> alter 'Students', {NAME=>'Grades', VERSIONS=>4}
Updating all regions with the new schema...
1/1 regions updated.
Done.
0 row(s) in 1.9060 seconds

hbase(main):029:0> describe 'Students'
Table Students is ENABLED
Students
COLUMN FAMILIES DESCRIPTION
{NAME => 'Grades', BLOOMFILTER => 'ROW', VERSIONS => '4', IN_MEMORY => 'false', KEEP_DE
LETED_CELLS => 'FALSE', DATA_BLOCK_ENCODING => 'NONE', TTL => 'FOREVER', COMPRESSION =>
 'NONE', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE => '65536', REPLICATION_S
COPE => '0'}
{NAME => 'StuInfo', BLOOMFILTER => 'ROW', VERSIONS => '1', IN_MEMORY => 'false', KEEP_D
ELETED_CELLS => 'FALSE', DATA_BLOCK_ENCODING => 'NONE', TTL => 'FOREVER', COMPRESSION =
> 'NONE', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE => '65536', REPLICATION_
SCOPE => '0'}
2 row(s) in 0.0250 seconds

```

## 增加/删除列族 - alter & delete
```shell
hbase(main):030:0> alter 'Students', 'Hobby'
Updating all regions with the new schema...
1/1 regions updated.
Done.
0 row(s) in 1.8800 seconds

hbase(main):032:0> alter 'Students', 'delete' => 'Hobby'
Updating all regions with the new schema...
1/1 regions updated.
Done.
0 row(s) in 2.1330 seconds
```

## 插入数据 - put
一条put语句插入或修改一个记录、一个列族里面的一个字段；
```shell
hbase(main):052:0> put 'Students', '0001', 'StuInfo:Name', 'Tome', 1
0 row(s) in 0.0470 seconds

hbase(main):053:0> put 'Students', '0001', 'StuInfo:Age', '18'
0 row(s) in 0.0100 seconds

hbase(main):054:0> put 'Students', '0001', 'StuInfo:Sex', 'Male'
0 row(s) in 0.0050 seconds

hbase(main):055:0> put 'Students', '0001', 'StuInfo:Mobile', '13811112222'
0 row(s) in 0.0040 seconds

hbase(main):056:0> put 'Students', '0001', 'Grades:English', '80'
0 row(s) in 0.0050 seconds

hbase(main):057:0> put 'Students', '0001', 'Grades:Math', '85'
0 row(s) in 0.0050 seconds

hbase(main):058:0> put 'Students', '0001', 'Grades:Chinese', '96'
0 row(s) in 0.0050 seconds

```

## 获取数据 - get
获取全部或部分字段
```shell
hbase(main):059:0> get 'Students', '0001'
COLUMN                 CELL
 Grades:Chinese        timestamp=1652351587028, value=96
 Grades:English        timestamp=1652351573490, value=80
 Grades:Math           timestamp=1652351579064, value=85
 StuInfo:Age           timestamp=1652351534074, value=18
 StuInfo:Mobile        timestamp=1652351560808, value=13811112222
 StuInfo:Name          timestamp=1, value=Tome
 StuInfo:Sex           timestamp=1652351544030, value=Male
1 row(s) in 0.0270 seconds

hbase(main):073:0> get 'Students', '0001', {COLUMN=>['StuInfo:Name', 'Grades:English']}
COLUMN                            CELL
 Grades:English                   timestamp=1652351573490, value=80
 StuInfo:Name                     timestamp=1, value=Tome
1 row(s) in 0.0110 seconds

```

## 扫描 & 过滤 - scan & filter
```shell
hbase(main):005:0> scan 'Students'
ROW                            COLUMN+CELL
 0001                          column=Grades:Chinese, timestamp=1652351587028, value=96
 0001                          column=Grades:English, timestamp=1652351573490, value=80
 0001                          column=Grades:Math, timestamp=1652351579064, value=85
 0001                          column=StuInfo:Age, timestamp=1652351534074, value=18
 0001                          column=StuInfo:Mobile, timestamp=1652351560808, value=13811112222
 0001                          column=StuInfo:Name, timestamp=1, value=Tome
 0001                          column=StuInfo:Sex, timestamp=1652351544030, value=Male
 0002                          column=Grades:English, timestamp=2, value=78
 0002                          column=Grades:Math, timestamp=2, value=83
 0002                          column=StuInfo:Age, timestamp=2, value=17
 0002                          column=StuInfo:Name, timestamp=2, value=Marry
 0002                          column=StuInfo:Sex, timestamp=2, value=Female
2 row(s) in 0.0160 seconds
```
**比较器**
* BinaryComparator	匹配完整字节数组
* BinaryPrefixComparator	匹配字节数组前缀
* BitComparator	匹配比特位
* NullComparator	匹配空值
* RegexStringComparator	匹配正则表达式
* SubstringComparator	匹配子字符串
  
**行键过滤器**
* RowFilter 配合比较器和运算符，实现行键字符串的比较和过滤
* PrefixFilter	行键前缀比较器，比较行键前缀	scan 'Student', FILTER => "PrefixFilter('0001')"
  同scan 'Student', FILTER => "RowFilter(=,'substring:0001')"
* KeyOnlyFilter	只对单元格的键进行过滤和显示，不显示值	scan 'Student', FILTER => "KeyOnlyFilter()"
* FirstKeyOnlyFilter	只扫描显示相同键的第一个单元格，其键值对会显示出来	scan 'Student', FILTER => "FirstKeyOnlyFilter()"
* InclusiveStopFilter	替代 ENDROW 返回终止条件行	scan 'Student', { STARTROW => '0001', FIILTER => "InclusiveStopFilter('binary:0002')" }
  同scan 'Student', { STARTROW => '0001', ENDROW => '0003' }
```shell
hbase(main):006:0> scan 'Students', FILTER=>"PrefixFilter('0001')"
ROW                            COLUMN+CELL
 0001                          column=Grades:Chinese, timestamp=1652351587028, value=96
 0001                          column=Grades:English, timestamp=1652351573490, value=80
 0001                          column=Grades:Math, timestamp=1652351579064, value=85
 0001                          column=StuInfo:Age, timestamp=1652351534074, value=18
 0001                          column=StuInfo:Mobile, timestamp=1652351560808, value=13811112222
 0001                          column=StuInfo:Name, timestamp=1, value=Tome
 0001                          column=StuInfo:Sex, timestamp=1652351544030, value=Male
1 row(s) in 0.0190 seconds
hbase(main):012:0> scan 'Students', FILTER=>"RowFilter(=, 'binary:0001')"
ROW                            COLUMN+CELL
 0001                          column=Grades:Chinese, timestamp=1652351587028, value=96
 0001                          column=Grades:English, timestamp=1652351573490, value=80
 0001                          column=Grades:Math, timestamp=1652351579064, value=85
 0001                          column=StuInfo:Age, timestamp=1652351534074, value=18
 0001                          column=StuInfo:Mobile, timestamp=1652351560808, value=13811112222
 0001                          column=StuInfo:Name, timestamp=1, value=Tome
 0001                          column=StuInfo:Sex, timestamp=1652351544030, value=Male
1 row(s) in 0.0160 seconds
```

**列族过滤器** 
列过滤器	描述	示例
* FamilyFilter 列族过滤
* QualifierFilter	列标识过滤器，只显示对应列名的数据
* ColumnPrefixFilter	对列名称的前缀进行过滤
* MultipleColumnPrefixFilter	可以指定多个前缀对列名称过滤
* ColumnRangeFilter	过滤列名称的范围
```shell
hbase(main):003:0> scan 'Students', FILTER => "FamilyFilter(=, 'substring:StuInfo')"
ROW                        COLUMN+CELL
 0001                      column=StuInfo:Age, timestamp=1652351534074, value=18
 0001                      column=StuInfo:Mobile, timestamp=1652351560808, value=13811112222
 0001                      column=StuInfo:Name, timestamp=1, value=Tome
 0001                      column=StuInfo:Sex, timestamp=1652351544030, value=Male
 0002                      column=StuInfo:Age, timestamp=2, value=17
 0002                      column=StuInfo:Name, timestamp=2, value=Marry
 0002                      column=StuInfo:Sex, timestamp=2, value=Female
2 row(s) in 0.0160 seconds
hbase(main):004:0> scan 'Students', FILTER => "QualifierFilter(=, 'substring:Math')"
ROW                        COLUMN+CELL
 0001                      column=Grades:Math, timestamp=1652351579064, value=85
 0002                      column=Grades:Math, timestamp=2, value=83
2 row(s) in 0.0130 seconds
```

**值过滤器**
* ValueFilter	值过滤器，找到符合值条件的键值对
同 get 'Student', '0001'
* SingleColumnValueFilter	在指定的列族和列中进行比较的值过滤器
* SingleColumnValueExcludeFilter	排除匹配成功的值

列出>80分的科目及成绩：
```shell
hbase(main):030:0> scan 'Students', FILTER=>"(FamilyFilter(=, 'binary:Grades') AND ValueFilter(>,'binary:80'))"
ROW                            COLUMN+CELL
 0001                          column=Grades:Chinese, timestamp=1652351587028, value=96
 0001                          column=Grades:Math, timestamp=1652351579064, value=85
 0002                          column=Grades:Math, timestamp=2, value=83
2 row(s) in 0.0060 seconds

```
**多个过滤器**
```shell
hbase(main):026:0> scan 'Students', FILTER=>"(RowFilter(=, 'binary:0001') AND QualifierFilter(=,'binary:Math'))"
ROW                            COLUMN+CELL
 0001                          column=Grades:Math, timestamp=1652351579064, value=85
1 row(s) in 0.0180 seconds

```

## 清空数据表 - truncate
```shell
hbase(main):048:0* truncate 'Students'
Truncating 'Students' table (it may take a while):
 - Disabling table...
 - Truncating table...
0 row(s) in 3.3760 seconds
```
## 删除表 - disable & drop
与其他数据库不同，需要先disable库名。

```shell
hbase(main):040:0> drop 'Students'

ERROR: Table Students is enabled. Disable it first.

Here is some help for this command:
Drop the named table. Table must first be disabled:
  hbase> drop 't1'
  hbase> drop 'ns1:t1'

hbase(main):041:0> disable 'Students'
0 row(s) in 2.2430 seconds

hbase(main):042:0> drop 'Students'
0 row(s) in 1.2410 seconds

hbase(main):043:0> exists 'Students'
Table Students does not exist
0 row(s) in 0.0130 seconds

```

