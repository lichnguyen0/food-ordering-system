package com.foodorderingsystem.controller.admin;

import com.foodorderingsystem.model.collection.Collection;
import com.foodorderingsystem.repository.collection.CollectionRepository;
import com.foodorderingsystem.repository.food.FoodRepository;
import com.foodorderingsystem.repository.restaurant.RestaurantRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/collections")
public class CollectionController {

    private final CollectionRepository collectionRepository;
    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;

    public CollectionController(CollectionRepository collectionRepository, 
                                FoodRepository foodRepository,
                                RestaurantRepository restaurantRepository) {
        this.collectionRepository = collectionRepository;
        this.foodRepository = foodRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("collections", collectionRepository.findAll());
        model.addAttribute("activeMenu", "marketing");
        model.addAttribute("activeSub", "list");
        return "admin/collections/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("collection", new Collection());
        model.addAttribute("selectedFoodIds", new java.util.ArrayList<Long>());
        model.addAttribute("allFoods", foodRepository.findAll());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        model.addAttribute("activeMenu", "marketing");
        return "admin/collections/form";
    }



    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {

        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        // Trích xuất danh sách ID món ăn đã chọn để so sánh ở View dễ dàng hơn
        List<Long> selectedFoodIds = collection.getFoods().stream()
                .map(food -> food.getFoodId())
                .collect(Collectors.toList());

        model.addAttribute("collection", collection);
        model.addAttribute("selectedFoodIds", selectedFoodIds);
        model.addAttribute("allFoods", foodRepository.findAll());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        model.addAttribute("activeMenu", "marketing");

        return "admin/collections/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Collection collection, @RequestParam(required = false) List<Long> foodIds) {
        if (foodIds != null) {
            collection.setFoods(foodRepository.findAllById(foodIds));
        }
        collectionRepository.save(collection);
        return "redirect:/admin/collections";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        collectionRepository.deleteById(id);
        return "redirect:/admin/collections";
    }
}
