# 车辆维修管理系统 - 核心功能SQL语句说明

## 概述

本文档详细说明了车辆维修管理系统中的核心SQL语句，涵盖基础CRUD操作、复杂查询、统计分析、监控报表等功能。系统采用Spring Boot + JPA架构，结合原生SQL和JPQL实现各种数据库操作。

## 1. 用户管理模块

### 1.1 用户注册与登录

**功能描述**: 用户注册时插入用户信息，登录时验证用户凭证

**SQL语句**:
```sql
-- 用户注册
INSERT INTO Users (username, password_hash, full_name, contact_phone, contact_email, address, registration_date)
VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);

-- 用户登录验证
SELECT user_id, username, password_hash, full_name, contact_phone, contact_email, address, registration_date
FROM Users
WHERE username = ? AND password_hash = ?;

-- 根据用户名查找用户
SELECT user_id, username, password_hash, full_name, contact_phone, contact_email, address, registration_date
FROM Users
WHERE username = ?;
```

**对应JPA方法**:
```java
// UserRepository.java
Optional<User> findByUsername(String username);
```

### 1.2 用户信息管理

**功能描述**: 管理员对用户信息进行增删改查操作

**SQL语句**:
```sql
-- 获取所有用户列表
SELECT user_id, username, full_name, contact_phone, contact_email, address, registration_date
FROM Users
ORDER BY registration_date DESC;

-- 更新用户信息
UPDATE Users
SET full_name = ?, contact_phone = ?, contact_email = ?, address = ?
WHERE user_id = ?;

-- 删除用户（级联删除相关记录）
DELETE FROM Users WHERE user_id = ?;
```

## 2. 车辆管理模块

### 2.1 车辆信息查询

**功能描述**: 根据用户ID查询其拥有的车辆信息

**SQL语句**:
```sql
-- 查询用户的所有车辆
SELECT v.vehicle_id, v.license_plate, v.make, v.model, v.year_of_manufacture, 
       v.vin, v.color, v.registration_date
FROM Vehicles v
WHERE v.user_id = ?
ORDER BY v.registration_date DESC;
```

**对应JPA方法**:
```java
// VehicleRepository.java
List<Vehicle> findByUser_UserId(Integer userId);
```

### 2.2 车辆信息维护

**SQL语句**:
```sql
-- 添加新车辆
INSERT INTO Vehicles (user_id, license_plate, make, model, year_of_manufacture, vin, color, registration_date)
VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);

-- 更新车辆信息
UPDATE Vehicles
SET make = ?, model = ?, year_of_manufacture = ?, color = ?
WHERE vehicle_id = ?;
```

## 3. 维修工单管理模块

### 3.1 工单创建与查询

**功能描述**: 用户提交维修申请，系统创建工单并支持多种查询方式

**SQL语句**:
```sql
-- 创建新工单
INSERT INTO RepairOrders (vehicle_id, user_id, description_of_issue, report_date, status)
VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'Pending Assignment');

-- 根据用户ID查询工单
SELECT ro.order_id, ro.description_of_issue, ro.report_date, ro.start_date, 
       ro.estimated_completion_date, ro.completion_date, ro.status,
       ro.total_material_cost, ro.total_labor_cost, ro.grand_total_cost, ro.notes,
       v.license_plate, v.make, v.model
FROM RepairOrders ro
JOIN Vehicles v ON ro.vehicle_id = v.vehicle_id
WHERE ro.user_id = ?
ORDER BY ro.report_date DESC;

-- 根据状态查询工单
SELECT ro.order_id, ro.description_of_issue, ro.report_date, ro.status,
       u.full_name, v.license_plate, v.make, v.model
FROM RepairOrders ro
JOIN Users u ON ro.user_id = u.user_id
JOIN Vehicles v ON ro.vehicle_id = v.vehicle_id
WHERE ro.status = ?
ORDER BY ro.report_date ASC;
```

**对应JPA方法**:
```java
// RepairOrderRepository.java
List<RepairOrder> findByUser_UserId(Integer userId);
List<RepairOrder> findByStatus(OrderStatus status);
```

### 3.2 工单统计查询

