package com.mpt.practyp.controller;

import com.mpt.practyp.model.Customer;
import com.mpt.practyp.model.Role;
import com.mpt.practyp.repository.RoleRepository;
import com.mpt.practyp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;


    private static final int PAGE_SIZE = 10;

    @GetMapping("/list")
    public String listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean showDeleted,
            Model model
    ) {
        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        Page<Customer> customerPage;
        if (search != null && !search.isEmpty()) {
            customerPage = customerService.search(search, showDeleted, pageable);
        } else {
            customerPage = customerService.findAll(showDeleted, pageable);
        }

        model.addAttribute("customerPage", customerPage);
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("showDeleted", showDeleted);

        return "customers-list";
    }

    @PostMapping("/delete")
    public String deleteCustomer(@RequestParam Long id, @RequestParam(required = false) boolean soft) {
        if (soft) customerService.softDelete(id);
        else customerService.hardDelete(id);
        return "redirect:/customers/list";
    }

    @PostMapping("/restore")
    public String restoreCustomer(@RequestParam Long id) {
        customerService.restore(id);
        return "redirect:/customers/list?showDeleted=true";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.findById(id);
        if (customer == null) return "redirect:/customers/list";

        model.addAttribute("customer", customer);
        model.addAttribute("roles", roleRepository.findAll());
        return "customer-form";
    }

    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute Customer customer,
                               @RequestParam(name = "roleIds", required = false) java.util.List<Long> roleIds) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        java.util.Set<Role> roles = new java.util.HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            roleRepository.findAllById(roleIds).forEach(roles::add);
        } else {
            roleRepository.findByName("USER").ifPresent(roles::add);
        }
        customer.setRoles(roles);

        customerService.create(customer);
        return "redirect:/customers/list";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable Long id,
                                 @ModelAttribute Customer customer,
                                 @RequestParam(name = "roleIds", required = false) java.util.List<Long> roleIds) {
        Customer existingCustomer = customerService.findById(id);
        if (existingCustomer == null) return "redirect:/customers/list";

        existingCustomer.setFirstName(customer.getFirstName());
        existingCustomer.setLastName(customer.getLastName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setUsername(customer.getUsername());
        if (roleIds != null) {
            java.util.Set<Role> roles = new java.util.HashSet<>();
            roleRepository.findAllById(roleIds).forEach(roles::add);
            existingCustomer.setRoles(roles);
        }

        customerService.update(existingCustomer);
        return "redirect:/customers/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("roles", roleRepository.findAll());
        return "customer-form";
    }
}