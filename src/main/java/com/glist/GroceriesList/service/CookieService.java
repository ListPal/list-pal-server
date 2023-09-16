package com.glist.GroceriesList.service;

import com.glist.GroceriesList.configuration.ListPalConfig;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CookieService {
    private final ListPalConfig listPalConfig;

    public Cookie makeAuthCookie(final String token) {
        Cookie jwtCookie = new Cookie("auth-jwt", token);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600 * 23 * 20); // 23 hrs * 20 (almost 20 days_
        jwtCookie.setHttpOnly(true);
        jwtCookie.setDomain(listPalConfig.getServerDomain());
//        jwtCookie.setSecure(true);
        return jwtCookie;
    }

    public Cookie deleteAuthCookie() {
        Cookie jwtCookie = new Cookie("auth-jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setDomain(listPalConfig.getServerDomain());
//        jwtCookie.setSecure(true);
        return jwtCookie;
    }
}
