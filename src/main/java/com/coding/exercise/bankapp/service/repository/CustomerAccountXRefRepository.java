package com.coding.exercise.bankapp.service.repository;

import com.coding.exercise.bankapp.model.CustomerAccountXRef;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerAccountXRefRepository extends CrudRepository<CustomerAccountXRef, String> {
    public void deleteAll();
}
