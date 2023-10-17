package com.glist.GroceriesList.repository;

import com.glist.GroceriesList.model.groceries.GroceryContainerType;
import com.glist.GroceriesList.model.groceries.GroceryList;
import com.glist.GroceriesList.model.groceries.GroceryListContainer;
import com.glist.GroceriesList.model.request.AuthenticationRequest;
import com.glist.GroceriesList.model.request.RegisterRequest;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.response.UserAuthenticationResponse;
import com.glist.GroceriesList.model.user.*;
import com.glist.GroceriesList.service.JwtService;
import com.mongodb.MongoWriteException;
import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Repository
@Data
@RequiredArgsConstructor
@Slf4j
public class UserRepo {
    private final UserDbRepository userDbRepository;
    private final ContainerDbRepository containerDbRepository;
    private final ListDbRepository listDbRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MongoTemplate mongoTemplate;

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
                .relevantUsers(new LinkedList<>())
                .userPreferences(UserPreferences.builder().theme(ThemeType.lightTheme).build())
                .build();

        // Create containers
        GroceryListContainer todo = GroceryListContainer.builder()
                .id(new ObjectId().toHexString() + GroceryContainerType.TODO)
                .collapsedLists(new ArrayList<>())
                .username(user.getUsername())
                .containerType(GroceryContainerType.TODO)
                .build();
        GroceryListContainer wishlist = GroceryListContainer.builder()
                .id(new ObjectId().toHexString() + GroceryContainerType.WISHLIST)
                .collapsedLists(new ArrayList<>())
                .username(user.getUsername())
                .containerType(GroceryContainerType.WISHLIST)
                .build();
        GroceryListContainer grocery = GroceryListContainer.builder()
                .id(new ObjectId().toHexString() + GroceryContainerType.GROCERY)
                .collapsedLists(new ArrayList<>())
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
        List<GroceryListContainer> newContainers = List.of(todo, wishlist, grocery);
        containerDbRepository.saveAll(newContainers);

        // Return generated token
        Map<String, Object> extraClaims = new LinkedHashMap<>();
        extraClaims.put("name", user.getName());
        extraClaims.put("lastName", user.getLastName());
        extraClaims.put("id", user.getId());
        String jwtToken = jwtService.generateToken(extraClaims, user);

