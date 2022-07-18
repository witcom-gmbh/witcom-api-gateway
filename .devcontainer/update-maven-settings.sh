#! /bin/bash
mkdir -p /home/vscode/.m2
#use different delimiter (~) for URLs
sed -e "s/NEXUSUSER/${NEXUS_USER}/g;s/NEXUSPASSWORD/${NEXUS_PASSWORD}/g;s~NEXUSURL~${NEXUS_URL}~g" ./.devcontainer/settings.xml.template > /home/vscode/.m2/settings.xml

