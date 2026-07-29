package com.rochak.payflow.session;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class IpExtractor {
    public String extract(HttpServletRequest request){
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if(xForwardedFor !=null && xForwardedFor.isBlank()){
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");

        if(xRealIp !=null && !xRealIp.isBlank()){
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
