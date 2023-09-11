package com.glist.GroceriesList.repository;

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
import com.glist.GroceriesList.service.JwtService;
import com.mongodb.MongoWriteException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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


    public UserAuthenticationResponse checkAuthentication(String token) throws Exception{
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

    public Response getUserInfoFromToken(String token) throws AccessDeniedException{
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

    public void ensureAuthorizedSubject(String token, String containerId) throws AccessDeniedException{
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
        // Extract username from jwt
        String username = jwtService.extractUsername(token);
        if (username == null) {
            throw new AccessDeniedException("No username in jwt found.");
        }

        // Ensure username matches the username in the requested container
        CollapsedList list = listDbRepository.findPeopleByListId(listId);
        if (!list.getPeople().contains(username)) {
            throw new AccessDeniedException("Not an authorized subject to request this asset");
        }
    }
}
