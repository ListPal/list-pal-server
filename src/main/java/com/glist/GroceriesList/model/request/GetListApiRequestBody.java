package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.groceries.GroceryListRole;

public class GetListApiRequestBody {
    public String containerId;
    public String listId;
    public GroceryListRole scope;
}
