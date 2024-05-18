package com.tianleyu.github;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Key;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.json.JSONObject;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import com.google.common.io.Files;

public class Utils {
    static private PrivateKey get(String filename) throws Exception {
        File directory = new File(filename);
        byte[] keyBytes = Files.toByteArray(new File(filename));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    static public String createJWT(String githubAppId, long ttlMillis, String keyFile) throws Exception {
        // The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // We will sign our JWT with our private key
        Key signingKey = get(keyFile);

        // Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setIssuer(githubAppId)
                .signWith(signingKey, signatureAlgorithm);

        // if it has been specified, let's add the expiration
        if (ttlMillis > 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        // Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }
    // public static void main(String[] args) throws Exception {
    // String jwtToken = createJWT("44435", 600000); // sdk-github-api-app-test
    // GitHub gitHubApp = new GitHubBuilder().withJwtToken(jwtToken).build();
    // }

    static public HttpResponse<String> post(String url, String body, String accessToken, HttpClient client)
            throws GitHubAppException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.github.com" + url))
                    .timeout(Duration.of(10L, ChronoUnit.SECONDS)) // Change 10 to 10L
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new GitHubAppException(e.getMessage());
        }
    }

    static public HttpResponse<String> get(String url, String accessToken, HttpClient client)
            throws GitHubAppException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.github.com" + url))
                    .timeout(Duration.of(10L, ChronoUnit.SECONDS)) // Change 10 to 10L
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .GET()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new GitHubAppException(e.getMessage());
        }
    }
}
