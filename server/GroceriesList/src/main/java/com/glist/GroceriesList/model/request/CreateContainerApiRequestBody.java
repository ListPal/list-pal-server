package com.glist.GroceriesList.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateContainerApiRequestBody {
    private String username;
}
