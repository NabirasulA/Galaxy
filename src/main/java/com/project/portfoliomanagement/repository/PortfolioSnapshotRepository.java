package com.project.portfoliomanagement.repository;

import com.project.portfoliomanagement.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PortfolioSnapshotRepository
        extends JpaRepository<PortfolioSnapshot, Long> {

    Optional<PortfolioSnapshot> findByDate(LocalDate date);
}
