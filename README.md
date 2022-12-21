# Log Processing API

This API is designed to process logs information and save the results in Redis database. Then, the API will trigger a sensor to send the information for backend that will process the information and expose the result through a REST API.


# Prerequisites

Before you can use this API, you will need to have the following installed on your system:

    Docker Desktop
    Kubernetes
    Helm
    Spark Operator
    Redis

# Clarifications
- **Compute the average time on flyer per user**.

The computeAverageTimeOnFlyerPerUser function takes in a DataFrame of events and returns a DataFrame containing the average time on flyer per user.

The first thing it does is import the necessary implicits and select the relevant columns from the input DataFrame, casting the timestamp column to a timestamp data type.

Next, it filters the data to only include "flyer_open" and "flyer_close" events, as these are the events that will be used to calculate the time spent on flyers.

Then, it creates a window function to group the data by user_id and flyer_id, and order the events by timestamp. This window function is used to add a new column with the row number for each event using the row_number function.

The data is then joined on user_id, flyer_id, and row number, and the relevant columns are selected. The start and end times for each session are then extracted and a new column is added with the duration of each session by subtracting the start time from the end time.

The data is then grouped by user_id and the sum of the duration and the count of sessions is calculated. The average time on flyer per user is then calculated by dividing the total duration by the number of sessions.

Finally, the average time on flyer per user is converted from seconds to hours and the resulting DataFrame is returned, containing the user_id and the average time on flyer per user in hours.
```Scala
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
```

- **Next, generate an output that will back a Business Intelligence (Bl) report that will be shared with our merchant partners.**

The output is generatd by our API. The API is responsible for receiving the logs and saving them in Redis. The API and the Spark job communicate with each other using Redis in real-time. So for every new logs detected by the API, the Spark job will process the logs and save the results in Redis. The API will then retrieve the results from Redis and return them to the user in the form of a JSON object.

Output example:
```JSON
    [
        {
        "user_id": "758d34b523a434594ba2c5a466e46d026e3879fb62cbe6158b79d5d24a98710e",
        "avg_time_on_flyer_per_user_hours": 0.17
        },
        {
        "user_id": "b38f97880ccb69e8f89201eedbccc149a9ea914ca86111340a9b1d9de1788a01",
        "avg_time_on_flyer_per_user_hours": 2.06
        },
        {
        "user_id": "9cedfdc6be9dde4f8f9223a1543a4a340f4f1365f036a4c41748bf6bac6431c0",
        "avg_time_on_flyer_per_user_hours": 1.62
        },
        {
        "user_id": "1285ebbfb7e7105ca2b10afdc0c2912fddeb4111b9f1563fc81fb22168530720",
        "avg_time_on_flyer_per_user_hours": 2.97
        }
        ...
    ]
```


- **Explain how your algorithm scales for:**

a) **1 Million Events (~10 MB of data):**
- For 1 million events (~10 MB of data), the algorithm should be able to scale well, as it is relatively simple and does not require a lot of computational resources. It should be able to handle this amount of data efficiently on a single machine, or even on a small cluster if desired.

b) **1 Trillion Events (~10 TB of data):**
- For 1 trillion events (~10 TB of data), the algorithm may start to become computationally expensive, as it requires iterating through the entire dataset and performing aggregations and calculations on each record. In this case, it may be more efficient to use a distributed processing framework such as Apache Spark to parallelize the computations and handle the large volume of data.
- Using Apache Spark, the algorithm could be implemented as a distributed job that is broken down into smaller tasks and distributed across a cluster of machines. This would allow it to scale to larger datasets and handle the increased computational demands more efficiently.
- It is also worth noting that the algorithm could be optimized further to improve its scalability. For example, the data could be pre-processed or filtered to remove any irrelevant events before performing the calculations(Our API is ready to do this), or the calculations could be optimized to be more efficient. Additionally, the use of distributed caching or in-memory storage techniques (Which we are using REDIS) could be considered to improve the performance of the algorithm.

# Architecture
![img.png](img.png)
- **Design a workflow to move the user behaviour event data from the application to a backend and provide insights into the data pipelines that you foresee.**
  - We have two backend services. The first backend service is the API. The API is responsible for receiving the logs and saving them in Redis. The second backend service is the Spark job. The Spark job is responsible for processing the logs and saving the results in Redis. The API and the Spark job communicate with each other using Redis in real-time. So for every new logs detected by the API, the Spark job will process the logs and save the results in Redis. The API will then retrieve the results from Redis and return them to the user.

- **Explain how the workflow would provide the data to the batch process in Part 1 Algorithm.**
  - The workflow would provide the data to the batch process in Part 1 Algorithm by saving the logs in Redis. The Spark job would then retrieve the logs from Redis and process them to generate the results.

- **Explain any adaptations that your work from Part 1 - Algorithm would need to work as a streaming process.**
  - The application Backend was built using Apache Spark Streaming, which is already a streaming process. So no adaptations are needed.

