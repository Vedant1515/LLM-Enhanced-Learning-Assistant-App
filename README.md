# LearnAI — LLM-Enhanced Learning Assistant App

An Android learning assistant app (SIT708 Tasks 6.1D + 10.1) that integrates with a Flask + Ollama AI backend to generate personalised quizzes, provide AI-generated hints, produce 7-day study plans, and track persistent learning progress with a full profile, history, and upgrade system. User data is stored in MongoDB Atlas with an offline fallback mode.

---

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 8 (compileOptions VERSION_1_8) |
| Android SDK | 34 (min 24) |
| AGP | 8.2.0 |
| Gradle | 8.2 |
| Retrofit2 | 2.9.0 |
| OkHttp3 | 4.12.0 |
| Gson | 2.10.1 |
| Material Components | 1.11.0 |
| Lottie | 6.3.0 |
| CircleImageView | 3.1.0 |
| FlexboxLayout | 3.0.0 |
| AndroidX SplashScreen | 1.0.1 |
| Google Pay API (play-services-wallet) | 19.3.0 |
| ZXing (QR code generation) | 3.5.2 |
| Flask (backend) | latest |
| PyMongo + certifi | 4.17 / 2025 |
| Ollama + Llama3 (optional) | latest |
| MongoDB Atlas | cloud (offline fallback) |

---

## Backend Setup

The backend is a Flask server that queries a local Ollama LLM and falls back to built-in topic templates if Ollama is unavailable — so the app works with or without an LLM running.

### 1. Navigate to the backend folder

```bash
cd T-6.1D
```

### 2. Install dependencies (first time only)

```bash
pip install flask requests pymongo[srv] certifi
```

### 3. (Optional) Install Ollama for real AI-generated questions

