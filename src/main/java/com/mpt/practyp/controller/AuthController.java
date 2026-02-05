package com.mpt.practyp.controller;

import com.mpt.practyp.model.Customer;
import com.mpt.practyp.model.Profile;
import com.mpt.practyp.model.Role;
import com.mpt.practyp.repository.CustomerRepository;
import com.mpt.practyp.repository.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "register";
    }
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("customer") Customer customer,
                           BindingResult bindingResult,
                           @RequestParam(required = false) String address,
                           Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Проверяем существование пользователя с таким логином
        if (repository.findByUsername(customer.getUsername()).isPresent()) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            return "register";
        }

        // Проверяем существование пользователя с таким email
        if (repository.findByEmail(customer.getEmail()).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "register";
        }

        // Создаём профиль
        Profile profile = new Profile();
        profile.setCustomer(customer);
        profile.setAddress(address != null && !address.trim().isEmpty()
                ? address.trim()
                : "Адрес не указан");
        profile.setDeleted(false);

        customer.setProfile(profile);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setDeleted(false);

        // ✅ Находим или создаём роль USER
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role("USER");
                    return roleRepository.save(newRole);
                });

        // ✅ Присваиваем пользователю роль
        customer.getRoles().add(userRole);

        // ✅ Сохраняем пользователя
        repository.save(customer);

        return "redirect:/login?success";
    }

}