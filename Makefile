all: clean generate-openapi-spl generate-openapi-command

clean:
	rm -rf /tmp/out

generate-openapi-spl:
	@echo "Get Serviceplanet API-specs"
	wget "${SERVICEPLANET_API_SPECS_URL}/remote/service/v1/docgen/swagger?tags=Login_V1" -O/tmp/spl-api.json
	# Add JSESSIONID cookie to all operations except the login-apis
	jq  '(.paths | to_entries | map({key: .key, value: (.value | to_entries | map( (.value.parameters += [{"name": "JSESSIONID","in":"cookie","required": true, "schema": {      "type": "string"    }}]  )     ) | from_entries ) })| from_entries) as $$updated | .paths=$$updated | del(.paths["/v1/login/authenticate2"].post.parameters[] | select(.name == "JSESSIONID")) | del(.paths["/v1/login/authenticate"].post.parameters[] | select(.name == "JSESSIONID")) ' /tmp/spl-api.json > /tmp/spl-api-updated.json
	# @echo "Convert specs to OAS 3"
	rm -rf /tmp/out
	java -jar /home/vscode/.local/bin/openapi-generator-cli.jar generate -g openapi-yaml -o /tmp/out -i /tmp/spl-api-updated.json
	cp /tmp/out/openapi/openapi.yaml src/main/resources/openapi/spl-api.yml
	mvn -Popenapi-codegen generate-sources

generate-openapi-command:
	@echo "Convert specs to OAS 3"
	rm -rf /tmp/out
	java -jar /home/vscode/.local/bin/openapi-generator-cli.jar generate -g openapi-yaml -o /tmp/out -i ./swagger/fntcommand-login-api.yml
	cp /tmp/out/openapi/openapi.yaml src/main/resources/openapi/command-api.yml
	mvn -Popenapi-codegen generate-sources
