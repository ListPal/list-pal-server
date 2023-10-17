package com.glist.GroceriesList.controller;

import com.glist.GroceriesList.model.request.*;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.user.UserLookupCriteria;
import com.glist.GroceriesList.service.JwtService;
import com.glist.GroceriesList.service.UserService;
import com.glist.GroceriesList.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/server")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("lookup-user")
    public ResponseEntity<Response> lookupUser(@RequestBody LookupUserRequestBody body) {
        try {
            Utils.validateInput(body.userIdentifier);
            Utils.validateInput(body.requesterUsername);
            Utils.validateInput(body.criteria.name());
            if (body.criteria.equals(UserLookupCriteria.USERNAME)) {
                return ResponseEntity.ok(userService.lookupUserByUsername(body.requesterUsername, body.userIdentifier));
            } else if (body.criteria.equals(UserLookupCriteria.PHONE)) {
                return ResponseEntity.ok(userService.lookupUserByPhone(body.requesterUsername, body.userIdentifier));
            } else {
                throw new IllegalArgumentException("Unsupported lookup criteria. None of PHONE or USERNAME was provided. User provided " + body.criteria);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            Response res = new Response(201, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.valueOf(201));
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/fetch-suggested-people")
    public ResponseEntity<Response> fetchSuggestedPeople(@RequestBody LookupUserRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            Utils.validateInput(body.userIdentifier);
            Utils.validateInput(body.criteria.name());
            return ResponseEntity.ok(userService.fetchSuggestedPeople(body.userIdentifier));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            Response res = new Response(201, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.valueOf(201));
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("update-email")
    public ResponseEntity<Response> updateEmail(@CookieValue("auth-jwt") String token, @RequestBody UpdateEmailRequestBody body) throws AccessDeniedException {
        try {
            // Validate input
            Utils.validateInput(body.email);
            final String username = jwtService.extractUsername(token);
            return ResponseEntity.ok(userService.updateEmail(username, body.email));
        }
        catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            Response res = new Response(201, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.valueOf(201));
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("update-phone")
    public ResponseEntity<Response> updatePhone(@CookieValue("auth-jwt") String token, @RequestBody UpdatePhoneRequestBody body) throws AccessDeniedException {
        try {
            log.info(body.phone);
            // Validate input
            Utils.validateInput(body.phone);
            final String username = jwtService.extractUsername(token);
            return ResponseEntity.ok(userService.updatePhone(username, body.phone));
        }
        catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            Response res = new Response(201, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.valueOf(201));
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("update-name")
    public ResponseEntity<Response> updateName(@CookieValue("auth-jwt") String token, @RequestBody UpdateNameRequestBody body) throws AccessDeniedException {
        try {
            // Validate input
            Utils.validateInput(body.name);
            Utils.validateInput(body.lastName);
            final String username = jwtService.extractUsername(token);
            return ResponseEntity.ok(userService.updateName(username, body.name, body.lastName));
        }
        catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            Response res = new Response(201, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.valueOf(201));
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("update-password")
    public ResponseEntity<Response> updatePassword(@CookieValue("auth-jwt") String token, @RequestBody UpdatePasswordRequestBody body) throws AccessDeniedException {
        try {
            // Validate input
            Utils.validateInput(body.currentPassword);
            Utils.validateInput(body.newPassword);
            final String username = jwtService.extractUsername(token);
            return ResponseEntity.ok(userService.updatePassword(username, body.currentPassword, body.newPassword));
        }
        catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            Response res = new Response(201, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.valueOf(201));
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("update-user-preferences")
    public ResponseEntity<Response> updateUserPreferences(@CookieValue("auth-jwt") String token, @RequestBody UserPreferencesRequestBody body) throws AccessDeniedException {
        try {
            // Validate input
            Utils.validateInput(body.theme.name());
            final String username = jwtService.extractUsername(token);
            return ResponseEntity.ok(userService.updateUserPreferences(username, body.theme));
        }
        catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            Response res = new Response(201, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.valueOf(201));
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
