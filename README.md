# 车辆维修管理系统

一个基于Spring Boot + Vue3 + MYsql的车辆维修管理系统演示项目，用于24春季复旦大学数据库课程设计。

## 项目概述

车辆维修管理系统是专为车辆维修业务设计的综合性数据库应用系统，旨在提高维修业务管理效率、降低运营成本，并为客户提供更优质的服务。该系统涵盖车辆信息管理、客户信息管理、维修工单管理、库存管理、财务管理等多个核心模块。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.x
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA + Hibernate
- **构建工具**: Maven
- **Java版本**: JDK 8+

### 前端
- **框架**: Vue 3
- **UI组件库**: Element Plus
- **构建工具**: Vite
- **包管理器**: npm

## 如何运行项目

### 环境要求

在开始之前，请确保您的开发环境已安装以下软件：

1. **Java Development Kit (JDK) 8或更高版本**
   ```bash
   java -version
   javac -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **Node.js 16+ 和 npm**
   ```bash
   node -v
   npm -v
   ```

4. **MySQL 8.0**
   - 确保MySQL服务正在运行
   - 创建数据库用户和数据库

### 数据库配置

1. **创建数据库**
   ```sql
   CREATE DATABASE vehicle_repair_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **创建数据库用户（可选）**
   ```sql
   CREATE USER 'repair_admin'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON vehicle_repair_system.* TO 'repair_admin'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **初始化数据库结构**
   - 数据库表结构会在应用启动时自动创建（Spring Boot的ddl-auto设置）
   - 或者手动执行 `backend_DBPJ/src/main/resources/db/init.sql`

### 后端启动步骤

1. **进入后端项目目录**
   ```bash
   cd backend_DBPJ
   ```

2. **配置数据库连接**
   
   编辑 `src/main/resources/application.yml` 文件，修改数据库连接信息：
   ```yaml
   spring:
     datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://localhost:3306/vehicle_repair_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
       username: repair_admin  # 替换为您的数据库用户名
       password: your_password # 替换为您的数据库密码
   ```

3. **安装依赖和构建项目**
   ```bash
   mvn clean install
   ```

4. **启动后端服务**
   ```bash
   mvn spring-boot:run
   ```
   
   或者使用IDE运行主类：
   ```bash
   # 或者直接运行编译后的jar文件
   mvn clean package
   java -jar target/backend_dbpj-0.0.1-SNAPSHOT.jar
   ```

5. **验证后端启动**
   - 后端服务默认运行在 `http://localhost:9080`
   - 可以访问 `http://localhost:9080/actuator/health` 检查服务状态

### 前端启动步骤

1. **进入前端项目目录**
   ```bash
   cd frontend_DBPJ
   ```

2. **安装依赖**
   ```bash
   npm install
   ```
   
   如果遇到网络问题，可以使用国内镜像：
   ```bash
   npm install --registry=https://registry.npmmirror.com
   ```

3. **配置后端API地址**
   
   检查 `src/api/index.js` 或相关配置文件中的后端API地址：
   ```javascript
   const BASE_URL = 'http://localhost:9080';
   ```

4. **启动前端开发服务器**
   ```bash
   npm run dev
   ```

5. **验证前端启动**
   - 前端应用默认运行在 `http://localhost:5173` (Vite默认端口)
   - 浏览器会自动打开应用页面

### 启动验证清单

完成以上步骤后，请验证以下内容：

- [ ] MySQL数据库服务正在运行
- [ ] 数据库 `vehicle_repair_system` 已创建
- [ ] 后端服务在 `http://localhost:9080` 正常运行
- [ ] 前端应用在 `http://localhost:5173` 正常运行
- [ ] 前后端能够正常通信（检查浏览器控制台是否有API错误）

### 常见问题解决

#### 数据库连接问题
```bash
# 检查MySQL服务状态
sudo systemctl status mysql  # Linux
brew services list | grep mysql  # macOS
net start mysql  # Windows

# 测试数据库连接
mysql -u repair_admin -p -h localhost vehicle_repair_system
```

#### 端口占用问题
```bash
# 检查端口占用情况
netstat -an | grep :9080  # 检查后端端口
netstat -an | grep :5173  # 检查前端端口

# 修改端口配置
# 后端：修改 application.yml 中的 server.port
# 前端：修改 vite.config.js 中的 server.port
```

#### 依赖安装问题
```bash
# 清理Maven缓存
mvn clean

# 清理npm缓存
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### 开发模式 vs 生产模式

#### 开发模式（当前说明）
- 后端：`mvn spring-boot:run`
- 前端：`npm run dev`
- 支持热重载，便于开发调试

#### 生产模式
```bash
# 后端打包
cd backend_DBPJ
mvn clean package -DskipTests

# 前端打包
cd frontend_DBPJ
npm run build

# 部署（将前端dist目录部署到Web服务器，后端jar文件部署到应用服务器）
```

### 默认访问信息

系统启动后，可以使用以下默认账户进行测试：

- **管理员账户**: admin / admin123
- **用户账户**: user001 / password123  
- **维修人员账户**: tech001 / password123

> 注意：这些是演示用的默认账户，实际使用时请修改密码或通过注册功能创建新账户。



