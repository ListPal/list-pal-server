package com.glist.GroceriesList.controller;

import com.glist.GroceriesList.model.request.*;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.response.UserAuthenticationResponse;
import com.glist.GroceriesList.service.AuthenticationService;
import com.glist.GroceriesList.service.CookieService;
import com.glist.GroceriesList.utils.Utils;
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
        // Hide token from server response
        res.setToken(null);
        // Send cookie in response header
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<UserAuthenticationResponse> authenticate(HttpServletRequest request, HttpServletResponse response, @RequestBody AuthenticationRequest body) throws AccessDeniedException {
        UserAuthenticationResponse res = authenticationService.authenticate(body);
        // Save jwt in browser cookie
        Cookie jwtCookie = cookieService.makeAuthCookie(res.getToken());
        // Send cookie in response header
        response.addCookie(jwtCookie);
        // Hide token from server response
        res.setToken(null);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> authenticate(HttpServletResponse response, @CookieValue(value = "auth-jwt", required = false) String token) throws AccessDeniedException {
        // Save null in browser cookie
        Cookie logoutCookie = authenticationService.logout(token);
        // Send empty cookie in response header
        response.addCookie(logoutCookie);
        return ResponseEntity.ok(new Response(200, "logged out"));
    }

    @PostMapping("/check-login-status")
    public ResponseEntity<Object> checkLoginStatus(@CookieValue("auth-jwt") String cookie, HttpServletResponse response) throws Exception {
        try {
            UserAuthenticationResponse res = authenticationService.checkAuthentication(cookie);

            // Send refresh token if necessary
            if (!cookie.equals(res.getToken())) {
                Cookie jwtCookie = cookieService.makeAuthCookie(res.getToken());
                response.addCookie(jwtCookie);
            }

            // Hide token from server response
            res.setToken(null);
            return ResponseEntity.ok(res);
        } catch (ExpiredJwtException e) {
            Response res = new Response(403, "Access Denied");
            Cookie logoutCookie = cookieService.deleteAuthCookie();
            response.addCookie(logoutCookie);
            return ResponseEntity.ok(res);
        }
    }

    @PostMapping("/get-user-info")
    public ResponseEntity<Response> getUserInfo(@CookieValue("auth-jwt") String authCookie) throws AccessDeniedException {
        return ResponseEntity.ok(authenticationService.getUserInfo(authCookie));
    }

//    @GetMapping("/reset-password")
//    public ResponseEntity<String> getResetPasswordLink() {
//        listpalEmailService.sendForgotPasswordLink("kate.anderson0608@gmail.com");
//        return ResponseEntity.ok("Successfully sent email");
//    }
//
//    @PostMapping("/password-reset")
//    public ResponseEntity<String> resetPassword() {
//
//        return ResponseEntity.ok(authenticationService.changePword());
//    }
}
