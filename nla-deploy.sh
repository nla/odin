#!/bin/bash
mvn package dependency:copy-dependencies
mkdir "$2"/{lib,content}
cp odin-server/target/*.jar odin-server/target/dependency/* "$2"/lib
cp -R odin-server/config/ "$2"/config/
unzip -d odin-ui/target/*.war "$2"/content
