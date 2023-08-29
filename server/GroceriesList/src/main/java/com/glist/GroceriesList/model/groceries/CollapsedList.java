package com.glist.GroceriesList.model.groceries;

import lombok.*;

import java.util.List;

@Data
@Builder
public class CollapsedList {
    private final String listName;
    private final String id;
    private GroceryListRole scope;
    private List<String> people;
}
