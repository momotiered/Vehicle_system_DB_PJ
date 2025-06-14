package com.example.backend_dbpj.service;

import com.example.backend_dbpj.dto.RepairPersonnelLoginRequestDto;
import com.example.backend_dbpj.dto.RepairPersonnelLoginResponseDto;
import com.example.backend_dbpj.dto.RepairPersonnelResponseDto;
import com.example.backend_dbpj.dto.RepairAssignmentDto;
import com.example.backend_dbpj.dto.PayrollRecordDto;
import com.example.backend_dbpj.dto.CreatePersonnelRequestDto;
import com.example.backend_dbpj.dto.UpdatePersonnelRequestDto;
import com.example.backend_dbpj.entity.PayrollRecord;
import com.example.backend_dbpj.entity.RepairAssignment;
import com.example.backend_dbpj.entity.RepairPersonnel;
import com.example.backend_dbpj.entity.enums.AssignmentStatus;
import com.example.backend_dbpj.repository.RepairAssignmentRepository;
import com.example.backend_dbpj.repository.RepairPersonnelRepository;
import com.example.backend_dbpj.repository.PayrollRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RepairPersonnelService {

    @Autowired
    private RepairPersonnelRepository repairPersonnelRepository;

    @Autowired
    private RepairAssignmentRepository repairAssignmentRepository;

    @Autowired
    private PayrollRecordRepository payrollRecordRepository;

    public RepairPersonnelLoginResponseDto login(RepairPersonnelLoginRequestDto requestDto) {
        Optional<RepairPersonnel> personnelOptional = repairPersonnelRepository.findByUsername(requestDto.getUsername());

        if (personnelOptional.isPresent()) {
            RepairPersonnel personnel = personnelOptional.get();
            // 简化原则：明文密码比对
            if (personnel.getPassword().equals(requestDto.getPassword())) {
                return new RepairPersonnelLoginResponseDto(
                        personnel.getPersonnelId(),
                        personnel.getUsername(),
                        personnel.getFullName(),
                        "登录成功"
                );
            }
        }
        // 用户名或密码错误
        throw new RuntimeException("用户名或密码错误");
    }

    public RepairPersonnelResponseDto getRepairPersonnelById(int id) {
        RepairPersonnel personnel = repairPersonnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("维修人员不存在，ID: " + id));
        return new RepairPersonnelResponseDto(personnel);
    }

    public List<RepairAssignmentDto> getAssignmentsByStatus(int personnelId, AssignmentStatus status) {
        List<RepairAssignment> assignments = repairAssignmentRepository.findByRepairPersonnel_PersonnelIdAndStatus(personnelId, status);
        return assignments.stream()
                .map(RepairAssignmentDto::new)
                .collect(Collectors.toList());
    }

    public List<PayrollRecordDto> getPayrollRecordsByPersonnelId(int personnelId) {
        List<PayrollRecord> records = payrollRecordRepository.findByRepairPersonnel_PersonnelId(personnelId);
        return records.stream()
                .map(PayrollRecordDto::new)
                .collect(Collectors.toList());
    }

    public List<RepairPersonnelResponseDto> getAvailablePersonnel() {
        List<RepairPersonnel> personnelList = repairPersonnelRepository.findByIsActive(true);
        return personnelList.stream()
                .map(RepairPersonnelResponseDto::new)
                .collect(Collectors.toList());
    }

    // --- Admin Management Methods ---

    public List<RepairPersonnel> getAllPersonnel() {
        return repairPersonnelRepository.findAll();
    }

    public RepairPersonnel createPersonnel(CreatePersonnelRequestDto createDto) {
        RepairPersonnel personnel = new RepairPersonnel();
        personnel.setUsername(createDto.getName()); // 简化处理，使用 name 作为 username
        personnel.setFullName(createDto.getName());
        personnel.setPassword(createDto.getPassword()); // 明文
        personnel.setWorkType(createDto.getSpecialization());
        personnel.setHourlyRate(createDto.getHourlyRate());
        personnel.setActive(true);
        return repairPersonnelRepository.save(personnel);
    }

    public RepairPersonnel updatePersonnel(int personnelId, UpdatePersonnelRequestDto updateDto) {
        RepairPersonnel personnel = repairPersonnelRepository.findById(personnelId)
                .orElseThrow(() -> new EntityNotFoundException("RepairPersonnel not found with id: " + personnelId));

        personnel.setWorkType(updateDto.getSpecialization());
        personnel.setHourlyRate(updateDto.getHourlyRate());
        if (updateDto.getContactInfo() != null) {
            personnel.setContactPhone(updateDto.getContactInfo());
        }
        return repairPersonnelRepository.save(personnel);
    }

    public void deletePersonnel(int personnelId) {
        if (!repairPersonnelRepository.existsById(personnelId)) {
            throw new EntityNotFoundException("RepairPersonnel not found with id: " + personnelId);
        }
        // 实际业务中可能不是物理删除，而是标记为 inactive
        repairPersonnelRepository.deleteById(personnelId);
    }
} 