package com.glist.GroceriesList.Utils;

import java.util.Set;

public class Utils {
    public static void validateInput(String input) {
        if (input == null) {
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
}
