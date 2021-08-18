Software support: MySQL,Hbase,Redis,Kafka

lanuch steps:
1: config is ready. To test locally, comment @ActiveProfiles("local").
3: compile and make jars, and enter work folder with prompt. kafka/hbase may die due to unusual exit and memory limit, just reboot.
4: execute ProductServiceTest.testInitiator() to create tables in hbase, create database: user/product/order seperately or allinone with tables as the file DDL.
5: execute start-1servers.bat->start-2ecommerce-center.bat->start-3ecommerce-services.bat
6: if does work, clear db, hbase, zookeeper, kafka and redis data. good luck.