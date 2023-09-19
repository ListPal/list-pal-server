package com.glist.GroceriesList.model.groceries;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Getter
@Setter
@ToString
@Document("groceryList")
public class GroceryList {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private GroceryListRole scope = GroceryListRole.PRIVATE;
    private String containerId;
    private String listName;
    private LocalDateTime dateCreated;
    private Set<String> people;
    private Set<GroceryListItem> groceryListItems;

    public GroceryList(String listName, String containerId) {
        this.id = containerId + listName.trim() + System.currentTimeMillis();
        this.containerId = containerId;
        this.listName = listName;
        this.groceryListItems = new HashSet<>();
        this.dateCreated = LocalDateTime.now();
        this.people = new HashSet<>();
    }
    public void addPeopleToList(List<String> people) {
        this.people.addAll(people);
    }

    public void removePeopleFromList(List<String> people) {
        people.forEach(this.people::remove);
    }
    public void addItem(GroceryListItem item) {
        this.groceryListItems.add(item);
    }

    public GroceryListItem findItemById(String itemId) {  // O(n)
        return groceryListItems.stream()
                .filter(element -> element.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    public GroceryListItem findItemByName(String itemName) { // O(n)
        return groceryListItems.stream()
                .filter(element -> element.getId().equals(itemName))
                .findFirst()
                .orElse(null);

    }
    public void deleteItemById(String itemId) {
        groceryListItems.removeIf(item -> item.getId().equals(itemId));
    }

    public void deleteItemByName(String itemName) { // O(n)
        groceryListItems.removeIf(item -> item.getName().equals(itemName));
    }
    public boolean removeItem(GroceryListItem item) {
        return this.groceryListItems.remove(item);
    }

    public void updateAllChecked(Set<String> itemIds) { // O(n)
        groceryListItems.forEach(item -> {
            item.setChecked(itemIds.contains(item.getId()));
        });
    }

    public void resetItems() {
        groceryListItems.clear();
    }
}
