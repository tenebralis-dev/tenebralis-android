# 🌌 【界影浮光】 Tenebralis Dream System Kotlin Client

> "Echoes of the void, rendered in light."
> 虚空的回响，于光影中显现。

![System Status](https://img.shields.io/badge/System-Initializing-yellow?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-Client-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-UI-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/License-AGPL_v3-blue.svg?style=for-the-badge)

## 📂 System Overview（系统概述）

**Tenebralis（界影浮光）** 是一个以"伪 OS / 幻想终端"为交互形态的沉浸式世界观载体——不是"聊天应用"，而是 **"快穿系统手机"**。

本仓库为 **Kotlin + Jetpack Compose 原生客户端**，目标平台为 **Android（首选）** + **Windows 桌面端（Compose Multiplatform 预留）**。以统一的 Dream OS 桌面拟态体验，承载身份系统、世界档案、AI 多 Agent 叙事、任务成就、个人数据工具等完整功能闭环。

**核心体验**：进入异世界 → 扮演身份 → 与 NPC/系统助手持续互动 → 完成任务、提升关系 → 将现实数据注入 AI 形成"现实-异世界"叙事循环。

当前版本：`0.1.0-alpha`（工程骨架阶段）

## 🛠️ Tech Stack（技术架构）

| 层次 | 技术选型 | 说明 |
|------|---------|------|
| **UI 框架** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | Android 原生声明式 UI |
| **设计系统** | [Material 3](https://m3.material.io/) | Material You 动态取色 + DreamOS 幻想风主题 |
| **架构模式** | MVVM | ViewModel + StateFlow + Repository |
| **依赖注入** | [Hilt](https://dagger.dev/hilt/) | 基于 Dagger 的编译期注入 |
| **后端交互** | [supabase-kt](https://github.com/supabase-community/supabase-kt) | GoTrue（Auth）/ Postgrest（数据）/ Realtime（订阅）/ Storage（文件）|
| **网络层** | [Ktor Client](https://ktor.io/) | supabase-kt 内置 + AI API 自定义调用 |
| **本地存储** | DataStore + Room | 偏好设置 + 离线缓存与草稿 |
| **序列化** | [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) | 与 supabase-kt / Ktor 无缝配合 |
| **图片加载** | [Coil 3](https://coil-kt.github.io/coil/) | Kotlin 优先，Compose 原生支持 |
| **导航** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) | Type-Safe Navigation |
| **构建系统** | Gradle Kotlin DSL + Version Catalog | `libs.versions.toml` 统一管理依赖版本 |

## 🏛️ Architecture（分层架构）

```
┌─────────────────────────────────────────────────┐
│               Presentation 层                    │
│  Screen (Composable) ← ViewModel (StateFlow)    │
│  Navigation · Theme · Components                │
├─────────────────────────────────────────────────┤
│                 Domain 层                        │
│  UseCase · Domain Model · Repository 接口        │
│  （纯 Kotlin，无 Android 依赖，为 KMP 预留）       │
├─────────────────────────────────────────────────┤
│                  Data 层                         │
│  Repository 实现                                 │
│  ├── Remote: Supabase DTO + DataSource           │
│  └── Local:  Room Entity + DAO + DataStore       │
├─────────────────────────────────────────────────┤
│                   DI 层                          │
│  Hilt Modules (Supabase Client, Room DB,         │
│  Repository, UseCase)                            │
└─────────────────────────────────────────────────┘
```

**数据流向（单向）**：
- UI 事件 → ViewModel → UseCase → Repository → Supabase / Room
- 数据响应 → Repository (Flow) → UseCase → ViewModel (StateFlow) → UI 重组

## 📱 Information Architecture（信息架构 · Dream OS）

### Dock 栏（常驻底部）
梦境 · 对话 · 任务 · 档案

### 主界面第 1 页
好感 · 身份 · 世界 · 论坛 · 商店 · 成就

### 主界面第 2 页
备忘 · 账本 · 相册 · 日历 · 番茄钟 · 音乐

### 主界面第 3 页
自定义 · 连接 · 记忆 · 设置

## 🚀 Initialization（启动指南）

如果你想在本地复刻这个世界：

1. **Clone the repository**

```bash
git clone https://github.com/tenebralis-dev/tenebralis-android.git
```

2. **Environment Requirements（环境要求）**

- Android Studio Meerkat (2025.1+) 或更高版本
- JDK 11+
- Android SDK 36（compileSdk）
- Gradle 9.2.1（已内置 Wrapper）

3. **Supabase Configuration（后端配置）**

本项目使用 [Supabase](https://supabase.com/) 作为后端。在 `local.properties` 中配置你的 Supabase 凭据：

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
```

> **注意**：`local.properties` 已在 `.gitignore` 中忽略，请勿提交包含敏感信息的配置。

4. **Sync & Build（同步与构建）**

```bash
# Gradle Sync
./gradlew --refresh-dependencies

# Debug 构建
./gradlew assembleDebug
```

5. **Ignite（启动）**

```bash
# 在连接的 Android 设备或模拟器上运行
./gradlew installDebug
```

或直接在 Android Studio 中点击 Run 按钮。

## 🏗️ Build（构建与发布）

Android APK：

```bash
./gradlew assembleRelease
```

Android App Bundle：

```bash
./gradlew bundleRelease
```

清理构建缓存：

```bash
./gradlew clean
```

## 📁 Project Structure（项目结构）

```
TenebralisApp/
├── app/                              -- Android 应用模块
│   └── src/main/kotlin/com/tenebralis/dreamos/
│       ├── TenebralisApp.kt          -- @HiltAndroidApp 入口
│       ├── MainActivity.kt           -- @AndroidEntryPoint 单 Activity
│       ├── di/                       -- Hilt 依赖注入模块
│       ├── data/                     -- 数据层
│       │   ├── remote/dto/           -- Supabase DTO（28 张表）
│       │   ├── remote/datasource/    -- 远端数据源
│       │   ├── local/                -- Room + DataStore
│       │   ├── repository/           -- Repository 实现
│       │   └── mapper/               -- DTO ↔ Domain 映射
│       ├── domain/                   -- 领域层（纯 Kotlin）
│       │   ├── model/                -- 领域模型
│       │   │   └── enums/            -- 全局约束枚举
│       │   ├── repository/           -- Repository 接口
│       │   └── usecase/              -- 业务用例
│       ├── presentation/             -- 表现层
│       │   ├── navigation/           -- NavGraph + 路由
│       │   ├── theme/                -- DreamOS 幻想风主题
│       │   ├── components/           -- 公共可复用组件
│       │   └── screens/              -- 各 App 屏幕（20+ 功能模块）
│       └── util/                     -- 工具类
├── prd/                              -- 产品需求文档
├── docs/                             -- 数据库设计文档
├── gradle/
│   └── libs.versions.toml            -- Version Catalog
├── build.gradle.kts                  -- 根构建文件
└── settings.gradle.kts
```

## 🗄️ Data Architecture（数据架构）

基于 Supabase (Postgres + JSONB) 的 28 张核心表：

| 领域 | 涉及表 |
|------|-------|
| **核心身份** | `users`、`user_settings` |
| **世界系统** | `worlds`、`user_world_identities`、`world_save_states` |
| **AI 交互** | `npcs`、`conversations`、`conversation_messages`、`user_npc_relationships` |
| **任务成就** | `tasks`、`user_tasks`、`achievements`、`user_achievements` |
| **社交经济** | `world_npc_personas`、`forum_posts`、`forum_comments`、`currency_accounts`、`currency_transactions`、`shop_items`、`user_inventory` |
| **个人数据** | `user_notes`、`user_calendar`、`user_ledger`、`user_media`、`pomodoro_sessions` |
| **系统配置** | `api_connections`、`ui_theme_presets` |
| **全局记忆** | `global_memories` |

## 📜 Roadmap（开发计划）

### Phase 1：主干可运行
- [x] **A1 工程骨架**：Hilt + supabase-kt + Navigation Compose + DreamOS 主题系统
- [ ] **A2 约束枚举**：13 个全局约束枚举定义（ScopeType、AiVisibility 等）
- [ ] **A3 数据基建**：Phase 1 涉及 10 张表的 DTO / Domain Model / Mapper / Repository
- [ ] **B2 Auth 流程**：登录/注册/登出 + session 管理 + 启动路由守卫
- [ ] **B3 DreamOS 桌面**：三页 Pager + 图标网格 + Dock + 状态栏 + 动态主题
- [ ] **B4 入梦主链路**：worlds → identities → save_states
- [ ] **B5 对话系统**：会话线程 + 消息持久化 + Chat UI

### Phase 2：成长反馈闭环
- [ ] **任务系统**：任务列表 + 进度推进 + 完成领奖
- [ ] **成就系统**：成就解锁与进度展示
- [ ] **好感系统**：NPC 好感度管理与展示

### Phase 3：现实同步闭环
- [ ] **个人数据 CRUD**：备忘 / 日历 / 账本 / 相册 / 番茄钟
- [ ] **AI 可见性控制**：`ai_visibility` + `scope_type` 精细管控
- [ ] **现实数据注入**：至少 2 类现实数据可被 AI 正确读取并影响叙事

### Phase 4：生态模块
- [ ] **论坛系统**：发帖评论 + NPC 发言 + 可见性配置
- [ ] **商店与背包**：商品购买 + 库存管理 + 积分流水
- [ ] **经济系统**：货币账户 + 交易记录

### Phase 5：记忆中枢
- [ ] **记忆管理**：global_memories 增删改查 + 置顶/归档/过期
- [ ] **聊天召回**：跨会话长期记忆引用与上下文注入

## 🧠 AI Context Assembly（AI 上下文编排）

对话请求的 Prompt 组装顺序：

1. **系统层** — 应用固定 System Prompt
2. **世界层** — `worlds.prompt_lore_text` + 规则 + AI 上下文
3. **身份层** — `user_world_identities.prompt_identity_text` + 角色设定
4. **存档层** — `world_save_states.prompt_progress_text` + 状态快照
5. **关系层** — `user_npc_relationships`（好感、状态）
6. **用户数据** — 按 `ai_visibility` 过滤的个人数据
7. **全局记忆** — `global_memories` 按置顶/重要度/最近召回排序取 Top N
8. **近期消息** — `conversation_messages` 最近若干条

## ⚖️ Protocol & Licensing（协议与授权）

### 🌍 Community Edition（社区版）
本系统源代码遵循 **GNU AGPLv3** 协议开源。

- ✅ **免费使用**：适合个人学习、开源项目或非营利性研究。
- 🔄 **强制开源**：如果你将本系统部署为网络服务供他人使用，你**必须**公开你的修改源码。

### 💼 Commercial / Enterprise（商业/企业版）
如果你希望将 **Tenebralis** 用于商业闭源产品，或不希望遵守 AGPL 的开源义务，你需要获取 **商业授权（Commercial License）**。

商业授权提供：
- 🔒 **私有化部署豁免**：无需公开你的源代码。
- 🛡️ **法律保障**：提供正式的软件授权合同。
- 🔧 **优先支持**：获得系统管理员（Kirenath）的直接技术支持。

> **System Alert**：检测到潜在的商业合作意向？
> 请通过邮件联系管理员进行协议升级：`kirenath@tuta.io`

---
*Generated by Kirenath | 2026*
