---
layout: post
title:  "Flume Practice"
date:   2022-06-13 15:19:00 +0800
categories: bigdata
tags:
    - Bigdata
    - Flume
---


## A simple example
create a configuration file in conf, named to 'example.conf':
```shell
# example.conf: A single-node Flume configuration

# Name the components on this agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# Describe/configure the source
a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444

# Describe the sink
a1.sinks.k1.type = logger

# Use a channel which buffers events in memory
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100

# Bind the source and sink to the channel
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```
this configuration defines a process work flow, listening data received from localhost 44444 port, buffers the data in memory, and finally sink/output the data to logger.  

start the flume as follows: 
```shell
[root@hadoop001 apache-flume-1.9.0-bin]# bin/flume-ng agent --conf conf --conf-file example.conf --name a1 -Dflume.root.logger=INFO,console
```

From another terminal, launch telnet at port 44444 and send sample data:

```shell
[root@hadoop001 apache-flume-1.9.0-bin]# telnet localhost 44444
Trying ::1...
telnet: connect to address ::1: Connection refused
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
hello world
OK
how are you
OK
today is a good day
OK
```

the original flume terminal will output the event in a log message:
```shell
2022-06-13 09:45:39,563 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:95)] Event: { headers:{} body: 68 65 6C 6C 6F 20 77 6F 72 6C 64 0D             hello world. }
2022-06-13 09:45:41,780 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:95)] Event: { headers:{} body: 68 6F 77 20 61 72 65 20 79 6F 75 0D             how are you. }
2022-06-13 09:45:48,407 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:95)] Event: { headers:{} body: 74 6F 64 61 79 20 69 73 20 61 20 67 6F 6F 64 20 today is a good  }
```

## Receive nginx logs and transcribe to Hdfs
```shell
[root@hadoop001 apache-flume-1.9.0-bin]# cat conf/nginx-to-hdfs.conf
# Name the components on this agent
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# Describe/configure the source
a1.sources.r1.type = exec
a1.sources.r1.command = tail -f /var/log/nginx/access.log

# Use a channel which buffers events in memory
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100

# Describe the interceptors to add timestamp
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = org.apache.flume.interceptor.TimestampInterceptor$Builder

# Describe the sink
a1.sinks.k1.type = hdfs
a1.sinks.k1.hdfs.path = hdfs://hadoop001:9000/flume/nginx/%Y%m%d
a1.sinks.k1.hdfs.filePrefix = events-
a1.sinks.k1.hdfs.fileType = DataStream

# Bind the source and sink to the channel
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```
caught exception after start the flume, **_Operation category READ is not supported in state standby_** , which means the Name Node may turn to standby mode from active. 
```shell
[root@hadoop001 apache-flume-1.9.0-bin]# ./bin/flume-ng agent -n a1 -c conf -f conf/nginx-to-hdfs.conf -Dflume.root.logger=INFO,console
...
2022-06-13 10:51:58,651 (agent-shutdown-hook) [INFO - org.apache.flume.sink.hdfs.HDFSEventSink.stop(HDFSEventSink.java:494)] Closing hdfs://hadoop001:9000/flume/nginx/20220613/events-
2022-06-13 10:51:58,651 (agent-shutdown-hook) [INFO - org.apache.flume.sink.hdfs.BucketWriter.doClose(BucketWriter.java:438)] Closing hdfs://hadoop001:9000/flume/nginx/20220613/events-.1655088664633.tmp
2022-06-13 10:51:58,651 (agent-shutdown-hook) [INFO - org.apache.flume.sink.hdfs.BucketWriter.doClose(BucketWriter.java:443)] HDFSWriter is already closed: hdfs://hadoop001:9000/flume/nginx/20220613/events-.1655088664633.tmp
2022-06-13 10:51:58,661 (agent-shutdown-hook) [WARN - org.apache.flume.sink.hdfs.BucketWriter.doClose(BucketWriter.java:462)] failed to rename() file (hdfs://hadoop001:9000/flume/nginx/20220613/events-.1655088664633.tmp). Exception follows.
org.apache.hadoop.ipc.RemoteException(org.apache.hadoop.ipc.StandbyException): Operation category READ is not supported in state standby
	at org.apache.hadoop.hdfs.server.namenode.ha.StandbyState.checkOperation(StandbyState.java:87)
	at org.apache.hadoop.hdfs.server.namenode.NameNode$NameNodeHAContext.checkOperation(NameNode.java:1779)
	at org.apache.hadoop.hdfs.server.namenode.FSNamesystem.checkOperation(FSNamesystem.java:1313)
	at org.apache.hadoop.hdfs.server.namenode.FSNamesystem.getFileInfo(FSNamesystem.java:3852)
	at org.apache.hadoop.hdfs.server.namenode.NameNodeRpcServer.getFileInfo(NameNodeRpcServer.java:1012)
	at org.apache.hadoop.hdfs.protocolPB.ClientNamenodeProtocolServerSideTranslatorPB.getFileInfo(ClientNamenodeProtocolServerSideTranslatorPB.java:843)
	at org.apache.hadoop.hdfs.protocol.proto.ClientNamenodeProtocolProtos$ClientNamenodeProtocol$2.callBlockingMethod(ClientNamenodeProtocolProtos.java)```