- **Highlight any important design decisions you make, and describe briefly why you made those decisions. Important parts to address include:**
  - Some additional design decisions that were made in this workflow, given the choice of technology stack (Redis + Spark + Spring Boot), include:

  - Using Spring Boot for the API: Spring Boot is a popular Java-based framework for building web applications and microservices. It provides a wide range of features and capabilities, including support for distributed systems, easy integration with other technologies such as Redis, and a robust architecture for building scalable and reliable applications.

  - Using Redis for temporary storage: Redis is an in-memory data store that is well-suited for use as a cache or for storing temporary data. It is fast and scalable, and can be easily integrated with other technologies such as Spring Boot and Spark.

  - Using Spark for data processing: Spark is a powerful distributed processing framework that is well-suited for handling large volumes of data in real-time. It provides a wide range of functionality and APIs, and is highly scalable and robust.
- **Some of the reasons why these technology choices were made include:**

  - **Latency:** The use of Redis as a cache helps to reduce latency, as the data is stored in-memory and is easily accessible. The use of Spark streaming allows for real-time processing of the data, further reducing latency.

  - **Scalability & Data Volume:** The use of a distributed pub-sub technology cluster allows for high scalability, as the data can be distributed across multiple machines and the pub-sub cluster can handle failures and retries

  - **Robustness:** Redis is a highly reliable and robust data store, with features such as in-memory storage, support for data persistence, and automatic failover. Apache Spark is a distributed processing framework that is designed for robustness and fault tolerance, with features such as automatic recovery from failures and the ability to handle late or out-of-order data. Spring Boot is a Java-based framework that provides a robust architecture for building scalable and reliable applications, with features such as support for distributed systems, easy integration with other technologies, and a modular design.

  - **Failure Modes:** Redis has the ability to automatically failover to a slave instance in the event of a failure, and supports data persistence to prevent data loss. Apache Spark can recover from failures and continue processing the data, and can handle late or out-of-order data. Spring Boot has a modular design that allows for easy replacement of faulty components, and can be configured to handle failures and retries automatically.

  - **Delivery Guarantees:** Redis is a highly reliable data store that is well-suited for use in distributed systems, and can ensure the delivery of data with low latency. Apache Spark is a distributed processing framework that is designed to handle large volumes of data in real-time, and can ensure the delivery of processed data with low latency
# How to run the API

To run the API, follow these steps:

1. Clone the repository to your local machine.
2. Open a terminal window and navigate to the root directory of the repository.
3. Run the following command:


First we need to install Apache Spark Operator. This will allow us to run Spark jobs on Kubernetes. To install Apache Spark Operator, run the following command:

    $ helm dependency build charts/spark-cluster
    $ helm install spark-cluster charts/spark-cluster 

Next, we need to install Redis. To install Redis, run the following command:

    $ helm dependency build charts/redis
    $ helm install redis charts/redis --set redis.password=redispw

Make sure to execute the following command to Redis gets deployed for outside cluster access:

    $ kubectl port-forward --namespace default svc/redis-master 55000:55000 &
    REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli -h 127.0.0.1 -p 55000


Finally, we can run the API. To run the API, run the following command:

    $ helm install api-processor charts/log-processing-api

To test the API, we need to access throught the API endpoint. To access the API endpoint, we have to port forward the service. To port forward the service, run the following command:

    $ kubectl port-forward --namespace default svc/api-processor 8080:8080

Now we can access the API endpoint at http://localhost:8080/api/health. To test the API, we can use the following command:

    $ curl http://localhost:8080/api/health

this should return the following response:

    {"status":"OK"}

For now, we need to install the second backend service. To install the second backend service, run the following command:

    $ helm install api-processor charts/log-processing-api

We can make some test and see the results in Redis. To make some test, we can use the following command:

    $ curl -X POST -H "Content-Type: application/json" -d '[  {
    "timestamp": "2018-10-01T13:54:59-04:00",
    "user_id": "9ea672779feb1e088caa593b61ec51440c1d8694e1bf3dfe95554f7c9698c47e",
    "event": "shopping_list_open",
    "flyer_id": "",
    "merchant_id": ""}]' http://localhost:8080/api/process

The output should be the result on which we can see the users with the average time on flyer:

```JSON
    [
        {
        "user_id": "758d34b523a434594ba2c5a466e46d026e3879fb62cbe6158b79d5d24a98710e",
        "avg_time_on_flyer_per_user_hours": 0.17
        },
        {
        "user_id": "b38f97880ccb69e8f89201eedbccc149a9ea914ca86111340a9b1d9de1788a01",
        "avg_time_on_flyer_per_user_hours": 2.06
        },
        {
        "user_id": "9cedfdc6be9dde4f8f9223a1543a4a340f4f1365f036a4c41748bf6bac6431c0",
        "avg_time_on_flyer_per_user_hours": 1.62
        },
        {
        "user_id": "1285ebbfb7e7105ca2b10afdc0c2912fddeb4111b9f1563fc81fb22168530720",
        "avg_time_on_flyer_per_user_hours": 2.97
        }
        ...
    ]
```




