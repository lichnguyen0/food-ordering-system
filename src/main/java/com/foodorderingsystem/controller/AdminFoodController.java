package com.foodorderingsystem.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

    /**
     * Hiển thị danh sách món ăn.
     * Nếu có keyword thì tìm kiếm theo từ khóa.
     */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       Model model) {

        List<Food> foods;

        if (keyword != null && !keyword.isEmpty()) {
            foods = foodRepository.search(keyword);
        } else {
            foods = foodRepository.findAll();
        }

        List<FoodDTO> foodDTOs = foods.stream()
                .map(foodMapper::toDTO)
                .collect(Collectors.toList());

        model.addAttribute("foods", foodDTOs);
        model.addAttribute("keyword", keyword);

        return "admin/food-list";
    }

    /**
     * Tìm kiếm món ăn bằng AJAX và trả về fragment bảng dữ liệu.
     */
    @GetMapping("/search-ajax")
    public String searchAjax(@RequestParam(required = false) String keyword,
                             Model model) {

        List<Food> foods = (keyword != null && !keyword.isEmpty())
                ? foodRepository.search(keyword)
                : foodRepository.findAll();

        List<FoodDTO> foodDTOs = foods.stream()
                .map(foodMapper::toDTO)
                .collect(Collectors.toList());

        model.addAttribute("foods", foodDTOs);

        return "admin/food-list :: foodTableFragment";
    }

    /**
     * Hiển thị form thêm mới món ăn.
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("foodDTO", new FoodDTO());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        model.addAttribute("statuses", FoodStatus.values());
        return "admin/food-form";
    }

    /**
     * Hiển thị form chỉnh sửa món ăn theo id.
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Food food = foodRepository.findById(id).orElseThrow();
        model.addAttribute("foodDTO", foodMapper.toDTO(food));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        model.addAttribute("statuses", FoodStatus.values());
        return "admin/food-form";
    }

    /**
     * Lưu món ăn mới hoặc cập nhật món ăn.
     * Đồng thời xử lý upload ảnh và validate dữ liệu.
     */


    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("foodDTO") FoodDTO foodDTO,
                       BindingResult result,
                       @RequestParam(value = "imageFiles", required = false)
                       List<MultipartFile> imageFiles,

                       @RequestParam(value = "action", required = false)
                       String action,

                       RedirectAttributes redirectAttributes,
                       Model model) throws IOException {

        // Kiểm tra validate form
        if (result.hasErrors()) {

            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("restaurants", restaurantRepository.findAll());
            model.addAttribute("statuses", FoodStatus.values());

            return "admin/food-form";
        }

        // Danh sách tên file ảnh đã upload
        List<String> uploadedImages = new ArrayList<>();

        // Upload nhiều ảnh
        if (imageFiles != null && !imageFiles.isEmpty()) {

            for (MultipartFile file : imageFiles) {

                if (!file.isEmpty()) {
                    uploadedImages.add(fileService.saveImage(file));
                }
            }
        }

        // Đặt hình ảnh chính nếu có
        if (!uploadedImages.isEmpty()) {

            foodDTO.setImage(uploadedImages.get(0));

            // Đặt hình ảnh bổ sung nếu có
            if (uploadedImages.size() > 1) {

                foodDTO.setAdditionalImages(
                        uploadedImages.subList(1, uploadedImages.size())
                );
            }
        }

        Food food = foodMapper.toEntity(foodDTO);

        foodRepository.save(food);

        redirectAttributes.addFlashAttribute(
                "success",
                "Food item saved successfully!"
        );

        // Phân biệt hành động dựa trên nút nhấn
        if ("save_and_add".equals(action)) {

            // Về form thêm mới
            return "redirect:/admin/foods/add";

        } else {

            // Về danh sách
            return "redirect:/admin/foods";
        }
    }

    /**
     * Xóa món ăn theo id.
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        foodRepository.deleteById(id);
        return "redirect:/admin/foods";
    }

    /**
     * Hiển thị danh sách món ăn dạng grid.
     * Có thể lọc theo category.
     */
    @GetMapping("/grid")
    public String grid(@RequestParam(required = false) Long categoryId,
                       Model model) {

        List<Food> foods;

        if (categoryId != null) {
            foods = foodRepository.findByCategory_CategoryId(categoryId);
        } else {
            foods = foodRepository.findAll();
        }

        List<FoodDTO> foodDTOs = foods.stream()
                .map(foodMapper::toDTO)
                .collect(Collectors.toList());

        model.addAttribute("foods", foodDTOs);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);

        return "admin/food-grid";
    }
}
