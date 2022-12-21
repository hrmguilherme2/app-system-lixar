package com.spark.redis.demo.service;

import com.redislabs.provider.redis.ReadWriteConfig;
import com.redislabs.provider.redis.RedisConfig;
import com.redislabs.provider.redis.RedisContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;


@Configuration
@PropertySource("classpath:application.properties")
public class SparkConfigRedis {

    @Autowired
    private Environment env;

    @Value("${app.name:spark-redis-test}")
    private String appName;

    @Value("${spark.home}")
    private String sparkHome;

    @Value("${master.uri:local}")
    private String masterUri;

    @Value("${spark.redis.host:localhost}")
    private String redisHost;

    @Value("${spark.redis.port:6379}")
    private String redisPort;

    @Value("${spark.redis.auth:}")
    private String redisAuth;

    @Bean
    public SparkConf sparkConf() {

        SparkConf sparkConf = new SparkConf()
                .setAppName(appName)
                .setSparkHome(sparkHome)
                //.setMaster("local[*]")
                .set("spark.redis.db", "1")
                //.set("spark.redis.host", "10.99.153.57")
                .set("spark.redis.port", "55000")
                .set("spark.redis.timeout", "0")
                //avoid GC overhead limit exceeded
                .set("spark.redis.auth", "redispw");


        return sparkConf;
    }

    @Bean
    public RedisConfig redisConfig()
    {
        return RedisConfig.fromSparkConf(sparkConf());
    }

    @Bean
    public JavaSparkContext javaSparkContext() {
        return new JavaSparkContext(sparkConf());
    }

    @Bean
    public ReadWriteConfig readWriteConfig() {
        return  ReadWriteConfig.fromSparkConf(sparkConf());
    }

    @Bean
    public RedisContext redisContext(){
        return new RedisContext(javaSparkContext().sc());
    }

    @Bean
    public SparkSession sparkSession() {

        return SparkSession
                .builder()
                .sparkContext(javaSparkContext().sc())
                .appName("Java Spark Redis basic example")
                .config("spark.sql.sources.partitionOverwriteMode", "dynamic")
                .config("spark.shuffle.memoryFraction", "0.0")
                .config("spark.debug.maxToStringFields", "200")
                .getOrCreate();

    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


}