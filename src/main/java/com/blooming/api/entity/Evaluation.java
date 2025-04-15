package com.blooming.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Data
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pot_id")
    private Pot pot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nursery_id")
    private Nursery nursery;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 500)
    private String comment;

    @Column(nullable = false)
    private boolean status = true;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    public Evaluation(User user, Pot pot, Nursery nursery, int rating, String comment) {
        this.user = user;
        this.pot = pot;
        this.nursery = nursery;
        this.rating = rating;
        this.comment = comment;
    }

    public Evaluation() {

    }
}
