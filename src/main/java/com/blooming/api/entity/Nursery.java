package com.blooming.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@Entity
@Data
public class Nursery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Getter
    @Setter
    private String imageUrl;

    @Column(nullable = false)
    private Double latitude; // Latitud del vivero

    @Column(nullable = false)
    private Double longitude; // Longitud del vivero

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nursery_admin_id")
    private User nurseryAdmin; // Usuario con el rol de NURSERY_ADMIN

    @OneToMany(mappedBy = "nursery", fetch = FetchType.LAZY)
    private List<Evaluation> evaluations;

    @OneToMany(mappedBy = "nursery", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Product> products;

    @Column(nullable = false)
    private boolean status = true;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
