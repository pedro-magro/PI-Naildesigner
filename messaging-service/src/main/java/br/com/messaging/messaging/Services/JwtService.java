package br.com.messaging.messaging.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret.key}") // DEVE SER A MESMA CHAVE DO AUTH-SERVICE
    private String SECRET_KEY;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        final Claims claims = extractAllClaims(token);
        // Supondo que "roles" foi armazenado como uma List<String> no token
        return claims.get("roles", List.class);
    }

    // Opcional: Se você também colocou o userId no token
    // public String extractUserId(String token) {
    //     final Claims claims = extractAllClaims(token);
    //     return claims.get("userId", String.class); // Ou o tipo que você usou
    // }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Valida assinatura e expiração. Não precisa de UserDetails aqui.
    public boolean isTokenSignatureAndExpirationValid(String token) {
        try {
            // A tentativa de parsear o token já valida a assinatura.
            // Se a assinatura for inválida ou o token malformado, Jwts.parserBuilder()... lançará uma exceção.
            // Se o token estiver expirado, isTokenExpired() retornará true.
            extractAllClaims(token); // Valida a assinatura ao tentar parsear
            return !isTokenExpired(token);
        } catch (Exception e) {
            // io.jsonwebtoken.ExpiredJwtException, io.jsonwebtoken.security.SignatureException, etc.
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token) // Esta chamada valida a assinatura
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
