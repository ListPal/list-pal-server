package com.glist.GroceriesList.controller;

import com.glist.GroceriesList.utils.Utils;
import com.glist.GroceriesList.model.groceries.GroceryListItem;
import com.glist.GroceriesList.model.groceries.GroceryListRole;
import com.glist.GroceriesList.model.request.*;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.user.CollapsedUser;
import com.glist.GroceriesList.service.AuthenticationService;
import com.glist.GroceriesList.service.GroceryListService;
import com.glist.GroceriesList.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/server")
public class PrivateController {
    private final GroceryListService groceryListService;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    // GET ALL LIST
    @ResponseBody
    @GetMapping("/get-lists")
    public ResponseEntity<Response> getLists(@RequestParam String containerId, @CookieValue("auth-jwt") String authCookie) throws Exception {
        try {
            // Validate input
            Utils.validateInput(containerId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensurePrivateSubject(authCookie, containerId);
            Response res = groceryListService.getAllLists(containerId);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET A SINGLE LIST
    @ResponseBody
    @PostMapping(value = "/get-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> getList(@RequestBody GetListApiRequestBody body, @CookieValue("auth-jwt") String authCookie) throws Exception {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            if (body.scope.equals(GroceryListRole.PRIVATE))
                authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            else if (body.scope.equals(GroceryListRole.RESTRICTED))
                authenticationService.ensureRestrictedSubject(authCookie, body.listId);
            else throw new AccessDeniedException("List scope doesn't match your authorization");
            Response res = groceryListService.getList(body.containerId, body.listId, body.scope.name());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            Response res = new Response(401, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // CREATE A LIST
    @PostMapping(value = "/create-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> createList(@RequestBody CreateListApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.userId);
            Utils.validateInput(body.listName);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested object
            authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            Response res = groceryListService.createList(body.containerId, body.listName.trim(), body.scope.name(), jwtService.extractUsername(authCookie));
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CREATE A LIST ITEM
    @PostMapping(value = "/create-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> createGroceryListItem(@RequestBody CreateListItemApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.name);
            Utils.validateInput(body.quantity);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            if (body.scope.equals(GroceryListRole.PRIVATE))
                authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            else if (body.scope.equals(GroceryListRole.RESTRICTED))
                authenticationService.ensureRestrictedSubject(authCookie, body.listId);
            else throw new AccessDeniedException("List scope doesn't match your authorization");
            // Create user from token
            String name = jwtService.extractClaim(authCookie, claims -> claims.get("name", String.class));
            String username = jwtService.extractUsername(authCookie);
            CollapsedUser collapsedUser = new CollapsedUser(name, username);
            // Create item
            GroceryListItem newItem = new GroceryListItem(body.listId, body.name, body.quantity, collapsedUser);
            Response res = groceryListService.createGroceryListItem(body.containerId, newItem, body.scope.name());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            Response res = new Response(401, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // UPDATE A LIST ITEM
    @PostMapping(value = "/update-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> updateListItem(@RequestBody UpdateListItemApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            // Validate input
            Utils.validateInput(body.id);
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.name);
            Utils.validateInput(body.category);
            Utils.validateInput(body.user.getUsername());
            Utils.validateInput(body.user.getName());
            Utils.validateInput(body.quantity);
            Utils.validateInput(body.priority);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            if (body.scope.equals(GroceryListRole.PRIVATE))
                authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            else if (body.scope.equals(GroceryListRole.RESTRICTED))
                authenticationService.ensureRestrictedSubject(authCookie, body.listId);
            else throw new AccessDeniedException("List scope doesn't match your authorization");
            // Create Collapsed user
            CollapsedUser collapsedUser = new CollapsedUser(body.user.getName().trim(), body.user.getUsername().trim());
            // Create new item
            GroceryListItem newItem = new GroceryListItem(body.listId, body.name.trim(), body.quantity, collapsedUser);
            newItem.setCategory(body.category.trim());
            newItem.setChecked(body.checked);
            newItem.setPriority(body.priority);
            // Call the service to update item
            Response res = groceryListService.updateGroceryListItem(body.containerId, newItem, body.id, body.scope.name());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            Response res = new Response(401, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CHECK ITEM LIST
    @PostMapping(value = "/check-list-items", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> checkItems(@RequestBody CheckItemsApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            // Validate input
            if (body.itemIds.isEmpty()) return ResponseEntity.ok(new Response(200, "No need to check items"));
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.scope.name());
            return ResponseEntity.ok(groceryListService.updateCheckItems(body.containerId, body.listId, body.itemIds, body.scope.name()));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            Response res = new Response(401, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // UPDATE LIST NAME
    @PostMapping(value = "/update-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> updateListName(@RequestBody UpdateLisNameApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.listName);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.updateList(body.containerId, body.listId, body.listName, body.scope));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            Response res = new Response(401, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ADD PEOPLE TO LIST
    @PostMapping(value = "/add-people", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> addPeopleToList(@RequestBody AddPeopleApiRequestBody body) {
        try {
            // TODO: Validate input

            return ResponseEntity.ok(groceryListService.addPeopleToList(body.containerId, body.listId, body.people));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            Response res = new Response(401, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE A LIST
    @DeleteMapping(value = "/delete-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> deleteList(@RequestBody GetListApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            log.info("DELETE: " + body.containerId);
            log.info("DELETE: " + body.listId);
            log.info("DELETE: " + body.scope);
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            if (body.scope.equals(GroceryListRole.RESTRICTED)) {
                // Ensure authorized subject for the requested asset
                authenticationService.ensureRestrictedSubject(authCookie, body.listId);
                return ResponseEntity.ok(groceryListService.deleteRestrictedList(body.containerId, body.listId));
            }
            // Ensure authorized subject for the requested asset
            authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.deleteList(body.containerId, body.listId));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            Response res = new Response(401, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE A LIST ITEM
    @DeleteMapping(value = "/delete-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> deleteListItem(@RequestBody DeleteItemApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.itemId);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            if (body.scope.equals(GroceryListRole.PRIVATE))
                authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            else if (body.scope.equals(GroceryListRole.RESTRICTED))
                authenticationService.ensureRestrictedSubject(authCookie, body.listId);
            else throw new AccessDeniedException("List scope doesn't match your authorization");
            return ResponseEntity.ok(groceryListService.deleteListItem(body.containerId, body.listId, body.itemId, body.scope.name()));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());
            Response res = new Response(401, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}