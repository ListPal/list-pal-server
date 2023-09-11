package com.glist.GroceriesList.utils;

import com.glist.GroceriesList.model.groceries.GroceryContainerType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {
    public static void validateInput(String input) throws  IllegalArgumentException{
        if (input == null || input.equals("null")) {
            throw new IllegalArgumentException("Argument cannot be null");
        }
        if (input.length() > 100) {
            throw new IllegalArgumentException("Size exceeds server limit.");
        }
        if (input.isBlank() || input.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be empty");
        }
    }
    public static void validateInput(int input) {
        if (input < 0) {
            throw new IllegalArgumentException("Argument cannot be negative");
        }
        if (input > 10000) {
            throw new IllegalArgumentException("Argument cannot be greater than 10, 000");
        }

    }

    public static GroceryContainerType inferTypeByContainerId(String containerId) throws Exception {
        if (containerId.contains(GroceryContainerType.GROCERY.name())) {
           return GroceryContainerType.GROCERY;
        } else if (containerId.contains(GroceryContainerType.TODO.name())) {
            return GroceryContainerType.TODO;
        } else if (containerId.contains(GroceryContainerType.WISHLIST.name())) {
            return GroceryContainerType.WISHLIST;
        } else {
            throw new Exception("No grocery list type found");
        }
    }
}
