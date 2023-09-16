package com.glist.GroceriesList.service;

import com.glist.GroceriesList.model.response.Response;
import com.glist.GroceriesList.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;

    public Response lookupUserByUsername(String username) throws UsernameNotFoundException  {
        return userRepo.lookUpUserByUsername(username);
    }

    public Response lookupUserByPhone(String phone) throws UsernameNotFoundException {
        return userRepo.lookUpUserByPhone(phone);
    }
}
