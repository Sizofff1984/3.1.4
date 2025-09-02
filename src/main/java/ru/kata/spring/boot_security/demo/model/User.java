package ru.kata.spring.boot_security.demo.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    public User() {}

    public User(String email, String password, int age, String firstName, String lastName, Set<Role> roles) {
        this.email = email;
        this.password = password;
        this.age = age;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    
    public Set<Role> getSortedRoles() {
        if (roles == null) return null;
        return roles.stream()
                .sorted((r1, r2) -> {
                    if (r1.getName().equals("ROLE_ADMIN") && r2.getName().equals("ROLE_USER")) {
                        return -1;
                    } else if (r1.getName().equals("ROLE_USER") && r2.getName().equals("ROLE_ADMIN")) {
                        return 1;
                    }
                    return r1.getName().compareTo(r2.getName());
                })
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }
    
    public String getSortedRoleNames() {
        if (roles == null) return "";
        return roles.stream()
                .sorted((r1, r2) -> {
                    if (r1.getName().equals("ROLE_ADMIN") && r2.getName().equals("ROLE_USER")) {
                        return -1;
                    } else if (r1.getName().equals("ROLE_USER") && r2.getName().equals("ROLE_ADMIN")) {
                        return 1;
                    }
                    return r1.getName().compareTo(r2.getName());
                })
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(java.util.stream.Collectors.joining(","));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}