```

Stop the dfs and yarn in hadoop001/003, and restart them:
```shell
[root@hadoop001 apache-flume-1.9.0-bin]# stop-dfs.sh
[root@hadoop003 apache-flume-1.9.0-bin]# stop-yarn.sh
[root@hadoop001 apache-flume-1.9.0-bin]# start-dfs.sh
[root@hadoop003 apache-flume-1.9.0-bin]# start-yarn.sh
```

Relaunch flume, this time it works fine!
```shell
[root@hadoop001 apache-flume-1.9.0-bin]# ./bin/flume-ng agent -n a1 -c conf -f conf/nginx-to-hdfs.conf  -Dflume.root.logger=DEBUG,console
```

Check the hdfs output folder after a while, we got nginx logs:
```shell
[root@hadoop001 apache-flume-1.9.0-bin]# hdfs dfs -ls -R /flume/
22/06/13 10:58:57 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
drwxr-xr-x   - root supergroup          0 2022-06-13 10:56 /flume/nginx
drwxr-xr-x   - root supergroup          0 2022-06-13 10:58 /flume/nginx/20220613
-rw-r--r--   3 root supergroup       2025 2022-06-13 10:56 /flume/nginx/20220613/events-.1655088988819
-rw-r--r--   3 root supergroup        214 2022-06-13 10:57 /flume/nginx/20220613/events-.1655088988820
-rw-r--r--   3 root supergroup        214 2022-06-13 10:57 /flume/nginx/20220613/events-.1655088988821
-rw-r--r--   3 root supergroup        198 2022-06-13 10:57 /flume/nginx/20220613/events-.1655088988822
-rw-r--r--   3 root supergroup       1386 2022-06-13 10:58 /flume/nginx/20220613/events-.1655088988823
[root@hadoop001 apache-flume-1.9.0-bin]# hdfs dfs -cat /flume/nginx/20220613/events-.1655088988819
22/06/13 10:59:11 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
101.87.93.129 - - [13/Jun/2022:10:44:00 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:01 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:01 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:02 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:02 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:02 +0800] "GET / HTTP/1.1" 304 0 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:07 +0800] "GET /aaa HTTP/1.1" 404 3650 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:11 +0800] "GET /bbbb HTTP/1.1" 404 3650 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:16 +0800] "GET /shoule-be-404 HTTP/1.1" 404 3650 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
101.87.93.129 - - [13/Jun/2022:10:44:24 +0800] "GET /shoule-be-404 HTTP/1.1" 404 3650 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36" "-"
[root@hadoop001 apache-flume-1.9.0-bin]#
```