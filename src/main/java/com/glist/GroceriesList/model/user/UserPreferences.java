package com.glist.GroceriesList.model.user;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class UserPreferences {
    @Enumerated(EnumType.STRING)
    private ThemeType theme;
}
