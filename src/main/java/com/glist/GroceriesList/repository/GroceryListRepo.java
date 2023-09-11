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
import org.springframework.web.bind.annotation.ResponseBody;

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
        Set<String> peopleSet = new HashSet<>();
        peopleSet.add(username);
        CollapsedList newCollapsedList = CollapsedList.builder()
                .id(newList.getId())
                .listName(newList.getListName())
                .scope(newList.getScope())
                .people(peopleSet)
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
            return new Response(401, "List scope doesn't match your authorization");
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
            return new Response(401, "List scope doesn't match your authorization");
        } else {
            list.deleteItemById(previousItemId); // O(1)
            list.addItem(updatedItem); // O(1)
            listDbRepository.save(list);
            return new Response(200, updatedItem);
        }
    }

    public Response deleteList(String containerId, String listId) throws Exception {
        GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
        if (container == null) {
            return new Response(400, "Could not find the container with id: " + containerId);
        }

        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        if (list == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else {
            // Update container in the container collection
            container.deleteCollapsedListById(listId);
            containerDbRepository.save(container);

            // Delete list from the list collection
            listDbRepository.delete(list);
        }
        return new Response(200, "List deleted successfully");
    }

    public Response deleteRestrictedList(String containerId, String listId) throws Exception {
        log.info("TRYING TO DELETE RESTRICTED LIST");
        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        if (list == null) {
            log.info("NO LIST FOUND");
            return new Response(400, "No list was found that matches id: " + listId);
        }

        if (!list.getContainerId().equals(containerId)) { // Not the owner of the list
            log.info("NOT THE OWNER OF THE LIST");
            // Delete reference to list
            GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
            if (container == null) {
                return new Response(400, "Could not find the container with id: " + containerId);
            }
            container.deleteCollapsedListById(listId);
            list.removePeopleFromList(List.of(container.getUsername()));
            listDbRepository.save(list);
            containerDbRepository.save(container);
        } else { // Owner of the list
            Set<String> people = list.getPeople();
            GroceryContainerType containerType = Utils.inferTypeByContainerId(containerId);
            // Delete all collapsed lists that match
            CollapsedList collapsedList = CollapsedList.builder()
                    .id(list.getId())
                    .scope(list.getScope())
                    .people(list.getPeople())
                    .listName(list.getListName())
                    .build();
            Query query = new Query(Criteria.where("username").in(people).and("containerType").is(containerType));
            Update update = new Update().pull("collapsedLists",  collapsedList);

            // Delete all references to the list
            mongoTemplate.updateMulti(query, update, GroceryListContainer.class);
            // Delete list from the list collection
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
            return new Response(401, "List scope doesn't match your authorization");
        } else {
            // Delete list item from list
            list.deleteItemById(itemId);
            // Save to database
            listDbRepository.save(list);
            return new Response(200, "Item deleted successfully");
        }
    }

    @ResponseBody
    public Response fetchAllLists(String containerId) throws Exception {
        GroceryListContainer container = containerDbRepository.findContainerByIdCollapsed(containerId);
        if (container == null) {
            log.error("Not container was found that matches id: " + containerId);
            return new Response(400, "Not container was found that matches id: " + containerId);
        }
        return new Response(200, container);
    }

    @ResponseBody
    public Response fetchList(String containerId, String listId, String scope) throws Exception {
        // Get list
        GroceryList list = listDbRepository.findListByIdExpanded(listId);
        if (list == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "List doesn't belong to the provided container id: " + containerId);
        } else if (!list.getScope().name().equals(scope)) {
            return new Response(401, "List scope doesn't match your authorization");
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
            return new Response(401, "List scope doesn't match your authorization");
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
        CollapsedList collapsedList = container.getCollapsedListById(listId);
        if (list == null || collapsedList == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!list.getContainerId().equals(containerId)) {
            return new Response(401, "No list was found that matches container id: " + containerId);
        } else {
            list.setListName(listName);
            list.setScope(scope);
            container.deleteCollapsedListById(listId);
            container.getCollapsedLists().add(CollapsedList.builder()
                    .listName(listName)
                    .scope(scope)
                    .id(collapsedList.getId())
                    .people(collapsedList.getPeople())
                    .build());
            listDbRepository.save(list);
            containerDbRepository.save(container);
            return new Response(200, "Success");
        }
    }

    public Response addPeopleToList(String containerId, String listId, List<String> people) throws Exception {
        GroceryContainerType containerType = Utils.inferTypeByContainerId(containerId);
        // Update people in list
        GroceryList groceryList = listDbRepository.findListByIdExpanded(listId);
        if (groceryList == null) {
            return new Response(400, "No list was found that matches id: " + listId);
        } else if (!groceryList.getContainerId().equals(containerId)) {
            return new Response(400, "No list was found that matches container id: " + containerId);
        } else {
            groceryList.addPeopleToList(people);
        }

        // Find containers (bulk) query
        Query query = new Query(Criteria.where("username").in(people).and("containerType").is(containerType));

        // Create an update operation to push the collapsedList to the existing collapsedLists array.
        CollapsedList collapsedList = CollapsedList.builder()
                .id(groceryList.getId())
                .scope(groceryList.getScope())
                .people(groceryList.getPeople())
                .listName(groceryList.getListName())
                .build();
        Update update = new Update().addToSet("collapsedLists", collapsedList);

        // Perform the update operation on multiple documents.
        mongoTemplate.updateMulti(query, update, GroceryListContainer.class);

        // Save groceryList
        listDbRepository.save(groceryList);

        return new Response(200, "OK");
    }
}
