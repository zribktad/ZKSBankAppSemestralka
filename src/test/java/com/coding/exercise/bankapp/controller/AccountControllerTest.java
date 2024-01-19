package com.coding.exercise.bankapp.controller;

import com.coding.exercise.bankapp.domain.*;
import com.coding.exercise.bankapp.service.BankingServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerTest {
    @Autowired
    private AccountController accountController;
    @Autowired
    private BankingServiceImpl bankingService;

    @BeforeAll
    void setUp() {

        BankInformation bankInformation = getBankInformation();

        CustomerDetails sampleCustomer = CustomerDetails.builder().firstName("John").lastName("Doe").middleName("M").customerNumber(123L).status("Active").customerAddress(AddressDetails.builder().address1("123 Main St").city("Cityville").state("CA").zip("12345").country("USA").build()).contactDetails(ContactDetails.builder().emailId("john.doe@example.com").homePhone("123-456-7890").workPhone("987-654-3210").build()).build();
        bankingService.addCustomer(sampleCustomer);

        // Create and save sample accounts for the customer
        AccountInformation sourceAccount = AccountInformation.builder()
                .accountType("Savings")
                .bankInformation(bankInformation)
                .accountBalance(500.0)
                .accountNumber(1L)
                .accountCreated(new Date())
                .build();

        bankingService.addNewAccount(sourceAccount, sampleCustomer.getCustomerNumber());

        AccountInformation destinationAccount = AccountInformation.builder()
                .accountType("Checking")
                .bankInformation(bankInformation)
                .accountBalance(200.0)
                .accountNumber(2L)
                .accountCreated(new Date())
                .build();

        bankingService.addNewAccount(destinationAccount, sampleCustomer.getCustomerNumber());
    }

    private BankInformation getBankInformation() {
        BankInformation bankInformation = BankInformation.builder()
                .branchName("Main Branch")
                .branchCode(123)
                .branchAddress(AddressDetails.builder()
                        .address1("456 Oak St")
                        .city("Townsville")
                        .state("CA")
                        .zip("54321")
                        .country("USA")
                        .build())
                .routingNumber(987654321)
                .build();
        return bankInformation;
    }

    @Test
    void testGetByAccountNumber_Success() {
        // Arrange
        Long accountNumber = 1L;

        // Act
        ResponseEntity<Object> result = accountController.getByAccountNumber(accountNumber);

        // Assert
        assertEquals(HttpStatus.FOUND, result.getStatusCode());
    }

    @Test
    void testGetByAccountNumber_NotFound() {
        // Arrange
        Long accountNumber = 67890L;

        // Act
        ResponseEntity<Object> result = accountController.getByAccountNumber(accountNumber);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void testAddNewAccount_Success() {
        // Arrange
        Long customerNumber = 123L;
        AccountInformation accountInformation = createValidAccountInformation();

        // Act
        ResponseEntity<Object> result = accountController.addNewAccount(accountInformation, customerNumber);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());

    }

    @Test
    void testAddNewAccount_ValidationFailure() {
        // Arrange
        Long customerNumber = 67890L;
        AccountInformation invalidAccount = new AccountInformation(); // missing required fields

        // Act
        ResponseEntity<Object> result = accountController.addNewAccount(invalidAccount, customerNumber);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

    }


    @Test
    void testTransferDetails_Success() {
        // Arrange
        Long customerNumber = 123L;
        TransferDetails transferDetails = createValidTransferDetails();

        // Act
        ResponseEntity<Object> result = accountController.transferDetails(transferDetails, customerNumber);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(Objects.requireNonNull(result.getBody()).toString().contains("Success"));
    }

    @Test
    void testTransferDetails_ValidationFailure() {
        // Arrange
        Long customerNumber = 123L;
        TransferDetails invalidTransfer = new TransferDetails(); // missing required fields


        // Act
        ResponseEntity<Object> result = accountController.transferDetails(invalidTransfer, customerNumber);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    }

    @Test
    void testGetTransactionByAccountNumber_Success() {
        // Arrange
        Long accountNumber = 1L;
        Long customerNumber = 123L;
        //Make money transfer
        TransferDetails transferDetails = createValidTransferDetails();
        ResponseEntity<Object> result_tmp = accountController.transferDetails(transferDetails, customerNumber);
        assertEquals(HttpStatus.OK, result_tmp.getStatusCode());

        List<TransactionDetails> transactions = Collections.singletonList(createValidTransactionDetails());


        // Act
        List<TransactionDetails> result = accountController.getTransactionByAccountNumber(accountNumber);

        // Assert
        assertFalse(result.isEmpty());
    }

    private AccountInformation createValidAccountInformation() {
        AccountInformation accountInformation = AccountInformation.builder()
                .accountType("Savings")
                .bankInformation(getBankInformation())
                .accountBalance(500.0)
                .accountNumber(new Random().nextLong())
                .accountCreated(new Date())
                .build();
        accountInformation.setAccountType("Savings");
        // Set other required fields
        return accountInformation;
    }

    private TransferDetails createValidTransferDetails() {
        TransferDetails transferDetails = new TransferDetails();
        transferDetails.setSourceAccount(1L);
        transferDetails.setDestinationAccount(2L);
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
