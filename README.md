WITCOM API Gateway
===============
Gateway fuer APIS & Microservices auf Basis von Spring-Cloud-Gateway

# Konfiguration mit Consul-KV
Routen & Filter werden ueber Spring-Cloud-Config konfiguriert. Die Konfiguration muss dabei in einem Consul-KV-Store vorgehalten werden.

Wird zum Zugriff auf einen Consul-Agent ein ACL-Token, benoetigt und/oder laeuft der Agent nicht auf localhost,
so kann der Consul-Client per Umgebungsvariable erfolgen

* SPRING_CLOUD_CONSUL_HOST=IP-ODER-HOSTNAME
* SPRING_CLOUD_CONSUL_PORT=443
* SPRING_CLOUD_CONSUL_CONFIG_ACL_TOKEN=****

Die Konfiguration erwartet die Applikation unter dem KV-Key **applicationconfig/witcom-api-gateway/data**

Es kann daher durchaus Sinn ergeben diese Parameter mit Umgebungsvariablen anzupassen

* SPRING_CLOUD_CONSUL_CONFIG_PREFIX=alternatives-prefix
* SPRING_APPLICATION_NAME=gw-release-2

Mit diesen Parametern wird die Konfiguration unter **alternatives-prefix/gw-release-2/data** erwartet

# K8S-Deployment

Erfolgt uber ein Helm-Chart, Details siehe WiTCOM Wiki ;-)

# Custom Filter - KeyCloak

Erfordert ein gueltiges JWT Bearer-Token, sowie die Mitgliedschaft einer konfigurierbaren Rolle.

## Globale Einstellungen
Die zu verwendende Keycloak-Instanz wird über die application-properties konfiguriert. Diese Properties können natürlich auch aus Consul geladen werden. Es ist auch nöglich dort Umgebungsvariablemn zu referenzieren 

```yaml
application:
  keycloak-config:
    keycloak-realm-id: ${KEYCLOAK_REALM_ID}
    keycloak-server-url: http:/my-url/auth
```

## Filter-Konfiguration
Der Filter wird wie folgt konfiguriert

KeyCloakFilter=requiredRole,RESOURCE:ROLE

RESOURCE ist dabei die Keycloak-Applikation (Client) die die Rolle definiert, ROLE ist die Rolle

Beispiel - zum Zugriff auf den Pfad /get muss der zugreifende User/Client die Rolle
samplewebservice01_resource_a_read in der Resource samplewebservice01 besitzen

```yaml
spring:
  cloud:
   gateway:
     routes:
      - id: dummy-api
        uri: http://httpbin.org:80
        predicates:
         - Path=/get/**
        filters:
           - KeyCloakFilter=requiredRole,samplewebservice01:samplewebservice01_resource_a_read
```

# Custom Filter - Service-Planet
Ein Filter der jeden Zugriff auf die Service-Planet-API mit einer gueltigen Session versieht.
Der Filter fuehrt einen Login bei Service-Planet durch um eine Session zu erhalten. Die Session-ID
wird in einem REDIS Key-Value-Store abgespeichert, um mehrere Instanzen des API-Gateways laufen zu lassen.

## Globale Einstellungen
Globale Filtereinstellungen über die application-properties konfiguriert. Diese Properties können natürlich auch aus Consul geladen werden. Es ist auch nöglich dort Umgebungsvariablemn zu referenzieren

```yaml
application:
  spl-config:
    spl-base-url: http://spl-url
    spl-tenant: optional
    spl-user: user
    spl-password: ${SPL_PASSWORD} 
```

Per Default verbindet sich das API-Gateway zu einer REDIS-Instanz deren Hostname in der Umgebungsvariable REDIS_HOST erwartet wird. Das Kennwort zur Instanz wird
in der Umgebungsvariablen REDIS_PASSWORD erwartet

## Beispiel
Im Idealfall kombiniert man den ServicePlanet-Filter mit dem KeyCloakFilter um eine Authorisierung zu erreichen.

```yaml
spring:
  cloud:
   gateway:
    globalcors:
      cors-configurations:
        '[/**]':
         allowedOrigins: "*"          
    routes:
      - id: spl
        uri: ${SPL_BASEURL}/serviceplanet/remote/service
        predicates:
         - Path=/smdb/**
        filters:
         - KeyCloakFilter=requiredRole,spl:service_read
         - ServicePlanetFilter
         - RewritePath=/smdb/(?<segment>.*), /serviceplanet/remote/service/$\{segment}
```