**功能描述**: 支持按日期范围、状态等条件进行工单统计

**SQL语句**:
```sql
-- 统计指定状态的工单数量
SELECT COUNT(*) FROM RepairOrders WHERE status = ?;

-- 统计指定日期范围内的工单数量
SELECT COUNT(*) FROM RepairOrders 
WHERE DATE(report_date) >= ? AND DATE(report_date) < ?;

-- 查询指定日期范围内完成的工单
SELECT ro.order_id, ro.completion_date, ro.total_material_cost, 
       ro.total_labor_cost, ro.grand_total_cost
FROM RepairOrders ro
WHERE ro.status = ? AND ro.completion_date >= ? AND ro.completion_date < ?;
```

**对应JPQL查询**:
```java
// RepairOrderRepository.java
@Query("SELECT COUNT(r) FROM RepairOrder r WHERE DATE(r.reportDate) >= :startDate AND DATE(r.reportDate) < :endDate")
Long countByReportDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

@Query("SELECT r FROM RepairOrder r WHERE r.status = :status AND r.completionDate >= :startDate AND r.completionDate < :endDate")
List<RepairOrder> findCompletedOrdersInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("status") OrderStatus status);
```

## 4. 维修人员管理模块

### 4.1 人员信息查询

**功能描述**: 维修人员登录验证和信息查询

**SQL语句**:
```sql
-- 维修人员登录验证
SELECT personnel_id, username, password_hash, full_name, work_type, hourly_rate, 
       contact_phone, hire_date, is_active
FROM RepairPersonnel
WHERE username = ? AND password_hash = ?;

-- 查询可用的维修人员
SELECT personnel_id, username, full_name, work_type, hourly_rate, contact_phone, is_active
FROM RepairPersonnel
WHERE is_active = true
ORDER BY full_name;

-- 根据工种查询维修人员
SELECT personnel_id, full_name, work_type, hourly_rate
FROM RepairPersonnel
WHERE work_type = ? AND is_active = true;
```

**对应JPA方法**:
```java
// RepairPersonnelRepository.java
Optional<RepairPersonnel> findByUsername(String username);
List<RepairPersonnel> findByIsActive(boolean isActive);
Long countByIsActive(boolean isActive);
```

### 4.2 人员统计分析

**功能描述**: 统计各工种人员分布情况

**SQL语句**:
```sql
-- 统计各工种人员数量
SELECT work_type, COUNT(*) as personnel_count
FROM RepairPersonnel
WHERE is_active = true
GROUP BY work_type
ORDER BY personnel_count DESC;

-- 统计在职人员总数
SELECT COUNT(*) FROM RepairPersonnel WHERE is_active = true;
```

## 5. 维修分配管理模块

### 5.1 工单分配

**功能描述**: 将维修工单分配给维修人员

**SQL语句**:
```sql
-- 创建维修分配记录
INSERT INTO RepairAssignments (order_id, personnel_id, assignment_date, status)
VALUES (?, ?, CURRENT_TIMESTAMP, 'Assigned');

-- 查询维修人员的分配记录
SELECT ra.assignment_id, ra.order_id, ra.assignment_date, ra.status,
       ra.hours_worked, ra.labor_cost_for_personnel, ra.notes,
       ro.description_of_issue, ro.report_date, ro.status as order_status,
       v.license_plate, v.make, v.model
FROM RepairAssignments ra
JOIN RepairOrders ro ON ra.order_id = ro.order_id
JOIN Vehicles v ON ro.vehicle_id = v.vehicle_id
WHERE ra.personnel_id = ? AND ra.status = ?
ORDER BY ra.assignment_date DESC;

-- 查询工单的所有分配记录
SELECT ra.assignment_id, ra.personnel_id, ra.assignment_date, ra.status,
       ra.hours_worked, ra.labor_cost_for_personnel,
       rp.full_name, rp.work_type, rp.hourly_rate
FROM RepairAssignments ra
JOIN RepairPersonnel rp ON ra.personnel_id = rp.personnel_id
WHERE ra.order_id = ?;
```

