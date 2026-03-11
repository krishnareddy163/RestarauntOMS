package com.example.restaurant.repository;

import com.example.restaurant.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);

    @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId AND d.status IN :statuses")
    List<Delivery> findActiveDeliveriesByDriver(@Param("driverId") Long driverId, @Param("statuses") List<Delivery.DeliveryStatus> statuses);

    @Query("SELECT d FROM Delivery d WHERE d.status = :status")
    List<Delivery> findByStatus(@Param("status") Delivery.DeliveryStatus status);
}

