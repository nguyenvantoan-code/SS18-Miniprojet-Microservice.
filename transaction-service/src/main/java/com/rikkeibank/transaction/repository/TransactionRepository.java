package com.rikkeibank.transaction.repository;
import com.rikkeibank.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccountIdOrTargetAccountId(Long sourceId, Long targetId);
    List<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    Optional<Transaction> findByTransferId(String transferId);
}
