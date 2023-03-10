spark2-submit --class $MAIN_CLASS \
--verbose \
--name $APP_NAME \
--master yarn \
--deploy-mode cluster \
--driver-memory 10g \
--driver-cores 14 \
--num-executors 60 \
--executor-cores 15 \
--executor-memory 14g \
--queue root.default \
--conf spark.dynamicAllocation.executorIdleTimeout=600s \
--jars $JDBC_LIBS \
--files=$CONFIG,$JAAS,$LOG4J \
--conf spark.dynamicAllocation.minExecutors=2 \
--conf spark.dynamicAllocation.maxExecutors=65 \
--conf spark.dynamicAllocation.initialExecutors=60 \
--conf spark.dynamicAllocation.enabled=true \
--conf "spark.executor.extraClassPath=hbase-default.xml,hbase-site.xml" \
--conf "spark.driver.extraClassPath=hbase-default.xml,hbase-site.xml" \
--conf spark.hadoop.fs.hdfs.impl.disable.cache=true \
--conf spark.yarn.maxAppAttempts=200 \
--conf spark.rpc.message.maxSize=1024 \
--driver-java-options -Dconfig.file=$CONFIG \
--conf "spark.driver.extraJavaOptions=-Dconfig.file=$CONFIG -Dlog4j.configuration=file:log4j.properties \
-Djava.security.auth.login.config=$JAAS -Djavax.security.auth.useSubjectCredsOnly=false " \
--conf spark.executor.extraJavaOptions=-Djava.security.auth.login.config=$JAAS \
--principal hdfs/cld-wn04.infra.elo@INFRA.ELO \
--keytab ${KEYTAB} \
$JARFILE