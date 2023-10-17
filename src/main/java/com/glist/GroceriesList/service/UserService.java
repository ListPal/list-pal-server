package com.glist.GroceriesList.service;

import com.glist.GroceriesList.model.groceries.GroceryListRole;
import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.model.user.ThemeType;
import com.glist.GroceriesList.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;

    public Response lookupUserByUsername(String requesterUsername, String requestedUsername) throws UsernameNotFoundException {
        return userRepo.lookUpUserByUsername(requesterUsername, requestedUsername);
    }

    public Response lookupUserByPhone(String requesterUsername, String phone) throws UsernameNotFoundException {
        return userRepo.lookUpUserByPhone(requesterUsername, phone);
    }

    public Response fetchSuggestedPeople(String userIdentifier) throws UsernameNotFoundException {
        return userRepo.fetchSuggestedPeople(userIdentifier);
    }

    public Response updateEmail(String username, String email) throws UsernameNotFoundException {
        return userRepo.updateEmail(username, email);
    }

    public Response updatePassword(String username, String currentPassword, String newPassword) throws UsernameNotFoundException {
        return userRepo.updatePassword(username, currentPassword, newPassword);
    }

    public Response updateName(String username, String name, String lastName) {
        return userRepo.updateName(username, name, lastName);
    }

    public Response updatePhone(String username, String phone) {
        return userRepo.updatePhone(username, phone);
    }

    public Response updateUserPreferences(String username, ThemeType theme) {
        return userRepo.updateUserPreferences(username, theme);
    }
}
