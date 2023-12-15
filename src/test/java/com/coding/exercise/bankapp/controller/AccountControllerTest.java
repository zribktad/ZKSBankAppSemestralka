package com.coding.exercise.bankapp.controller;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.TransactionDetails;
import com.coding.exercise.bankapp.domain.TransferDetails;
import com.coding.exercise.bankapp.service.BankingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    private AccountController accountController;
    private BankingServiceImpl bankingService;

    @BeforeEach
    void setUp() {
        bankingService = mock(BankingServiceImpl.class);
        accountController = new AccountController();
        accountController.setBankingService(bankingService);
    }

    @Test
    void testGetByAccountNumber_Success() {
        // Arrange
        Long accountNumber = 12345L;
        when(bankingService.findByAccountNumber(accountNumber)).thenReturn(new ResponseEntity<>("Account details", HttpStatus.OK));

        // Act
        ResponseEntity<Object> result = accountController.getByAccountNumber(accountNumber);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Account details", result.getBody());
        verify(bankingService, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void testGetByAccountNumber_NotFound() {
        // Arrange
        Long accountNumber = 67890L;
        when(bankingService.findByAccountNumber(accountNumber)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // Act
        ResponseEntity<Object> result = accountController.getByAccountNumber(accountNumber);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(bankingService, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void testAddNewAccount_Success() {
        // Arrange
        Long customerNumber = 67890L;
        AccountInformation accountInformation = createValidAccountInformation();
        when(bankingService.addNewAccount(accountInformation, customerNumber))
                .thenReturn(new ResponseEntity<>("Account added", HttpStatus.OK));

        // Act
        ResponseEntity<Object> result = accountController.addNewAccount(accountInformation, customerNumber);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Account added", result.getBody());
        verify(bankingService, times(1)).addNewAccount(accountInformation, customerNumber);
    }

    @Test
    void testAddNewAccount_ValidationFailure() {
        // Arrange
        Long customerNumber = 67890L;
        AccountInformation invalidAccount = new AccountInformation(); // missing required fields
        when(bankingService.addNewAccount(invalidAccount, customerNumber))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Act
        ResponseEntity<Object> result = accountController.addNewAccount(invalidAccount, customerNumber);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
        verify(bankingService, times(1)).addNewAccount(invalidAccount, customerNumber);
    }


    @Test
    void testTransferDetails_Success() {
        // Arrange
        Long customerNumber = 67890L;
        TransferDetails transferDetails = createValidTransferDetails();
        when(bankingService.transferDetails(transferDetails, customerNumber))
                .thenReturn(new ResponseEntity<>("Funds transferred", HttpStatus.OK));

        // Act
        ResponseEntity<Object> result = accountController.transferDetails(transferDetails, customerNumber);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Funds transferred", result.getBody());
        verify(bankingService, times(1)).transferDetails(transferDetails, customerNumber);
    }

    @Test
    void testTransferDetails_ValidationFailure() {
        // Arrange
        Long customerNumber = 67890L;
        TransferDetails invalidTransfer = new TransferDetails(); // missing required fields
        when(bankingService.transferDetails(invalidTransfer, customerNumber))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Act
        ResponseEntity<Object> result = accountController.transferDetails(invalidTransfer, customerNumber);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
        verify(bankingService, times(1)).transferDetails(any(), any()); // Verify that transferDetails is never called
    }

    @Test
    void testGetTransactionByAccountNumber_Success() {
        // Arrange
        Long accountNumber = 12345L;
        List<TransactionDetails> transactions = Collections.singletonList(createValidTransactionDetails());
        when(bankingService.findTransactionsByAccountNumber(accountNumber)).thenReturn(transactions);

        // Act
        List<TransactionDetails> result = accountController.getTransactionByAccountNumber(accountNumber);

        // Assert
        assertEquals(transactions, result);
        verify(bankingService, times(1)).findTransactionsByAccountNumber(accountNumber);
    }

    private AccountInformation createValidAccountInformation() {
        AccountInformation accountInformation = new AccountInformation();
        accountInformation.setAccountType("Savings");
        // Set other required fields
        return accountInformation;
    }

    private TransferDetails createValidTransferDetails() {
        TransferDetails transferDetails = new TransferDetails();
        transferDetails.setSourceAccount(12345L);
        transferDetails.setDestinationAccount(67890L);
        transferDetails.setAmount(100.0);
        // Set other required fields
        return transferDetails;
    }

    private TransactionDetails createValidTransactionDetails() {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setTransactionType("Deposit");
        transactionDetails.setAmount(50.0);
        // Set other required fields
        return transactionDetails;
    }
}
