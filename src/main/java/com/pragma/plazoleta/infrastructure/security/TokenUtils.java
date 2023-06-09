package com.pragma.plazoleta.infrastructure.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenUtils {

    private final static String ACCESS_TOKEN_SECRET="$2a$10$MPN3YWfMk5dp/5L7ERNxeO2Z7hKkXFX1Lx/qI94YW9.64224245i";

    private final static Long ACCESS_TOKEN_VALIDITY_SECONDS = 2_592_000L;

    public static String createToken(String nombre,String correo, String rol){
        Long expirationTime = ACCESS_TOKEN_VALIDITY_SECONDS * 1_000;
        Date expirationDate = new Date(System.currentTimeMillis()+expirationTime);


        Map<String,Object> extra = new HashMap<>();
        extra.put("nombre",nombre);
        extra.put("rol",rol);
        return Jwts.builder()
                .setSubject(correo)
                .setExpiration(expirationDate)
                .addClaims(extra)
                .signWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes()))
                .compact();
    }

    public static Long getUserAuthenticatedId(String token){
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(ACCESS_TOKEN_SECRET.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("id",Long.class);
        } catch (JwtException e){
            return null;
        }
    }

    public static String getUserAuthenticatedEmail(String token){
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(ACCESS_TOKEN_SECRET.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("correo",String.class);
        } catch (JwtException e){
            return null;
        }
    }

    public static UsernamePasswordAuthenticationToken getAuthentication(String token){
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(ACCESS_TOKEN_SECRET.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String correo = claims.getSubject();

            Collection<SimpleGrantedAuthority> authorities =
                    Arrays.stream(claims.get("rol").toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            return new UsernamePasswordAuthenticationToken(correo, null, authorities);
        } catch (JwtException e){
            return null;
        }
    }

}
