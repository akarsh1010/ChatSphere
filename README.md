# 💬 CoreChat (ChatSphere)

> A lightweight, full-stack real-time 1-on-1 chat platform built in pure **Core Java (Java SE)** without external frameworks, featuring persistent **File I/O**, a modern **Glassmorphic Web Interface**, and a dedicated **Super Admin Moderation Console**.

[![Live Demo](https://img.shields.io/badge/🚀_Live_Demo-Launch_ChatSphere-success?style=for-the-badge&logo=render&logoColor=white)](https://chatsphere-d261.onrender.com)
[![GitHub stars](https://img.shields.io/github/stars/alok0770/ChatSphere?style=for-the-badge&color=ffd700)](https://github.com/alok0770/ChatSphere)

[![Java](https://img.shields.io/badge/Language-Java%2017%2B-orange.svg)](https://www.oracle.com/java/)
[![Version](https://img.shields.io/badge/Version-v2.0%20(Latest)-blue.svg)]()
[![Server](https://img.shields.io/badge/Server-Zero--Dependency%20HttpServer-teal.svg)]()
[![Frontend](https://img.shields.io/badge/Frontend-Vanilla%20JS%20%7C%20HTML5%20%7C%20CSS3-yellow.svg)]()
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> [!TIP]
> 🌐 **Try it Live in your Browser:** **[https://chatsphere-d261.onrender.com](https://chatsphere-d261.onrender.com)**  
> *No local installation required! Works seamlessly across Desktop and Mobile devices with full audio chimes and real-time chat.*

---

## 🚀 Version Evolution: v1.0 vs v2.0

CoreChat evolved from a terminal-based CLI application to a containerized full-stack web messenger with complete moderation powers.

| Feature Category | 📟 v1.0 (Console Edition) | 🌐 v2.0 (Web & Admin Edition) |
| :--- | :--- | :--- |
| **Interface** | Terminal / Command Line Interface | Modern responsive Web App (Glassmorphism) |
| **Architecture** | CLI Menu Loop + OOP Services | Built-in Java SE `HttpServer` + REST APIs |
| **Dependencies** | Pure Java Standard Library | 100% Zero Frameworks (No Spring Boot / Maven) |
| **Persistence** | File I/O (`users.txt`, `messages.txt`) | File I/O + Atomic Batch Overwrite Operations |
| **Timestamps** | None | Formatted 12-hour timestamps (`hh:mm a`) |
| **Notifications** | Console print outputs | Web Audio API synthesize chime sounds |
| **Admin Controls** | ❌ None | 👑 Super Admin Dashboard (`@alokrana`) |
| **Cloud Deployment** | Local execution only | 🐳 Dockerfile + Cloud dynamic `$PORT` (Render) |

---

### 📟 v1.0 — CLI / Terminal Edition
The foundational release focusing on pure Core Java concepts, clean Object-Oriented design, and basic persistence:
- **Terminal User Flow**: Interactive menu-driven navigation (Register, Login, Chat, Exit).
- **Authentication**: Case-insensitive usernames and password validation.
- **Continuous Chat Loop**: Seamless messaging loop without exiting after every message.
- **File I/O Persistence**: Safe user and message storage in `data/users.txt` and `data/messages.txt`.
- **Input Crash Protection**: Protected against invalid inputs using `InputMismatchException` handlers.

---

### 🌐 v2.0 — Full-Stack Web & Super Admin Edition *(Current)*
A major overhaul transforming CoreChat into a complete, modern web chat platform:
- **Zero-Dependency Web Server**: Built using native `com.sun.net.httpserver.HttpServer`. Serves static web assets and handles REST API endpoints (`/api/register`, `/api/login`, `/api/users`, `/api/messages`, `/api/send`, `/api/admin`).
- **Modern Glassmorphism UI**: Royal blue & midnight slate aesthetic, auto-generated user avatars, password visibility eye toggle, and double-tick (`✓✓`) read receipts.
- **Live Polling & Web Audio Chimes**: Auto-refreshes conversations every 1.5 seconds and plays pleasant Web Audio synthesized notification chimes upon receiving new messages.
- **Timestamp Engine**: Every message records accurate send times (`hh:mm a`) via Java 8 `LocalTime` and `DateTimeFormatter`.
- **👑 Super Admin Moderation Console**:
  - Exclusive access for **`@alokrana`** with protected status (cannot be banned).
  - **Live Platform Stats**: Total registered users, total messages exchanged, and server health.
  - **1-Click User Kick/Ban**: Permanently remove offending users and purge their message history.
  - **Official Admin Notices**: Dispatch administrative warning alerts directly into any user's conversation.
- **🐳 Cloud & Container Ready**: Includes a production-ready `Dockerfile` (OpenJDK 17) configured for 1-click cloud deployment on **Render**, **Railway**, or **Fly.io** using dynamic `$PORT` binding.

---

## 🏗️ Project Architecture

```text
CoreChat/
├── Dockerfile                   # Cloud container configuration (Render / Docker)
├── README.md                    # Project documentation & evolution guide
├── data/
│   ├── users.txt                # Persisted user records (username,password)
│   └── messages.txt             # Persisted messages (sender,receiver,content,timestamp)
├── src/
│   └── corechat/
│       ├── AdminService.java    # 👑 Super Admin logic & moderation controls
│       ├── ChatWebServer.java   # 🌐 Zero-dependency Web Server & REST API router
│       ├── ChatApplication.java # 📟 v1.0 Console/CLI entry point
│       ├── ChatService.java     # v1.0 Console chat loop service
│       ├── FileManager.java     # File I/O persistence engine (read, write, overwrite)
│       ├── MessageManager.java  # In-memory message collection manager
│       ├── Message.java         # Message entity with timestamp support
│       └── User.java            # User domain model
└── web/
    ├── index.html               # Semantic HTML5 layout (Auth, Chat View, Admin Modal)
    ├── style.css                # Royal blue & slate glassmorphism styling
    └── app.js                   # Vanilla JS client (Polling, Audio chimes, Admin logic)
```

---

## 🚀 How to Run

### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher.

### 📥 1. Clone & Compile
```bash
git clone https://github.com/alok0770/ChatSphere.git
cd CoreChat
javac -d out/production/CoreChat src/corechat/*.java
```

### 🌐 2. Run v2.0 Web Edition (Recommended)

- **🚀 Live Cloud Demo**: Open directly at **[https://chatsphere-d261.onrender.com](https://chatsphere-d261.onrender.com)** (No local setup required!)
- **💻 Or Run Locally on Machine**:
  ```bash
  java -cp out/production/CoreChat corechat.ChatWebServer
  ```
  👉 Open your browser at **`http://localhost:8080`**!

> **Super Admin Login**: Log in with username **`alokrana`** (password: `100`) to unlock the **👑 Crown Button** and access the Super Admin Console.

### 📟 3. Run v1.0 Console Edition (Terminal)
```bash
java -cp out/production/CoreChat corechat.ChatApplication
```

### 🐳 4. Run via Docker
```bash
docker build -t corechat .
docker run -p 8080:8080 corechat
```

---

## 👨‍💻 Author

**Alok Rana**
- **GitHub:** [@alok0770](https://github.com/alok0770)
- **Email:** [alokrana0770@gmail.com](mailto:alokrana0770@gmail.com)
- **Project Repo:** [ChatSphere](https://github.com/alok0770/ChatSphere)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
