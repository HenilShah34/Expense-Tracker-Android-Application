<h1 align="center">💰 Expense Tracker Android Application</h1>

<p align="center">
  <b>An offline-first Android app that helps users track, analyze, and manage personal expenses —</b><br/>
  with all data stored securely and privately on-device, no internet connection or cloud account required.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white"/>
  <img src="https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white"/>
  <img src="https://img.shields.io/badge/Material_Design-757575?style=for-the-badge&logo=materialdesign&logoColor=white"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"/>
</p>

---

## 🎯 Overview

Most expense trackers assume you're fine handing your financial data to a cloud server. This one doesn't make that assumption. Every transaction, budget, and category is stored **locally via SQLite** — the app works fully offline, requires no account creation, and never sends personal financial data anywhere. For users who care about privacy as much as functionality, that's the core value proposition.

On top of that offline-first foundation, the app handles the full expense-management lifecycle: recording transactions, setting budgets with real-time alerts, visualizing spending by category, and exporting data on demand — all through a clean Material Design interface.

---

## 📱 Features

- 🔐 User authentication with Login & Registration
- 🔑 Secure PIN-based access with Forgot Username/Password recovery
- ➕ Add, edit, and delete expenses with categories and notes
- 📅 Daily / Weekly / Monthly budget setup
- 🚨 Budget limit alerts when spending exceeds set thresholds
- 📊 Category-wise expense visualization using Pie Charts
- 📤 Export expense data to CSV format
- 📴 Fully offline functionality via local SQLite database — no internet required
- 🎨 Clean, user-friendly UI built with Material Design

---

## 🖼️ Screenshot Gallery

> **Swap in your actual screenshots below** — replace each `Images/screenshotX.png` path with your real file from the `/Images` directory. A visual gallery is the single highest-impact section for a mobile app repo; clients form their first impression here before reading a word further.

| Login & Registration | Dashboard | Add Expense |
|:---:|:---:|:---:|
| ![Login](Images/screenshot1.png) | ![Dashboard](Images/screenshot2.png) | ![Add Expense](Images/screenshot3.png) |

| Budget Setup | Category Chart | Navigation Drawer |
|:---:|:---:|:---:|
| ![Budget Setup](Images/screenshot4.png) | ![Category Chart](Images/screenshot5.png) | ![Navigation Drawer](Images/screenshot6.png) |

| Account Management |
|:---:|
| ![Account Management](Images/screenshot7.png) |

---

## 🧩 Application Modules

- 🔐 **User Authentication Module**
  Login, Registration, Forgot Username & Password recovery

- 💵 **Expense Management Module**
  Add, edit, and delete expenses with input validation

- 🎯 **Budget Management Module**
  Set budget limits and receive real-time overspending alerts

- 📊 **Expense Analysis Module**
  Category-wise summaries and interactive pie chart visualizations

- ⚙️ **Export & Settings Module**
  CSV export and user profile management

---

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| **Language** | Kotlin |
| **UI Design** | XML (Material Design Components) |
| **Database** | SQLite (Local Storage) |
| **IDE** | Android Studio (Giraffe or newer) |
| **Build Tool** | Gradle |

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/HenilShah34/Expense-Tracker-Android-Application.git
```

### 2. Open in Android Studio

- Launch **Android Studio** (Giraffe or newer)
- Select **Open** and choose the cloned project folder
- Let Gradle sync automatically (this may take a few minutes on first open)

### 3. Run the App

- Connect an Android device (with USB debugging enabled) or start an emulator
- Click **Run ▶** in Android Studio, or use:

```bash
./gradlew installDebug
```

---

## 📂 Project Structure

```
Expense-Tracker-Android-Application/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Kotlin source files
│   │   │   ├── res/           # Layouts, drawables, strings
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle
├── Images/                    # App screenshots
├── build.gradle
└── README.md
```

---

## 📊 Impact / Learning Outcomes

> *Fill this in with real specifics — this is what shows a client you can reflect critically on your own work, not just describe features.*
- Designed and implemented a full offline-first data layer using SQLite, including schema design for transactions, categories, and budgets
- Built a real-time budget-alert system triggered by SQLite queries against user-defined thresholds
- Implemented category-wise data visualization using chart libraries integrated with live SQLite data
- Handled secure local authentication (PIN-based access, recovery flow) without relying on a backend service

---

## 🔮 Future Improvements

- [ ] Add optional cloud backup/sync while preserving offline-first default
- [ ] Support multiple currencies
- [ ] Add recurring transaction scheduling
- [ ] Migrate from XML layouts to Jetpack Compose
- [ ] Add biometric authentication as an alternative to PIN
- [ ] Add dark mode support

---

## 👤 Author

**Henil Shah**
Software Developer | Backend & Automation Specialist

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/HenilShah34)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/henil-shah-5958b327a)
[![Upwork](https://img.shields.io/badge/Upwork-6FDA44?style=for-the-badge&logo=upwork&logoColor=white)](https://www.upwork.com/freelancers/~01c3c0b0cda5f0df04)
