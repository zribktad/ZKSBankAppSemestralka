package com.coding.exercise.bankapp.service;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.CustomerDetails;
import com.coding.exercise.bankapp.domain.TransactionDetails;
import com.coding.exercise.bankapp.domain.TransferDetails;
import com.coding.exercise.bankapp.model.*;
import com.coding.exercise.bankapp.service.helper.BankingServiceHelper;
import com.coding.exercise.bankapp.service.repository.AccountRepository;
import com.coding.exercise.bankapp.service.repository.CustomerAccountXRefRepository;
import com.coding.exercise.bankapp.service.repository.CustomerRepository;
import com.coding.exercise.bankapp.service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class BankingServiceImpl implements BankingService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CustomerAccountXRefRepository custAccXRefRepository;
    @Autowired
    private BankingServiceHelper bankingServiceHelper;

    public BankingServiceImpl(CustomerRepository repository) {
        this.customerRepository = repository;
    }

    public void deleteRepository() {
        accountRepository.deleteAll();
        transactionRepository.deleteAll();
        customerRepository.deleteAll();
        custAccXRefRepository.deleteAll();
    }

    public List<CustomerDetails> findAll() {

        List<CustomerDetails> allCustomerDetails = new ArrayList<>();

        Iterable<Customer> customerList = customerRepository.findAll();

        customerList.forEach(customer -> {
            allCustomerDetails.add(bankingServiceHelper.convertToCustomerDomain(customer));
        });

        return allCustomerDetails;
    }

    /**
     * CREATE Customer
     *
     * @param customerDetails
     * @return
     */
    public ResponseEntity<Object> addCustomer(CustomerDetails customerDetails) {
        if (customerDetails.isValidCustomerDetails()) {
            Customer customer = bankingServiceHelper.convertToCustomerEntity(customerDetails);
            customer.setCreateDateTime(new Date());
            customerRepository.save(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body("New Customer created successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid customer details.");
        }
    }

    /**
     * READ Customer
     *
     * @param customerNumber
     * @return
     */

    public CustomerDetails findByCustomerNumber(Long customerNumber) {
        if (isValidCustomerNumber(customerNumber)) {
            Optional<Customer> customerEntityOpt = customerRepository.findByCustomerNumber(customerNumber);

            if (customerEntityOpt.isPresent()) {
                CustomerDetails customerDetails = bankingServiceHelper.convertToCustomerDomain(customerEntityOpt.get());

                if (customerDetails.isValidCustomerDetails()) {
                    return customerDetails;
                } else {
                    // Log or handle invalid customer details
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * UPDATE Customer
     *
     * @param customerDetails
     * @param customerNumber
     * @return
     */
    public ResponseEntity<Object> updateCustomer(CustomerDetails customerDetails, Long customerNumber) {
        if (isValidCustomerNumber(customerNumber) && (customerDetails.isValidCustomerDetails())) {
            Optional<Customer> managedCustomerEntityOpt = customerRepository.findByCustomerNumber(customerNumber);
            Customer unmanagedCustomerEntity = bankingServiceHelper.convertToCustomerEntity(customerDetails);

            if (managedCustomerEntityOpt.isPresent()) {
                Customer managedCustomerEntity = managedCustomerEntityOpt.get();

                if (Optional.ofNullable(unmanagedCustomerEntity.getContactDetails()).isPresent()) {

                    Contact managedContact = managedCustomerEntity.getContactDetails();
                    if (managedContact != null) {
                        managedContact.setEmailId(unmanagedCustomerEntity.getContactDetails().getEmailId());
                        managedContact.setHomePhone(unmanagedCustomerEntity.getContactDetails().getHomePhone());
                        managedContact.setWorkPhone(unmanagedCustomerEntity.getContactDetails().getWorkPhone());
                    } else
                        managedCustomerEntity.setContactDetails(unmanagedCustomerEntity.getContactDetails());
                }

                if (Optional.ofNullable(unmanagedCustomerEntity.getCustomerAddress()).isPresent()) {

                    Address managedAddress = managedCustomerEntity.getCustomerAddress();
                    if (managedAddress != null) {
                        managedAddress.setAddress1(unmanagedCustomerEntity.getCustomerAddress().getAddress1());
                        managedAddress.setAddress2(unmanagedCustomerEntity.getCustomerAddress().getAddress2());
                        managedAddress.setCity(unmanagedCustomerEntity.getCustomerAddress().getCity());
                        managedAddress.setState(unmanagedCustomerEntity.getCustomerAddress().getState());
                        managedAddress.setZip(unmanagedCustomerEntity.getCustomerAddress().getZip());
                        managedAddress.setCountry(unmanagedCustomerEntity.getCustomerAddress().getCountry());
                    } else
                        managedCustomerEntity.setCustomerAddress(unmanagedCustomerEntity.getCustomerAddress());
                }

                managedCustomerEntity.setUpdateDateTime(new Date());
                managedCustomerEntity.setStatus(unmanagedCustomerEntity.getStatus());
                managedCustomerEntity.setFirstName(unmanagedCustomerEntity.getFirstName());
                managedCustomerEntity.setMiddleName(unmanagedCustomerEntity.getMiddleName());
                managedCustomerEntity.setLastName(unmanagedCustomerEntity.getLastName());
                managedCustomerEntity.setUpdateDateTime(new Date());

                customerRepository.save(managedCustomerEntity);

                return ResponseEntity.status(HttpStatus.OK).body("Success: Customer updated.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer Number " + customerNumber + " not found.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid customer number or customer details.");
        }
    }

// Additional helper methods


    /**
     * DELETE Customer
     *
     * @param customerNumber
     * @return
     */
    public ResponseEntity<Object> deleteCustomer(Long customerNumber) {
        if (isValidCustomerNumber(customerNumber)) {
            Optional<Customer> managedCustomerEntityOpt = customerRepository.findByCustomerNumber(customerNumber);

            if (managedCustomerEntityOpt.isPresent()) {
                Customer managedCustomerEntity = managedCustomerEntityOpt.get();
                customerRepository.delete(managedCustomerEntity);

                // TODO: Add logic to delete all customer entries from CustomerAccountXRef

                return ResponseEntity.status(HttpStatus.OK).body("Success: Customer deleted.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer Number " + customerNumber + " not found.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid customer number.");
        }
    }

    private boolean isValidCustomerNumber(Long customerNumber) {
        // Add conditions for valid customer numbers
        // Example: Check if customerNumber is not null and positive
        return customerNumber != null && customerNumber > 0;
    }

    /**
     * Find Account
     *
     * @param accountNumber
     * @return
     */
    public ResponseEntity<Object> findByAccountNumber(Long accountNumber) {
        if (isValidAccountNumber(accountNumber)) {
            Optional<Account> accountEntityOpt = accountRepository.findByAccountNumber(accountNumber);

            if (accountEntityOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.FOUND).body(bankingServiceHelper.convertToAccountDomain(accountEntityOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account Number " + accountNumber + " not found.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid account number.");
        }
    }

    private boolean isValidAccountNumber(Long accountNumber) {
        // Add conditions for valid account numbers
        // Example: Check if accountNumber is not null and positive
        return accountNumber != null && accountNumber > 0;
    }

    /**
     * Create new account
     *
     * @param accountInformation
     * @param customerNumber
     * @return
     */
    public ResponseEntity<Object> addNewAccount(AccountInformation accountInformation, Long customerNumber) {
        if (accountInformationIsValid(accountInformation) && customerNumberIsValid(customerNumber)) {
            Optional<Customer> customerEntityOpt = customerRepository.findByCustomerNumber(customerNumber);

            if (customerEntityOpt.isPresent()) {
                // Add conditions for accountInformation with minimum and maximum values
                if (isValidAccountBalance(accountInformation.getAccountBalance())) {
                    accountRepository.save(bankingServiceHelper.convertToAccountEntity(accountInformation));

                    // Add an entry to the CustomerAccountXRef
                    custAccXRefRepository.save(CustomerAccountXRef.builder()
                            .accountNumber(accountInformation.getAccountNumber())
                            .customerNumber(customerNumber)
                            .build());

                    return ResponseEntity.status(HttpStatus.CREATED).body("New Account created successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid account balance.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer Number " + customerNumber + " not found.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid AccountInformation or customerNumber.");
        }
    }

// Additional helper methods

    private boolean accountInformationIsValid(AccountInformation accountInformation) {
        // Add conditions for minimum and maximum values for AccountInformation fields
        // Example: Check if accountBalance is within valid range
        return accountInformation != null &&
                isValidAccountBalance(accountInformation.getAccountBalance()) &&
                isValidAccountType(accountInformation.getAccountType()); // Add more conditions as needed
    }

    private boolean isValidAccountType(String accountType) {
        // Add conditions for valid account types
        // Example: Check if accountType is one of the allowed types
        List<String> allowedAccountTypes = Arrays.asList("Savings", "Checking", "Business"); // Add more types as needed
        return accountType != null && allowedAccountTypes.contains(accountType);
    }

    private boolean customerNumberIsValid(Long customerNumber) {
        // Add conditions for minimum and maximum values for customerNumber
        // Example: Check if customerNumber is within a valid range
        return customerNumber != null && customerNumber > 0; // Add more conditions as needed
    }

    private boolean isValidAccountBalance(Double accountBalance) {
        // Add conditions for minimum and maximum values for accountBalance
        // Example: Check if accountBalance is within a valid range
        return accountBalance != null && accountBalance >= 0; // Add more conditions as needed
    }


    /**
     * Transfer funds from one account to another for a specific customer
     *
     * @param transferDetails
     * @param customerNumber
     * @return
     */
    public ResponseEntity<Object> transferDetails(TransferDetails transferDetails, Long customerNumber) {

        if (!isValidCustomerNumber(customerNumber)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer Number " + customerNumber + " not found.");
        }

        List<Account> accountEntities = new ArrayList<>();
        Account fromAccountEntity = null;
        Account toAccountEntity = null;

        Optional<Customer> customerEntityOpt = customerRepository.findByCustomerNumber(customerNumber);

        // If customer is present
        if (customerEntityOpt.isPresent()) {

            // get FROM ACCOUNT info
            Optional<Account> fromAccountEntityOpt = accountRepository.findByAccountNumber(transferDetails.getFromAccountNumber());
            if (fromAccountEntityOpt.isPresent()) {
                fromAccountEntity = fromAccountEntityOpt.get();
            } else {
                // if from request does not exist, 404 Bad Request
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("From Account Number " + transferDetails.getFromAccountNumber() + " not found.");
            }


            // get TO ACCOUNT info
            Optional<Account> toAccountEntityOpt = accountRepository.findByAccountNumber(transferDetails.getToAccountNumber());
            if (toAccountEntityOpt.isPresent()) {
                toAccountEntity = toAccountEntityOpt.get();
            } else {
                // if from request does not exist, 404 Bad Request
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("To Account Number " + transferDetails.getToAccountNumber() + " not found.");
            }


            // if not sufficient funds, return 400 Bad Request
            if (transferDetails.getTransferAmount() <= 0 || fromAccountEntity.getAccountBalance() < transferDetails.getTransferAmount()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Transfer Amount or Insufficient Funds.");
            }

            synchronized (this) {
                // update FROM ACCOUNT
                fromAccountEntity.setAccountBalance(fromAccountEntity.getAccountBalance() - transferDetails.getTransferAmount());
                fromAccountEntity.setUpdateDateTime(new Date());
                accountEntities.add(fromAccountEntity);

                // update TO ACCOUNT
                toAccountEntity.setAccountBalance(toAccountEntity.getAccountBalance() + transferDetails.getTransferAmount());
                toAccountEntity.setUpdateDateTime(new Date());
                accountEntities.add(toAccountEntity);

                accountRepository.saveAll(accountEntities);

                // Create transaction for FROM Account
                Transaction fromTransaction = bankingServiceHelper.createTransaction(transferDetails, fromAccountEntity.getAccountNumber(), "DEBIT");
                transactionRepository.save(fromTransaction);

                // Create transaction for TO Account
                Transaction toTransaction = bankingServiceHelper.createTransaction(transferDetails, toAccountEntity.getAccountNumber(), "CREDIT");
                transactionRepository.save(toTransaction);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Success: Amount transferred for Customer Number " + customerNumber);


        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer Number " + customerNumber + " not found.");
        }

    }

    /**
     * Get all transactions for a specific account
     *
     * @param accountNumber
     * @return
     */
    public List<TransactionDetails> findTransactionsByAccountNumber(Long accountNumber) {

        List<TransactionDetails> transactionDetails = new ArrayList<>();
        Optional<Account> accountEntityOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountEntityOpt.isPresent()) {
            Optional<List<Transaction>> transactionEntitiesOpt = transactionRepository.findByAccountNumber(accountNumber);
            if (transactionEntitiesOpt.isPresent()) {
                transactionEntitiesOpt.get().forEach(transaction -> {
                    transactionDetails.add(bankingServiceHelper.convertToTransactionDomain(transaction));
                });
            }
        }

        return transactionDetails;
    }


}
