package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.groceries.CollapsedList;

import java.util.List;

public class ReorderCollapsedListsRequestBody {
    public String containerId;
    public List<CollapsedList> collapsedLists;
}
