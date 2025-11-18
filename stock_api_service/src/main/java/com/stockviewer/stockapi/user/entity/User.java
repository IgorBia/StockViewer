package com.stockviewer.stockapi.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stockviewer.stockapi.watchlist.entity.Watchlist;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.stockviewer.stockapi.wallet.entity.Wallet;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema="user_management", name="app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name="email")
    private String email;

    @JsonIgnore
    @Column(name="password")
    private String password;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_role",
            schema = "user_management",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Watchlist> watchlists = new HashSet<>();

    @CreationTimestamp
    @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Wallet> wallets = new HashSet<>();

    @PrePersist
    private void createDefaultWatchlist() {
        Watchlist defaultWatchlist = new Watchlist();
        defaultWatchlist.setUser(this);
        defaultWatchlist.setName("Default");
        watchlists.add(defaultWatchlist);
    }
}
