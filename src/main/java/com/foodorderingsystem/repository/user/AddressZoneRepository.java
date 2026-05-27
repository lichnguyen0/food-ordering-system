package com.foodorderingsystem.repository.user;

import com.foodorderingsystem.model.user.AddressZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressZoneRepository extends JpaRepository<AddressZone, Long> {
    Optional<AddressZone> findByWardAndCity(String ward, String city);
}