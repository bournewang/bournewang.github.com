---
layout: post
title:  "HDFS commands"
date:   2022-05-02 15:00:26 +0800
categories: bigdata
tags:
    - Bigdata
    - HDFS
---

Most of the HDFS commands just like the same commands in linux, such as _cat,chgrp,chmod,chown,cp,df,du,find,ls,mkdir,mv,rm,rmdir,stat_, while also provide some other enhanced commands: 

## Common commands
```shell
[root@hadoop001 ~]# hdfs dfs -ls /
[root@hadoop001 ~]# hdfs dfs -put hello /
[root@hadoop001 ~]# hdfs dfs -ls /
Found 1 items
-rw-r--r--   3 root supergroup         12 2022-05-02 14:20 /hello
[root@hadoop001 ~]# hdfs dfs -get /hello h1
[root@hadoop001 ~]# ll
total 2043328
-rw-r--r--  1 root root          12 May  2 14:21 h1
-rw-r--r--  1 root root          12 May  2 14:19 hello
[root@hadoop001 ~]# cat h1
hello world
[root@hadoop001 ~]# hdfs dfs -chmod a+w /hello
[root@hadoop001 ~]# hdfs dfs -ls /
Found 1 items
-rw-rw-rw-   3 root supergroup         12 2022-05-02 14:20 /hello
[root@hadoop001 ~]# hdfs dfs -chmod a-w /hello
[root@hadoop001 ~]# hdfs dfs -ls /
Found 1 items
-r--r--r--   3 root supergroup         12 2022-05-02 14:20 /hello
[root@hadoop001 ~]# hdfs dfs -chmod o+w /hello
[root@hadoop001 ~]# hdfs dfs -ls /
22/05/02 14:24:10 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Found 1 items
-r--r--rw-   3 root supergroup         12 2022-05-02 14:20 /hello
[root@hadoop001 ~]# hdfs dfs -df /
Filesystem          Size    Used    Available  Use%
hdfs://ns   127219077120  106496  94618918912    0%
[root@hadoop001 ~]# hdfs dfs -df -h /
Filesystem     Size   Used  Available  Use%
hdfs://ns   118.5 G  104 K     88.1 G    0%
[root@hadoop001 ~]# hdfs dfs -du -h /
12  /hello
[root@hadoop001 ~]# hdfs dfs -mkdir /dir1
[root@hadoop001 ~]# hdfs dfs -mkdir -p /dir2/dir22/dir222
[root@hadoop001 ~]# hdfs dfs -find /
/
/dir1
/dir2
/dir2/dir22
/dir2/dir22/dir222
/hello
[root@hadoop001 ~]# hdfs dfs -ls -R /
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir1
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir2
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir2/dir22
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir2/dir22/dir222
-r--r--rw-   3 root supergroup         12 2022-05-02 14:20 /hello
[root@hadoop001 ~]# hdfs dfs -du /
0   /dir1
0   /dir2
12  /hello
[root@hadoop001 ~]# hdfs dfs -cp /hello /dir2/dir22
[root@hadoop001 ~]# hdfs dfs -ls -R /
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir1
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir2
drwxr-xr-x   - root supergroup          0 2022-05-02 14:27 /dir2/dir22
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir2/dir22/dir222
-rw-r--r--   3 root supergroup         12 2022-05-02 14:27 /dir2/dir22/hello
-r--r--rw-   3 root supergroup         12 2022-05-02 14:20 /hello
[root@hadoop001 ~]# hdfs dfs -mv /hello /dir1
[root@hadoop001 ~]# hdfs dfs -ls -R /
drwxr-xr-x   - root supergroup          0 2022-05-02 14:28 /dir1
-r--r--rw-   3 root supergroup         12 2022-05-02 14:20 /dir1/hello
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir2
drwxr-xr-x   - root supergroup          0 2022-05-02 14:27 /dir2/dir22
drwxr-xr-x   - root supergroup          0 2022-05-02 14:26 /dir2/dir22/dir222
-rw-r--r--   3 root supergroup         12 2022-05-02 14:27 /dir2/dir22/hello
```

## Enhanced commands
### appendToFile
```shell
[root@hadoop001 ~]# echo "hello, guys" > guys
[root@hadoop001 ~]# hdfs dfs -appendToFile guys /dir1/hello
[root@hadoop001 ~]# hdfs dfs -cat /dir1/hello
hello world
hello, guys
```

### createSnapshot
```shell
[root@hadoop001 ~]# hdfs dfs -createSnapshot  /dir1
createSnapshot: Directory is not a snapshottable directory: /dir1
[root@hadoop001 ~]# hdfs dfsadmin -allowSnapshot /dir1
Allowing snaphot on /dir1 succeeded
[root@hadoop001 ~]# hdfs dfs -createSnapshot  /dir1
Created snapshot /dir1/.snapshot/s20220502-144142.019
```
### lsSnapshottableDir
```shell 
[root@hadoop001 ~]# hdfs lsSnapshottableDir
drwxr-xr-x 0 root supergroup 0 2022-05-02 14:41 1 65536 /dir1
```

### deleteSnapshot
```shell
[root@hadoop001 ~]# hdfs dfs -deleteSnapshot  /dir1 s20220502-144142.019
[root@hadoop001 ~]# hdfs dfs -ls -R /dir1/.snapshot/
```

### moveFrom local <localsrc> <dst>
```shell
[root@hadoop001 ~]# ll
total 16
-rw-r--r-- 1 root root   12 May  2 14:32 guys
-rw-r--r-- 1 root root   12 May  2 14:21 h1
-rw-r--r-- 1 root root   12 May  2 14:19 hello
drwxr-xr-x 3 root root 4096 May  2 14:32 tmp
[root@hadoop001 ~]#
[root@hadoop001 ~]# hdfs dfs -moveFromLocal hello /
[root@hadoop001 ~]# ll
total 12
-rw-r--r-- 1 root root   12 May  2 14:32 guys
-rw-r--r-- 1 root root   12 May  2 14:21 h1
drwxr-xr-x 3 root root 4096 May  2 14:32 tmp
```
