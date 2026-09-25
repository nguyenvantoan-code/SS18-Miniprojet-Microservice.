package com.rikkeibank.account.event;
public enum TransferEventType {
    TRANSFER_INITIATED,
    DEBIT_COMPLETED,
    DEBIT_FAILED,
    CREDIT_COMPLETED,
    CREDIT_FAILED,
    TRANSFER_COMPLETED,
    TRANSFER_CANCELLED,
    COMPENSATE_CREDIT     // rollback: hoàn tiền về tài khoản nguồn
}
