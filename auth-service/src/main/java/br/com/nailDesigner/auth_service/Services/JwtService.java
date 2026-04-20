package br.com.nailDesigner.auth_service.Services;

import br.com.nailDesigner.auth_service.Models.User; 
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class    JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;


    private long jwtExpiration = 1000 * 60 * 60 * 24; // 24 horas em milissegundos

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Método para gerar token apenas com UserDetails (mantido para compatibilidade)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // MÉTODO PRINCIPAL DE GERAÇÃO DE TOKEN - MODIFICADO PARA INCLUIR ROLES e USERID
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(extraClaims); // Copia claims extras se houver

        // Adiciona as roles (authorities) como uma claim
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);


        if (userDetails instanceof User) {
            claims.put("userId", ((User) userDetails).getId());

        }

        String subjectId = ((User) userDetails).getId().toString();
        logger.debug("Gerando token JWT para o usuário {}", subjectId);

        return Jwts.builder()
                .setClaims(claims) // Define todas as claims (incluindo as customizadas)
                .setSubject(subjectId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        // Extrai o ID (que está no campo 'subject') do token
        final String userIdFromToken = extractUserId(token);

        // Pega o ID do objeto UserDetails (fazendo o cast)
        final String userIdFromUserDetails = ((User) userDetails).getId().toString();

        // Compara os dois IDs e verifica se o token não expirou
        return (userIdFromToken.equals(userIdFromUserDetails)) && !isTokenExpired(token);
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
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
