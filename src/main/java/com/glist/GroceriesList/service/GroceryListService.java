package com.glist.GroceriesList.service;

import com.glist.GroceriesList.model.groceries.GroceryList;
import com.glist.GroceriesList.model.groceries.GroceryListItem;
import com.glist.GroceriesList.model.groceries.GroceryListRole;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.repository.GroceryListRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class GroceryListService {
    private final GroceryListRepo groceryListRepo;

    @Autowired
    public GroceryListService(GroceryListRepo glRepo) {
        this.groceryListRepo = glRepo;
    }

    public Response createList(String containerId, String listName, String scope, String username) throws Exception {
        return groceryListRepo.createList(containerId, listName, scope, username);
    }

    public Response getList(String containerId, String listId, String scope) throws Exception {
        return groceryListRepo.fetchList(containerId, listId, scope);
    }

    public Response getAllLists(String containerId) throws Exception {
        return groceryListRepo.fetchAllLists(containerId);
    }

    public Response createGroceryListItem(String containerId, GroceryListItem newItem, String scope) throws Exception {
        return groceryListRepo.createGroceryListItem(containerId, newItem, scope);
    }

    public Response updateGroceryListItem(String containerId, GroceryListItem newItem, String previousItemId, String scope) throws Exception {
        return groceryListRepo.updateGroceryListItem(containerId, newItem, previousItemId, scope);
    }

    public Response deleteList(String containerId, String listId) throws Exception {
        return groceryListRepo.deleteList(containerId, listId);
    }

    public Response deleteListItem(String containerId, String listId, String  itemId, String scope) throws Exception {
        return groceryListRepo.deleteListItem(containerId, listId, itemId, scope);
    }

    public Response updateCheckItems(String containerId, String listId, Set<String> itemIds, String scope) throws Exception {
        return groceryListRepo.updateCheckItems(containerId, listId, itemIds, scope);
    }

    public Response updateList(String containerId, String listId, String listName, GroceryListRole scope) throws Exception {
        return groceryListRepo.updateList(containerId, listId, listName, scope);
    }

    public Response addPeopleToList(String containerId, String listId, List<String> people) throws Exception {
        return groceryListRepo.addPeopleToList(containerId, listId, people);
    }

    public Response deleteRestrictedList(String containerId, String listId) throws Exception{
        return groceryListRepo.deleteRestrictedList(containerId, listId);
    }

    public Response removePeopleFromList(String containerId, String listId, List<String> people) throws Exception {
        return groceryListRepo.removePeopleFromList(containerId, listId, people);
    }

    public Response getPeopleFromList(String containerId, String listId) throws Exception{
        return groceryListRepo.getPeopleFromList(containerId, listId);
    }
}
