package com.wire.bots.roman;

import io.jsonwebtoken.Jwts;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    static String generateToken(UUID botId, long exp) {
        Date now = new Date();
        return Jwts.builder()
                .setIssuer("https://wire.com")
                .setSubject(botId.toString())
                .signWith(Application.getKey())
                .setExpiration(new Date(now.getTime() + exp))
                .compact();
    }

    public static String decodeBase64(final String base64String) {
        byte[] keyBytes = Base64.getDecoder().decode(base64String);
        return new String(keyBytes, StandardCharsets.UTF_8);
    }
}
