package com.glist.GroceriesList.model.user;

import lombok.Builder;
import lombok.Data;

@Data
public class CollapsedUser {
    private String name;
    private String username;

    public CollapsedUser(final String name, final String username) {
        this.name = name;
        this.username = username;
    }
}
