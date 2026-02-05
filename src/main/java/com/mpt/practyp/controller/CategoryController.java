package com.mpt.practyp.controller;

import com.mpt.practyp.model.Category;
import com.mpt.practyp.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    private static final int PAGE_SIZE = 10;

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("category", new Category());
        return "category-form";
    }

    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute Category category, BindingResult result) {
        if (result.hasErrors()) {
            return "category-form";
        }
        categoryService.create(category);
        return "redirect:/categories/list";
    }

    @GetMapping("/list")
    public String showCategoryList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean showDeleted,
            Model model
    ) {
        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(direction, sort));

        Page<Category> categoryPage;
        if (search != null && !search.isEmpty()) {
            categoryPage = categoryService.search(search, showDeleted, pageable);
        } else {
            categoryPage = categoryService.findAll(showDeleted, pageable);
        }

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("search", search);
        model.addAttribute("showDeleted", showDeleted);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

        return "categories-list";
    }

    @PostMapping("/delete")
    public String deleteCategory(@RequestParam Long id, @RequestParam(required = false) boolean soft) {
        if (soft) categoryService.softDelete(id);
        else categoryService.hardDelete(id);
        return "redirect:/categories/list";
    }

    @PostMapping("/restore")
    public String restoreCategory(@RequestParam Long id) {
        categoryService.restore(id);
        return "redirect:/categories/list?showDeleted=true";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        if (category == null) return "redirect:/categories/list";
        model.addAttribute("category", category);
        return "category-form";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("category") Category category,
                                 BindingResult result) {
        if (result.hasErrors()) return "category-form";
        category.setId(id);
        categoryService.update(category);
        return "redirect:/categories/list";
    }
}
