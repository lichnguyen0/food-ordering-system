package com.foodorderingsystem.controller.admin;

import com.foodorderingsystem.model.category.Category;
import com.foodorderingsystem.repository.category.CategoryRepository;
import com.foodorderingsystem.service.FileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    public AdminCategoryController(CategoryRepository categoryRepository, FileService fileService) {
        this.categoryRepository = categoryRepository;
        this.fileService = fileService;
    }

    /**
     * Danh sách danh mục món ăn có phân trang server-side.
     * Hỗ trợ tìm kiếm theo tên danh mục.
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        Pageable pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("categoryName").ascending());

        Page<Category> categoryPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            categoryPage = categoryRepository.findByCategoryNameContaining(keyword.trim(), pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword", keyword);

        return "admin/categories/list";
    }

    // Show add form
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        model.addAttribute("category", category);
        return "admin/categories/form";
    }

    // Lưu (Tạo hoặc Cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute("category") Category category,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       RedirectAttributes redirectAttributes) throws IOException {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tên danh mục không được để trống!");
            return "redirect:/admin/categories/add";
        }

        // Xử lý upload ảnh local
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileService.saveImage(imageFile);
            category.setImage(imagePath);
        } else if (category.getCategoryId() != null) {
            // Giữ lại ảnh cũ nếu chỉnh sửa và không tải lên ảnh mới
            Category existing = categoryRepository.findById(category.getCategoryId()).orElse(null);
            if (existing != null) {
                category.setImage(existing.getImage());
            }
        }
        
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success", "Danh mục đã được lưu thành công!");
        return "redirect:/admin/categories";
    }

    // Xóa danh mục
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục này do đang liên kết với món ăn!");
        }
        return "redirect:/admin/categories";
    }
}
