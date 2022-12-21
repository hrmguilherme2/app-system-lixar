package com.lixar.events.util

import com.typesafe.config.ConfigFactory

object AppConfig {
  private val config =  ConfigFactory.load()
  private lazy val root = config.getConfig("app")


  object  kafkaConfig {
    private val kafkaconfig = root.getConfig("kafka")

    lazy val metadataBrokerList: String = kafkaconfig.getString("metadataBrokerList")
    lazy val topics: String = kafkaconfig.getString("topics")
  }

  object sparkConfig {
    private val sparkconfig = root.getConfig("spark")

    lazy val reduceWindow: Int = sparkconfig.getInt("reduceWindow")
    lazy val batchWindow: Int = sparkconfig.getInt("batchWindow")
    lazy val master: String = sparkconfig.getString("master")
  }

}
