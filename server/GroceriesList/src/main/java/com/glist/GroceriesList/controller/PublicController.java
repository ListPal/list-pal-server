package com.glist.GroceriesList.controller;

import com.glist.GroceriesList.model.groceries.GroceryListItem;
import com.glist.GroceriesList.model.groceries.GroceryListRole;
import com.glist.GroceriesList.model.request.*;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.user.CollapsedUser;
import com.glist.GroceriesList.service.GroceryListService;
import com.glist.GroceriesList.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/shared")
public class PublicController {
    private  final GroceryListService groceryListService;
    private final JwtService jwtService;

    // GET PUBLIC LIST
    @GetMapping("/get-list")
    public ResponseEntity<Response> getList(@RequestParam String containerId, @RequestParam String listId, @RequestParam String cx) throws Exception {
        // Input validation
        if (containerId == null || listId == null) {
            Response res = new Response(400, "Insufficient data passed to server");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        // Validated public link
        return ResponseEntity.ok(groceryListService.getList(containerId, listId, GroceryListRole.PUBLIC.name()));
    }

    // CREATE A LIST ITEM
    @PostMapping(value = "/create-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> createGroceryListItem(@RequestBody CreateListItemApiRequestBody body, @CookieValue(value = "auth-jwt", required = false) String token) throws Exception {
        CollapsedUser collapsedUser;
        String name;
        String username;
        // Validate input
        if (body.containerId == null || body.listId == null || body.name == null) {
            Response res = new Response(400, "Insufficient data passed to server");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        // Derive user info
        if (token != null) {
            name = jwtService.extractClaim(token, claims -> claims.get("name", String.class));
            username = jwtService.extractUsername(token);
        } else {
            name = "unnamed";
            username = "unknown";
        }
        collapsedUser = new CollapsedUser(name, username);

        // Create item
        GroceryListItem newItem = new GroceryListItem(body.listId, body.name, body.quantity, collapsedUser);
        Response res = groceryListService.createGroceryListItem(body.containerId, newItem, GroceryListRole.PUBLIC.name());
        return ResponseEntity.ok(res);
    }

    // UPDATE ITEM
    @PostMapping(value = "/update-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> updateListItem(@RequestBody UpdateListItemApiRequestBody body) throws Exception {
        if (body.containerId == null || body.listId == null || body.id == null || body.name == null || body.category == null || body.user.getUsername() == null || body.user.getName() == null) {
            Response res = new Response(400, "Insufficient data passed to server");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        // Create Collapsed user
        CollapsedUser collapsedUser = new CollapsedUser(body.user.getName(), body.user.getUsername());
        // Create new item
        GroceryListItem newItem = new GroceryListItem(body.listId, body.name, body.quantity, collapsedUser);
        newItem.setCategory(body.category);
        newItem.setChecked(body.checked);
        newItem.setPriority(body.priority);
        // Call the service to update item
        Response res = groceryListService.updateGroceryListItem(body.containerId, newItem, body.id, GroceryListRole.PUBLIC.name());
        return ResponseEntity.ok(res);
    }

    // CHECK ITEM LIST
    @PostMapping(value = "/check-list-items", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> checkItems(@RequestBody CheckItemsApiRequestBody body) throws Exception {
        if (body.containerId == null || body.listId == null) {
            Response res = new Response(400, "Insufficient data passed to server");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(groceryListService.updateCheckItems(body.containerId, body.listId, body.itemIds, GroceryListRole.PUBLIC.name()));
    }

    // DELETE A LIST ITEM
    @DeleteMapping(value = "/delete-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> deleteListItem(@RequestBody DeleteItemApiRequestBody body) throws Exception {
        // Validate input
        if (body.containerId == null || body.listId == null || body.itemId == null) {
            Response res = new Response(400, "Insufficient data passed to server");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(groceryListService.deleteListItem(body.containerId, body.listId, body.itemId, GroceryListRole.PUBLIC.name()));
    }

    @GetMapping("/check")
    public String check() {
        return "Running";
    }
}