Download from [ollama.com](https://ollama.com), then pull the model:

```bash
ollama run llama3
```

> Without Ollama, the server automatically uses the built-in question templates. The app will still show 3 questions per quiz.

### 4. Start the Flask server

```bash
python main.py
```

Expected output:
```
App running on port 5000
MongoDB Atlas: connected        (or "not available (offline mode)" if unreachable)
LLM backend: Ollama (http://localhost:11434) — falls back to templates if unavailable
 * Running on http://0.0.0.0:5000
```

Keep this terminal open while running the app.

> **MongoDB Atlas**: The backend connects to a shared Atlas cluster. If the connection fails (SSL error, IP not whitelisted), the server starts in offline mode — all endpoints still work but data is not persisted between sessions. To enable Atlas: add your IP to the Atlas Network Access allowlist at cloud.mongodb.com.

---

## Android Setup

1. Open the project in **Android Studio Hedgehog (2023.1.1)** or newer.
2. Click **Sync Now** when prompted to sync Gradle.
3. Start an Android Emulator (API 24+, API 34 recommended). **For Google Pay, the AVD must use a "Google Play" system image** (shows Play Store icon in AVD Manager) — not a "Google APIs" image.
4. Sign into a Google account in the emulator (Settings → Accounts) for the Google Pay sheet to appear.
5. Make sure the Flask backend is running first.
5. Click **Run ▶** or build from terminal:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Screens

| Screen | Description |
|--------|-------------|
| Splash | Launches briefly, checks login state |
| Login | Local mock authentication |
| Sign Up | Registration with username, email, phone |
| Select Interests | Choose up to 10 topics |
| Dashboard | 4 task cards + bottom nav (Home / History / Profile) + Upgrade button |
| Quiz | AI-generated 3-question quiz with hint generator and study plan |
| Results | Score display, answer explainer, 7-day study plan |
| **Profile** *(new)* | Stats display, AI summary of incorrect answers, share functionality |
| **History** *(new)* | Persistent quiz history with expandable question details |
| **Upgrade** *(new)* | 3 pricing tiers with Google Pay + simulated payment fallback |

---

## How to Use

1. **Splash Screen** — launches briefly, checks existing login session.
2. **Login** — enter username (≥1 char) and password (≥6 chars). Uses local mock authentication.
3. **Sign Up** — fill in username, email, confirm email, password, confirm password, and phone.
4. **Select Interests** — pick up to 10 topics from the available tags. Tap **Next**.
5. **Dashboard** — personalised greeting with 4 learning task cards based on your interests.
   - Bottom nav: tap **History** to see past quizzes, tap **Profile** to view your stats.
   - Top right: tap **Upgrade** to see subscription tiers.
6. **Tap a Task Card → Quiz Screen**
   - The app calls `GET /getQuiz?topic=<topic>` and displays 3 multiple-choice questions.
   - Tap **Get a Hint** for an AI hint on the first question (shows prompt + response).
   - Tap **7-Day Study Plan** to generate a personalised study plan (shows prompt + plan).
   - Select your answers and tap **Submit Quiz**.
7. **Results Screen** — shows your score (e.g., 2/3) with correct/wrong badges per question. Each completed quiz is automatically saved to history.
8. **Profile Screen** — view your cumulative stats. Tap **Summarised by AI** to query the LLM for a learning-gap summary. Tap **Share Profile** to share your stats via any installed app.
9. **History Screen** — see all past quizzes sorted by date. Tap the chevron on any entry to expand and review all questions and answers.
10. **Upgrade Screen** — choose Starter, Intermediate, or Advanced tier. Google Pay is used if available; otherwise a simulated dialog handles the purchase.
11. **Logout** — tap the Logout button on the Dashboard to return to the Login screen.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Health check |
| `GET` | `/getQuiz?topic=<topic>` | Returns 3 quiz questions for the given topic |
| `POST` | `/register` | Register a new user |
| `POST` | `/login` | Authenticate a user |
| `POST` | `/saveInterests` | Save selected topic interests for a user |
| `POST` | `/saveQuizResult` | Persist a completed quiz result |
| `GET` | `/getHistory?username=<u>` | Fetch all quiz results for a user (most recent first) |
| `GET` | `/getProfile?username=<u>` | Fetch profile data and aggregated quiz stats |
| `POST` | `/saveUpgradeTier` | Save the purchased upgrade tier for a user |

### Quiz Response Format

```json
{
  "quiz": [
    {
      "question": "Which layout arranges its children in a single row or column?",
      "options": [
        "ConstraintLayout",
        "RelativeLayout",
        "LinearLayout",
        "FrameLayout"
      ],
      "correct_answer": "LinearLayout"
    }
  ]
}
```

> `correct_answer` is always the **full option text**, not a letter like "A" or "B".

---

## LLM Utilities

All utilities display the exact prompt sent, the AI response, a loading spinner, and a retry state on failure.

### 1. Hint Generator (QuizActivity)
- **Trigger:** Tap "Get a Hint" on the quiz screen
- **Prompt:** `"Generate a helpful hint for this question without revealing the answer: [question]"`
- **Response:** A contextual hint guiding the student without giving away the answer

### 2. 7-Day Study Plan (QuizActivity)
- **Trigger:** Tap "7-Day Study Plan" on the quiz screen
- **Prompt:** `"Create a 7-day study plan for a student learning [topic] based on their quiz performance."`
- **Response:** Day-by-day breakdown (Mon–Sun) with specific tasks covering fundamentals through review

### 3. Answer Explainer (ResultsActivity)
- **Trigger:** Tap "Explain this answer" on any result item
- **Response:** Contextual explanation of why the correct answer is right

### 4. AI Summary of Incorrect Answers (ProfileActivity)
- **Trigger:** Tap "Summarised by AI" on the Profile screen
- **Prompt:** `"Summarise the learning gaps for a student who answered [N] out of [M] questions incorrectly on topics including [recent topics]"`
- **Response:** Key areas to review based on recent quiz performance

---

## Task 10.1 — New Features

### Profile Screen
- Displays username, email, and avatar initials from the user's account
- Shows cumulative stats: Total Questions, Correctly Answered, Incorrect Answers (synced from Atlas)
- "Summarised by AI" section queries the LLM and displays a learning-gap summary based on recent quiz topics
- **Share Profile**: tap Share to open a bottom sheet with a live-generated QR code (ZXing) encoding the user's stats — share as QR image or plain text via the Android system share chooser

### History Screen
- Lists all completed quizzes sorted most-recent-first (fetched from Atlas, falls back to local SharedPreferences)
- Each card shows timestamp, topic, and score badge
- Tap the chevron to expand a card and review all 3 questions with colour-coded answers:
  - Red dot = user's wrong answer
  - Green dot = correct answer
  - Grey dot = other option
- Empty state displayed when no quizzes have been completed yet

### Upgrade Screen
- Three subscription tiers: Starter ($4.99/mo), Intermediate ($9.99/mo), Advanced ($19.99/mo)
- "Best Seller!" badge on the Intermediate tier with a pulse animation
- **Google Pay TEST integration**: launches the real Google Pay sheet (no real charges, no real card needed — Google fills test card data automatically). Falls back to a simulated confirmation dialog if the sheet cannot open
- Purchased tier is saved to Atlas and persists after app restart showing "Current Plan ✓" with a green border

### MongoDB Atlas Backend
- All user registrations, logins, interests, quiz results, and upgrade tiers are stored in Atlas
- Backend starts in **offline mode** automatically if Atlas is unreachable — all endpoints still return valid responses
- SSL fallback handles Windows environments where antivirus/firewall intercepts TLS connections

---

## Setup Notes

- History is persisted in MongoDB Atlas and locally via SharedPreferences as a fallback.
- Upgrade purchases use Google Pay TEST environment — no real charges are made.
- Share uses the Android native share intent — no additional setup required.
- QR code generation runs on a background thread to keep the UI responsive.

---

## Network Configuration

The Android Emulator maps the host machine's `localhost` to `10.0.2.2`. Since Flask runs on plain HTTP, Android 9+ blocks cleartext traffic by default. This is allowed via `res/xml/network_security_config.xml`:

```xml
<network-security-config>
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">10.0.2.2</domain>
  </domain-config>
</network-security-config>
```

The Retrofit base URL is `http://10.0.2.2:5000/` with a **10-minute read timeout** to accommodate LLM generation time.

---

## Known Limitations

- The Flask backend must be running on the host machine before making quiz requests.
- Ollama LLM responses can take up to a minute; the 10-minute timeout accommodates this.
- MongoDB Atlas requires your IP to be whitelisted in the Atlas Network Access panel; otherwise the backend starts in offline mode.
- Google Pay requires a "Google Play" AVD image — "Google APIs" images do not include the required Play Services components.
- Without Ollama installed, all quiz questions come from the built-in template bank.

---

## Author

**Student:** Vedant Pandya
**Unit:** SIT708 — Mobile Application Development
**Tasks:** 6.1D + 10.1 — LLM-Enhanced Learning Assistant App
