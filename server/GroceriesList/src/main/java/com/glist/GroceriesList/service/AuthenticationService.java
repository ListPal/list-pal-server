package com.glist.GroceriesList.service;

import com.glist.GroceriesList.model.groceries.CollapsedList;
import com.glist.GroceriesList.model.groceries.GroceryContainerType;
import com.glist.GroceriesList.model.groceries.GroceryList;
import com.glist.GroceriesList.model.groceries.GroceryListContainer;
import com.glist.GroceriesList.model.request.AuthenticationRequest;
import com.glist.GroceriesList.model.request.RegisterRequest;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.response.UserAuthenticationResponse;
import com.glist.GroceriesList.model.user.Role;
import com.glist.GroceriesList.model.user.User;
import com.glist.GroceriesList.repository.ContainerDbRepository;
import com.glist.GroceriesList.repository.UserDbRepository;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserDbRepository userDbRepository;
    private final ContainerDbRepository containerDbRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserAuthenticationResponse register(RegisterRequest req) throws MongoWriteException {
        // Create User
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail().trim())
                .name(req.getName().trim())
                .lastName(req.getLastName().trim())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER_ROLE)
                .phone(req.getPhone().trim())
                .build();

        // Create containers
        GroceryListContainer todo = GroceryListContainer.builder()
                .id(new ObjectId().toHexString() + GroceryContainerType.TODO)
                .collapsedLists(new ArrayList<CollapsedList>())
                .username(user.getUsername())
                .containerType(GroceryContainerType.TODO)
                .build();
        GroceryListContainer wishlist = GroceryListContainer.builder()
                .id(new ObjectId().toHexString() + GroceryContainerType.WISHLIST)
                .collapsedLists(new ArrayList<CollapsedList>())
                .username(user.getUsername())
                .containerType(GroceryContainerType.WISHLIST)
                .build();
        GroceryListContainer grocery = GroceryListContainer.builder()
                .id(new ObjectId().toHexString() + GroceryContainerType.GROCERY)
                .collapsedLists(new ArrayList<CollapsedList>())
                .username(user.getUsername())
                .containerType(GroceryContainerType.GROCERY)
                .build();

        // Set user container ids
        user.setTodoContainerId(todo.getId());
        user.setWishlistContainerId(wishlist.getId());
        user.setGroceryContainerId(grocery.getId());

        // Save user to db
        userDbRepository.insert(user);

        // Save containers to db
        containerDbRepository.insert(todo);
        containerDbRepository.insert(wishlist);
        containerDbRepository.insert(grocery);

        // Return generated token
        Map<String, Object> extraClaims = new LinkedHashMap<>();
        extraClaims.put("name", user.getName());
        extraClaims.put("lastName", user.getLastName());
        extraClaims.put("id", user.getId());

        var jwtToken = jwtService.generateToken(extraClaims, user);

        return UserAuthenticationResponse.builder()
                .user(user)
                .status(200)
                .token(jwtToken)
                .build();
    }

    public UserAuthenticationResponse authenticate(AuthenticationRequest req) throws AccessDeniedException {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        User user = userDbRepository.findByUsername(req.getUsername());
        if (user == null) {
            throw new AccessDeniedException("User Not Found");
        }
        Map<String, Object> extraClaims = new LinkedHashMap<>();
        extraClaims.put("name", user.getName());
        extraClaims.put("lastName", user.getLastName());
        extraClaims.put("id", user.getId());

        String jwtToken = jwtService.generateToken(extraClaims, user);
        log.info("Authenticated");
        return UserAuthenticationResponse.builder()
                .user(user)
                .status(200)
                .token(jwtToken)
                .build();
    }

    public UserAuthenticationResponse checkAuthentication(String token) throws Exception {
        User user = userDbRepository.findByUsername(jwtService.extractUsername(token));
        if (user == null) {
            throw new AccessDeniedException("User Not Found");
        }
        return UserAuthenticationResponse.builder()
                .user(user)
                .status(200)
                .token(token)
                .build();
    }

    public void ensureAuthorizedSubject(String token, String containerId) throws AccessDeniedException {
        // Ensure token exists
        if (token == null) {
            throw new AccessDeniedException("No token was found.");
        }

        // Extract username from jwt
        String username = jwtService.extractUsername(token);
        if (username == null) {
            throw new AccessDeniedException("No username in jwt found.");
        }

        // Ensure username matches the username in the requested container
        GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
        if (!container.getUsername().equals(username)) {
            throw new AccessDeniedException("Not an authorized subject to request this asset");
        }
    }

    public Response getUserInfo(String token) throws AccessDeniedException {
        if (token == null)
            throw new AccessDeniedException("No token found.");

        // Extract claims from token
        String firstName = jwtService.extractClaim(token, claims -> claims.get("name", String.class));
        String id = jwtService.extractClaim(token, claims -> claims.get("id", String.class));
        String username = jwtService.extractUsername(token);
        // Build user map and return
        Map<String, String> user = new LinkedHashMap<>();
        user.put("username", username);
        user.put("name", firstName);
        user.put("id", id);

        return new Response(200, user);
    }

    public UserAuthenticationResponse refreshToken(String token) throws Exception {
        Date expirationDate = jwtService.extractClaim(token, Claims::getExpiration);
        Instant instantToCheck = expirationDate.toInstant();
        Instant currentInstant = Instant.now();
        Duration duration = Duration.between(instantToCheck, currentInstant);
        if (duration.toHours() <= 24) { // refresh if within 24hrs
            return checkAuthentication(token);
        } else {
            return null;
        }
    }
}
