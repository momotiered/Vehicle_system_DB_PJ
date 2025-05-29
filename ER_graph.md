    erDiagram
        Users {
            int user_id PK "用户ID (主键)"
            varchar username "登录用户名 (唯一)"
            varchar password_hash "密码哈希"
            varchar full_name "用户全名"
            varchar contact_phone "联系电话 (唯一)"
            varchar contact_email "联系邮箱 (唯一)"
            text address "联系地址"
            timestamp registration_date "注册日期"
        }

        Vehicles {
            int vehicle_id PK "车辆ID (主键)"
            int user_id FK "车主用户ID (外键)"
            varchar license_plate "车牌号 (唯一)"
            varchar make "品牌"
            varchar model "型号"
            year year_of_manufacture "制造年份"
            varchar vin "车辆识别码 (唯一)"
            varchar color "颜色"
            timestamp registration_date "车辆信息录入日期"
        }

        RepairPersonnel {
            int personnel_id PK "维修人员ID (主键)"
            varchar username "登录用户名 (唯一)"
            varchar password_hash "密码哈希"
            varchar full_name "维修人员全名"
            varchar work_type "工种"
            decimal hourly_rate "时薪"
            varchar contact_phone "联系电话 (唯一)"
            date hire_date "入职日期"
            boolean is_active "是否在职"
        }

        RepairOrders {
            int order_id PK "工单ID (主键)"
            int vehicle_id FK "维修车辆ID (外键)"
            int user_id FK "报修用户ID (外键)"
            text description_of_issue "故障描述"
            timestamp report_date "报修日期"
            datetime start_date "维修开始日期"
            datetime estimated_completion_date "预计完成日期"
            datetime completion_date "实际完成日期"
            enum status "工单状态"
            decimal total_material_cost "总材料费"
            decimal total_labor_cost "总工时费"
            decimal grand_total_cost "总费用"
            text notes "维修备注"
        }

        Materials {
            int material_id PK "材料ID (主键)"
            varchar material_name "材料名称 (唯一)"
            text description "材料描述"
            int stock_quantity "库存数量"
            decimal unit_price "当前单位价格"
        }

        FaultTypes {
            int fault_type_id PK "故障类型ID (主键)"
            varchar fault_name "故障类型名称 (唯一)"
            text description "故障类型描述"
        }

        RepairAssignments {
            int assignment_id PK "分配ID (主键)"
            int order_id FK "维修工单ID (外键)"
            int personnel_id FK "维修人员ID (外键)"
            timestamp assignment_date "分配日期"
            enum status "分配状态"
            decimal hours_worked "工时"
            decimal labor_cost_for_personnel "工时费"
            text notes "维修人员备注"
        }

        OrderMaterialsUsed {
            int order_material_id PK "工单材料ID (主键)"
            int order_id FK "维修工单ID (外键)"
            int material_id FK "材料ID (外键)"
            int quantity_used "使用数量"
            decimal price_per_unit_at_time_of_use "使用时单价"
            decimal total_cost "该材料总费用"
        }

        RepairOrderFaults {
            int order_fault_id PK "工单故障关联ID (主键)"
            int order_id FK "维修工单ID (外键)"
            int fault_type_id FK "故障类型ID (外键)"
        }

        Feedback {
            int feedback_id PK "反馈ID (主键)"
            int order_id FK "关联工单ID (外键, 唯一)"
            int user_id FK "用户ID (外键)"
            tinyint rating_score "评分"
            text comments "评论"
            timestamp feedback_date "反馈日期"
        }

        PayrollRecords {
            int payroll_record_id PK "发放记录ID (主键)"
            int personnel_id FK "维修人员ID (外键)"
            date payment_date "发放日期"
            date period_start_date "薪资周期开始"
            date period_end_date "薪资周期结束"
            decimal total_hours_worked "总工时"
            decimal total_amount_paid "发放总金额"
            text notes "备注"
        }

        Administrators {
            int admin_id PK "管理员ID (主键)"
            varchar username "登录用户名 (唯一)"
            varchar password_hash "密码哈希"
            varchar full_name "管理员全名"
            varchar email "联系邮箱 (唯一)"
            timestamp last_login "上次登录时间"
        }

        Users ||--o{ Vehicles : "拥有 (owns)"
        Users ||--o{ RepairOrders : "报修 (reports)"
        Vehicles ||--o{ RepairOrders : "关联 (related to)"
        Users ||--o{ Feedback : "提交 (submits)"

        RepairOrders }o--|| RepairAssignments : "包含 (includes)"
        RepairPersonnel ||--o{ RepairAssignments : "分配给 (assigned to)"

        RepairOrders }o--|| OrderMaterialsUsed : "消耗 (consumes)"
        Materials ||--o{ OrderMaterialsUsed : "用于 (used in)"

        RepairOrders }o--|| RepairOrderFaults : "具有 (has)"
        FaultTypes ||--o{ RepairOrderFaults : "归类于 (classified as)"

        RepairOrders }o--o| Feedback : "获得 (receives)"
        RepairPersonnel ||--o{ PayrollRecords : "接收 (receives)"

        Administrators : "管理 (manages system)"