**对应JPA方法**:
```java
// RepairAssignmentRepository.java
List<RepairAssignment> findByRepairPersonnel_PersonnelIdAndStatus(int personnelId, AssignmentStatus status);
List<RepairAssignment> findByRepairOrderOrderId(int orderId);
```

### 5.2 工时和费用更新

**SQL语句**:
```sql
-- 更新工时和费用
UPDATE RepairAssignments
SET hours_worked = ?, labor_cost_for_personnel = ?, status = ?, notes = ?
WHERE assignment_id = ?;

-- 更新工单总费用
UPDATE RepairOrders
SET total_labor_cost = ?, total_material_cost = ?, grand_total_cost = ?
WHERE order_id = ?;
```

## 6. 材料管理模块

### 6.1 库存查询

**功能描述**: 查询材料库存信息，包括低库存预警

**SQL语句**:
```sql
-- 查询所有材料信息
SELECT material_id, material_name, description, stock_quantity, unit_price
FROM Materials
ORDER BY material_name;

-- 查询低库存材料（库存量小于阈值）
SELECT material_id, material_name, stock_quantity, unit_price
FROM Materials
WHERE stock_quantity < ?
ORDER BY stock_quantity ASC;
```

**对应JPQL查询**:
```java
// MaterialRepository.java
@Query("SELECT m FROM Materials m WHERE m.stockQuantity < :threshold")
List<Material> findLowStockMaterials(@Param("threshold") int threshold);
```

### 6.2 材料消耗记录

**SQL语句**:
```sql
-- 记录材料消耗
INSERT INTO OrderMaterialsUsed (order_id, material_id, quantity_used, price_per_unit_at_time_of_use)
VALUES (?, ?, ?, ?);

-- 查询工单的材料消耗记录
SELECT omu.order_material_id, omu.quantity_used, omu.price_per_unit_at_time_of_use, omu.total_cost,
       m.material_name, m.description
FROM OrderMaterialsUsed omu
JOIN Materials m ON omu.material_id = m.material_id
WHERE omu.order_id = ?;

-- 更新材料库存
UPDATE Materials
SET stock_quantity = stock_quantity - ?
WHERE material_id = ?;
```

## 7. 反馈管理模块

### 7.1 反馈查询与统计

**功能描述**: 用户反馈的查询和满意度统计分析

**SQL语句**:
```sql
-- 创建用户反馈
INSERT INTO Feedback (order_id, user_id, rating_score, comments, feedback_date)
VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP);

-- 计算指定时间范围内的平均评分
SELECT AVG(rating_score) as avg_rating
FROM Feedback
WHERE feedback_date >= ? AND feedback_date < ?;

-- 统计指定时间范围内的反馈总数
SELECT COUNT(*) as feedback_count
FROM Feedback
WHERE feedback_date >= ? AND feedback_date < ?;

-- 按评分分组统计数量
SELECT rating_score, COUNT(*) as count
FROM Feedback
WHERE feedback_date >= ? AND feedback_date < ?
GROUP BY rating_score
ORDER BY rating_score;

-- 计算总体平均评分
SELECT AVG(rating_score) as overall_avg_rating FROM Feedback;

-- 统计各评分的数量
SELECT rating_score, COUNT(*) as count
FROM Feedback
GROUP BY rating_score
ORDER BY rating_score;
```

**对应JPQL查询**:
```java
// FeedbackRepository.java
@Query("SELECT AVG(f.ratingScore) FROM Feedback f WHERE f.feedbackDate >= :startDate AND f.feedbackDate < :endDate")
Double getAverageRatingInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

@Query("SELECT COUNT(f) FROM Feedback f WHERE f.feedbackDate >= :startDate AND f.feedbackDate < :endDate")
Long countFeedbackInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

@Query("SELECT f.ratingScore, COUNT(f) FROM Feedback f WHERE f.feedbackDate >= :startDate AND f.feedbackDate < :endDate GROUP BY f.ratingScore")
List<Object[]> getRatingDistributionInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
```

## 8. 统计分析模块

### 8.1 运营监控统计

**功能描述**: 系统运营数据的实时监控和统计分析

