---
layout: post
title:  "Hadoop Tutorial"
date:   2022-04-11 10:05:22 +0800
categories: bigdata
tags:
  - Bigdata
  - Hadoop
---

## 1. Configure access each host with ssh keys
Let say if we want to rename the hosts to: "hadoop001/hadoop002/hadoop003",
login to each of the host as root, and run the commands to set host name:
```shell 
hostname hadoop001  # temporarily
hostnamectl set-hostname hadoop001 # persistently 
ssh-keygen -t rsa # just press [enter]s to create keys 
```
Change to 002/003 according to the host you login.

Add following lines to your /etc/hosts on each hosts to bind hostname with IPs:
```shell
192.168.1.101 hadoop001
192.168.1.102 hadoop002
192.168.1.103 hadoop003
```

Next, run the ssh-copy-id command from each of hadoop001/002/003 to hadoop001(input the password),
```shell
ssh-copy-id hadoop001
```
After that, we can check the .ssh/authorized_keys file, it should looks like:
```shell
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC/....../EVLSyg5VOcFCgehqX9nhqMZvAGvo40HMxZslWWT root@hadoop001
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC/....../y3CrBijJw+9pR4uTZ/2BHdanhdlajt+//2C+RE root@hadoop002
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQD/....../BG+5TSio3+rGCHgn/XTMXDUDN6mLVHkHxWDXxr root@hadoop003
```

Now, copy this authorized_keys file to the other hosts(input the password):
```shell
scp .ssh/authorized_keys hadoop002:~/.ssh/
scp .ssh/authorized_keys hadoop003:~/.ssh/
```

Finally, you can login to every host from any host without input password:
```shell
ssh hadoop001 
ssh hadoop002 
ssh hadoop003 
```

## 2. Install Hadoop
1. uncompress jdk and hadoop tarball;
2. add ENV variables to /etc/profile;
3. start services, including zookeeper, name/journal/data node;
I put all of these steps in scripts, 
download here [install hadoop](https://github.com/bournewang/install_hadoop){:target="_blank"}.

## 3. Check installing
run _jps_ to see processes on each hosts:
```shell
[root@hadoop001 ~]# jps
30788 Jps
23045 ResourceManager
22630 DataNode
22774 DFSZKFailoverController
22313 JournalNode
22090 QuorumPeerMain
22892 NodeManager
22494 NameNode
```

## 4. Check hdfs
let check hdfs, seems everything goes right:
```shell
[root@hadoop001 ~]# hdfs dfs -ls /
[root@hadoop001 ~]# hdfs dfs -mkdir /dir1
[root@hadoop001 ~]# hdfs dfs -ls /
Found 1 items
drwxr-xr-x   - root supergroup          0 2022-04-11 13:01 /dir1
[root@hadoop001 ~]# hdfs dfs -touchz /emptyfile 
[root@hadoop001 ~]# hdfs dfs -ls /
Found 2 items
drwxr-xr-x   - root supergroup          0 2022-04-11 13:01 /dir1
-rw-r--r--   3 root supergroup          0 2022-04-11 13:01 /emptyfile

[root@hadoop001 ~]# ll / > list
[root@hadoop001 ~]# hdfs dfs -put list /
[root@hadoop001 ~]# hdfs dfs -ls /
Found 3 items
drwxr-xr-x   - root supergroup          0 2022-04-11 13:01 /dir1
-rw-r--r--   3 root supergroup          0 2022-04-11 13:01 /emptyfile
-rw-r--r--   3 root supergroup       1052 2022-04-11 13:03 /list
```

## 5. Run a hadoop task
let run a words count task build-in hadoop example, first create a file with multiple words,
format of running a hadoop task is: <span style="color:darkblue">_hadoop jar /path/to/xxx.jar class_name input output_dir_</span>

```shell
[root@hadoop001 ~]# echo "hello world,
> hello python
> hello java,
> hello hadoop" > words
[root@hadoop001 ~]# cat words
hello world,
hello python
hello java,
hello hadoop
[root@hadoop001 ~]# hdfs dfs -put words /
[root@hadoop001 ~]# hdfs dfs -ls /
Found 4 items
drwxr-xr-x   - root supergroup          0 2022-04-11 13:01 /dir1
-rw-r--r--   3 root supergroup          0 2022-04-11 13:01 /emptyfile
-rw-r--r--   3 root supergroup       1052 2022-04-11 13:03 /list
-rw-r--r--   3 root supergroup         51 2022-04-11 13:16 /words
[root@hadoop001 ~]# hadoop jar /opt/module/hadoop-2.10.1/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.10.1.jar wordcount /words /output
.
.
.
22/04/11 13:17:32 INFO mapred.Task: Final Counters for attempt_local646404325_0001_m_000000_0: Counters: 23
22/04/11 13:17:32 INFO mapred.LocalJobRunner: Finishing task: attempt_local646404325_0001_r_000000_0
22/04/11 13:17:32 INFO mapred.LocalJobRunner: reduce task executor complete.
22/04/11 13:17:33 INFO mapreduce.Job: Job job_local646404325_0001 running in uber mode : false
22/04/11 13:17:33 INFO mapreduce.Job:  map 100% reduce 100%
22/04/11 13:17:33 INFO mapreduce.Job: Job job_local646404325_0001 completed successfully

[root@hadoop001 ~]# hdfs dfs -ls /
Found 5 items
drwxr-xr-x   - root supergroup          0 2022-04-11 13:01 /dir1
-rw-r--r--   3 root supergroup          0 2022-04-11 13:01 /emptyfile
-rw-r--r--   3 root supergroup       1052 2022-04-11 13:03 /list
drwxr-xr-x   - root supergroup          0 2022-04-11 13:17 /output
-rw-r--r--   3 root supergroup         51 2022-04-11 13:16 /words
[root@hadoop001 ~]# hdfs dfs -ls /output
Found 2 items
-rw-r--r--   3 root supergroup          0 2022-04-11 13:17 /output/_SUCCESS
-rw-r--r--   3 root supergroup         43 2022-04-11 13:17 /output/part-r-00000
[root@hadoop001 ~]# hdfs dfs -cat /output/part-r-00000
hadoop	1
hello	4
java,	1
python	1
world,	1
```

Good.
