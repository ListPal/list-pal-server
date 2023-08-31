package com.glist.GroceriesList.model.response;

import com.glist.GroceriesList.model.user.User;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAuthenticationResponse {
    private User user;
    private int status;
    private String token;
}
