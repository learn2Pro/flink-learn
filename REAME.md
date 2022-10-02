## RUN WITH CLASS
1. fraud detection
   1. ./flink-1.15.2-bin/bin/flink run -c org.flink.learn.FraudDetectionJob target/flink-learn-1.0.0_20220905.jar
2. socket word count
   1. ./flink-1.15.2-bin/bin/flink run -c org.flink.learn.SocketWordCountJob target/flink-learn-1.0.0_20220905.jar
3. socket word count
   1. ./flink-1.15.2-bin/bin/flink run -c org.flink.learn.NewSourceWordCountJob target/flink-learn-1.0.0_20220905.jar
   
4. mysql sink example
   1. pre init schema
   ```
      DROP TABLE IF EXISTS `student`;
      CREATE TABLE `student` (
      `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
      `name` varchar(25) COLLATE utf8_bin DEFAULT NULL,
      `age` int(10) DEFAULT NULL,
      PRIMARY KEY (`id`)
      ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
   
   ```
   2. ./flink-1.15.2-bin/bin/flink run -c org.flink.learn.MySqlSinkJob target/flink-learn-1.0.0_20220905.jar
