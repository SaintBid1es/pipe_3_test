package com.mpt.practyp.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название роли обязательно")
    @Column(unique = true, nullable = false)
    private String name; // Пример: "ADMIN", "MANAGER", "USER"

    @ManyToMany(mappedBy = "roles")
    private Set<Customer> customers = new HashSet<>();

    public Role() {}

    public Role(String name) {
        this.name = name;
    }



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Customer> getCustomers() { return customers; }
    public void setCustomers(Set<Customer> customers) { this.customers = customers; }
}