
FROM gcr.io/spark-operator/spark:v3.1.1 as builder

# Switch to user root so we can add additional jars and configuration files.
USER root

# Setup dependencies for Google Cloud Storage access.
RUN rm $SPARK_HOME/jars/guava-14.0.1.jar
ADD https://repo1.maven.org/maven2/com/google/guava/guava/23.0/guava-23.0.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/guava-23.0.jar
# Add the connector jar needed to access Google Cloud Storage using the Hadoop FileSystem API.
ADD https://storage.googleapis.com/hadoop-lib/gcs/gcs-connector-latest-hadoop3.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/gcs-connector-latest-hadoop3.jar
ADD https://storage.googleapis.com/spark-lib/bigquery/spark-bigquery-latest_2.12.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/spark-bigquery-latest_2.12.jar
ADD https://storage.googleapis.com/spark-lib/bigquery/spark-bigquery-with-dependencies_2.12-0.25.2.jar $SPARK_HOME/jars
RUN chmod 644 $SPARK_HOME/jars/spark-bigquery-with-dependencies_2.12-0.25.2.jar
COPY postgresql-42.2.13.jar $SPARK_HOME/jars
USER ${spark_uid}
# using root user
USER root:root
# create directory for apps
# RUN mkdir -p /app

# copy spark program
COPY target/spark-redis-demo-0.0.1-SNAPSHOT.jar /opt/spark/work-dir/
COPY gson-2.2.4.jar /opt/spark/jars/

# set work directory
WORKDIR /opt/spark/work-dir
#ENV HADOOP_CLASSPATH=/opt/spark/jars/gcs-connector-hadoop3-latest.jar:$HADOOP_CLASSPATH
# user
USER 1001
ENTRYPOINT ["/opt/entrypoint.sh"]

