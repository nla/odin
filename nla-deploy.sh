#!/bin/bash
mvn package dependency:copy-dependencies
mkdir "$2"/{lib,content}
cp odin-server/target/*.jar odin-server/target/dependency/* "$2"/lib
cp -R odin-server/config/ "$2"/config/
unzip -d odin-ui/target/*.war "$2"/content

# FIXME: make the app look at environment variables
sed -i 's/^\(loki.http.request.adapter.port = \).*/\1'"$PORT/" "$2"/config/http_adapter.properties
sed -i 's/^\(loki.http.request.adapter.host = \).*/\1'"${HOST:-127.0.0.1}/" "$2"/config/http_adapter.properties