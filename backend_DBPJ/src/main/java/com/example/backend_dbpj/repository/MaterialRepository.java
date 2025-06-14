package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {
} 