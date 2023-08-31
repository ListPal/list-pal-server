package com.glist.GroceriesList.service;

import com.glist.GroceriesList.model.request.AuthenticationRequest;
import com.glist.GroceriesList.model.request.RegisterRequest;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.response.UserAuthenticationResponse;
import com.glist.GroceriesList.repository.ContainerDbRepository;
import com.glist.GroceriesList.repository.UserRepo;
import com.mongodb.MongoWriteException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final ContainerDbRepository containerDbRepository;
    private final JwtService jwtService;
    private final UserRepo userRepo;

    public UserAuthenticationResponse register(RegisterRequest req) throws MongoWriteException {
        return userRepo.register(req);
    }

    public UserAuthenticationResponse authenticate(AuthenticationRequest req) throws AccessDeniedException {
        return userRepo.authenticate(req);
    }

    public UserAuthenticationResponse checkAuthentication(String token) throws Exception {
        if (token == null) throw new AccessDeniedException("No token found.");
        return userRepo.checkAuthentication(token);
    }

    public void ensureAuthorizedSubject(String token, String containerId) throws AccessDeniedException {
        if (token == null) throw new AccessDeniedException("No token found.");
        userRepo.ensureAuthorizedSubject(token, containerId);
    }

    public Response getUserInfo(String token) throws AccessDeniedException {
        if (token == null) throw new AccessDeniedException("No token found.");
        return userRepo.getUserInfoFromToken(token);
    }

    // TODO: Needs implementing
    public Boolean refreshToken(String token) throws Exception {
        if (token == null) throw new AccessDeniedException("No token found.");
        Date expirationDate = jwtService.extractClaim(token, Claims::getExpiration);
        Instant instantToCheck = expirationDate.toInstant();
        Instant currentInstant = Instant.now();
        Duration duration = Duration.between(instantToCheck, currentInstant);
        // refresh if within 24hrs
        return duration.toHours() <= 24;
    }
}
