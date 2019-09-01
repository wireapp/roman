package com.wire.bots.roman;

import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.UUID;

public class Tools {

    public static String validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(Application.getKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public static String generateToken(UUID botId) {
        return Jwts.builder()
                .setIssuer("https://wire.com")
                .setSubject(botId.toString())
                .signWith(Application.getKey())
                .compact();
    }

    public static String generateToken(UUID botId, long exp) {
        Date now = new Date();
        return Jwts.builder()
                .setIssuer("https://wire.com")
                .setSubject(botId.toString())
                .signWith(Application.getKey())
                .setExpiration(new Date(now.getTime() + exp))
                .compact();
    }
}
