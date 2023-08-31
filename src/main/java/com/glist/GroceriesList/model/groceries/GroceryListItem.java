package com.glist.GroceriesList.model.groceries;

import com.glist.GroceriesList.model.user.CollapsedUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class GroceryListItem {
    private String id;
    private String listId;
    private String name;
    private String category;
    private int quantity;
    private boolean checked;
    private int priority;
    private CollapsedUser user;

    // constructors
    public GroceryListItem(String listId, String name, int quantity, CollapsedUser user) {
        this.listId = listId;
        this.id = listId + name + System.currentTimeMillis();
        this.name = name;
        this.category = "Misc";
        this.quantity = quantity;
        this.checked = false;
        this.user = user;
    }
}
