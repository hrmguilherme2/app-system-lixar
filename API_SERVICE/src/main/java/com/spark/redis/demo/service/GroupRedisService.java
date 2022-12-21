package com.spark.redis.demo.service;

import com.redislabs.provider.redis.ReadWriteConfig;
import com.redislabs.provider.redis.RedisConfig;
import com.redislabs.provider.redis.RedisContext;
import com.spark.redis.demo.model.*;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GroupRedisService {
    @Autowired
    private SparkSession sparkSession;
    @Async()
    public CompletableFuture processTransctions(List<Transaction> groupList)  throws InterruptedException {
        System.out.println("Initiating spark job");

        //For each group, create thread and process and return completable future
        List<Boolean> results = Collections.singletonList(processGroup(groupList, sparkSession));

        System.out.println("Spark job completed");
        Progress.getInstance().setProgress(false);
        return CompletableFuture.completedFuture(results);
    }

    private Boolean processGroup(List<Transaction> collection, SparkSession sparkSession) {
        //collection to list

        RedisConfig redisConfig = RedisConfig.fromSparkConf(sparkSession.sparkContext().getConf());
        ReadWriteConfig readWriteConfig = ReadWriteConfig.fromSparkConf(sparkSession.sparkContext().getConf());
        RedisContext redisContext = new RedisContext(sparkSession.sparkContext());

        //Create a dataframe from the list
        Dataset<Row> groupDF = sparkSession.createDataFrame(collection, Transaction.class).na().fill("null").withColumn("key", org.apache.spark.sql.functions.concat(org.apache.spark.sql.functions.col("user_id"), org.apache.spark.sql.functions.lit("_"), org.apache.spark.sql.functions.col("timestamp")));
        groupDF.show();

        groupDF.write().format("org.apache.spark.sql.redis").option("table", "events").option("key.column", "key").mode(SaveMode.Append).save();
        List<String> data = Arrays.asList("1");

        // convert data to RDd
        JavaSparkContext jsc = new JavaSparkContext(sparkSession.sparkContext());
        RDD<String> rdd = jsc.parallelize(data).rdd();

        redisContext.toRedisLIST(rdd, "sensor_events", 1, redisConfig, readWriteConfig);
        System.out.println("Saved to redis");
        return true;
    }

    public List<Object> readGroup(List<Object> groupList) {
        System.out.println("Reading group");
        for(Object group: groupList) {
            switch (group.toString()) {
                case "all":
                    Dataset<Row> df = sparkSession.read().format("org.apache.spark.sql.redis").option("table", "usersaverage").load();
                    List<Row> groupRows = df.collectAsList();
                    return groupRows.parallelStream().map(new Function<Row, userInformation>() {
                        @Override
                        public userInformation apply(Row row) {
                            return new userInformation(row.getString(0), row.getDouble(1));
                        }
                    }).collect(Collectors.toList());
                default:
                    System.out.println("Event not found");
                    return null;
            }
        }
        return null;
    }

}