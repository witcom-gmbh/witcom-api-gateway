#!/bin/bash
#optional as cli argument
dockerRepo=${1:-docker.io}/witcom/witcom-api-gateway

builderImage=pub-registry.dev.witcom.services/witcom-s2i/springboot-java:jdk11

# get the version. MVN sucks in doing it properly
RELEASE=`docker run --rm --name xmlparser -v "$(pwd)":/apps -w /apps alpine/xml sh -c "xq -r .project.version < pom.xml"`

retVal=$?
if [ $retVal -ne 0 ]; then
    echo "Unable to retrieve RELEASE"
fi

# prepare maven-settings
TMP_DIR=$(mktemp -d)
mkdir $TMP_DIR/.m2
cat <<EOF > $TMP_DIR/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">
  <profiles>
   <profile>
     <id>default</id>
     <repositories>
       <repository>
      <id>witcom-central</id>
      <name>witcom-central</name>
      <url>https://nexus.dev.witcom.services/repository/maven-public/</url>
       </repository>
     </repositories>
   </profile>
 </profiles>
 <activeProfiles>
   <activeProfile>default</activeProfile>
 </activeProfiles>
</settings>
EOF

s2i build . ${builderImage} ${dockerRepo}:latest --inject ${TMP_DIR} -e MAVEN_ARGS_APPEND=" --s /opt/app-root/src/.m2/settings.xml -Pprod"

docker tag ${dockerRepo}:latest ${dockerRepo}:${RELEASE}
docker push ${dockerRepo}:latest
docker push ${dockerRepo}:${RELEASE}

docker rmi ${dockerRepo}:latest
docker rmi ${dockerRepo}:${RELEASE}

rm -rf ${TMP_DIR}