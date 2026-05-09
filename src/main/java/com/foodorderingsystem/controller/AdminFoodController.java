package com.foodorderingsystem.controller;

import com.foodorderingsystem.dto.FoodDTO;
import com.foodorderingsystem.mapper.FoodMapper;
import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.model.FoodStatus;
import com.foodorderingsystem.repository.CategoryRepository;
import com.foodorderingsystem.repository.FoodRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/admin/foods")
public class AdminFoodController {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;
    private final FoodMapper foodMapper;
    private final com.foodorderingsystem.service.FileService fileService;

    public AdminFoodController(FoodRepository foodRepository, 
                               CategoryRepository categoryRepository,
                               FoodMapper foodMapper,
                               com.foodorderingsystem.service.FileService fileService) {
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
        this.foodMapper = foodMapper;
        this.fileService = fileService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("foods", foodRepository.findAll());
        return "admin/food-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("foodDTO", new FoodDTO());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statuses", FoodStatus.values());
        return "admin/food-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Food food = foodRepository.findById(id).orElseThrow();
        model.addAttribute("foodDTO", foodMapper.toDTO(food));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statuses", FoodStatus.values());
        return "admin/food-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("foodDTO") FoodDTO foodDTO, 
                       BindingResult result, 
                       @RequestParam(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
                       Model model) throws java.io.IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("statuses", FoodStatus.values());
            return "admin/food-form";
        }
        
        // Handle file upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileService.saveImage(imageFile);
            foodDTO.setImage(imagePath);
        }
        
        Food food = foodMapper.toEntity(foodDTO);
        foodRepository.save(food);
        
        return "redirect:/admin/foods";
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        foodRepository.deleteById(id);
        return "redirect:/admin/foods";
    }
}
