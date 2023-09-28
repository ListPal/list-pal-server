package com.glist.GroceriesList.model.groceries;

import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class CollapsedList {
    private final String listName;
    private final String id;
    private GroceryListRole scope;
    private final String reference;
    private Integer order;
}
