package com.lixar.events

// Java libs
import com.lixar.events.util.AppConfig.kafkaConfig
import com.redislabs.provider.redis.streaming.toRedisStreamingContext
import org.apache.spark.sql.Row.empty.schema
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.storage.StorageLevel

import java.time.LocalDateTime
import org.apache.spark.streaming.Minutes
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, LocationStrategies}

import scala.collection.JavaConverters._

// Logs
import org.apache.log4j.LogManager
import org.apache.log4j.{Level, Logger}

// Kafka
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.format.DateTimeFormatter

// Avro


import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.spark.sql.avro.SchemaConverters

// Dependencias Spark Streaming e kakfa
import org.apache.spark.sql.functions._
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types._
import org.apache.spark.streaming.{Seconds, StreamingContext}

// Dependencias Spark customizadas

// Dependencias scala
import scala.util.Try

object StreamingSensor extends App{
  RunnerStreamingSensor.run()
}

object RunnerStreamingSensor {

  def run(): Unit = {
    print(">>> streaming sensor")
    val sparkConf = SparkSession.builder()
      .config("spark.redis.port", "55000")
      .config("spark.redis.auth", "redispw")
      .config("spark.redis.db", 1)
      .config("spark.redis.timeout", 0)
      .config("spark.debug.maxToStringFields","200").getOrCreate()

    //Criando o context spark
    val ssc = new StreamingContext(sparkConf.sparkContext, Seconds(10))
    import sparkConf.implicits._

    val transaction_stream = ssc.createRedisStream(Array("sensor_events"))
    transaction_stream.foreachRDD(rdd => {
      println(">>> rdd " + rdd)
      if (!rdd.isEmpty()) {
        val events = sparkConf.read.format("org.apache.spark.sql.redis").option("table", "events").option("key.column", "key").load.withColumn("timestamp", to_timestamp($"timestamp", "yyyy-MM-dd'T'HH:mm:ssz").cast(TimestampType))
        computeAverageTimeOnFlyerPerUser(events).write.format("org.apache.spark.sql.redis").option("table", "usersaverage").mode(SaveMode.Overwrite).save();
      }
    })

    // set checkpoint
    ssc.start()
    ssc.awaitTermination()
    }

  def computeAverageTimeOnFlyerPerUser(events: DataFrame): DataFrame = {
    import events.sparkSession.implicits._

    // Select the relevant columns and convert the timestamp to a timestamp data type
    val data = events.select($"timestamp".cast("timestamp"), $"user_id", $"event", $"flyer_id", $"merchant_id")

    // Filter the data to only include "flyer_open" and "flyer_close" events
    val flyerEvents = data.filter($"event" === "flyer_open" || $"event" === "flyer_close")

    // Create a window function to group the data by user_id and flyer_id, and order the events by timestamp
    val window = Window.partitionBy($"user_id", $"flyer_id").orderBy($"timestamp")

    // Use the window function to add a new column with the row number for each event
    val ranked = flyerEvents.withColumn("row_number", row_number().over(window))

    // Join the data on user_id, flyer_id, and row number, and select only the relevant columns
    val joined = ranked.as("a").join(ranked.as("b"),
      $"a.user_id" === $"b.user_id" &&
        $"a.flyer_id" === $"b.flyer_id" &&
        $"a.row_number" + 1 === $"b.row_number", "inner")
      .select($"a.timestamp".alias("start_time"), $"b.timestamp".alias("end_time"), $"a.user_id".alias("user_id"))

    // Calculate the duration of each session by subtracting the start time from the end time
    val sessions = joined.withColumn("duration", unix_timestamp($"end_time") - unix_timestamp($"start_time"))

    // Group the data by user_id and calculate the sum of the duration and the count of sessions
    val grouped = sessions.groupBy($"user_id").agg(sum($"duration").alias("total_duration"), count($"duration").alias("num_sessions"))

    // Calculate the average time on flyer per user by dividing the total duration by the number of sessions
    val avgTimeOnFlyerPerUser = grouped.withColumn("avg_time_on_flyer_per_user", $"total_duration" / $"num_sessions")

    // Convert the average time on flyer per user from seconds to hours
    val avgTimeOnFlyerPerUserHours = avgTimeOnFlyerPerUser.withColumn("avg_time_on_flyer_per_user_hours", round($"avg_time_on_flyer_per_user" / 3600,2) )
    avgTimeOnFlyerPerUserHours.select($"user_id", $"avg_time_on_flyer_per_user_hours")
  }


}
