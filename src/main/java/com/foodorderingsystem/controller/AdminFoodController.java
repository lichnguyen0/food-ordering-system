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
    private final com.foodorderingsystem.repository.RestaurantRepository restaurantRepository;

    public AdminFoodController(FoodRepository foodRepository, 
                               CategoryRepository categoryRepository,
                               FoodMapper foodMapper,
                               com.foodorderingsystem.service.FileService fileService,
                               com.foodorderingsystem.repository.RestaurantRepository restaurantRepository) {
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
        this.foodMapper = foodMapper;
        this.fileService = fileService;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        java.util.List<Food> foods;
        if (keyword != null && !keyword.isEmpty()) {
            foods = foodRepository.search(keyword);
        } else {
            foods = foodRepository.findAll();
        }

        java.util.List<FoodDTO> foodDTOs = foods.stream()
                .map(foodMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("foods", foodDTOs);
        model.addAttribute("keyword", keyword);
        return "admin/food-list";
    }

    @GetMapping("/search-ajax")
    public String searchAjax(@RequestParam(required = false) String keyword, Model model) {
        java.util.List<Food> foods = (keyword != null && !keyword.isEmpty()) 
                ? foodRepository.search(keyword) 
                : foodRepository.findAll();

        java.util.List<FoodDTO> foodDTOs = foods.stream()
                .map(foodMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("foods", foodDTOs);
        return "admin/food-list :: foodTableFragment";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("foodDTO", new FoodDTO());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        model.addAttribute("statuses", FoodStatus.values());
        return "admin/food-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Food food = foodRepository.findById(id).orElseThrow();
        model.addAttribute("foodDTO", foodMapper.toDTO(food));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        model.addAttribute("statuses", FoodStatus.values());
        return "admin/food-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("foodDTO") FoodDTO foodDTO,
                       BindingResult result,
                       @RequestParam(value = "imageFiles", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> imageFiles,
                       @RequestParam(value = "action", required = false) String action,
                       org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
                       Model model) throws java.io.IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("restaurants", restaurantRepository.findAll());
            model.addAttribute("statuses", FoodStatus.values());
            return "admin/food-form";
        }
        
        java.util.List<String> uploadedImages = new java.util.ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    uploadedImages.add(fileService.saveImage(file));
                }
            }
        }

        // Set primary image if available
        if (!uploadedImages.isEmpty()) {
            foodDTO.setImage(uploadedImages.get(0));
            
            // Set additional images if any
            if (uploadedImages.size() > 1) {
                foodDTO.setAdditionalImages(uploadedImages.subList(1, uploadedImages.size()));
            }
        }
        
        Food food = foodMapper.toEntity(foodDTO);
        foodRepository.save(food);

        redirectAttributes.addFlashAttribute("success", "Food item saved successfully!");

        // Phân biệt hành động dựa trên nút nhấn
        if ("save_and_add".equals(action)) {
            return "redirect:/admin/foods/add";  // Về form thêm mới
        } else {
            return "redirect:/admin/foods";  // Về danh sách
        }
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        foodRepository.deleteById(id);
        return "redirect:/admin/foods";
    }

    @GetMapping("/grid")
    public String grid(@RequestParam(required = false) Long categoryId, Model model) {
        java.util.List<Food> foods;
        if (categoryId != null) {
            foods = foodRepository.findByCategory_CategoryId(categoryId);
        } else {
            foods = foodRepository.findAll();
        }
        
        java.util.List<FoodDTO> foodDTOs = foods.stream()
                .map(foodMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("foods", foodDTOs);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        return "admin/food-grid";
    }
}
