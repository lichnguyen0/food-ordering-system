package com.foodorderingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueResponse {
    private List<FoodDTO> foods;
    private boolean hasMore;
    private int currentPage;
}
