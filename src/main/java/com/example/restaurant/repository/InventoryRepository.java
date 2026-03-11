package com.example.restaurant.repository;

import com.example.restaurant.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMenuItemId(Long menuItemId);

    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable - i.quantityReserved <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();
}
