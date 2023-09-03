package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.groceries.GroceryListRole;

import java.util.List;

public class UpdateLisNameApiRequestBody {
    public String containerId;
    public String listId;
    public String listName;
    public GroceryListRole scope;
    public List<String> people;

}