        return UserAuthenticationResponse.builder()
                .user(user)
                .token(jwtToken)
                .status(200)
                .build();
    }

    public UserAuthenticationResponse authenticate(AuthenticationRequest req) throws AccessDeniedException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        User user = userDbRepository.findByUsername(req.getUsername());
        if (user == null) {
            throw new AccessDeniedException("User Not Found");
        }
        if (user.getUserPreferences() == null) {
            user.setUserPreferences(UserPreferences.builder().theme(ThemeType.lightTheme).build());
        }

        Map<String, Object> extraClaims = new LinkedHashMap<>();
        extraClaims.put("name", user.getName());
        extraClaims.put("lastName", user.getLastName());
        extraClaims.put("id", user.getId());

        String jwtToken = jwtService.generateToken(extraClaims, user);
        return UserAuthenticationResponse.builder()
                .user(user)
                .status(200)
                .token(jwtToken)
                .build();
    }

    //TODO: Needs implementing
    public Boolean refreshToken(String token) throws Exception {
        if (token == null) throw new AccessDeniedException("No token found.");
        Date expirationDate = jwtService.extractClaim(token, Claims::getExpiration);
        Instant instantToCheck = expirationDate.toInstant();
        Instant currentInstant = Instant.now();
        Duration duration = Duration.between(currentInstant, instantToCheck);
        // refresh if within 10 days
        return duration.toDays() <= 10;
    }

    public UserAuthenticationResponse checkAuthentication(String token) throws Exception {
        String jwtToken = token;
        User user = userDbRepository.findByUsername(jwtService.extractUsername(token));
        if (user == null) {
            throw new AccessDeniedException("User Not Found");
        }
        if (user.getUserPreferences() == null) {
            user.setUserPreferences(UserPreferences.builder().theme(ThemeType.lightTheme).build());
        }
        // Send a refresh token
        if (refreshToken(jwtToken)) {
            log.info("Attempting to refresh token");
            Map<String, Object> extraClaims = new LinkedHashMap<>();
            extraClaims.put("name", user.getName());
            extraClaims.put("lastName", user.getLastName());
            extraClaims.put("id", user.getId());
            jwtToken = jwtService.generateToken(extraClaims, user);
        }

        return UserAuthenticationResponse.builder()
                .user(user)
                .status(200)
                .token(jwtToken)
                .build();
    }

    public Response getUserInfoFromToken(String token) throws AccessDeniedException {
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

    public void ensureAuthorizedSubject(String token, String containerId) throws AccessDeniedException {
        log.debug("CHECKING PRIVATE AUTHORITY");
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

    public void ensureRestrictedSubject(String token, String listId) throws AccessDeniedException {
        log.debug("CHECKING RESTRICTED AUTHORITY");
        // Extract username from jwt
        String username = jwtService.extractUsername(token);
        if (username == null) {
            throw new AccessDeniedException("No username in jwt found.");
        }

        // Ensure username matches the username in the requested container
        GroceryList list = listDbRepository.findListByIdCollapsed(listId);
        log.debug(username);
        if (!list.getPeople().contains(username)) {
            throw new AccessDeniedException("Not an authorized subject to request this asset");
        }
    }

    public Response lookUpUserByUsername(String requesterUsername, String requestedUsername) throws UsernameNotFoundException {
        // TODO: OPTIMIZE BY USING BULK
        User requester = userDbRepository.findByUsername(requesterUsername);
        User requested = userDbRepository.findByUsername(requestedUsername);
        if (requester == null) {
            throw new UsernameNotFoundException("No user matches username: " + requesterUsername);
        }
        if (requested == null) {
            throw new UsernameNotFoundException("No user matches username: " + requestedUsername);
        }

        // Update relevant people
        requester.addRelevantPeople(requested.getUsername());
        userDbRepository.save(requester);

        CollapsedUser collapsedUser = new CollapsedUser(requested.getName(), requested.getUsername(), requested.getLastName());
        return new Response(200, collapsedUser);
    }

    public Response lookUpUserByPhone(String requesterUsername, String requestedPhone) throws UsernameNotFoundException {
        // TODO: OPTIMIZE BY USING BULK
        User requesterUser = userDbRepository.findByUsername(requesterUsername);
        List<User> users = userDbRepository.findByPhone(requestedPhone);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("No user matches username: " + requestedPhone);
        }
        if (requesterUser == null) {
            throw new UsernameNotFoundException("No user matches username: " + requesterUsername);
        }

        // Collapse users and send to client
        List<CollapsedUser> collapsedUsers = new ArrayList<>();
        users.forEach(user -> {
            // Update relevant people
            requesterUser.addRelevantPeople(user.getUsername());
            collapsedUsers.add(new CollapsedUser(user.getName(), user.getUsername(), user.getLastName()));
        });
        users.add(requesterUser);
        userDbRepository.saveAll(users);
        return new Response(200, collapsedUsers);
    }

    public Response fetchSuggestedPeople(String userIdentifier) throws UsernameNotFoundException {
        User user = userDbRepository.findByUsername(userIdentifier);
        if (user == null) {
            throw new UsernameNotFoundException("No user matches username: " + userIdentifier);
        }
        log.debug("Fetching suggested people..." + user.getRelevantUsers().toString());
        // Perform a bulk query to all the suggested users
        Query query = new Query(Criteria.where("username").in(user.getRelevantUsers()));
        List<CollapsedUser> users = new ArrayList<>();
        mongoTemplate.find(query, User.class).forEach(thisuser -> users.add(new CollapsedUser(thisuser.getName(), thisuser.getUsername(), thisuser.getLastName())));
        log.info("Done fetching suggested people..." + users);
        return new Response(200, users);
    }

    public Response updatePhone(String username, String phone) throws UsernameNotFoundException {
        // Fetch user by username
        User user = userDbRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user matches username: " + username);
        }
        user.setPhone(phone);
        userDbRepository.save(user);
        return new Response(200, user);

    }

    public Response updateName(String username, String name, String lastName) throws UsernameNotFoundException {
        // Fetch user by username
        User user = userDbRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user matches username: " + username);
        }
        user.setName(name);
        user.setLastName(lastName);
        userDbRepository.save(user);
        return new Response(200, user);

    }

    public Response updatePassword(String username, String currentPassword, String newPassword) throws UsernameNotFoundException{
        try {
            // Fetch user by username
            User user = userDbRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("No user matches username: " + username);
            }
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, currentPassword));
            user.setPassword(passwordEncoder.encode(newPassword));
            userDbRepository.save(user);
            return new Response(200, user);
        } catch (AuthenticationException e) {
            return new Response(201, null);
        }
    }

    public Response updateEmail(String username, String email) throws UsernameNotFoundException {
        // Fetch user by username
        User user = userDbRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user matches username: " + username);
        }
        user.setEmail(email);
        userDbRepository.save(user);
        return new Response(200, user);
    }

    public Response updateUserPreferences(String username, ThemeType theme) {
        // Fetch user by username
        User user = userDbRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user matches username: " + username);
        }

        if (user.getUserPreferences() == null) {
            user.setUserPreferences(UserPreferences.builder().theme(theme).build());
        }
        else {
            user.getUserPreferences().setTheme(theme);
        }
        userDbRepository.save(user);
        return new Response(200, user.getUserPreferences());
    }
}
