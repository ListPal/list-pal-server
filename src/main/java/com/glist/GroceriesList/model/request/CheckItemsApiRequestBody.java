package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.groceries.GroceryListRole;

import java.util.Set;

public class CheckItemsApiRequestBody {
    public String containerId;
    public String listId;
    public Set<String> itemIds;
    public GroceryListRole scope;
}
