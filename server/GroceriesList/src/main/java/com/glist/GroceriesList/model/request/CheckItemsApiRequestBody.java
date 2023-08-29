package com.glist.GroceriesList.model.request;

import java.util.Set;

public class CheckItemsApiRequestBody {
    public String containerId;
    public String listId;
    public Set<String> itemIds;
}
