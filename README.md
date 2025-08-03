# 🗳️ Online Voting App

An Android application for secure and easy online voting. Built with **Jetpack Compose**, **Firebase Authentication**, and **Cloud Firestore**. It features two roles — **Admin** and **Voter** — with distinct access and functionality.

---

## 📱 Features

### 🔐 Authentication
- Role-based login (Admin / Voter)
- Firebase Authentication (Email & Password)
- Password reset functionality

### 🧑‍💼 Admin
- Manage polls (create, edit, delete)
- Manage candidates
- Manage voter list
- View poll results
- Secure access via admin login

### 🗳️ Voter
- Login or Sign Up
- View available polls
- Vote securely (one vote per poll)
- Real-time results display
- Firebase Firestore ensures data persistence

---

## 🧑‍🎨 Built With

| Tech                | Usage                           |
|---------------------|---------------------------------|
| **Kotlin**          | Android app logic               |
| **Jetpack Compose** | UI framework for modern Android |
| **Firebase Auth**   | Login/signup/reset via email    |
| **Cloud Firestore** | Store polls, users, votes       |

---

## 🏗️ Architecture

- MVVM Pattern using `ViewModel`
- Firestore SnapshotListeners for real-time UI updates
- Secure Firestore rules to prevent unauthorized access
- Navigation using `NavHostController`
- Role-based UI navigation (Admin vs Voter)

---

### 📸 Screenshots

#### 🔐 Authentication Screens

| Auth Screen                            | Login Screen                                                | Registration Screen                                   |
|----------------------------------------|-------------------------------------------------------------|-------------------------------------------------------|
| ![Auth](screenshots/auth_screen.png)   | ![Auth](screenshots/login_screen.png)                       | ![Auth](screenshots/registration_screen.png)          |
| Choose admin or voter                  | Login Screen for Voter; admin login screen looks similar    | Registration Screen for Voter, not for admins         |

---

#### 🏠 Landing Pages

| Admin Home Screen                    | Voter Poll List                                        |
|--------------------------------------|--------------------------------------------------------|
| ![Admin](screenshots/admin_home.png) | ![Polls](screenshots/voter_polls.png)                  |
| Home/Landing screen for Admin        | Landing page for Voter/User. Here they can select Poll |

---

#### 🗳️ Voter Screens

| Poll Action Screen                           | Vote Screen                             | Result Screen                     |
|----------------------------------------------|-----------------------------------------|-----------------------------------|
| ![VoteOrResult](screenshots/poll_action.png) | ![Vote](screenshots/vote.png)           | ![Result](screenshots/result.png) |
| Poll actions: Vote or View Result for Voter  | Voter selects and votes for a candidate | Voter sees the voting result      |

---

## 📂 File Structure

```
com.alokkumar.onlinevotingapp
│
├── AppNavigation.kt              # App navigation controller
├── MainActivity.kt               # Entry point for the app
│
├── model                         # Data models
│   ├── Candidate.kt
│   ├── Poll.kt
│   ├── PollDocument.kt
│   ├── UserModel.kt
│   └── VoteModel.kt
│
├── ui                            # UI components and screens
│   ├── screens
│   │   ├── admin                 # Admin-specific screens
│   │   │   ├── AdminHomeScreen.kt
│   │   │   ├── ManagePollScreen.kt
│   │   │   ├── ManageVoterScreen.kt
│   │   │   ├── MonitorVotesScreen.kt
│   │   │   ├── ViewStatsScreen.kt
│   │   │   └── polls             # Subscreens for poll management
│   │   │       ├── AddOrEditCandidateScreen.kt
│   │   │       ├── AddOrEditPollScreen.kt
│   │   │       ├── PollResultScreen.kt
│   │   │       └── VoteDetailScreen.kt
│   │   │
│   │   ├── auth                  # Authentication screens
│   │   │   ├── AdminLoginScreen.kt
│   │   │   ├── AuthScreen.kt
│   │   │   ├── UserLoginScreen.kt
│   │   │   └── UserRegistrationScreen.kt
│   │   │
│   │   ├── common                # Shared UI components
│   │   │   └── CandidateListSection.kt
│   │   │
│   │   └── user                  # Voter/user-specific screens
│   │       ├── PollActionsScreen.kt
│   │       ├── PollResultScreen.kt
│   │       ├── UserHomeScreen.kt
│   │       └── VoteScreen.kt
│   │
│   └── theme                     # UI theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── viewmodel                     # ViewModels following MVVM
├── admin
│   ├── ManagePollViewModel.kt
│   ├── MonitorVotesViewModel.kt
│   ├── ViewStatsViewModel.kt
│   └── polls
│       ├── AddOrEditCandidateViewModel.kt
│       ├── AddOrEditPollViewModel.kt
│       └── PollResultViewModel.kt
│
├── auth
│   ├── AuthViewModel.kt
│   └── SessionViewModel.kt
│
├── common
│   └── CandidateListViewModel.kt
│
└── user
├── PollResultViewModel.kt
├── UsersViewModel.kt
└── VoteViewModel.kt
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Firebase Project with:
  - Email/Password Authentication enabled
  - Firestore database setup
  - SHA1 & SHA256 keys added for debug/release

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/your-username/online-voting-app.git
   cd online-voting-app 
   ```

2. Connect to Firebase:
  Download google-services.json from Firebase Console
  Place it in app/ directory

3. Run the project:
  - Connect emulator or device
  - Click ▶️ Run in Android Studio

### ✅ TODO (Future Enhancements)

- [ ] 🔐 Admin analytics dashboard
- [ ] 📈 Voting stats chart
- [ ] 🌐 Multi-language support

### 📫 Contact
  Feel free to reach out via:
- Gmail: dev.alokkumar.droid@gmail.com
- Telegram: https://t.me/dev_alokkumar_droid
