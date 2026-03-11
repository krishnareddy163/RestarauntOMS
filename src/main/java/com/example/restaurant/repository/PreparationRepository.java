package com.example.restaurant.repository;

import com.example.restaurant.entity.Preparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreparationRepository extends JpaRepository<Preparation, Long> {
    Optional<Preparation> findByOrderId(Long orderId);

    @Query("SELECT p FROM Preparation p WHERE p.status = :status ORDER BY p.createdAt ASC")
    List<Preparation> findByStatus(@Param("status") Preparation.PreparationStatus status);
}

