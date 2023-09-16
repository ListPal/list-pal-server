package com.glist.GroceriesList.model.user;

import lombok.Data;

@Data
public class CollapsedUser {
    private String name;
    private String lastName;
    private String username;

    public CollapsedUser(final String name, final String username, final String lastName) {
        this.name = name;
        this.username = username;
        this.lastName = lastName;
    }
}
