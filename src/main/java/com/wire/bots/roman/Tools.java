package com.wire.bots.roman;

import io.jsonwebtoken.Jwts;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.net.URI;
import java.net.URISyntaxException;

public class Tools {

    public static String validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(Application.getKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    static String generateToken(UUID botId) {
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

    public static String getPubkey(String hostname) throws IOException {
        String str = null;
        String raw_hostname = URI.create(hostname).getHost();
        PublicKey publicKey = getPublicKey(raw_hostname);
        if (publicKey != null)
            str = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        final String start = "-----BEGIN PUBLIC KEY-----";
        final String end = "-----END PUBLIC KEY-----";
        return String.format("%s\n%s\n%s", start, str, end);
    }

    private static PublicKey getPublicKey(String hostname) throws IOException {
        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(hostname, 443);
        socket.startHandshake();
        Certificate[] certs = socket.getSession().getPeerCertificates();
        Certificate cert = certs[0];
        return cert.getPublicKey();
    }
}
