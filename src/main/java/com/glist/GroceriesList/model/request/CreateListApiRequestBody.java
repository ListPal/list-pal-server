package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.groceries.GroceryListRole;

public class CreateListApiRequestBody {
    public String userId;
    public String listName;
    public String containerId;
    public GroceryListRole scope;
}
