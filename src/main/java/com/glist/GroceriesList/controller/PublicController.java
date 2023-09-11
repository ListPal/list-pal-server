package com.glist.GroceriesList.controller;

import com.glist.GroceriesList.model.groceries.GroceryListItem;
import com.glist.GroceriesList.model.groceries.GroceryListRole;
import com.glist.GroceriesList.model.request.*;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.user.CollapsedUser;
import com.glist.GroceriesList.service.GroceryListService;
import com.glist.GroceriesList.service.JwtService;
import com.glist.GroceriesList.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/shared")
public class PublicController {
    private final GroceryListService groceryListService;
    private final JwtService jwtService;

    // GET PUBLIC LIST
    @GetMapping("/get-list")
    public ResponseEntity<Response> getList(@RequestParam String containerId, @RequestParam String listId) {
        try {
            // Validate input
            Utils.validateInput(containerId);
            Utils.validateInput(listId);
            // Validated public link
            return ResponseEntity.ok(groceryListService.getList(containerId, listId, GroceryListRole.PUBLIC.name()));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Response res = new Response(400, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e.getMessage());
            Response res = new Response(500, e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // CREATE A LIST ITEM
    @PostMapping(value = "/create-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> createGroceryListItem(@RequestBody CreateListItemApiRequestBody body, @CookieValue(value = "auth-jwt", required = false) String token) {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.name);
            Utils.validateInput(body.quantity);
            String name, username;
            // Derive user info
            if (token != null) {
                name = jwtService.extractClaim(token, claims -> claims.get("name", String.class));
                username = jwtService.extractUsername(token);
            } else {
                name = "unnamed";
                username = "unknown";
            }
            CollapsedUser collapsedUser = new CollapsedUser(name, username);
            // Create item
            GroceryListItem newItem = new GroceryListItem(body.listId, body.name, body.quantity, collapsedUser);
            Response res = groceryListService.createGroceryListItem(body.containerId, newItem, GroceryListRole.PUBLIC.name());
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

    // UPDATE ITEM
    @PostMapping(value = "/update-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> updateListItem(@RequestBody UpdateListItemApiRequestBody body) {
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

    // CHECK ITEM LIST
    @PostMapping(value = "/check-list-items", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> checkItems(@RequestBody CheckItemsApiRequestBody body) {
        try {
            // Validate input
            if (body.itemIds.isEmpty()) return ResponseEntity.ok(new Response(200, "No need to check items"));
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            return ResponseEntity.ok(groceryListService.updateCheckItems(body.containerId, body.listId, body.itemIds, GroceryListRole.PUBLIC.name()));
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


    // DELETE A LIST ITEM
    @DeleteMapping(value = "/delete-list-item", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> deleteListItem(@RequestBody DeleteItemApiRequestBody body) {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.itemId);
            return ResponseEntity.ok(groceryListService.deleteListItem(body.containerId, body.listId, body.itemId, GroceryListRole.PUBLIC.name()));
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

    // SERVER CHECK ENDPOINT
    @PostMapping("/check")
    public ResponseEntity<String> check() {
        return ResponseEntity.ok("Running");
    }

    // ADD PEOPLE TO LIST
    @PostMapping(value = "/add-people", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> addPeopleToList(@RequestBody AddPeopleApiRequestBody body) {
        try {
            log.info("Hit endpoint with body: {");
            log.info("list: " + body.containerId );
            log.info("list: " + body.listId );
            log.info("people: " + body.people);
            log.info("}");
            // TODO: Validate input

            return ResponseEntity.ok(groceryListService.addPeopleToList(body.containerId, body.listId, body.people));
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
}
