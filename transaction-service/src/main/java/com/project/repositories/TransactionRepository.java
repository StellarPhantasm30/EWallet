package com.project.repositories;

import com.project.models.Transaction;
import com.project.models.TransactionStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Modifying
    @Transactional
    @Query("update Transaction t set t.transactionStatus = :transactionStatus where t.externalId = :externalId")
    void update(String externalId, TransactionStatus transactionStatus);
}
