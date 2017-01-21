#!/bin/bash
MCVERSION=$(basename $THE_JAR | sed "s/exparvis-\(.*\)-.*.jar/\1/g")
./gradlew curseforge
curl -i -H "X-API-Key: $MODSIO_API_KEY" -F body="{\"version\":{\"name\":\"$(basename $THE_JAR | sed "s/exparvis-.*-\(.*\).jar/\1/g")\",\"minecraft\":\"$MCVERSION\"},\"filename\": \"$(basename $THE_JAR)\"}" -F file=@${THE_JAR}  https://mods.io/mods/1253/versions/create.json
