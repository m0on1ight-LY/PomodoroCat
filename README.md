<div align="center">

<img src="docs/images/app_icon.png" width="130" height="130" style="border-radius: 28px; box-shadow: 0 8px 24px rgba(0,0,0,0.12);" alt="番茄猫 PomodoroCat Icon" />

# 🐾 番茄猫 · PomodoroCat

**基于 Jetpack Compose 与 Room 构建的治愈系猫咪陪伴番茄钟与专注养成 Android 应用**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026+-3DDC84?style=flat-square&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Database-Room%20SQLite-F57C00?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![Version](https://img.shields.io/badge/Version-v2.0.0-E91E63?style=flat-square)](https://github.com/m0on1ight-LY/PomodoroCat/releases)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

[📥 下载体验 (Releases)](https://github.com/m0on1ight-LY/PomodoroCat/releases) · [✨ 功能特色](#-核心功能特色) · [🏗️ 技术架构](#-技术架构与选型) · [🚀 快速开始](#-快速开始与构建)

</div>

---

## 📖 项目简介 (Overview)

**番茄猫 (PomodoroCat)** 是一款将 **经典番茄工作法 (Pomodoro Technique)** 与 **治愈系猫咪养成机制** 深度结合的轻量、纯粹、无广告的本地优先 Android 专注工具。

每一段认真的专注时光，都会转化为喂养猫咪的香脆小鱼干 🐟。在柔和的白噪音伴奏下，可爱的猫咪在身旁安静守护；专注结束后记录属于你的专注日记，解锁多种品种猫咪与闪耀成就勋章，让自律成为一件温暖治愈的事。

---

## ✨ 核心功能特色 (Key Features)

### 1. ⏱️ 智能番茄钟专注系统 (`Focus Timer`)
- **自由模式切换**：支持 `[🍅 专注 25m]`、`[☕ 短休 5m]`、`[🌴 长休 15m]` 灵活点选与自定义时长。
- **状态感知控制栏**：严密的状态机设计，支持动态 **开始 / 暂停 / 放弃(重置) / 继续 / 跳过**，绝不出现状态脱节或文案混淆。
- **前台保活服务 (`TimerService`)**：后台倒计时精准不被系统误杀，支持状态栏通知实时常驻进度。

### 2. 🐱 纯过程化矢量猫咪引擎与猫舍养成 (`Cat Sanctuary`)
- **5 大品种猫咪独立图鉴**：
  - 🍊 **元气橘猫** (橘白虎斑纹 · 饱满干饭王)
  - 🎨 **软萌三花** (黑红相间小锦鲤)
  - 🎩 **优雅奶牛** (倒V白鼻梁小警长)
  - ☕ **学霸暹罗** (标志性巧克力糊脸面具 · 湛蓝眼眸)
  - 💎 **贵族英短** (经典高贵纯蓝灰毛 · 铜金眼瞳)
- **亲密度装扮系统**：投喂小鱼干升级亲密度，解锁专属装扮（绅士小领结、微光爱心、守护金皇冠）与个性互动台词。
- **无依赖纯 Canvas 绘制**：不消耗多余位图显存，动画 60fps 丝滑流畅，支持抚摸互动。

### 3. 🏷️ 灵活任务分类标签 (`Task Tags`)
- 预置 **💻 工作**、**📖 学习**、**📚 阅读**、**🏃 运动** 等高频场景。
- 支持自由新建自定义标签、点选精美图标与专属主题色。

### 4. 📊 多维数据复盘仪表盘 (`Analytics & Diary`)
- **时间维度统计**：今日 / 本周 / 本月 / 累计专注时长与完成番茄数。
- **打卡热力方块**：直观反映近 7 天每日专注热度。
- **任务分布环形图**：清晰呈现不同任务标签的时间投入占比。
- **专注日记时间轴**：每次专注结算后支持 1~5 星自评与心得感悟记录。

### 5. 🎧 沉浸式环境白噪音混音台 (`Mixer Engine`)
- 提供 **🌧️ 治愈雨声**、**☕ 温暖咖啡馆**、**🏕️ 篝火噼啪**、**🌊 宁静海浪** 4 大核心音轨。
- 支持独立音量平滑混音与一键静音。

### 6. 🎨 现代 Material 3 视觉与昼夜模式 (`UI/UX`)
- **☀️ / 🌙 顶栏一键切换**：完美支持系统跟随、强制亮色模式与深邃暗夜模式。
- **清爽 M3 底部导航**：移除生硬遮罩色块，精致图标与字体加粗高亮。
- **全新 3D 治愈桌面图标**：全套无损切图与 Android 13+ 自适应图标适配。

---

## 🏗️ 技术架构与选型 (Architecture)

本项目遵循 Google 推荐的 **Modern Android Architecture (MAD)** 架构与 **单向数据流 (UDF)** 设计模式：

```text
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose M3)                  │
│   MainScreen  │  AnalyticsScreen  │  SanctuaryScreen        │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Events
┌──────────────────────────────▼──────────────────────────────┐
│                    Service & State Machine                  │
│       TimerService (Foreground Service) + MixerEngine       │
└──────────────────────────────┬──────────────────────────────┘
                               │ Coroutines Flow
┌──────────────────────────────▼──────────────────────────────┐
│                      Repository Layer                       │
│                     PomodoroRepository                      │
└──────────────────────────────┬──────────────────────────────┘
                               │ Room DAOs
┌──────────────────────────────▼──────────────────────────────┐
│                   Data Layer (Room Database)                │
│   SessionsTable │ TagsTable │ CatsTable │ BadgesTable       │
└─────────────────────────────────────────────────────────────┘
```

- **编程语言**：100% Kotlin
- **UI 框架**：Jetpack Compose + Material 3 (Declarative UI)
- **本地存储**：Room Database (SQLite) + Encrypted Shared Preferences
- **异步响应式**：Kotlin Coroutines + StateFlow / SharedFlow
- **音频引擎**：Android MediaPlayer / ExoPlayer 混音管理
- **绘图引擎**：Compose Pure Canvas Vector Graphics (Zero-bitmap procedural render)

---

## 🚀 快速开始与构建 (Getting Started)

### 环境要求
- **Android Studio**：Hedgehog (2023.1.1) 或更高版本
- **JDK**：OpenJDK 17 / 1.8
- **Gradle**：8.14+
- **最低兼容 Android 版本**：Android 8.0 (API Level 26)
- **目标编译版本**：Android 14 (API Level 34)

### 编译运行步骤

1. **克隆代码仓库**：
   ```bash
   git clone https://github.com/m0on1ight-LY/PomodoroCat.git
   cd PomodoroCat
   ```

2. **使用 Gradle 编译安装包**：
   ```bash
   # 编译 Debug 测试包
   ./gradlew assembleDebug

   # 直接安装至已连接的手机/模拟器
   ./gradlew installDebug
   ```

3. **生成 APK 产物路径**：
   - Debug 包：`app/build/outputs/apk/debug/app-debug.apk`
   - Release 包：`app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 📄 开源许可证 (License)

本项目采用 [MIT License](LICENSE) 许可证开源。欢迎学习交流、提交 Issue 或 PR！🐾

---

<div align="center">
  <sub>Made with ❤️ and 🐾 for all focus lovers.</sub>
</div>
