package com.mpt.practyp.controller;

import com.mpt.practyp.model.Customer;
import com.mpt.practyp.model.Role;
import com.mpt.practyp.repository.CustomerRepository;
import com.mpt.practyp.repository.RoleRepository;
import com.mpt.practyp.security.JwtTokenUtil;
import com.mpt.practyp.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);

            Optional<Customer> customer = customerRepository.findByUsername(loginRequest.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", userDetails.getUsername());
            response.put("roles", userDetails.getAuthorities());
            customer.ifPresent(c -> {
                response.put("firstName", c.getFirstName());
                response.put("lastName", c.getLastName());
                response.put("email", c.getEmail());
            });

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Customer customer) {
        try {
            if (customerRepository.findByUsername(customer.getUsername()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Username already exists");
                return ResponseEntity.badRequest().body(error);
            }

            if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email already exists");
                return ResponseEntity.badRequest().body(error);
            }

            com.mpt.practyp.model.Profile profile = new com.mpt.practyp.model.Profile();
            profile.setCustomer(customer);
            profile.setAddress("Адрес не указан");
            profile.setDeleted(false);

            customer.setProfile(profile);
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            customer.setDeleted(false);

            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        Role newRole = new Role("USER");
                        return roleRepository.save(newRole);
                    });

            customer.getRoles().add(userRole);

            Customer savedCustomer = customerRepository.save(customer);

            final UserDetails userDetails = userDetailsService.loadUserByUsername(savedCustomer.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", savedCustomer.getUsername());
            response.put("firstName", savedCustomer.getFirstName());
            response.put("lastName", savedCustomer.getLastName());
            response.put("email", savedCustomer.getEmail());
            response.put("roles", savedCustomer.getRoles()
                    .stream()
                    .map(Role::getName)
                    .toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }



    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}