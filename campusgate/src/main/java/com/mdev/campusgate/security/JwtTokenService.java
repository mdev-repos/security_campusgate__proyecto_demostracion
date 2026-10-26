package com.mdev.campusgate.security;

import com.mdev.campusgate.configuration.JwtProperties;
import com.mdev.campusgate.model.UserSec;
import com.mdev.campusgate.model.enums.Permission;
import com.mdev.campusgate.repository.IUserRepository;
import com.mdev.campusgate.utils.EmailNormalizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final IUserRepository userRepo;
    public static final String PERMISSIONS_CLAIM = "permissions";

    public JwtTokenService(JwtEncoder jwtEncoder,
                           JwtProperties jwtProperties,
                           IUserRepository userRepository){
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.userRepo = userRepository;
    }

    public String generateToken(Authentication authentication){
        String mail = authentication.getName();
        UserSec user = userRepo.findByEmail(EmailNormalizer.normalize(mail))
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));

        String userid = user.getId().toString();

        // Spring Security 7 also adds FACTOR_* authorities (how the user authenticated); only real permissions go in the token.
        Set<String> knownPermissions = Arrays.stream(Permission.values()).map(Permission::name).collect(Collectors.toSet());
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(knownPermissions::contains)
                .toList();

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(userid)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtProperties.expirationSeconds()))
                .id(UUID.randomUUID().toString())
                .claim(PERMISSIONS_CLAIM, authorities)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}