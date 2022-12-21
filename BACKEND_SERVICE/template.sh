
SPARK_APPLICATION_JAR_LOCATION=`find /app/target/scala-2.12/ -iname 'atz*.jar' | head -n1`
export SPARK_APPLICATION_JAR_LOCATION
echo $SPARK_APPLICATION_JAR_LOCATION
if [ -z "$SPARK_APPLICATION_JAR_LOCATION" ]; then
	echo "Can't find a file -*.jar in /app/target/scala-2.12/"
	exit 1
fi

/submit.sh