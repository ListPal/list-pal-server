package com.glist.GroceriesList.controller;

import com.glist.GroceriesList.Utils.Utils;
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
            /// Validate input
            Utils.validateInput(containerId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensureAuthorizedSubject(authCookie, containerId);
            Response res = groceryListService.getAllLists(containerId);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
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
            // Ensure authorized subject for the requested asset
            authenticationService.ensureAuthorizedSubject(authCookie, body.containerId);
            Response res = groceryListService.getList(body.containerId, body.listId, GroceryListRole.PRIVATE.name());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

    }

    // CREATE A LIST
    @PostMapping(value = "/create-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> createList(@RequestBody CreateListApiRequestBody body, @CookieValue("auth-jwt") String authCookie) throws Exception {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.userId);
            Utils.validateInput(body.listName);
            // Ensure authorized subject for the requested object
            authenticationService.ensureAuthorizedSubject(authCookie, body.containerId);
            Response res = groceryListService.createList(body.containerId, body.listName.trim(), body.scope);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // CREATE A LIST ITEM
    @PostMapping(value = "/create-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> createGroceryListItem(@RequestBody CreateListItemApiRequestBody body, @CookieValue("auth-jwt") String authCookie) throws Exception {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.name);
            Utils.validateInput(body.quantity);
            // Ensure authorized subject for the requested asset
            authenticationService.ensureAuthorizedSubject(authCookie, body.containerId);
            // Get user info from token
            String name = jwtService.extractClaim(authCookie, claims -> claims.get("name", String.class));
            String username = jwtService.extractUsername(authCookie);
            CollapsedUser collapsedUser = new CollapsedUser(name, username);
            // Create item
            GroceryListItem newItem = new GroceryListItem(body.listId, body.name, body.quantity, collapsedUser);
            Response res = groceryListService.createGroceryListItem(body.containerId, newItem, GroceryListRole.PRIVATE.name());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // UPDATE A LIST ITEM
    @PostMapping(value = "/update-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> updateListItem(@RequestBody UpdateListItemApiRequestBody body, @CookieValue("auth-jwt") String authCookie) throws Exception {
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
            // Ensure authorized subject for the requested asset
            authenticationService.ensureAuthorizedSubject(authCookie, body.containerId);
            // Create Collapsed user
            CollapsedUser collapsedUser = new CollapsedUser(body.user.getName().trim(), body.user.getUsername().trim());
            // Create new item
            GroceryListItem newItem = new GroceryListItem(body.listId, body.name.trim(), body.quantity, collapsedUser);
            newItem.setCategory(body.category.trim());
            newItem.setChecked(body.checked);
            newItem.setPriority(body.priority);
            // Call the service to update item
            Response res = groceryListService.updateGroceryListItem(body.containerId, newItem, body.id, GroceryListRole.PRIVATE.name());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // CHECK ITEM LIST
    @PostMapping(value = "/check-list-items", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> checkItems(@RequestBody CheckItemsApiRequestBody body, @CookieValue("auth-jwt") String authCookie) throws Exception {
        try {
            // Validate input
            if (body.itemIds.isEmpty()) return ResponseEntity.ok(new Response(200, "No need to check items"));
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensureAuthorizedSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.updateCheckItems(body.containerId, body.listId, body.itemIds, GroceryListRole.PRIVATE.name()));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // UPDATE LIST NAME
    @PostMapping(value = "/update-list-name", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> updateListName(@RequestBody UpdateLisNameApiRequestBody body, @CookieValue("auth-jwt") String authCookie) throws Exception {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.listName);
            // Ensure authorized subject for the requested asset
            authenticationService.ensureAuthorizedSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.updateListName(body.containerId, body.listId, body.listName));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // DELETE A LIST
    @DeleteMapping(value = "/delete-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> deleteList(@RequestBody GetListApiRequestBody body, @CookieValue("auth-jwt") String authCookie) throws Exception {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensureAuthorizedSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.deleteList(body.containerId, body.listId));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // DELETE A LIST ITEM
    @DeleteMapping(value = "/delete-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> deleteListItem(@RequestBody DeleteItemApiRequestBody body, @CookieValue("auth-jwt") String authCookie) throws Exception {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.itemId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensureAuthorizedSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.deleteListItem(body.containerId, body.listId, body.itemId, GroceryListRole.PRIVATE.name()));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }
}