# Custom Filter - Basic-Auth
Fuegt einem Request eine Basic-Authentifizierugn hinzu. Falls der eingehende Request bereits einen Authorization-Header hat,
muss dieser mit dem Filter RemoveRequestHeader entfernt werden.

## Konfiguration & Beispiel
Der Filter erwartet Benutzername & Passwort

BasicAuthFilter=USER,PASSWORD

Es ist moeglich Benutzername & Passwort aus Umgebungsvariablen zu beziehen.

```yaml
spring:
  cloud:
   gateway:
    globalcors:
      cors-configurations:
        '[/**]':
         allowedOrigins: "*"          
    routes:
      - id: dsl-api
        uri: https://secure.mk-netzdienste.de/kundenportal
        predicates:
         - Path=/dslrecherche/**
        filters:
          - KeyCloakFilter=requiredRole,service_read
          - RemoveRequestHeader=Authorization    
          - BasicAuthFilter=${MK_USER},${MK_PASS}
```

# Custom Filter - FNT-Command mit Keycloak-Authorisierung
Ein Filter der jeden Zugriff auf die Command-Business-Gateway-API mit einer gueltigen Session versieht.
Der Filter fuehrt einen Login bei FNT Command durch um eine Session zu erhalten. Die Session-ID
wird in einem REDIS Key-Value-Store abgespeichert, um mehrere Instanzen des API-Gateways laufen zu lassen.

## Globale Einstellungen
Globale Filtereinstellungen über die application-properties konfiguriert. Diese Properties können natürlich auch aus Consul geladen werden. Es ist auch möglich dort Umgebungsvariablemn zu referenzieren

```yaml
application:
  command-config:
    base-url: ${RMDB_BASEURL:http://localhost:8080/axis}
    user: ${RMDB_USER}
    password: ${RMDB_PASSWORD}
    group: ${RMDB_GROUP}
    mandant: ${RMDB_MANDANT}
  keycloak-config:
    keycloak-realm-id: ${KEYCLOAK_REALM_ID}
    keycloak-server-url: http:/my-url/auth
```

Per Default verbindet sich das API-Gateway zu einer REDIS-Instanz deren Hostname in der Umgebungsvariable REDIS_HOST erwartet wird. Das Kennwort zur Instanz wird
in der Umgebungsvariablen REDIS_PASSWORD erwartet

## Authorisierung 
Sehr simple Authorisierung die zwischen READ & WORK Rollen unterscheidet. Die Unterscheidung wird anhand des API-Paths getroffen. Das Schema ist hierbei wie folgt

*  /PATH/api/rest/entity/ENTITYNAME/query* -> READ
*  /PATH/api/rest/entity/ENTITYNAME/ELID/Operation-mit-Grossbuchstaben -> READ, da Relation
* der Rest erfordert WORK Rechte.

## Vorbereitung Keycloak
Keycloak Resource-Server anlegen (als Confidential-Client, ohne Standard-Flow). Es werden mindestens 2 Rollen benötigt

* read
* work

## Beispiel
Sehr simple Konfiguration die nicht zwischen Entitäts-Klassen unterscheidet. Es werden die Default-rollen verwendet

* read = lesen
* work = arbeiten ;-)

```yaml
spring:
  cloud:
   gateway:
    globalcors:
      cors-configurations:
        '[/**]':
         allowedOrigins: "*"          
    routes:
      - id: command
        uri: ${RMDB_BASEURL:http://localhost:8080/axis}
        predicates:
         - Path=/rmdb/**
        filters:
         - name: KeycloakCommandFilter
           args:
             resource-id: rmdb-resource-server
         - RewritePath=/rmdb/(?<segment>.*), /axis/$\{segment}
```

Eine etwas granularere Konfiguration die der Rolle base-work Work-Rechte für einige Entitäts-Klassen gibt. Für den Rest wird eine andere Rolle benötigt
Kann man machen sofern es nicht zu granular = unübersichtlich wird. Dafür sollte man dann lieber auf Keycloak-Authorization-Services wechseln  

