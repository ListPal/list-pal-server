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
    public ResponseEntity<Response> getLists(@RequestParam String containerId, @CookieValue("auth-jwt") String authCookie) {
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
    public ResponseEntity<Response> getList(@RequestBody GetListApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            log.debug(body.listId);
            log.debug("CONTAINER ID: " + body.containerId);
            log.debug("CONTAINER ID: " + body.scope);
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            authenticationService.ensureRestrictedSubject(authCookie, body.listId);
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
            Utils.validateInput(body.category);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            authenticationService.ensureRestrictedSubject(authCookie, body.listId);
            // Create user from token
            String name = jwtService.extractClaim(authCookie, claims -> claims.get("name", String.class));
            String username = jwtService.extractUsername(authCookie);
            CollapsedUser collapsedUser = new CollapsedUser(name, username, null);
            // Create item
            GroceryListItem newItem = new GroceryListItem(body.listId, body.name, body.category, body.quantity, collapsedUser);
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
            authenticationService.ensureRestrictedSubject(authCookie, body.listId);
            // Create Collapsed user
            CollapsedUser collapsedUser = new CollapsedUser(body.user.getName().trim(), body.user.getUsername().trim(), null);
            // Create new item
            GroceryListItem newItem = new GroceryListItem(body.listId, body.name.trim(), body.category.trim(), body.quantity, collapsedUser);
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
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            authenticationService.ensureRestrictedSubject(authCookie, body.listId);
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

    // GET PEOPLE FROM LIST
    @PostMapping(value = "/get-people-from-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> getPeopleFromList(@RequestBody AddPeopleApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.getPeopleFromList(body.containerId, body.listId));
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
    public ResponseEntity<Response> addPeopleToList(@RequestBody AddPeopleApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensureRestrictedSubject(authCookie, body.listId);
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

    // REDEFINE LIST ORDER
    @PostMapping(value = "/updated-list-order", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> setNewListOrder(@RequestBody DefineListOrderRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensureRestrictedSubject(authCookie, body.listId);
            return ResponseEntity.ok(groceryListService.setNewListOrder(body.containerId, body.listId, body.items, body.scope));
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

    @PostMapping(value = "/update-collapsed-list-order", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> refactorCollapsedLists(@RequestBody ReorderCollapsedListsRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            Utils.validateInput(body.containerId);
            // Ensure authorized subject for the requested asset
            return ResponseEntity.ok(groceryListService.refactoreCollapsedLists(body.containerId, body.collapsedLists));
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

    // DELETE PEOPLE FROM LIST
    @DeleteMapping("/remove-people-from-list")
    public ResponseEntity<Response> removePeopleFromList(@RequestBody AddPeopleApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            // Ensure authorized subject for the requested asset
            authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.removePeopleFromList(body.containerId, body.listId, body.people));
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

    // RESET A LIST
    @DeleteMapping(value = "/delete-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Response> deleteList(@RequestBody GetListApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            log.debug("DELETE: " + body.containerId);
            log.debug("DELETE: " + body.listId);
            log.debug("DELETE: " + body.scope);
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            if (body.scope.equals(GroceryListRole.RESTRICTED)) {
                // Ensure authorized subject for the requested asset
                authenticationService.ensureRestrictedSubject(authCookie, body.listId);
                return ResponseEntity.ok(groceryListService.deleteRestrictedList(body.containerId, body.listId, body.scope));
            }
            // Ensure authorized subject for the requested asset
            authenticationService.ensurePrivateSubject(authCookie, body.containerId);
            return ResponseEntity.ok(groceryListService.deleteList(body.containerId, body.listId, body.scope));
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
            authenticationService.ensureRestrictedSubject(authCookie, body.listId);
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

    @DeleteMapping("/reset-list")
    public ResponseEntity<Response> resetList(@RequestBody GetListApiRequestBody body, @CookieValue("auth-jwt") String authCookie) {
        try {
            // Validate input
            Utils.validateInput(body.containerId);
            Utils.validateInput(body.listId);
            Utils.validateInput(body.scope.name());
            // Ensure authorized subject for the requested asset
            authenticationService.ensureRestrictedSubject(authCookie, body.listId);
            return ResponseEntity.ok(groceryListService.resetList(body.containerId, body.listId, body.scope));
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