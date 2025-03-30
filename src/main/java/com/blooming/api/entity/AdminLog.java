package com.blooming.api.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
public class AdminLog {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @CreationTimestamp
    private Date date;

    @Getter
    @Setter
    @Column(nullable = false)
    private String userEmail;

    @Setter
    @Getter
    @Column(nullable = false)
    private String description;
}
