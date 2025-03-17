package com.blooming.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Data
public class User implements UserDetails {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Setter
    @Column(nullable = false)
    private String password;

    @Getter
    @Setter
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<PlantIdentified> identifiedPlants;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Evaluation> evaluations;

    @OneToOne(mappedBy = "user")
    private Cart cart;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Purchase> purchases;

    @Setter
    @Getter
    @Column(nullable = false)
    private boolean isActive;

    @Setter
    @Getter
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @Setter
    @Getter
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.getName().toString());
        return List.of(authority);
    }

    @Getter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role role;

    public User() {
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}

