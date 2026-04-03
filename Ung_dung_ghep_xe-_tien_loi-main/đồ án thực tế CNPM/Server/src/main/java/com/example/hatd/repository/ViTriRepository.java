package com.example.hatd.repository;

import com.example.hatd.model.ViTri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViTriRepository extends JpaRepository<ViTri, Long> {
}
