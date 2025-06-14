package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Integer> {
    List<PayrollRecord> findByRepairPersonnel_PersonnelId(int personnelId);
} 