**SQL语句**:
```sql
-- 今日工单统计
SELECT COUNT(*) as today_orders
FROM RepairOrders
WHERE DATE(report_date) = CURDATE();

-- 各状态工单数量统计
SELECT 
    SUM(CASE WHEN status = 'Pending Assignment' THEN 1 ELSE 0 END) as pending_orders,
    SUM(CASE WHEN status = 'Assigned' THEN 1 ELSE 0 END) as assigned_orders,
    SUM(CASE WHEN status = 'In Progress' THEN 1 ELSE 0 END) as in_progress_orders,
    SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END) as completed_orders,
    SUM(CASE WHEN status = 'Cancelled by User' THEN 1 ELSE 0 END) as cancelled_orders
FROM RepairOrders;

-- 总用户数统计
SELECT COUNT(*) as total_users FROM Users;

-- 在职维修人员数统计
SELECT COUNT(*) as active_personnel FROM RepairPersonnel WHERE is_active = true;
```

### 8.2 财务统计分析

**功能描述**: 收入、成本等财务数据的统计分析

**SQL语句**:
```sql
-- 本月完成工单的财务统计
SELECT 
    COUNT(*) as completed_orders_count,
    SUM(grand_total_cost) as total_revenue,
    SUM(total_material_cost) as material_revenue,
    SUM(total_labor_cost) as labor_revenue,
    AVG(grand_total_cost) as avg_order_value,
    MAX(grand_total_cost) as max_order_value
FROM RepairOrders
WHERE status = 'Completed' 
  AND YEAR(completion_date) = YEAR(CURDATE())
  AND MONTH(completion_date) = MONTH(CURDATE());

-- 按月份统计收入趋势
SELECT 
    YEAR(completion_date) as year,
    MONTH(completion_date) as month,
    COUNT(*) as orders_count,
    SUM(grand_total_cost) as monthly_revenue,
    SUM(total_material_cost) as material_cost,
    SUM(total_labor_cost) as labor_cost
FROM RepairOrders
WHERE status = 'Completed'
  AND completion_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
GROUP BY YEAR(completion_date), MONTH(completion_date)
ORDER BY year DESC, month DESC;
```

### 8.3 车型维修统计

**功能描述**: 分析不同车型的维修情况和故障类型

**SQL语句**:
```sql
-- 统计各车型维修次数和平均费用
SELECT 
    v.make,
    v.model,
    COUNT(ro.order_id) as repair_count,
    AVG(ro.grand_total_cost) as avg_repair_cost,
    SUM(ro.grand_total_cost) as total_repair_cost
FROM Vehicles v
JOIN RepairOrders ro ON v.vehicle_id = ro.vehicle_id
WHERE ro.status = 'Completed'
GROUP BY v.make, v.model
HAVING repair_count > 0
ORDER BY repair_count DESC, avg_repair_cost DESC;

-- 分析特定车型最常出现的故障类型
SELECT 
    ft.fault_name,
    COUNT(rof.order_fault_id) as fault_count,
    ROUND(COUNT(rof.order_fault_id) * 100.0 / 
          (SELECT COUNT(*) FROM RepairOrderFaults rof2 
           JOIN RepairOrders ro2 ON rof2.order_id = ro2.order_id
           JOIN Vehicles v2 ON ro2.vehicle_id = v2.vehicle_id
           WHERE v2.make = ? AND v2.model = ?), 2) as fault_percentage
FROM FaultTypes ft
JOIN RepairOrderFaults rof ON ft.fault_type_id = rof.fault_type_id
JOIN RepairOrders ro ON rof.order_id = ro.order_id
JOIN Vehicles v ON ro.vehicle_id = v.vehicle_id
WHERE v.make = ? AND v.model = ?
GROUP BY ft.fault_type_id, ft.fault_name
ORDER BY fault_count DESC;
```

### 8.4 人员工作量统计

