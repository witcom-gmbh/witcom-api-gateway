WITCOM API Gateway
===============
Gateway fuer APIS&Microservices auf Basis von Spring-Cloud-Gateway

# Bootstrapping
Routen & Filter werden ueber Spring-Boot konfiguriert, und zwar ueber Consul.

Wird zum Zugriff auf einen Consul-Agent ein ACL-Token, benoetigt und/oder laeuft der Agent nicht auf localhost,
so kann der Consul-Client per Umgebungsvariable erfolgen

* SPRING_CLOUD_CONSUL_HOST=IP-ODER-HOSTNAME
* SPRING_CLOUD_CONSUL_PORT=443
* SPRING_CLOUD_CONSUL_CONFIG_ACL_TOKEN=****

Die Konfiguation wird im Consul-KV unter dem KV-Key **applicationconfig/witcom-api-gateway** erwartet.
Profilspezifische Konfiguration 

* applicationconfig/witcom-api-gateway::dev
* applicationconfig/witcom-api-gateway::prod

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



# OpenShift - Deployment Produktiv

Erfordert Springboot-S2I Image (https://github.com/iceman91176/openshift-s2i-springboot-java) im Openshift-Namespace

## SSH-Key zu Private-Repository als Github-Secret verfuegbar machen
Das Repository ist ein private Repository, daher wird ein SSH-Key benoetigt um das Image zu bauen.

Detaillierte Anleitung hier https://blog.openshift.com/private-git-repositories-part-2a-repository-ssh-keys/

Kurzfassung

* Private Key in Secret importieren `oc secrets new-sshauth repo-at-github --ssh-privatekey=PRIVATE-KEY-FILE`
* Secret mit dem Builder verlinken `oc secrets link builder repo-at-github`
* Secret fuer Build aus diesem Repo verfuegbar machen `oc annotate secret/repo-at-github 'build.openshift.io/source-secret-match-uri-1=ssh://github.com/PFAD-ZUM-REPO'`

## Deploy REDIS
Standard Template verwenden, Servicename redis-apigw

## Deploy API-Gateway
Das Template openshift-template.json verwenden. 
Wenn alles richtig gemacht wurde, wird die Applikation gebaut, startet & laeuft ;-)

## Routen & Filter in Consul konfigurieren
In der konfigurierten Consul-Instanz muss unter applicationconfig/witcom-api-gateway::prod/data die benötigte Konfiguration 
abgelegt werden. Aenderungen werden direkt uebernommen.








