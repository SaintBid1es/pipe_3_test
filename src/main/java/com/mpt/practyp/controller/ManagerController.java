package com.mpt.practyp.controller;

import com.mpt.practyp.repository.CategoryRepository;
import com.mpt.practyp.repository.ProductRepository;
import com.mpt.practyp.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired

    private TagRepository tagRepository;

    @GetMapping("/dashboard")
    public String managerDashboard(Model model) {
        long productCount = productRepository.count();
        long categoryCount = categoryRepository.count();
        long tagCount = tagRepository.count();

        model.addAttribute("productCount", productCount);
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("tagCount", tagCount);

        return "manager/dashboard";
    }
}