# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.195.0/containers/java/.devcontainer/base.Dockerfile
# [Choice] Java version (use -bullseye variants on local arm64/Apple Silicon): 8, 11, 16, 8-bullseye, 11-bullseye, 16-bullseye, 8-buster, 11-buster, 16-buster
ARG VARIANT=17-bookworm
FROM mcr.microsoft.com/vscode/devcontainers/java:1-${VARIANT}

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

# This is needed for extension-installing in vscode
ENV NODE_EXTRA_CA_CERTS=/usr/local/share/ca-certificates/wit-ca-crt-19f321a4661f40884bb8af5146932975.crt
# this would be the brute-force-variant
# ENV NODE_TLS_REJECT_UNAUTHORIZED=0

ARG INSTALL_OPENAPI_CLI="false"
ARG OPENAPI_CLI_VERSION="6.6.0"
RUN if [ "${INSTALL_OPENAPI_CLI}" = "true" ]; then \
    su vscode -c "umask 0002 && mkdir -p /home/vscode/.local/bin && wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/${OPENAPI_CLI_VERSION}/openapi-generator-cli-${OPENAPI_CLI_VERSION}.jar -O/home/vscode/.local/bin/openapi-generator-cli.jar"; \
    fi

# [Optional] Uncomment this section to install additional OS packages.
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive && \
    apt-get -y install moreutils

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment this line to install global node packages.
# RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1
