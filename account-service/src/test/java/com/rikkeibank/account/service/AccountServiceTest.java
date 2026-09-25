package com.rikkeibank.account.service;

import com.rikkeibank.account.client.CustomerClient;
import com.rikkeibank.account.dto.*;
import com.rikkeibank.account.entity.*;
import com.rikkeibank.account.event.*;
import com.rikkeibank.account.repository.BankAccountRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock BankAccountRepository accountRepo;
    @Mock CustomerClient customerClient;
    @Mock KafkaTemplate<String, TransferEvent> kafkaTemplate;
    @InjectMocks AccountService accountService;

    @Test
    void initiateTransfer_insufficientBalance_throws() {
        BankAccount source = BankAccount.builder()
            .id(1L).balance(new BigDecimal("100")).status(AccountStatus.ACTIVE).build();
        BankAccount target = BankAccount.builder()
            .id(2L).balance(new BigDecimal("0")).status(AccountStatus.ACTIVE).build();

        when(accountRepo.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(target));

        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L); req.setTargetAccountId(2L);
        req.setAmount(new BigDecimal("500"));

        assertThatThrownBy(() -> accountService.initiateTransfer(req, "user1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Số dư không đủ");
    }

    @Test
    void initiateTransfer_success_publishesEvent() {
        BankAccount source = BankAccount.builder()
            .id(1L).accountNumber("RKK001").balance(new BigDecimal("1000000")).status(AccountStatus.ACTIVE).build();
        BankAccount target = BankAccount.builder()
            .id(2L).accountNumber("RKK002").balance(new BigDecimal("0")).status(AccountStatus.ACTIVE).build();

        when(accountRepo.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(target));
        when(accountRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L); req.setTargetAccountId(2L);
        req.setAmount(new BigDecimal("200000"));

        String transferId = accountService.initiateTransfer(req, "customer1");

        assertThat(transferId).isNotBlank();
        assertThat(source.getBalance()).isEqualByComparingTo("800000");
        verify(kafkaTemplate, times(1)).send(eq("banking-transfer"), eq(transferId), any());
    }
}
