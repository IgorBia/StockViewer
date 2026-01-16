package com.stockviewer.stockapi.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stockviewer.stockapi.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema="user_management", name="wallet")
public class Wallet {

    public Wallet(User user){
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalletWorthSnapshot> worthSnapshots;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managed_asset_id", nullable = false)
    private Asset managedAsset;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OwnedAsset> ownedAssets = new HashSet<>();

    public Set<OwnedAsset> getOwnedAssets() {
        return ownedAssets;
    }

    public void addOwnedAsset(OwnedAsset oa) {
        if (oa == null) return;
        oa.setWallet(this);
        this.ownedAssets.add(oa);
    }
}
