package com.project.portfoliomanagement.repository;

import com.project.portfoliomanagement.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    // No extra methods needed for now
}


