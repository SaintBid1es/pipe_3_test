package com.mpt.practyp.controller;

import com.mpt.practyp.model.Customer;
import com.mpt.practyp.model.Order;
import com.mpt.practyp.model.OrderItem;
import com.mpt.practyp.model.CartItem;
import com.mpt.practyp.model.Product;
import com.mpt.practyp.repository.CustomerRepository;
import com.mpt.practyp.repository.ProductRepository;
import com.mpt.practyp.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderService orderService;

    @GetMapping("/user/products")
    public String showProducts(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "user/products";
    }

    @GetMapping("/user/profile")
    public String showProfile(Model model, Principal principal) {
        Customer customer = customerRepository.findByUsername(principal.getName()).orElse(null);
        if (customer == null) return "redirect:/login";
        if (customer.getProfile() == null) {
            com.mpt.practyp.model.Profile profile = new com.mpt.practyp.model.Profile();
            profile.setCustomer(customer);
            profile.setAddress("Адрес не указан");
            customer.setProfile(profile);
        }
        model.addAttribute("customer", customer);
        return "user/profile";
    }

    @GetMapping("/user/profile/edit")
    public String editProfile(Model model, Principal principal) {
        Customer customer = customerRepository.findByUsername(principal.getName()).orElse(null);
        if (customer == null) return "redirect:/login";
        if (customer.getProfile() == null) {
            com.mpt.practyp.model.Profile profile = new com.mpt.practyp.model.Profile();
            profile.setCustomer(customer);
            customer.setProfile(profile);
        }
        model.addAttribute("customer", customer);
        return "user/edit_profile";
    }

    @PostMapping("/user/profile/edit")
    public String updateProfile(@ModelAttribute("customer") Customer customerForm, Principal principal) {
        Customer customer = customerRepository.findByUsername(principal.getName()).orElse(null);
        if (customer != null) {
            customer.setFirstName(customerForm.getFirstName());
            customer.setLastName(customerForm.getLastName());
            customer.setEmail(customerForm.getEmail());
            // Обновление профиля (адрес)
            if (customer.getProfile() == null) {
                com.mpt.practyp.model.Profile profile = new com.mpt.practyp.model.Profile();
                profile.setCustomer(customer);
                customer.setProfile(profile);
            }
            if (customerForm.getProfile() != null) {
                customer.getProfile().setAddress(customerForm.getProfile().getAddress());
                customer.getProfile().setPhone(customerForm.getProfile().getPhone());
            }
            customerRepository.save(customer);
        }
        return "redirect:/user/profile";
    }

    // ===== Корзина в сессии =====
    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(org.springframework.web.context.request.WebRequest webRequest) {
        Object attr = webRequest.getAttribute("CART_ITEMS", org.springframework.web.context.request.WebRequest.SCOPE_SESSION);
        if (attr == null) {
            List<CartItem> items = new ArrayList<>();
            webRequest.setAttribute("CART_ITEMS", items, org.springframework.web.context.request.WebRequest.SCOPE_SESSION);
            return items;
        }
        return (List<CartItem>) attr;
    }

    @PostMapping("/user/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            org.springframework.web.context.request.WebRequest webRequest) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/user/products";
        List<CartItem> cart = getCart(webRequest);

        Optional<CartItem> existing = cart.stream().filter(i -> i.getProductId().equals(productId)).findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem item = new CartItem(product.getId(), product.getName(), product.getPrice(), quantity);
            cart.add(item);
        }
        return "redirect:/user/cart";
    }

    @GetMapping("/user/cart")
    public String viewCart(Model model, org.springframework.web.context.request.WebRequest webRequest) {
        List<CartItem> cart = getCart(webRequest);
        java.math.BigDecimal total = cart.stream()
                .map(CartItem::getSubtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("items", cart);
        model.addAttribute("total", total);
        return "user/cart";
    }

    @PostMapping("/user/cart/update")
    public String updateCart(@RequestParam Long productId,
                             @RequestParam Integer quantity,
                             org.springframework.web.context.request.WebRequest webRequest) {
        List<CartItem> cart = getCart(webRequest);
        cart.stream().filter(i -> i.getProductId().equals(productId)).findFirst()
                .ifPresent(i -> i.setQuantity(Math.max(1, quantity)));
        return "redirect:/user/cart";
    }

    @PostMapping("/user/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 org.springframework.web.context.request.WebRequest webRequest) {
        List<CartItem> cart = getCart(webRequest);
        cart.removeIf(i -> i.getProductId().equals(productId));
        return "redirect:/user/cart";
    }

    @PostMapping("/user/checkout")
    public String checkout(Principal principal, org.springframework.web.context.request.WebRequest webRequest) {
        Customer customer = customerRepository.findByUsername(principal.getName()).orElse(null);
        if (customer == null) return "redirect:/login";
        List<CartItem> cart = getCart(webRequest);
        if (cart.isEmpty()) return "redirect:/user/cart";

        Order order = new Order(customer);
        for (CartItem ci : cart) {
            Product p = productRepository.findById(ci.getProductId()).orElse(null);
            if (p == null) continue;
            OrderItem oi = new OrderItem(order, p, ci.getQuantity(), ci.getPrice() != null ? ci.getPrice().doubleValue() : 0.0);
            order.addOrderItem(oi);
        }
        order.calculateTotalAmount();
        orderService.create(order);

        // Очистить корзину
        webRequest.removeAttribute("CART_ITEMS", org.springframework.web.context.request.WebRequest.SCOPE_SESSION);
        return "redirect:/user/orders";
    }

    @GetMapping("/user/orders")
    public String myOrders(Model model, Principal principal) {
        Customer customer = customerRepository.findByUsername(principal.getName()).orElse(null);
        if (customer == null) return "redirect:/login";
        List<Order> orders = orderService.findByCustomerId(customer.getId());
        model.addAttribute("orders", orders);
        return "user/orders";
    }
}