package com.wire.bots.ealarming;

import io.jsonwebtoken.Jwts;

public class Tools {

    public static String validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(Service.getKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
