
import sbt.Keys.{dependencyOverrides, libraryDependencies}
import sbt.Resolver
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy

lazy val commonSettings = Seq(
  organization := "com.lixar.events",
  version := "0.0.1",
  scalaVersion := "2.12.8"
)

val sparkVersion = "3.1.1"
resolvers ++= Seq(
  Resolver.sonatypeRepo("public"),
  "Confluent Maven Repo" at "https://packages.confluent.io/maven/",
  "Hortonworks repo" at "https://repo.hortonworks.com/content/repositories/releases/",
  "MVN" at "https://mvnrepository.com/artifact/"
)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.lixar.events",
      scalaVersion := "2.12.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    assemblyJarName in assembly := "lixar-events-processor.jar",
    name := "backend-filter-group",
    libraryDependencies += "org.apache.kafka" %% "kafka" % "2.0.0",
    libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion,
    libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion,
    libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion,// % "provided",
    libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
    libraryDependencies += "org.apache.avro"  %  "avro"  %  "1.7.7",
    libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "3.1.1",
    libraryDependencies += "com.typesafe" % "config" % "1.3.1",
    libraryDependencies += "com.twitter" %% "bijection-avro" % "0.9.3",
    libraryDependencies += "org.apache.spark" %% "spark-avro" % "3.1.1",
    dependencyOverrides += "com.google.cloud.bigdataoss" % "gcs-connector" % "hadoop3-2.2.6",
    libraryDependencies += "org.apache.spark" %% "spark-kubernetes" % sparkVersion,
    dependencyOverrides += "com.google.cloud.spark" %% "spark-bigquery" % "0.25.2",
    dependencyOverrides += "com.google.cloud" % "google-cloud-bigquery" % "1.123.2",
    dependencyOverrides += "com.google.cloud" % "google-cloud-bigquerystorage" % "1.6.0",
    libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.1.1",
    libraryDependencies += "com.redislabs" %% "spark-redis" % "3.0.0",
    libraryDependencies += "redis.clients" % "jedis" % "3.9.0",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.13",

    dependencyOverrides += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.1",
    dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.1",
    dependencyOverrides += "org.apache.commons" % "commons-lang3" % "3.9",


    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    // append several options to the list of options passed to the Java compiler
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    unmanagedResourceDirectories in Compile += {baseDirectory.value / "lib"},
    // uses compile classpath for the run task, including "provided" jar (cf http://stackoverflow.com/a/21803413/3827)
    run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated
  )
