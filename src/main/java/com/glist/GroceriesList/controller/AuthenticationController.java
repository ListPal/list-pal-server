package com.glist.GroceriesList.controller;

import com.glist.GroceriesList.model.request.AuthenticationRequest;
import com.glist.GroceriesList.model.request.RegisterRequest;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.response.UserAuthenticationResponse;
import com.glist.GroceriesList.service.AuthenticationService;
import com.glist.GroceriesList.service.CookieService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<UserAuthenticationResponse> register(HttpServletResponse response, @RequestBody RegisterRequest body) throws Exception {
        // Create user
        UserAuthenticationResponse res = authenticationService.register(body);
        // Save jwt in browser cookie
        Cookie jwtCookie = cookieService.makeAuthCookie(res.getToken());
        // Send cookie in response header
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<UserAuthenticationResponse> authenticate(HttpServletRequest request, HttpServletResponse response, @RequestBody AuthenticationRequest body) throws AccessDeniedException {
        log.info("HOST: " + request.getHeader("Host"));
        UserAuthenticationResponse res = authenticationService.authenticate(body);
        // Save jwt in browser cookie
        Cookie jwtCookie = cookieService.makeAuthCookie(res.getToken());
        // Send cookie in response header
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout") // TODO expire token also
    public ResponseEntity<Response> authenticate(HttpServletResponse response) throws AccessDeniedException {
        // Save null in browser cookie
        Cookie jwtCookie = cookieService.deleteAuthCookie();
        // Send empty cookie in response header
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(new Response(200, "logged out"));
    }

    @PostMapping("/check-login-status")
    public ResponseEntity<Object> checkLoginStatus(@CookieValue(value = "auth-jwt", required = false) String cookie, HttpServletResponse response) throws Exception {
        try {
            if (cookie == null) {
                return ResponseEntity.ok(new Response(403, "You don't have access to this resource"));
            }
            UserAuthenticationResponse res = authenticationService.checkAuthentication(cookie);
            return ResponseEntity.ok(res);
        } catch (ExpiredJwtException e) {
            Response res = new Response(403, "Access Denied");
            Cookie deleteCookie = cookieService.deleteAuthCookie();
            response.addCookie(deleteCookie);
            return ResponseEntity.ok(res);
        }
    }

    @PostMapping("/get-user-info")
    public ResponseEntity<Response> getUserInfo(@CookieValue("auth-jwt") String authCookie) throws AccessDeniedException {
        return ResponseEntity.ok(authenticationService.getUserInfo(authCookie));
    }
}
