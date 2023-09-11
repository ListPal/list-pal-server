package com.glist.GroceriesList.model.request;


import com.glist.GroceriesList.model.groceries.GroceryListItem;
import com.glist.GroceriesList.model.groceries.GroceryListRole;
import lombok.ToString;

@ToString
public class CreateListItemApiRequestBody {
    public String id;
    public String containerId;
    public String name;
    public String listId;
    public String user;
    public String username;
    public int quantity;
    public GroceryListRole scope;
}
