package com.glist.GroceriesList.service;

import com.glist.GroceriesList.model.groceries.GroceryListRole;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;

    public Response lookupUserByUsername(String requesterUsername, String requestedUsername) throws UsernameNotFoundException  {
        return userRepo.lookUpUserByUsername(requesterUsername, requestedUsername);
    }

    public Response lookupUserByPhone(String requesterUsername, String phone) throws UsernameNotFoundException {
        return userRepo.lookUpUserByPhone(requesterUsername, phone);
    }

    public Response fetchSuggestedPeople(String userIdentifier) throws UsernameNotFoundException {
        return userRepo.fetchSuggestedPeople(userIdentifier);
    }
}
