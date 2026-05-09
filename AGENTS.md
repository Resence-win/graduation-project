# AGENTS.md

本文件用于规范 AI Agent 在本仓库内的代码修改、文档维护与验证方式。适用范围为仓库根目录及其所有子目录。

## 项目概览

- 项目名称：校园一卡通管理系统毕业设计项目。
- 前端目录：`campus-card-web`，技术栈为 Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts。
- 后端目录：`campus-card-server`，技术栈为 Spring Boot 3、Java 17、MyBatis-Plus、OpenGauss JDBC、Redis、EasyExcel。
- 数据库与升级脚本：根目录 `init.sql` 与 `sql/`。
- 项目文档：根目录中文 Markdown 文件，如 `接口文档.md`、`数据库设计.md`、`整体流程.md`、`系统架构图.md`、`ER图.md` 等。

## 通用工作原则

- 优先保持现有目录结构、命名风格和业务边界，不做无关重构。
- 修改应聚焦用户提出的问题，避免顺手改动无关文件。
- 涉及前后端联动时，同步检查接口路径、请求参数、响应字段和页面展示逻辑。
- 涉及数据库字段、表结构或枚举值时，同步更新 SQL 脚本、实体类、DTO、Mapper、Service、接口文档和前端调用。
- 不提交真实密码、密钥、个人隐私信息或本机环境路径。
- 不主动执行破坏性 Git 操作，例如 `git reset --hard`、强制推送或删除用户文件。

## 前端规范

- 页面文件放在 `campus-card-web/src/views/`，按业务模块分目录。
- 接口封装放在 `campus-card-web/src/api/`，优先复用 `src/utils/request.js` 中的 Axios 实例。
- 全局状态放在 `campus-card-web/src/store/`，路由配置放在 `campus-card-web/src/router/`。
- 组件命名使用 PascalCase，普通变量和函数使用 camelCase。
- Vue 单文件组件保持 `<template>`、`<script>`、`<style>` 的清晰分区。
- 新增页面优先复用 Element Plus 组件，保持现有后台管理界面的视觉和交互一致。
- 表格、表单、弹窗、分页、查询条件应与现有页面写法保持一致。
- 不在组件中硬编码后端基础地址，应通过已有请求封装统一处理。

## 后端规范

- Java 版本为 17，Spring Boot 版本为 3.2.x。
- 包根路径为 `com.qms.campuscard`。
- 控制器放在 `controller`，业务接口放在 `service`，实现类放在 `service/impl`。
- 实体类放在 `entity`，请求或响应对象放在 `dto`，Mapper 放在 `mapper`。
- 接口响应优先使用项目已有的 `Result` 结构，异常处理优先接入 `GlobalExceptionHandler`。
- 控制器只负责参数接收、基础校验和响应封装，核心业务逻辑放在 Service 层。
- 数据访问优先使用 MyBatis-Plus 既有 Mapper 和条件构造器，避免在 Controller 中直接写查询逻辑。
- 涉及金额、余额、数量等字段时，注意精度、非负校验和事务一致性。
- 涉及登录、角色、权限、卡状态、审批状态等业务规则时，先查找现有实现并保持语义一致。
- Redis 缓存逻辑应集中复用 `RedisUtil` 或已有配置，不在业务代码中散落重复连接配置。

## 数据库与 SQL 规范

- 初始化结构优先维护 `init.sql`。
- 增量变更脚本放入 `sql/`，文件名应表达业务含义，例如 `xxx_upgrade.sql` 或 `add_xxx_column.sql`。
- 表字段命名优先使用 snake_case，并与 Java 实体字段形成清晰映射。
- 修改表结构时，同步检查实体类注解、DTO、前端字段和接口文档。
- 不直接删除历史升级脚本，除非用户明确要求。

## 文档规范

- 用户面向文档优先使用中文。
- 新增或修改接口时，同步更新 `接口文档.md`。
- 修改业务流程时，同步更新 `整体流程.md` 或相关专题文档。
- 修改数据库结构时，同步更新 `数据库设计.md`、`ER图.md` 或对应 SQL 文件。
- 文档中的接口路径、请求字段、响应字段应与代码保持一致。

## 常用命令

前端：

```bash
cd campus-card-web
npm install
npm run dev
npm run build
```

后端：

```bash
cd campus-card-server
mvn test
mvn spring-boot:run
mvn package
```

## 验证要求

- 修改前端代码后，优先运行 `npm run build` 检查构建。
- 修改后端代码后，优先运行 `mvn test` 或至少运行 `mvn package` 检查编译。
- 若验证失败，先判断是否由本次改动引入；不要修复无关历史问题，只需在回复中说明。
- 若因依赖、数据库、Redis 或本机环境限制无法验证，应在最终回复中明确说明。

## Agent 回复规范

- 默认使用中文回复。
- 最终回复应简洁说明：改了什么、涉及哪些文件、是否验证、还有哪些注意事项。
- 引用文件路径时使用可点击路径，并尽量带上起始行号。
- 不输出大段完整文件内容，除非用户明确要求。
- 如果需要用户确认范围或业务规则，先提出简短问题；若能根据现有代码合理推断，则直接执行。

## 特别注意

- 本项目是毕业设计类项目，优先保证功能完整、结构清晰、文档可说明。
- 不为追求复杂架构而引入额外框架或中间件。
- 保持前后端字段命名、枚举含义、状态流转和中文页面文案一致。
- 上传文件、图片和本地 IDE 配置通常不应被无关修改。
