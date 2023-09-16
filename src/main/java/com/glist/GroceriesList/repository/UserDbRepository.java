package com.glist.GroceriesList.repository;

import com.glist.GroceriesList.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserDbRepository extends MongoRepository<User, String> {
   @Query(value="{id:'?0'}")
   User findUserById(String userId);

   @Query(value="{username:'?0'}")
   User findByUsername(String username);

   @Query(value="{phone:'?0'}")
   List<User> findByPhone(String phone);

}
