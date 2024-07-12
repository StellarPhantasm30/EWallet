package com.project.repositories;

import com.project.models.Wallet;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Wallet findByWalletId(String walletId);

    // sender - 30, receiver - 10 , amount = 15
    // update(s, -15)
    // update(r, 15)

    @Transactional
    @Modifying
    @Query("update Wallet w set w.balance = w.balance + :amount where w.walletId = :walletId")
    void updateWallet(String walletId, Long amount);

    /* If we want to make 2 separate methods
    @Transactional
    @Modifying
    @Query("update Wallet w set w.balance = w.balance + :amount where w.walletId = :walletId")
    void incrementWallet(String walletId, Long amount);

    @Transactional
    @Modifying
    @Query("update Wallet w set w.balance = w.balance - :amount where  w.walletId = :walletId")
    void decrementWallet(String walletId, Long amount)
    */
}
