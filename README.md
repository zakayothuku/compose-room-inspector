# 🗄️ compose-room-inspector

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-MinSDK%2024-green.svg)](https://developer.android.com)

> **In-App SQLite & Room Database Browser and SQL Query Editor with Jetpack Compose Overlay for Android.**

`compose-room-inspector` is a lightweight developer tool that enables mobile developers and QA engineers to **browse tables, inspect live schemas, execute raw SQL queries, and export database records** directly on-device without tethering to USB cables or desktop inspection tools.

<p align="center">
  <img src="docs/room_inspector_preview.jpg" alt="compose-room-inspector UI Preview" width="360" />
</p>

---

## ✨ Features

- 🔍 **Automatic Table & Schema Discovery**: Discovers all user tables, primary keys (`🔑`), data types, and row counts via `PRAGMA table_info`.
- 📊 **2D Scrollable Data Grid**: Sticky column headers, row numbering, and interactive horizontal/vertical scrolling.
- ⚡ **Interactive SQL Console**: Execute arbitrary raw SQL (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `PRAGMA`) with real-time execution timing (`⚡ 12 ms`) and autocomplete snippets.
- 🔎 **Instant Row Filtering & Pagination**: Search records across all columns in real-time with configurable page sizing.
- 📋 **1-Tap Export**: Export table rows directly as CSV or formatted JSON to the clipboard.
- 📱 **Floating Jetpack Compose Overlay**: Non-intrusive floating badge and expandable Material 3 bottom-sheet.

---

## 📦 Installation

Add JitPack to your `settings.gradle.kts`:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    debugImplementation("com.github.zakayothuku:compose-room-inspector:v1.0.0")
}
```

---

## 🚀 Quickstart

### 1. Register Database in Application / Activity

```kotlin
// Option A: With AndroidX Room Database
ComposeRoomInspector.registerRoomDatabase("AppDatabase", roomDatabase)

// Option B: With SupportSQLiteDatabase
ComposeRoomInspector.registerDatabase("MainDb", sqliteDatabase)
```

### 2. Attach Floating Compose UI Overlay

Place `ComposeRoomInspectorOverlay()` inside your top-level `Surface` or `Scaffold`:

```kotlin
@Composable
fun AppContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        YourMainNavigation()

        // Attach floating Room DB inspector overlay
        ComposeRoomInspectorOverlay()
    }
}
```

---

## 🧪 Testing

Run library unit tests:

```bash
./gradlew :library:test
```

Build sample application:

```bash
./gradlew :app:assembleDebug
```

---

## 📄 License & Author

Developed & maintained by **Zakayo Thuku** ([@zakayothuku](https://github.com/zakayothuku)).

```
MIT License - Copyright (c) 2026 Zakayo Thuku
```
