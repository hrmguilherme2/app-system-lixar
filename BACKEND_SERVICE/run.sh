#Create variable for version value
version=5.0.0
helm uninstall lixar-events-sensor
# copy file to /app/target/scala-2.12/
cp /Users/guilherme/Downloads/Sr_DE_Takehome/Backend/target/scala-2.12/lixar-events-processor.jar /Users/guilherme/Documents/sandbox/elo/mvp/Backend-Filter-Group-Step-2/
docker build --rm=true -t lixar/lixar-events-sensor:$version .
docker tag lixar/lixar-events-sensor:$version hrmguilherme/lixar-events-sensor:$version
docker push hrmguilherme/lixar-events-sensor:$version

helm install lixar-events-sensor charts/spark-app-sensor