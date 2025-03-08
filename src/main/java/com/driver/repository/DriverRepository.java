package com.driver.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.driver.model.Driver;
@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer>{
    @Query(value = "select d from Driver d order by d.driverId", nativeQuery = false)
    List<Driver> findAllOrderByDriverId();

    Page<Driver> findAll(Pageable pageable);
}
