# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.195.0/containers/java/.devcontainer/base.Dockerfile
# [Choice] Java version (use -bullseye variants on local arm64/Apple Silicon): 8, 11, 16, 8-bullseye, 11-bullseye, 16-bullseye, 8-buster, 11-buster, 16-buster
ARG VARIANT=17-buster
FROM mcr.microsoft.com/vscode/devcontainers/java:0-${VARIANT}

# Brute force around certificate-inspection
ARG SKIP_APT_PEER_VERIFY="false"
RUN if [ "${SKIP_APT_PEER_VERIFY}" = "true" ]; then \
    touch /etc/apt/apt.conf.d/99verify-peer.conf && echo >>/etc/apt/apt.conf.d/99verify-peer.conf "Acquire { https::Verify-Peer false }"; \
    fi

# Custom CA integration for Java
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive && \
    apt-get -y install ca-certificates-java
    
# Add custom CAs
COPY ./ca-trust/*.crt /usr/local/share/ca-certificates/
RUN update-ca-certificates && \
    # if adding additional JDKs -> repeat
    ln -sf /etc/ssl/certs/java/cacerts /usr/lib/jvm/msopenjdk-current/lib/security/cacerts

# [Option] Install Maven
ARG INSTALL_MAVEN="false"
ARG MAVEN_VERSION=""
# [Option] Install Gradle
ARG INSTALL_GRADLE="false"
ARG GRADLE_VERSION=""
RUN if [ "${INSTALL_MAVEN}" = "true" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""; fi \
    && if [ "${INSTALL_GRADLE}" = "true" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install gradle \"${GRADLE_VERSION}\""; fi

#Install Additional JDK runtimes
#RUN su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install java 11.0.18-ms"

# [Choice] Node.js version: none, lts/*, 16, 14, 12, 10
ARG NODE_VERSION="none"
RUN if [ "${NODE_VERSION}" != "none" ]; then su vscode -c "umask 0002 && . /usr/local/share/nvm/nvm.sh && nvm install ${NODE_VERSION} 2>&1"; fi

ARG INSTALL_NGROK="false"
RUN if [ "${INSTALL_NGROK}" = "true" ]; then su vscode -c "umask 0002 && mkdir -p /home/vscode/.local/bin && wget -qO- https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.tgz | tar xvz -C /home/vscode/.local/bin && chmod +x /home/vscode/.local/bin/ngrok"; fi

ARG INSTALL_COCOGITTO="true"
ARG COCOGITTO_VERSION="6.0.1"

RUN if [ "${INSTALL_COCOGITTO}" = "true" ]; then \
    su vscode -c "umask 0002 && mkdir -p /home/vscode/.local/bin && wget -qO- https://github.com/cocogitto/cocogitto/releases/download/${COCOGITTO_VERSION}/cocogitto-${COCOGITTO_VERSION}-x86_64-unknown-linux-musl.tar.gz | tar xvz --strip-components=1 -C /home/vscode/.local/bin && chmod +x /home/vscode/.local/bin/cog"; \
    fi

ARG INSTALL_YQ="false"
ARG YQ_VERSION="latest"
RUN if [ "${INSTALL_YQ}" = "true" ]; then \
    su vscode -c "umask 0002 && mkdir -p /home/vscode/.local/bin && wget -qO/home/vscode/.local/bin/yq https://github.com/mikefarah/yq/releases/${YQ_VERSION}/download/yq_linux_amd64  && chmod a+x /home/vscode/.local/bin/yq"; \
    fi

ARG INSTALL_OPENAPI_CLI="false"
ARG OPENAPI_CLI_VERSION="6.6.0"
RUN if [ "${INSTALL_OPENAPI_CLI}" = "true" ]; then \
    su vscode -c "umask 0002 && mkdir -p /home/vscode/.local/bin && wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/${OPENAPI_CLI_VERSION}/openapi-generator-cli-${OPENAPI_CLI_VERSION}.jar -O/home/vscode/.local/bin/openapi-generator-cli.jar"; \
    fi


# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment this line to install global node packages.
# RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1