```yaml
spring:
  cloud:
   gateway:
    globalcors:
      cors-configurations:
        '[/**]':
         allowedOrigins: "*"          
    routes:
      - id: command-base
        uri: ${RMDB_BASEURL:http://localhost:8080/axis}
        predicates:
         - Path=/rmdb/api/rest/entity/campus/**,/rmdb/api/rest/entity/building/**,/rmdb/api/rest/entity/floor/**,/rmdb/api/rest/entity/room/**
        filters:
         - name: KeycloakCommandFilter
           args:
             resource-id: rmdb-resource-server
             role-work: base-work
             role-read: read
         - RewritePath=/rmdb/(?<segment>.*), /axis/$\{segment}
#catch-all
      - id: command-default
        uri: ${RMDB_BASEURL:http://localhost:8080/axis}
        predicates:
         - Path=/rmdb/**
        filters:
         - name: KeycloakCommandFilter
           args:
             resource-id: rmdb-resource-server
             role-work: super-work
             role-read: read             
         - RewritePath=/rmdb/(?<segment>.*), /axis/$\{segment}
```

# Custom Filter - Ciena MCP mit Keycloak-Authorisierung
Ein Filter der jeden Zugriff auf die Ciena MCP mit einem gueltigen MCP eigenen OAuth2 Access-Token versieht.
Der Filter fuehrt einen Login bei FNT Command durch um eine OAuth2-Token zu erhalten. Dieses Token
wird in einem REDIS Key-Value-Store abgespeichert, um mehrere Instanzen des API-Gateways laufen lassen zu können.

## Globale Einstellungen
Globale Filtereinstellungen über die application-properties konfiguriert. Diese Properties können natürlich auch aus Consul geladen werden. Es ist auch möglich dort Umgebungsvariablemn zu referenzieren

```yaml
application:
  mcp-config:
    base-url: ${MCP_BASEURL:http://localhost:8080/axis}
    user: ${MCP_USER}
    password: ${MCP_PASSWORD}
  keycloak-config:
    keycloak-realm-id: ${KEYCLOAK_REALM_ID}
    keycloak-server-url: http:/my-url/auth
```

Per Default verbindet sich das API-Gateway zu einer REDIS-Instanz deren Hostname in der Umgebungsvariable REDIS_HOST erwartet wird. Das Kennwort zur Instanz wird
in der Umgebungsvariablen REDIS_PASSWORD erwartet

## Authorisierung 
Sehr simple Authorisierung die zwischen READ & WORK Rollen unterscheidet. Die Unterscheidung wird anhand der API-Methode getroffen

* GET/HEAD Requests erfordern eine READ-Rolle
* DELETE Requests erfordern eine DELETE-Rolle
* POST/PUT/PATCH Requests erfordern eine WORK-Rolle

## Vorbereitung Keycloak
Keycloak Resource-Server anlegen (als Confidential-Client, ohne Standard-Flow). Es werden mindestens 3 Rollen benötigt

* read
* work
* delete

## Beispiel
Sehr simple Konfiguration die nicht zwischen MCP-APIs unterscheidet. Es werden die Default-rollen verwendet

* read = lesen
* work = arbeiten ;-)
* delete = loeschen

```yaml
spring:
  cloud:
   gateway:
    globalcors:
      cors-configurations:
        '[/**]':
         allowedOrigins: "*"          
    routes:
      - id: mcp
        uri: ${MCP_BASEURL:http://localhost:8080/axis}
        predicates:
         - Path=/mcp/**
        filters:
         - name: KeycloakMcpFilter
           args:
             resource-id: mcp-resource-server
         - RewritePath=/mcp/(?<segment>.*), /$\{segment}
```

Eine etwas granularere Konfiguration die der Rolle base-work Work-Rechte für einige APIs (search api,Network elements ) gibt. Für den Rest wird eine andere Rolle benötigt
Kann man machen sofern es nicht zu granular = unübersichtlich wird. Dafür sollte man dann lieber auf Keycloak-Authorization-Services wechseln  

```yaml
spring:
  cloud:
   gateway:
    globalcors:
      cors-configurations:
        '[/**]':
         allowedOrigins: "*"          
    routes:
      - id: mcp-base
        uri: ${RMDB_BASEURL:http://localhost:8080/axis}
        predicates:
         - Path=/mcp/nsi/api/search/**,/mcp/nsi/api/networkConstructs/**
        filters:
         - name: KeycloakMcpFilter
           args:
             resource-id: mcp-resource-server
             role-work: base-work
             role-read: read
             role-delete: delete
         - RewritePath=/mcp/(?<segment>.*), /$\{segment}
#catch-all
      - id: mcp-default
        uri: ${RMDB_BASEURL:http://localhost:8080/axis}
        predicates:
         - Path=/mcp/**
        filters:
         - name: KeycloakMcpFilter
           args:
             resource-id: mcp-resource-server
             role-work: super-work
             role-read: read
             role-delete: super-delete             
         - RewritePath=/mcp/(?<segment>.*), /$\{segment}
```









