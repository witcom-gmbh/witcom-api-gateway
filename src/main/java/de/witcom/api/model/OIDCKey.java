package de.witcom.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OIDCKey {
    public String kid;
    public String kty;
    public String alg;
    public String use;
    public String n;
    public String e;
    
}
