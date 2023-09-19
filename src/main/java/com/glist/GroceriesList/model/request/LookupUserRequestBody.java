package com.glist.GroceriesList.model.request;

import com.glist.GroceriesList.model.groceries.GroceryListRole;
import com.glist.GroceriesList.model.user.UserLookupCriteria;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class LookupUserRequestBody {
    @Enumerated(EnumType.STRING)
    public UserLookupCriteria criteria;
    public String requesterUsername;
    public String userIdentifier;
}
