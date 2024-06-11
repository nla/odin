#!/bin/bash
mvn -v
javac -version
echo $JAVA_HOME
mvn package dependency:copy-dependencies
mkdir "$1"/{lib,content}
cp odin-server/target/*.jar odin-server/target/dependency/* "$1"/lib
cp -R odin-server/config/ "$1"/config/
unzip -d "$1"/content odin-ui/target/*.war

mkdir "$1"/content/root/
mv "$1"/content/index.html "$1"/content/root/index.html

# FIXME: make the app look at environment variables
sed -i 's/^\(loki.http.request.adapter.port = \).*/\1'"$PORT/" "$1"/config/http_adapter.properties
sed -i 's/^\(loki.http.request.adapter.host = \).*/\1'"${HOST:-127.0.0.1}/" "$1"/config/http_adapter.properties
