-- 创建数据库
CREATE DATABASE IF NOT EXISTS vehicle_repair_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vehicle_repair_system;

-- 1. Users (用户信息表)
CREATE TABLE IF NOT EXISTS Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    full_name VARCHAR(255) COMMENT '用户全名',
    contact_phone VARCHAR(50) UNIQUE COMMENT '联系电话',
    contact_email VARCHAR(255) UNIQUE COMMENT '联系邮箱',
    address TEXT COMMENT '联系地址',
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册日期'
) COMMENT '用户信息表';

-- 2. Administrators (系统管理员表)
CREATE TABLE IF NOT EXISTS Administrators (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    full_name VARCHAR(255) COMMENT '管理员全名',
    email VARCHAR(255) UNIQUE COMMENT '联系邮箱',
    last_login TIMESTAMP NULL COMMENT '上次登录时间'
) COMMENT '系统管理员表';

-- 3. Vehicles (车辆信息表)
CREATE TABLE IF NOT EXISTS Vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '车主用户ID',
    license_plate VARCHAR(50) NOT NULL UNIQUE COMMENT '车牌号',
    make VARCHAR(100) COMMENT '品牌 (如: Toyota)',
    model VARCHAR(100) COMMENT '型号 (如: Camry)',
    year_of_manufacture INT COMMENT '制造年份',
    vin VARCHAR(100) UNIQUE COMMENT '车辆识别码 (VIN)',
    color VARCHAR(50) COMMENT '颜色',
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '车辆信息录入日期',
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    INDEX idx_vehicles_user_id (user_id)
) COMMENT '车辆信息表';

-- 4. RepairPersonnel (维修人员信息表)
CREATE TABLE IF NOT EXISTS RepairPersonnel (
    personnel_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    full_name VARCHAR(255) NOT NULL COMMENT '维修人员全名',
    work_type VARCHAR(100) NOT NULL COMMENT '工种 (如: 发动机, 电路, 钣金)',
    hourly_rate DECIMAL(10, 2) NOT NULL COMMENT '时薪',
    contact_phone VARCHAR(50) UNIQUE COMMENT '联系电话',
    hire_date DATE COMMENT '入职日期',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否在职',
    INDEX idx_repairpersonnel_work_type (work_type)
) COMMENT '维修人员信息表';

-- 5. Materials (库存材料表)
CREATE TABLE IF NOT EXISTS Materials (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    material_name VARCHAR(255) NOT NULL UNIQUE COMMENT '材料名称',
    description TEXT COMMENT '材料描述',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT '当前单位价格',
    CHECK (stock_quantity >= 0),
    CHECK (unit_price >= 0)
) COMMENT '库存材料表';

-- 6. FaultTypes (故障类型表)
CREATE TABLE IF NOT EXISTS FaultTypes (
    fault_type_id INT AUTO_INCREMENT PRIMARY KEY,
    fault_name VARCHAR(255) NOT NULL UNIQUE COMMENT '故障类型名称',
    description TEXT COMMENT '故障类型描述'
) COMMENT '故障类型表';

-- 7. RepairOrders (维修工单表)
CREATE TABLE IF NOT EXISTS RepairOrders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL COMMENT '维修车辆ID',
    user_id INT NOT NULL COMMENT '报修用户ID',
    description_of_issue TEXT NOT NULL COMMENT '用户描述的故障问题',
    report_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '报修日期',
    start_date DATETIME NULL COMMENT '维修开始日期',
    estimated_completion_date DATETIME NULL COMMENT '预计完成日期',
    completion_date DATETIME NULL COMMENT '实际完成日期',
    status ENUM(
        'Pending Assignment', 'Assigned', 'In Progress', 'Awaiting Parts', 
        'Completed', 'Cancelled by User', 'Cannot Repair', 'Pending User Confirmation'
    ) NOT NULL DEFAULT 'Pending Assignment' COMMENT '工单状态',
    total_material_cost DECIMAL(12, 2) DEFAULT 0.00 COMMENT '总材料费',
    total_labor_cost DECIMAL(12, 2) DEFAULT 0.00 COMMENT '总工时费',
    grand_total_cost DECIMAL(12, 2) DEFAULT 0.00 COMMENT '总费用',
    notes TEXT COMMENT '维修备注',
    FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE RESTRICT,
    INDEX idx_repairorders_vehicle_id (vehicle_id),
    INDEX idx_repairorders_user_id (user_id),
    INDEX idx_repairorders_status (status),
    INDEX idx_repairorders_report_date (report_date)
) COMMENT '维修工单表';

