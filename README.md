# 💬 CoreChat (v1.0)

> A robust, console-based 1-on-1 chat application built with **Core Java**, clean **Object-Oriented Programming (OOP)** design, and persistent **File I/O** storage.

[![Java](https://img.shields.io/badge/Language-Java%2017%2B-orange.svg)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-OOP%20%7C%20MVC--inspired-blue.svg)]()
[![Status](https://img.shields.io/badge/Status-v1.0%20(Stable)-success.svg)]()
[![License](https://img.shields.io/badge/License-MIT-green.svg)]()

---

## 📌 Overview

**CoreChat** is a terminal-based messaging platform designed to simulate a real-world chat system. Unlike basic command-line prototypes that lose data upon termination, CoreChat features a custom **File I/O Persistence Engine** that safely stores users and conversation histories across sessions.

---

## ✨ Key Features

- **🔐 User Authentication & Validation**
  - Registration with duplicate username prevention.
  - Case-insensitive username lookup with password verification.

- **💬 Real-Time Continuous Chat Loop**
  - Interactive chat session between users (no need to repeatedly re-select chat partners after every message).
  - Clean chatroom interface with commands to exit/return (`exit`, `back`).

- **📜 Persistent Chat History**
  - Seamlessly reloads previous messages between two users upon opening their chat.
  - Automatically identifies whether a user is the sender or receiver.

- **💾 File-Based Persistence (File I/O)**
  - Stores credentials and messages permanently under the `data/` directory (`users.txt` and `messages.txt`).
  - No database required; uses standard Java I/O streams (`BufferedReader`, `BufferedWriter`).

- **🛡️ Exception & Error Resilience**
  - Built-in crash protection against non-numeric menu inputs (`InputMismatchException`).
  - Empty messages and whitespace-only inputs are safely ignored.

---

## 🏗️ Project Architecture

```
CoreChat/
├── data/
│   ├── users.txt                # Persisted user records (username,password)
│   └── messages.txt             # Persisted messages (sender,receiver,content)
├── src/
│   └── corechat/
│       ├── ChatApplication.java # Console edition entry point (CLI router & menus)
│       ├── ChatWebServer.java   # 🌐 Web edition server (built-in Java HttpServer & REST APIs)
│       ├── ChatService.java     # Console continuous chat session loop
│       ├── FileManager.java     # File I/O persistence manager (read/write operations)
│       ├── MessageManager.java  # In-memory message management & aggregation
│       ├── Message.java         # Domain entity representing a single chat message
│       └── User.java            # Domain entity representing an authenticated user
├── web/                         # 🌐 Frontend web assets
│   ├── index.html               # Modern responsive web layout (Login/Register & Chat views)
│   ├── style.css                # Dark-mode styling, responsive bubbles, custom scrollbars
│   └── app.js                   # Vanilla JS client (Fetch API, live auto-polling)
├── README.md
└── CoreChat.iml
```

### Class Responsibilities:
- **`ChatWebServer`**: Zero-dependency Web Server serving the frontend assets and REST APIs (`/api/login`, `/api/register`, `/api/users`, `/api/messages`, `/api/send`).
- **`ChatApplication`**: Console/CLI entry point coordinating terminal menus and user interactions.
- **`ChatService`**: Orchestrates active console chat sessions and drives the continuous messaging loop.
- **`FileManager`**: Decoupled persistence layer handling serialization and parsing of data files.
- **`MessageManager`**: Acts as an in-memory repository managing the active message list.
- **`User` & `Message`**: Encapsulated domain models with appropriate accessors and equality contracts.

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher recommended.
- **IDE**: IntelliJ IDEA, Eclipse, VS Code, or command-line terminal.

### 📥 Installation & Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/alok0770/ChatSphere.git
   cd CoreChat
   ```

2. **Compile the source files:**
   ```bash
   javac -d out/production/CoreChat src/corechat/*.java
   ```

3. **Run the Application:**

   - **Option A — Web Edition (Browser):**
     ```bash
     java -cp out/production/CoreChat corechat.ChatWebServer
     ```
     👉 Then open `http://localhost:8080` in your web browser!

   - **Option B — Console Edition (Terminal):**
     ```bash
     java -cp out/production/CoreChat corechat.ChatApplication
     ```

*(Alternatively, open the project in **IntelliJ IDEA** and right-click **Run** on either `ChatWebServer.java` or `ChatApplication.java`.)*

---

## 🖥️ Usage Demonstration

### 1. Main Menu
```text
==========================
        CORE CHAT         
==========================

Welcome to CoreChat!
Starting Application...

---------Menu---------
1. Register 
2. Login 
3. Exit

Enter your choice : 1
```

### 2. Live Chat Session
```text
========================================
 Chatting with @rahul
 (Type 'exit' or 'back' to leave chat)  
========================================
rahul : Hey! How is the CoreChat project going?
alok : It's going great! Just integrated File I/O persistence.
----------------------------------------
alok > Working on v1.0 right now!
alok > exit

Left chat with @rahul.
```

---

## 🗺️ Roadmap & Changelog

- [x] **Message Timestamps**: Accurate time logging (`11:45 PM`) stored in `messages.txt`.
- [x] **Web Edition UI**: Full-stack modern responsive web messenger with live polling.
- [x] **Zero-Dependency HTTP Server**: Built-in Java SE `HttpServer` with REST APIs.
- [x] **Cloud Containerization**: 1-click Dockerfile for instant deployment on Render.
- [ ] **Socket Programming**: WebSockets for push-based real-time communication.
- [ ] **Group Chats**: Multi-user rooms and channel broadcasting.

---

## 👨‍💻 Author

**Alok Rana**
- **GitHub:** [@alok0770](https://github.com/alok0770)
- **Email:** [alokrana0770@gmail.com](mailto:alokrana0770@gmail.com)

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
