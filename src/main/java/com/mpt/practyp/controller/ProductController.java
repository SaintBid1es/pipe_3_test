package com.mpt.practyp.controller;

import org.springframework.ui.Model;
import com.mpt.practyp.model.Product;
import com.mpt.practyp.service.CategoryService;
import com.mpt.practyp.service.ProductService;
import com.mpt.practyp.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TagService tagService;

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll(false, Pageable.unpaged()));
        model.addAttribute("tags", tagService.findAll(false, Pageable.unpaged()));
        return "product-form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute Product product, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll(false, Pageable.unpaged()));
            model.addAttribute("tags", tagService.findAll(false, Pageable.unpaged()));
            return "product-form";
        }
        productService.create(product);
        return "redirect:/products/list";
    }

    @GetMapping("/list")
    public String listProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(defaultValue = "false") boolean showDeleted,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size,
                dir.equals("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending());

        Page<Product> productPage = productService.searchByName(search, categoryId, pageable, showDeleted);

        model.addAttribute("productPage", productPage);
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("showDeleted", showDeleted);
        model.addAttribute("categories", categoryService.findAll(false, Pageable.unpaged()));
        return "products-list";
    }



    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll(false, Pageable.unpaged()));
        model.addAttribute("tags", tagService.findAll(false, Pageable.unpaged()));
        return "product-form";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam Long id, @RequestParam(required = false) boolean soft) {
        if (soft) {
            productService.softDelete(id);
        } else {
            productService.hardDelete(id);
        }
        return "redirect:/products/list";
    }
    @PostMapping("/restore")
    public String restoreProduct(@RequestParam Long id) {
        Product product = productService.findById(id);
        if (product != null && product.isDeleted()) {
            product.setDeleted(false);
            productService.update(product);
        }
        return "redirect:/products/list";
    }



}
