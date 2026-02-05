package com.mpt.practyp.controller;

import com.mpt.practyp.model.Profile;
import com.mpt.practyp.model.Customer;
import com.mpt.practyp.service.ProfileService;
import com.mpt.practyp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Controller
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private CustomerService customerService;

    private static final int PAGE_SIZE = 10;

    @GetMapping("/list")
    public String listProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean showDeleted,
            Model model
    ) {
        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(direction, sort));

        Page<Profile> profilePage;
        if (search != null && !search.isEmpty()) {
            profilePage = profileService.search(search, showDeleted, pageable);
        } else {
            profilePage = profileService.findAll(showDeleted, pageable);
        }

        // Получаем список всех пользователей для выпадающего списка
        // Используем пагинацию с большим размером страницы
        Pageable customersPageable = PageRequest.of(0, 1000); // Большой размер для получения всех
        Page<Customer> customersPage = customerService.findAll(false, customersPageable);
        List<Customer> customers = customersPage.getContent();

        model.addAttribute("profilePage", profilePage);
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("showDeleted", showDeleted);

        return "profiles-list";
    }

    @PostMapping("/delete")
    public String deleteProfile(@RequestParam Long id, @RequestParam(required = false) boolean soft) {
        if (soft) profileService.softDelete(id);
        else profileService.hardDelete(id);
        return "redirect:/profiles/list";
    }

    @PostMapping("/restore")
    public String restoreProfile(@RequestParam Long id) {
        profileService.restore(id);
        return "redirect:/profiles/list?showDeleted=true";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Profile profile = profileService.findById(id);
        if (profile == null) return "redirect:/profiles/list";

        // Получаем список всех пользователей
        Pageable customersPageable = PageRequest.of(0, 1000);
        Page<Customer> customersPage = customerService.findAll(false, customersPageable);
        List<Customer> customers = customersPage.getContent();

        model.addAttribute("profile", profile);
        model.addAttribute("customers", customers);
        return "profile-form";
    }

    @PostMapping("/save")
    public String saveProfile(@ModelAttribute Profile profile, @RequestParam Long customerId) {
        Customer customer = customerService.findById(customerId);
        if (customer != null) {
            profile.setCustomer(customer);
        }
        profileService.create(profile);
        return "redirect:/profiles/list";
    }

    @PostMapping("/update/{id}")
    public String updateProfile(@PathVariable Long id, @ModelAttribute Profile profile, @RequestParam Long customerId) {
        Profile existingProfile = profileService.findById(id);
        if (existingProfile == null) return "redirect:/profiles/list";

        Customer customer = customerService.findById(customerId);
        if (customer != null) {
            existingProfile.setCustomer(customer);
        }

        existingProfile.setAddress(profile.getAddress());
        profileService.update(existingProfile);
        return "redirect:/profiles/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        // Получаем список всех пользователей
        Pageable customersPageable = PageRequest.of(0, 1000);
        Page<Customer> customersPage = customerService.findAll(false, customersPageable);
        List<Customer> customers = customersPage.getContent();

        model.addAttribute("profile", new Profile());
        model.addAttribute("customers", customers);
        return "profile-form";
    }
}