**SQL语句**:
```sql
-- 统计维修人员工作量
SELECT 
    rp.personnel_id,
    rp.full_name,
    rp.work_type,
    COUNT(ra.assignment_id) as total_assignments,
    SUM(ra.hours_worked) as total_hours,
    SUM(ra.labor_cost_for_personnel) as total_earnings,
    AVG(ra.hours_worked) as avg_hours_per_assignment
FROM RepairPersonnel rp
LEFT JOIN RepairAssignments ra ON rp.personnel_id = ra.personnel_id
WHERE rp.is_active = true
  AND ra.status = 'Work Completed'
  AND ra.assignment_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY rp.personnel_id, rp.full_name, rp.work_type
ORDER BY total_assignments DESC;

-- 统计不同工种的任务量和占比
SELECT 
    rp.work_type,
    COUNT(ra.assignment_id) as task_count,
    SUM(ra.hours_worked) as total_hours,
    ROUND(COUNT(ra.assignment_id) * 100.0 / 
          (SELECT COUNT(*) FROM RepairAssignments WHERE status = 'Work Completed'), 2) as task_percentage
FROM RepairPersonnel rp
JOIN RepairAssignments ra ON rp.personnel_id = ra.personnel_id
WHERE ra.status = 'Work Completed'
  AND ra.assignment_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY rp.work_type
ORDER BY task_count DESC;
```

## 9. 复合查询和报表

### 9.1 综合运营报表

**SQL语句**:
```sql
-- 综合运营日报
SELECT 
    DATE(CURDATE()) as report_date,
    (SELECT COUNT(*) FROM RepairOrders WHERE DATE(report_date) = CURDATE()) as new_orders,
    (SELECT COUNT(*) FROM RepairOrders WHERE status = 'Completed' AND DATE(completion_date) = CURDATE()) as completed_orders,
    (SELECT COUNT(*) FROM RepairOrders WHERE status = 'Pending Assignment') as pending_orders,
    (SELECT COUNT(*) FROM RepairOrders WHERE status = 'In Progress') as in_progress_orders,
    (SELECT COALESCE(SUM(grand_total_cost), 0) FROM RepairOrders WHERE status = 'Completed' AND DATE(completion_date) = CURDATE()) as daily_revenue,
    (SELECT COUNT(*) FROM RepairPersonnel WHERE is_active = true) as active_personnel,
    (SELECT COUNT(*) FROM Materials WHERE stock_quantity < 20) as low_stock_materials;
```

### 9.2 客户满意度分析报表

**SQL语句**:
```sql
-- 客户满意度详细分析
SELECT 
    DATE_FORMAT(f.feedback_date, '%Y-%m') as month,
    COUNT(f.feedback_id) as feedback_count,
    AVG(f.rating_score) as avg_rating,
    SUM(CASE WHEN f.rating_score >= 4 THEN 1 ELSE 0 END) as positive_feedback,
    SUM(CASE WHEN f.rating_score <= 2 THEN 1 ELSE 0 END) as negative_feedback,
    ROUND(SUM(CASE WHEN f.rating_score >= 4 THEN 1 ELSE 0 END) * 100.0 / COUNT(f.feedback_id), 2) as satisfaction_rate
FROM Feedback f
WHERE f.feedback_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
GROUP BY DATE_FORMAT(f.feedback_date, '%Y-%m')
ORDER BY month DESC;

-- 负面反馈工单及涉及员工分析
SELECT 
    f.feedback_id,
    f.order_id,
    f.rating_score,
    f.comments,
    u.full_name as customer_name,
    v.license_plate,
    GROUP_CONCAT(rp.full_name SEPARATOR ', ') as assigned_personnel
FROM Feedback f
JOIN RepairOrders ro ON f.order_id = ro.order_id
JOIN Users u ON f.user_id = u.user_id
JOIN Vehicles v ON ro.vehicle_id = v.vehicle_id
LEFT JOIN RepairAssignments ra ON ro.order_id = ra.order_id
LEFT JOIN RepairPersonnel rp ON ra.personnel_id = rp.personnel_id
WHERE f.rating_score <= 2
GROUP BY f.feedback_id, f.order_id, f.rating_score, f.comments, u.full_name, v.license_plate
ORDER BY f.feedback_date DESC;
```

## 10. 数据维护和完整性

### 10.1 数据清理和维护

