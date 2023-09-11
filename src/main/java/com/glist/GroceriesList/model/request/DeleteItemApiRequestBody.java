package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.groceries.GroceryListRole;

public class DeleteItemApiRequestBody {
    public String itemId;
    public String listId;
    public String containerId;
    public GroceryListRole scope;
}
