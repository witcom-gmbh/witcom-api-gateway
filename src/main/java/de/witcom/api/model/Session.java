package de.witcom.api.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.annotation.Id;

@RedisHash("Session")
public class Session implements Serializable {
    
	private static final long serialVersionUID = -6100707433571389630L;
    private String sessionId;
   	@Id    
    private String applicationId;
    private Date expires;
    
    
    public Session(String applicationId,String sessionId){
        this.applicationId=applicationId;
        this.sessionId=sessionId;
    }

    public String getApplicationId(){
        return applicationId;
    }
    public void setApplicationId(String applicationId ){
        this.applicationId=applicationId;
    }

    
    public String getSessionId(){
        return sessionId;
    }
    public void setSessionId(String sessionId ){
        this.sessionId=sessionId;
    }
    
    public Date getExpires(){
        return expires;
        
    }
    public void setExpires(Date expires){
        this.expires = expires;
        
    }
    
    @Override
    public String toString() {
        return "Session{" + "applicationId='" + applicationId + '\'' + ", sessionId='" + sessionId + '}';
}

    
}