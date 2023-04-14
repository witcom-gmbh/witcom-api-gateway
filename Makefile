PROJECT_NAME=witcom-api-gateway

include ./Makefile.docker

all: docker_clean docker_build docker_release

docker_%: docker_%_default
	@  true

.PHONY: clean build release