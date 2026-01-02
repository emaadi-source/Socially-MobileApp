# 📱 Social Media App – Firebase to PHP Migration

This project is an **Android social media application** that was migrated from **Firebase** to a **custom PHP + MySQL backend**, while keeping the app **offline-first** and reliable.

The goal of this migration was to gain **full backend control**, reduce dependency on Firebase, and implement a system that works smoothly even without an internet connection.

---

## 🚀 Project Overview

The app allows users to:
- Create accounts and log in
- Create posts and stories
- Like, comment, and follow other users
- Send and receive messages
- Use the app offline and sync automatically when online

Instead of Firebase, all data is now handled by **PHP REST APIs**, stored in **MySQL**, and synced with the Android app using a local database.

---

## 🔧 How the App Works (Simple Flow)

1. **User Action**  
   The user creates a post, comment, like, or message.

2. **Offline First**  
   - Data is first saved locally using **SQLite**
   - App works normally even without internet

3. **Online Sync**  
   - When internet is available, data is sent to the **PHP backend**
   - Background sync handles retries automatically

4. **Backend Processing**  
   - PHP APIs process requests
   - MySQL stores users, posts, messages, followers, etc.

---

## 🧩 What’s Inside the Project

### 📱 Android App
- Kotlin-based Android application
- Offline storage using SQLite
- Automatic background sync
- Network detection
- Clean separation of data logic

Key components:
- API client (handles server requests)
- Local database helper
- Sync manager
- Offline repository

---

### 🌐 PHP Backend
- REST-style PHP APIs
- Handles authentication, posts, stories, messaging, and follows
- Secure password handling
- Clean API structure

Main API features:
- Login & Signup
- Create / fetch posts
- Stories
- Likes & comments
- Follow system
- Messaging
- Session tracking

---

### 🗄️ MySQL Database
- Structured relational database
- Multiple tables with proper relationships
- Indexed for better performance
- Stores all app data securely

