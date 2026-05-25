package com.foodorderingsystem.controller.api;

import com.foodorderingsystem.model.option.OptionGroup;
import com.foodorderingsystem.repository.food.FoodRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/food")
public class FoodApiController {

    private final FoodRepository foodRepository;

    public FoodApiController(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @GetMapping("/{id}/options")
    public ResponseEntity<List<OptionGroup>> getFoodOptions(@PathVariable Long id) {
        return foodRepository.findById(id)
                .map(food -> ResponseEntity.ok(food.getOptionGroups()))
                .orElse(ResponseEntity.notFound().build());
    }
}
