#Create variable for version value
version=0.0.3
helm uninstall api-group-engine
# copy file to /app/target/scala-2.12/
docker build --rm=true -t elo/api-group-engine:$version .
docker tag elo/api-group-engine:$version hrmguilherme/api-group-engine:$version
docker push hrmguilherme/api-group-engine:$version

helm install api-group-engine charts/mvp-app
