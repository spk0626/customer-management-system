package com.customermgmt.repository;

import com.customermgmt.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c JOIN FETCH c.country WHERE LOWER(c.name) = LOWER(:name)")
    Optional<City> findByNameIgnoreCase(@Param("name") String name);
}