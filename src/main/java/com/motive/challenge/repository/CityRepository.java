package com.motive.challenge.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.motive.challenge.model.City;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByStateCodeIgnoreCaseOrderByCityAsc(String stateCode);
}
