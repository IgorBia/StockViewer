package com.stockviewer.stockapi.wallet.service;

import com.stockviewer.stockapi.wallet.entity.Wallet;
import com.stockviewer.stockapi.wallet.entity.WalletWorthSnapshot;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service 
public class WalletWorthSnapshotScheduler {

    private final WalletRepository walletRepository;
    private final WalletWorthSnapshotService walletWorthSnapshotService;
    private final static Logger logger = LoggerFactory.getLogger(WalletWorthSnapshotScheduler.class);

    @Autowired
    public WalletWorthSnapshotScheduler(WalletRepository walletRepository,
                                        WalletWorthSnapshotService walletWorthSnapshotService) {
        this.walletRepository = walletRepository;
        this.walletWorthSnapshotService = walletWorthSnapshotService;
    }

    @Transactional(readOnly = false)
    @Scheduled(cron = "0 * * * * *") // Every minute
    public void takeSnapshotsForAllWallets() {
        List<Wallet> wallets = walletRepository.findAll();
        for (Wallet wallet : wallets) {
            logger.info("Taking wallet worth snapshot for wallet from user: " + wallet.getUser().getEmail());
            walletWorthSnapshotService.takeSnapshot(wallet);
        }
    }
}