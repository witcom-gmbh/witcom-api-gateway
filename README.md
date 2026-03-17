WITCOM API Gateway
===============
Gateway fuer APIs & Microservices-Zugriffe.

Das Gateway funktioniert nur noch als OAuth-Proxy für den Zugriff auf FNT-Command mit vordefinierten Rollen. Alle weiteren Routen sind nicht aktiviert.

# K8S-Deployment

Siehe deployment/witcom-api-gateway

# Custom Filter - FNT-Command mit Keycloak-Authorisierung
Ein Filter der jeden Zugriff auf die Command-Business-Gateway-API mit einer gueltigen Session versieht.
Der Filter fuehrt einen Login bei FNT Command durch um eine Session zu erhalten. Die Session-ID
wird in einem REDIS Key-Value-Store abgespeichert, um mehrere Instanzen des API-Gateways laufen zu lassen.

Zugriffe auf Command-REST-Endpunkte benötigen ein gültiges JWT.

Filter-Einstellungen (insbesondere Endpunkte, sowie dafür benötigte Client-Rollen) siehe src/main/java/de/witcom/api/config/GatewayConfiguration.java

## Parameter zur Konfiguration des Filters

| *Umgebungs-Variable* | *Default-Value* | *Beschreibung* |
| --- | --- | --- |
| `COMMAND_ENABLED` | `true` | Aktivierung von Filter & Session-Management | 
| `COMMAND_BASE_URL` | - | Basis-URL der Command-Installation | 
| `COMMAND_USER` | - | User dessen Session genutzt wird | 
| `COMMAND_PASSWORD` | - | Passwort für `COMMAND_USER` | 
| `COMMAND_GROUP` | - | Gruppe die beim Login gewählt wird |
| `KEYCLOAK_SERVER_URL` | - | Keycloak-Instanz gegen die das präsentierte JWT validiert wird|  
| `KEYCLOAK_REALM_ID` | - | Keycloak-Realm gegen die das präsentierte JWT validiert wird |
