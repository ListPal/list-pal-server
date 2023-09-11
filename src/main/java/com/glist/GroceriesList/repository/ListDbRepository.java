package com.glist.GroceriesList.repository;

import com.glist.GroceriesList.model.groceries.CollapsedList;
import com.glist.GroceriesList.model.groceries.GroceryList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ListDbRepository extends MongoRepository<GroceryList, String> {
    @Query(value="{id:'?0'}")
    GroceryList findListByIdExpanded(String id);

    @Query(value="{id:'?0'}", fields="{'groceryListItems':0}")
    GroceryList findListByIdCollapsed(String id);

    @Query(value="{id:'?0'}")
    List<GroceryList> findAll(String userId);
}
