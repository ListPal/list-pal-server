package com.glist.GroceriesList.model.user;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Builder
@Document("users")
public class User implements UserDetails {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;
    private String name;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String todoContainerId;
    private String wishlistContainerId;
    private String groceryContainerId;
    private LinkedList<String> relevantUsers;
    private UserPreferences userPreferences;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void addRelevantPeople(String username) {
        // If list contains the user, pop it and add to front
        relevantUsers.remove(username);

        // If list is at capacity use FIFO as the replacement policy
        if (relevantUsers.size() >= 10) {
            relevantUsers.removeLast();
        }

        // Add user to the beginning list
        relevantUsers.addFirst(username);
    }
}
