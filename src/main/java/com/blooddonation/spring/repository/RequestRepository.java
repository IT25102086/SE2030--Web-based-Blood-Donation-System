package com.blooddonation.spring.repository;

import com.blooddonation.spring.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {

    List<Request> findByStatus(String status);

    List<Request> findByBloodGroup(String bloodGroup);

