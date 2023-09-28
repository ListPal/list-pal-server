package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.groceries.GroceryListItem;
import com.glist.GroceriesList.model.groceries.GroceryListRole;

import java.util.List;
import java.util.Set;

public class DefineListOrderRequestBody {
    public String containerId;
    public String listId;
    public List<GroceryListItem> items;
    public GroceryListRole scope;
}
