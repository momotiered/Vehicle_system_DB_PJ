# 数据库演示后端开发快速指南

本文档为车辆维修管理系统提供一个最简化的后端开发路径，旨在快速实现核心功能以**展示数据库设计**，而非构建一个生产级应用。

---

## 第一步：确认项目结构

请确保你的Spring Boot项目包含以下几个核心包，用于组织代码：

- `com.example.vehicle_repair_system.model`: 存放所有数据库表对应的JPA实体类。
- `com.example.vehicle_repair_system.repository`: 存放JPA Repository接口，用于直接与数据库交互。
- `com.example.vehicle_repair_system.service`: 存放核心业务逻辑（可选，简单逻辑可直接写在Controller中）。
- `com.example.vehicle_repair_system.controller`: 存放API接口，供前端调用。

---

## 第二步：创建JPA实体类 (Entity)

这是最关键的一步。根据你的`ER_graph.md`，为**每一个表**创建一个对应的Java类，并放在`model`包下。

**核心要点**:

1.  使用`@Entity`注解标记此类是一个数据库实体。
2.  使用`@Id`和`@GeneratedValue`标记主键。
3.  使用`@ManyToOne`和`@OneToMany`等注解正确表示表之间的**外键关联**。

**示例：`RepairOrders.java`**
```java
package com.example.vehicle_repair_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "repair_orders")
public class RepairOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    private String descriptionOfIssue;
    private Timestamp reportDate;
    private String status; // 使用String简化，如 "待分配", "维修中", "已完成"

    // 关联到车辆 (多对一)
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    // 关联到用户 (多对一)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    // 关联到此工单的分配记录 (一对多)
    @OneToMany(mappedBy = "repairOrder")
    private List<RepairAssignment> assignments;

    // ... 其他字段和关联
}
```
**你需要为ER图中的所有表（如`Users`, `Vehicles`, `RepairPersonnel`等）重复此过程。**

---

## 第三步：开发任务清单（按优先级）

按照以下顺序实现API接口，可以最快地构建出核心业务流程。

### 阶段一：基础数据管理 (管理员)
目标：能够录入系统运行所需的基础信息。

- [ ] **1. 材料管理**: 实现对 `Materials` 表的增、删、改、查 (CRUD)。
- [ ] **2. 维修人员管理**: 实现对 `RepairPersonnel` 表的 CRUD。
- [ ] **3. 故障类型管理**: 实现对 `FaultTypes` 表的 CRUD。

### 阶段二：核心业务 - 工单创建 (用户)
目标：让用户可以提交维修请求。

- [ ] **1. 用户注册**: 提供一个API，向 `Users` 表插入新用户。
- [ ] **2. 车辆管理**: 用户可以为自己名下添加车辆信息到 `Vehicles` 表。
- [ ] **3. 提交维修工单**: 用户选择自己的车辆，提交故障描述，向 `RepairOrders` 表插入一条新记录，状态为“待分配”。

### 阶段三：核心业务 - 工单处理 (管理员 & 维修人员)
目标：模拟工单的流转过程。

- [ ] **1. 分配工单**: 管理员为 `RepairOrders` 表中的待分配工单指派维修人员。这会向 `RepairAssignments` 表插入一条新记录。
- [ ] **2. 更新维修**: 维修人员可以更新分配给自己的任务 (`RepairAssignments`) 状态，并记录消耗的材料。这会向 `OrderMaterialsUsed` 表插入记录，并更新 `Materials` 表的库存。
- [ ] **3. 完成工单**: 维修人员完成维修后，更新 `RepairOrders` 的状态为“已完成”，并计算出总费用填入相应字段。

### 阶段四：查询与反馈
目标：提供必要的信息查询和反馈功能。

- [ ] **1. 用户查询**: 用户可以查询自己所有的 `RepairOrders` 历史记录。
- [ ] **2. 提交反馈**: 用户可以为已完成的工单提交一条 `Feedback` 记录。
- [ ] **3. 维修人员查询**: 维修人员可以查询分配给自己的所有任务 (`RepairAssignments`)。

---

## 第四步：核心API接口参考

以下是实现上述任务清单所需的最小API集合。

| 作用           | 方法 | URL                                       | 请求体/参数示例                                  |
| -------------- | ---- | ----------------------------------------- | ------------------------------------------------ |
| **基础数据**   |      |                                           |                                                  |
| 添加材料       | POST | `/api/materials`                          | `{ "materialName": "机油", "stock": 100 }`        |
| 添加维修人员   | POST | `/api/personnel`                          | `{ "fullName": "张三", "workType": "发动机" }`      |
| **用户操作**   |      |                                           |                                                  |
| 用户注册       | POST | `/api/register`                           | `{ "username": "user1", "password": "123" }`     |
| 用户添加车辆   | POST | `/api/users/{userId}/vehicles`            | `{ "licensePlate": "京A88888" }`                 |
| 用户提交工单   | POST | `/api/repair-orders`                      | `{ "userId": 1, "vehicleId": 1, "issue": "..." }` |
| **工单流程**   |      |                                           |                                                  |
| 管理员分配工单 | POST | `/api/orders/{orderId}/assign`            | `{ "personnelId": 1 }`                            |
| 维修员记录材料 | POST | `/api/orders/{orderId}/materials`         | `{ "materialId": 1, "quantity": 2 }`             |
| 维修员完成工单 | PUT  | `/api/orders/{orderId}/complete`          | (无)                                             |
| **查询**       |      |                                           |                                                  |
| 查询用户工单   | GET  | `/api/users/{userId}/orders`              | (无)                                             |
| 提交反馈       | POST | `/api/feedback`                           | `{ "orderId": 1, "rating": 5, "comment": "..." }` |


---

## 第五步：简化原则（重要）

为了快速开发，请严格遵守以下简化原则：

- **无认证**: **不要做任何登录、鉴权**。所有需要用户ID的地方，直接通过API参数（如 `/api/users/{userId}/...`）传入。
- **无加密**: **密码直接用明文存储**在数据库中。
- **最简业务**: 费用计算直接相加；工单分配直接指定，无需自动分配算法。
- **信任前端**: 假设前端传来的数据都是合法的，后端**只做最基础的非空判断**。
