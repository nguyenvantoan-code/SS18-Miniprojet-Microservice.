package com.rikkeibank.account.repository;
import com.rikkeibank.account.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    List<BankAccount> findByCustomerId(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankAccount> findWithLockById(Long id);
}
