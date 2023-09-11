package com.glist.GroceriesList.repository;

import java.util.List;

import com.glist.GroceriesList.model.groceries.GroceryList;
import com.glist.GroceriesList.model.groceries.GroceryListContainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ContainerDbRepository extends MongoRepository<GroceryListContainer, String> {
    @Query(value="{id:'?0'}")
    GroceryListContainer findContainerByIdExpanded(String id);

    @Query(value="{id:'?0'}")
    GroceryListContainer findContainerByIdCollapsed(String id);

    @Query(value="{username:'?0'}")
    List<GroceryListContainer> findAll(String username);

    @Query("{'collapsedLists.id': ?0}")
    void removeCollapsedListsByListId(String listId);
}