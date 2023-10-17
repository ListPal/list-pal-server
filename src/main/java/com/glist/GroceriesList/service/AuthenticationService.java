package com.glist.GroceriesList.service;

import com.glist.GroceriesList.model.request.AuthenticationRequest;
import com.glist.GroceriesList.model.request.RegisterRequest;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.response.UserAuthenticationResponse;
import com.glist.GroceriesList.repository.UserRepo;
import com.mongodb.MongoWriteException;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final CookieService cookieService;
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

    public void ensurePrivateSubject(String token, String containerId) throws AccessDeniedException {
        if (token == null) throw new AccessDeniedException("No token found.");
        userRepo.ensureAuthorizedSubject(token, containerId);
    }

    public void ensureRestrictedSubject(String token, String listId) throws AccessDeniedException {
        if (token == null) throw new AccessDeniedException("No token found.");
        userRepo.ensureRestrictedSubject(token, listId);
    }

    public Response getUserInfo(String token) throws AccessDeniedException {
        if (token == null) throw new AccessDeniedException("No token found.");
        return userRepo.getUserInfoFromToken(token);
    }

    public Cookie logout(String token) {
        if (token != null) {
            log.debug(token);
        }
        // Return a logout cookie
        return cookieService.deleteAuthCookie();
    }
}