**SQL语句**:
```sql
-- 清理超过一年的已完成工单的详细记录（保留主记录）
DELETE FROM OrderMaterialsUsed 
WHERE order_id IN (
    SELECT order_id FROM RepairOrders 
    WHERE status = 'Completed' 
    AND completion_date < DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
);

-- 归档历史数据
CREATE TABLE RepairOrders_Archive AS
SELECT * FROM RepairOrders
WHERE status = 'Completed' 
AND completion_date < DATE_SUB(CURDATE(), INTERVAL 2 YEAR);

-- 数据一致性检查
SELECT 'Inconsistent total cost' as issue, COUNT(*) as count
FROM RepairOrders
WHERE grand_total_cost != (COALESCE(total_material_cost, 0) + COALESCE(total_labor_cost, 0))

UNION ALL

SELECT 'Orders without assignments' as issue, COUNT(*) as count
FROM RepairOrders ro
LEFT JOIN RepairAssignments ra ON ro.order_id = ra.order_id
WHERE ro.status IN ('Assigned', 'In Progress') AND ra.assignment_id IS NULL;
```

### 10.2 触发器和存储过程支持

**功能描述**: 用于维护数据一致性的触发器示例

**SQL语句**:
```sql
-- 工单状态更新时自动计算总费用的触发器
DELIMITER //
CREATE TRIGGER tr_update_order_total_cost
BEFORE UPDATE ON RepairOrders
FOR EACH ROW
BEGIN
    IF NEW.status = 'Completed' THEN
        SET NEW.grand_total_cost = COALESCE(NEW.total_material_cost, 0) + COALESCE(NEW.total_labor_cost, 0);
    END IF;
END //
DELIMITER ;

-- 材料使用后自动更新库存的触发器
DELIMITER //
CREATE TRIGGER tr_update_material_stock
AFTER INSERT ON OrderMaterialsUsed
FOR EACH ROW
BEGIN
    UPDATE Materials 
    SET stock_quantity = stock_quantity - NEW.quantity_used
    WHERE material_id = NEW.material_id;
END //
DELIMITER ;

-- 批量生成工时费记录的存储过程
DELIMITER //
CREATE PROCEDURE sp_generate_payroll_records(
    IN p_period_start DATE,
    IN p_period_end DATE
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_personnel_id INT;
    DECLARE v_total_hours DECIMAL(10,2);
    DECLARE v_total_amount DECIMAL(12,2);
    
    DECLARE personnel_cursor CURSOR FOR
        SELECT 
            rp.personnel_id,
            COALESCE(SUM(ra.hours_worked), 0) as total_hours,
            COALESCE(SUM(ra.labor_cost_for_personnel), 0) as total_amount
        FROM RepairPersonnel rp
        LEFT JOIN RepairAssignments ra ON rp.personnel_id = ra.personnel_id
            AND ra.assignment_date >= p_period_start 
            AND ra.assignment_date < p_period_end
            AND ra.status = 'Work Completed'
        WHERE rp.is_active = true
        GROUP BY rp.personnel_id;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN personnel_cursor;
    
    read_loop: LOOP
        FETCH personnel_cursor INTO v_personnel_id, v_total_hours, v_total_amount;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        IF v_total_hours > 0 THEN
            INSERT INTO PayrollRecords (
                personnel_id, payment_date, period_start_date, period_end_date,
                total_hours_worked, total_amount_paid
            ) VALUES (
                v_personnel_id, CURDATE(), p_period_start, p_period_end,
                v_total_hours, v_total_amount
            );
        END IF;
    END LOOP;
    
    CLOSE personnel_cursor;
END //
DELIMITER ;
```

## 总结

本文档涵盖了车辆维修管理系统的核心SQL语句，包括：

1. **基础CRUD操作**: 用户、车辆、工单、维修人员等基本数据的增删改查
2. **复杂查询**: 多表关联查询、条件查询、分组统计等
3. **统计分析**: 运营监控、财务分析、满意度统计等业务报表
4. **数据维护**: 数据清理、一致性检查、触发器和存储过程

这些SQL语句通过Spring Boot + JPA框架实现，结合了原生SQL的高性能和JPA的开发便利性，能够满足车辆维修管理系统的各种业务需求。系统采用合理的索引策略和查询优化，确保在数据量增长的情况下仍能保持良好的性能表现。 