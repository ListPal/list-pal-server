package com.glist.GroceriesList.configuration;

import com.glist.GroceriesList.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException, ExpiredJwtException {
        try {
            log.debug("[AUTHENTICATION]cookies: " + Arrays.toString(request.getCookies()));

            final String jwt = getJwt(request);
            if (jwt == null) {
                filterChain.doFilter(request, response);
                log.debug("No jwt found. Unauthorized");
                return;
            }
            final String username = jwtService.extractUsername(jwt);
            log.debug("[AUTHENTICATION]jwt: " + jwt);
            log.debug("[AUTHENTICATION]username: " + username);

            // if email and user not authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // fetch user from db
                log.debug("[AUTHENTICATION]Fetching user from db");
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // validate token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("[AUTHENTICATION]Token is valid");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // update security context holders
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.debug("[AUTHENTICATION]Token is invalid. Continuing stream");
                }
            }
            // continue stream
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT found: " + e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    private String getJwt(HttpServletRequest request) {
        if (isJwtFromAuthHeader(request)) {
            return getJwtFromAutorizationHeader(request);
        }
        log.debug("No Authorization header found");
        if (isJwtFromCookies(request)) {
            return getJwtFromCookies(request);
        }
        log.debug("No auth-jwl Cookie found");
        return null;
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        log.debug("jwt Cookie found.");
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("auth-jwt")) {
                log.debug(cookie.getValue());
                return cookie.getValue();
            }
        }
        return null;
    }

    private String getJwtFromAutorizationHeader(HttpServletRequest request) {
        try {
            log.info("Authorization header found.");
            final String authHeader = request.getHeader("Authorization");
            // `Bearer [jwt here]`
            return authHeader.substring(7);
        } catch (
                IndexOutOfBoundsException e) {
            log.error("JWT retrieval produced an out of bounds exception: " + e.getMessage());
            return null;
        }

    }

    private boolean isJwtFromAuthHeader(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        return (authorizationHeader != null && authorizationHeader.startsWith("Bearer "));
    }

    private boolean isJwtFromCookies(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return false;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("auth-jwt")) {
                return true;
            }
        }
        return false;
    }
}