-- 8. RepairAssignments (维修分配表)
CREATE TABLE IF NOT EXISTS RepairAssignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL COMMENT '维修工单ID',
    personnel_id INT NOT NULL COMMENT '维修人员ID',
    assignment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配日期',
    status ENUM(
        'Assigned', 'Accepted', 'Rejected', 
        'Work Started', 'Work Paused', 'Work Completed'
    ) NOT NULL DEFAULT 'Assigned' COMMENT '分配状态',
    hours_worked DECIMAL(6, 2) DEFAULT 0.00 COMMENT '该人员在此工单上的工时',
    labor_cost_for_personnel DECIMAL(10, 2) DEFAULT 0.00 COMMENT '该人员在此工单上的工时费',
    notes TEXT COMMENT '维修人员备注',
    FOREIGN KEY (order_id) REFERENCES RepairOrders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (personnel_id) REFERENCES RepairPersonnel(personnel_id) ON DELETE RESTRICT,
    UNIQUE KEY uk_order_personnel (order_id, personnel_id),
    INDEX idx_repairassignments_personnel_id (personnel_id),
    INDEX idx_repairassignments_status (status)
) COMMENT '维修分配表';

-- 9. OrderMaterialsUsed (工单材料消耗表)
CREATE TABLE IF NOT EXISTS OrderMaterialsUsed (
    order_material_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL COMMENT '维修工单ID',
    material_id INT NOT NULL COMMENT '材料ID',
    quantity_used INT NOT NULL COMMENT '使用数量',
    price_per_unit_at_time_of_use DECIMAL(10, 2) NOT NULL COMMENT '使用时材料单价',
    total_cost DECIMAL(10, 2) GENERATED ALWAYS AS (quantity_used * price_per_unit_at_time_of_use) STORED COMMENT '该材料总费用',
    FOREIGN KEY (order_id) REFERENCES RepairOrders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES Materials(material_id) ON DELETE RESTRICT,
    CHECK (quantity_used > 0),
    INDEX idx_ordermaterials_material_id (material_id)
) COMMENT '工单材料消耗表';

-- 10. RepairOrderFaults (工单故障关联表)
CREATE TABLE IF NOT EXISTS RepairOrderFaults (
    order_fault_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL COMMENT '维修工单ID',
    fault_type_id INT NOT NULL COMMENT '故障类型ID',
    FOREIGN KEY (order_id) REFERENCES RepairOrders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (fault_type_id) REFERENCES FaultTypes(fault_type_id) ON DELETE RESTRICT,
    UNIQUE KEY uk_order_fault (order_id, fault_type_id),
    INDEX idx_repairorderfaults_fault_type_id (fault_type_id)
) COMMENT '工单故障关联表';

-- 11. Feedback (用户反馈表)
CREATE TABLE IF NOT EXISTS Feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE COMMENT '关联的维修工单ID',
    user_id INT NOT NULL COMMENT '提交反馈的用户ID',
    rating_score TINYINT NULL COMMENT '评分 (1-5)',
    comments TEXT NULL COMMENT '文字评论',
    feedback_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '反馈日期',
    FOREIGN KEY (order_id) REFERENCES RepairOrders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE RESTRICT,
    CHECK (rating_score IS NULL OR (rating_score >= 1 AND rating_score <= 5)),
    INDEX idx_feedback_user_id (user_id),
    INDEX idx_feedback_rating_score (rating_score)
) COMMENT '用户反馈表';

-- 12. PayrollRecords (工时费发放记录表)
CREATE TABLE IF NOT EXISTS PayrollRecords (
    payroll_record_id INT AUTO_INCREMENT PRIMARY KEY,
    personnel_id INT NOT NULL COMMENT '维修人员ID',
    payment_date DATE NOT NULL COMMENT '发放日期',
    period_start_date DATE NOT NULL COMMENT '薪资周期开始日期',
    period_end_date DATE NOT NULL COMMENT '薪资周期结束日期',
    total_hours_worked DECIMAL(10, 2) NOT NULL COMMENT '该周期总工时',
    total_amount_paid DECIMAL(12, 2) NOT NULL COMMENT '该周期发放总金额',
    notes TEXT COMMENT '备注',
    FOREIGN KEY (personnel_id) REFERENCES RepairPersonnel(personnel_id) ON DELETE RESTRICT,
    INDEX idx_payroll_personnel_id (personnel_id),
    INDEX idx_payroll_payment_date (payment_date),
    INDEX idx_payroll_period (period_start_date, period_end_date)
) COMMENT '工时费发放记录表'; 