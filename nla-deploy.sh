#!/bin/bash
JAVA_HOME=/usr/lib/jvm/java-11-openjdk
mvn package dependency:copy-dependencies

mkdir "$1"/lib/
mkdir "$1"/content/
mkdir "$1"/content/odin_ui/
mkdir "$1"/content/root/

cp odin-server/target/*.jar odin-server/target/dependency/* "$1"/lib
cp -R odin-server/config/ "$1"/config/
cp -R odin-server/documents/ "$1"/documents/
cp -R odin-ui/war/* "$1"/content/
cp odin-ui-compile/target/odin-ui-*/odin_ui/* "$1"/content/odin_ui/
mv "$1"/content/index.html "$1"/content/root/index.html

# FIXME: make the app look at environment variables
sed -i 's/^\(loki.http.request.adapter.port = \).*/\1'"$PORT/" "$1"/config/http_adapter.properties
sed -i 's/^\(loki.http.request.adapter.host = \).*/\1'"${HOST:-127.0.0.1}/" "$1"/config/http_adapter.properties
