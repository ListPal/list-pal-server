package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.user.CollapsedUser;

public class UpdateListItemApiRequestBody {
    public String id;
    public String containerId;
    public String name;
    public String listId;
    public CollapsedUser user;
    public int quantity;
    public boolean checked;
    public String category;
    public int priority;

}
