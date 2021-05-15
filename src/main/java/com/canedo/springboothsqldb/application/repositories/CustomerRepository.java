package com.canedo.springboothsqldb.application.repositories;

import com.canedo.springboothsqldb.application.entities.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {}
