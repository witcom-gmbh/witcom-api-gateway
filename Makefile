all: clean generate-openapi-spl generate-openapi-command

clean:
	rm -rf /tmp/out

generate-openapi-spl:
	@echo "Get Serviceplanet API-specs"
	wget "${SERVICEPLANET_API_SPECS_URL}/remote/service/v1/docgen/swagger?tags=Login_V1" -O/tmp/spl-api.json
	# @echo "Convert specs to OAS 3"
	rm -rf /tmp/out
	java -jar /home/vscode/.local/bin/openapi-generator-cli.jar generate -g openapi-yaml -o /tmp/out -i /tmp/spl-api.json
	cp /tmp/out/openapi/openapi.yaml src/main/resources/openapi/spl-api.yml
	mvn -Popenapi-codegen generate-sources

generate-openapi-command:
	@echo "Convert specs to OAS 3"
	rm -rf /tmp/out
	java -jar /home/vscode/.local/bin/openapi-generator-cli.jar generate -g openapi-yaml -o /tmp/out -i ./swagger/fntcommand-login-api.yml
	cp /tmp/out/openapi/openapi.yaml src/main/resources/openapi/command-api.yml
	mvn -Popenapi-codegen generate-sources
