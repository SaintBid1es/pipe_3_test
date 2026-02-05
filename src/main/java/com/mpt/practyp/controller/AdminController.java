package com.mpt.practyp.controller;

import com.mpt.practyp.model.Customer;
import com.mpt.practyp.model.Order;
import com.mpt.practyp.model.Role;
import com.mpt.practyp.repository.CategoryRepository;
import com.mpt.practyp.repository.CustomerRepository;
import com.mpt.practyp.repository.OrderRepository;
import com.mpt.practyp.repository.ProductRepository;
import com.mpt.practyp.repository.ProfileRepository;
import com.mpt.practyp.repository.RoleRepository;
import com.mpt.practyp.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("")
    public String adminRootRedirect() {
        return "redirect:/";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        long customerCount = customerRepository.count();
        long productCount = productRepository.count();
        long categoryCount = categoryRepository.count();
        long tagCount = tagRepository.count();
        long profileCount = profileRepository.count();
        long totalCount = customerCount + productCount + categoryCount + tagCount + profileCount;

        model.addAttribute("customerCount", customerCount);
        model.addAttribute("productCount", productCount);
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("tagCount", tagCount);
        model.addAttribute("profileCount", profileCount);
        model.addAttribute("totalCount", totalCount);

        return "index";
    }


    @GetMapping("/roles")
    public String rolesPage(Model model) {
        List<Customer> users = customerRepository.findAll();
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        return "admin/users-roles";
    }

    @PostMapping("/roles/{userId}/grant")
    public String grantRole(@PathVariable Long userId, @RequestParam("role") String roleName) {
        Optional<Customer> customerOpt = customerRepository.findById(userId);
        if (customerOpt.isPresent()) {
            Customer c = customerOpt.get();
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName)));
            c.getRoles().add(role);
            customerRepository.save(c);
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/roles/{userId}/revoke")
    public String revokeRole(@PathVariable Long userId, @RequestParam("role") String roleName) {
        Optional<Customer> customerOpt = customerRepository.findById(userId);
        if (customerOpt.isPresent()) {
            Customer c = customerOpt.get();
            c.getRoles().removeIf(r -> r.getName().equalsIgnoreCase(roleName));
            customerRepository.save(c);
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/users/{userId}/restore")
    public String restoreUser(@PathVariable Long userId) {
        customerRepository.findById(userId).ifPresent(c -> {
            c.setDeleted(false);
            customerRepository.save(c);
        });
        return "redirect:/customers/list";
    }

    @PostMapping("/users/{userId}/hard-delete")
    public String hardDeleteUser(@PathVariable Long userId) {
        customerRepository.findById(userId).ifPresent(c -> customerRepository.delete(c));
        return "redirect:/customers/list";
    }


    @GetMapping("/export/customers.csv")
    public ResponseEntity<byte[]> exportCustomersCsv() {
        List<Customer> customers = customerRepository.findAll();
        StringJoiner sj = new StringJoiner("\n");
        sj.add("id,username,email,firstName,lastName,roles,deleted");
        for (Customer c : customers) {
            String roles = c.getRoles().stream().map(Role::getName).reduce((a,b) -> a+"|"+b).orElse("");
            sj.add(String.format("%d,%s,%s,%s,%s,%s,%s",
                    c.getId(), safe(c.getUsername()), safe(c.getEmail()), safe(c.getFirstName()), safe(c.getLastName()), safe(roles), String.valueOf(c.isDeleted())));
        }
        return csv("customers.csv", sj.toString());
    }

    @GetMapping("/export/orders.csv")
    public ResponseEntity<byte[]> exportOrdersCsv() {
        List<Order> orders = orderRepository.findAll();
        StringJoiner sj = new StringJoiner("\n");
        sj.add("id,customerId,totalAmount,itemsCount");
        for (Order o : orders) {
            int items = o.getOrderItems() != null ? o.getOrderItems().size() : 0;
            double total = o.getTotalAmount() != null ? o.getTotalAmount() : 0.0;
            Long customerId = o.getCustomer() != null ? o.getCustomer().getId() : null;
            sj.add(String.format("%d,%s,%.2f,%d", o.getId(), customerId != null ? customerId.toString() : "", total, items));
        }
        return csv("orders.csv", sj.toString());
    }

    @GetMapping("/export/products.csv")
    public ResponseEntity<byte[]> exportProductsCsv() {
        var all = productRepository.findAll();
        StringJoiner sj = new StringJoiner("\n");
        sj.add("id,name,price,category,deleted");
        all.forEach(p -> sj.add(String.format("%d,%s,%s,%s,%s",
                p.getId(), safe(p.getName()), p.getPrice() != null ? p.getPrice().toPlainString() : "", p.getCategory() != null ? safe(p.getCategory().getName()) : "", String.valueOf(p.isDeleted()))));
        return csv("products.csv", sj.toString());
    }

    @GetMapping("/export/categories.csv")
    public ResponseEntity<byte[]> exportCategoriesCsv() {
        var all = categoryRepository.findAll();
        StringJoiner sj = new StringJoiner("\n");
        sj.add("id,name,deleted");
        all.forEach(c -> sj.add(String.format("%d,%s,%s", c.getId(), safe(c.getName()), String.valueOf(c.isDeleted()))));
        return csv("categories.csv", sj.toString());
    }

    private ResponseEntity<byte[]> csv(String fileName, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replaceAll(",", " ").replaceAll("\n", " ").replaceAll("\r", " ");
    }
}