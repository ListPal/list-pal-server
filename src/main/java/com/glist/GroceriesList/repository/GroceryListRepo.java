package com.glist.GroceriesList.repository;

import com.glist.GroceriesList.model.groceries.*;
import com.glist.GroceriesList.model.response.Response;

import com.glist.GroceriesList.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJson;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GroceryListRepo {
    private final ListDbRepository listDbRepository;
    private final ContainerDbRepository containerDbRepository;
    private final MongoTemplate mongoTemplate;

    public Response createList(String containerId, String listName, String scope, String username) throws Exception {
        // Fetch container from database
        GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
        if (container == null) {
            return new Response(400, "Not a valid container id given");
        }

        // Create list and insert into container
        GroceryList newList = new GroceryList(listName, containerId);
        newList.addPeopleToList(List.of(username));
        if (scope != null) {
            newList.setScope(GroceryListRole.valueOf(scope));
        }
        CollapsedList newCollapsedList = CollapsedList.builder()
                .id(newList.getId())
                .listName(newList.getListName())
                .scope(newList.getScope())
                .reference(containerId)
                .build();
        container.addGroceryCollapsedList(newCollapsedList);

        // Send new list to database
        listDbRepository.insert(newList);
        containerDbRepository.save(container);

        return new Response(200, newCollapsedList);
    }

    public Response createGroceryListItem(String containerId, GroceryListItem newItem, String scope) throws Exception {
        GroceryList list = listDbRepository.findListByIdExpanded(newItem.getListId());
        if (list == null) {
            return new Response(400, "No list was found that matches id: " + newItem.getListId());
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else if (!list.getScope().name().equals(scope)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            list.addItem(newItem);
            listDbRepository.save(list);
            return new Response(200, newItem);
        }
    }

    public Response updateGroceryListItem(String containerId, GroceryListItem updatedItem, String previousItemId, String scope) throws Exception {
        GroceryList list = listDbRepository.findListByIdExpanded(updatedItem.getListId());

        if (list == null) {
            return new Response(400, "No list was found that matches id: " + updatedItem.getListId());
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else if (!list.getScope().name().equals(scope)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            list.deleteItemById(previousItemId); // O(1)
            list.addItem(updatedItem); // O(1)
            listDbRepository.save(list);
            return new Response(200, updatedItem);
        }
    }

    public Response deleteList(String containerId, String listId, GroceryListRole scope) throws Exception {
        GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
        if (container == null) {
            return new Response(400, "Could not find the container with id: " + containerId);
        }

        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        if (list == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else if (!list.getScope().equals(scope)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            // Update container in the container collection
            container.deleteCollapsedListById(listId);
            containerDbRepository.save(container);

            // Delete list from the list collection
            listDbRepository.delete(list);
        }
        return new Response(200, "List deleted successfully");
    }

    public Response deleteRestrictedList(String containerId, String listId, GroceryListRole scope) throws Exception {
        log.debug("TRYING TO DELETE RESTRICTED LIST");
        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        if (list == null) {
            log.debug("NO LIST FOUND");
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!list.getScope().equals(scope)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else if (!list.getContainerId().equals(containerId)) {
            log.debug("NOT THE OWNER OF THE LIST");
            // Delete reference to list
            GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
            if (container == null) {
                return new Response(400, "Could not find the container with id: " + containerId);
            }
            container.deleteCollapsedListById(listId);
            list.removePeopleFromList(List.of(container.getUsername()));
            listDbRepository.save(list);
            containerDbRepository.save(container);
        } else {
            log.debug("OWNER OF THE LIST");
            Set<String> people = list.getPeople();
            GroceryContainerType containerType = Utils.inferTypeByContainerId(containerId);
            // Create a copy of the reference to be removed
            CollapsedList collapsedList = CollapsedList.builder()
                    .id(list.getId())
                    .scope(list.getScope())
                    .listName(list.getListName())
                    .reference(containerId)
                    .build();
            // Find containers from each people
            Query query = new Query(Criteria.where("username").in(people).and("containerType").is(containerType));
            // Delete the reference to the list to be removed
            Update update = new Update().pull("collapsedLists", collapsedList);

            // Perform updates
            mongoTemplate.updateMulti(query, update, GroceryListContainer.class);
            listDbRepository.delete(list);

        }
        return new Response(200, "List deleted successfully");
    }

    public Response deleteListItem(String containerId, String listId, String itemId, String scope) throws Exception {
        // Get list
        GroceryList list = listDbRepository.findListByIdExpanded(listId);

        if (list == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else if (!list.getScope().name().equals(scope)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            // Delete list item from list
            list.deleteItemById(itemId);
            // Save to database
            listDbRepository.save(list);
            return new Response(200, "Item deleted successfully");
        }
    }

    public Response fetchAllLists(String containerId) throws Exception {
        GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
        if (container == null) {
            log.error("Not container was found that matches id: " + containerId);
            return new Response(400, "Not container was found that matches id: " + containerId);
        }
        return new Response(200, container);
    }

    public Response fetchList(String containerId, String listId, String scope) throws Exception {
        // Get list
        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        if (list == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "List doesn't belong to the provided container id: " + containerId);
        } else if (!list.getScope().name().equals(scope)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            return new Response(200, list);
        }
    }

    public Response updateCheckItems(String containerId, String listId, Set<String> itemIds, String scope) throws Exception {
        // Get list
        GroceryList list = listDbRepository.findListByIdExpanded(listId);

        if (list == null) {
            return new Response(400, "No list or container was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else if (!list.getScope().name().equals(scope)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            list.updateAllChecked(itemIds);
            listDbRepository.save(list);
            return new Response(200, "Corresponding items checked");
        }
    }

    public Response updateList(String containerId, String listId, String listName, GroceryListRole scope) throws Exception {
        // Get list
        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
        if (list == null) {
            return new Response(401, "No list was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else {
            // If scope is changed, delete all other references to this list
            if (!list.getScope().equals(scope) && list.getPeople().size() > 1) {
                log.debug("ATTEMPTING TO DELETE ALL OTHER REFERENCES TO THIS LIST");
                // Reference that will be deleted
                CollapsedList collapsedList = CollapsedList.builder()
                        .id(list.getId())
                        .scope(list.getScope())
                        .listName(list.getListName())
                        .reference(containerId)
                        .build();
                // Find containers (bulk query) that match people and container type
                Query query = new Query(Criteria.where("username").in(list.getPeople()).and("containerType").is(container.getContainerType()));
                Update update = new Update().pull("collapsedLists", collapsedList);
                // Update all the containers (bulk)
                mongoTemplate.updateMulti(query, update, GroceryListContainer.class);
            }
            // Reset people if changing scope
            if (!list.getScope().equals(scope)) {
                Set<String> people = new HashSet<>();
                people.add(container.getUsername());
                list.setPeople(people);
                list.setScope(scope);
            }
            // Update other parameters
            list.setListName(listName);
            if (container.getCollapsedListById(listId) != null)
                container.deleteCollapsedListById(listId);
            container.getCollapsedLists().add(CollapsedList.builder()
                    .listName(listName)
                    .scope(scope)
                    .id(list.getId())
                    .reference(containerId)
                    .build());
            listDbRepository.save(list);
            containerDbRepository.save(container);
            return new Response(200, list);
        }
    }

    public Response addPeopleToList(String containerId, String listId, List<String> people) throws Exception {
        GroceryContainerType containerType = Utils.inferTypeByContainerId(containerId);
        // Verify list and parent container
        GroceryList groceryList = listDbRepository.findListByIdExpanded(listId);
        if (groceryList == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!groceryList.getContainerId().equals(containerId)) {
            return new Response(400, "No list was found that matches container id: " + containerId);
        } else if (!groceryList.getScope().equals(GroceryListRole.RESTRICTED)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {// Update people in list
            groceryList.addPeopleToList(people);
        }

        // Find containers (bulk) query
        Query query = new Query(Criteria.where("username").in(people).and("containerType").is(containerType));

        // Create a list reference
        CollapsedList collapsedList = CollapsedList.builder()
                .id(groceryList.getId())
                .scope(groceryList.getScope())
                .listName(groceryList.getListName())
                .reference(containerId)
                .build();
        // Create an update operation to push the reference to the existing references array.
        Update update = new Update().addToSet("collapsedLists", collapsedList);

        // Create the list references to all the people's accounts
        mongoTemplate.updateMulti(query, update, GroceryListContainer.class);

        // Save groceryList with the updated people
        listDbRepository.save(groceryList);

        return new Response(200, "OK");
    }

    public Response removePeopleFromList(String containerId, String listId, List<String> people) throws Exception {
        GroceryContainerType containerType = Utils.inferTypeByContainerId(containerId);
        // Verify list and parent container
        GroceryList groceryList = listDbRepository.findListByIdExpanded(listId);
        if (groceryList == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!groceryList.getContainerId().equals(containerId)) {
            return new Response(400, "No list was found that matches container id: " + containerId);
        } else if (!groceryList.getScope().equals(GroceryListRole.RESTRICTED)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            // Update people in list
            groceryList.addPeopleToList(people);

            // Reference that will be deleted
            CollapsedList collapsedList = CollapsedList.builder()
                    .id(groceryList.getId())
                    .scope(groceryList.getScope())
                    .listName(groceryList.getListName())
                    .reference(containerId)
                    .build();
            // Find containers (bulk query) that match people to be removed
            Query query = new Query(Criteria.where("username").in(people).and("containerType").is(containerType));
            Update update = new Update().pull("collapsedLists", collapsedList);
            // Update all the containers (bulk)
            mongoTemplate.updateMulti(query, update, GroceryListContainer.class);

            // Save groceryList with the updated people
            groceryList.removePeopleFromList(people);
            listDbRepository.save(groceryList);

            return new Response(200, "OK");
        }
    }

    public Response getPeopleFromList(String containerId, String listId) throws AccessDeniedException {
        // Get list
        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        if (list == null) {
            return new Response(400, "No list or container was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else if (!list.getScope().equals(GroceryListRole.RESTRICTED)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            return new Response(200, list.getPeople());
        }
    }

    public Response resetList(String containerId, String listId, GroceryListRole scope) throws AccessDeniedException {
        // Get list
        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        if (list == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "List doesn't belong to the provided container id: " + containerId);
        } else if (!list.getScope().equals(scope)) {
            throw new AccessDeniedException("List scope doesn't match your authorization");
        } else {
            list.resetItems();
            listDbRepository.save(list);
            return new Response(200, list);
        }
    }
}
