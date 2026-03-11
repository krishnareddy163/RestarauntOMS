package com.example.restaurant.repository;

import com.example.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @Query("SELECT m FROM MenuItem m WHERE m.category = :category AND m.available = true")
    List<MenuItem> findByCategory(@Param("category") String category);

    @Query("SELECT m FROM MenuItem m WHERE m.available = true")
    List<MenuItem> findAllAvailable();
}

