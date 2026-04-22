# LearnAI — LLM-Enhanced Learning Assistant App

An Android learning assistant app (SIT708 Task 6.1D) that integrates with a Flask + Ollama AI backend to generate personalised quizzes, provide AI-generated hints, and produce 7-day study plans.

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
| Flask (backend) | latest |
| Ollama + Llama3 (optional) | latest |

---

## Backend Setup

The backend is a Flask server that queries a local Ollama LLM and falls back to built-in topic templates if Ollama is unavailable — so the app works with or without an LLM running.

### 1. Navigate to the backend folder

```bash
cd T-6.1D
```

### 2. Install dependencies (first time only)

```bash
pip install flask requests
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
LLM backend: Ollama (http://localhost:11434) — falls back to templates if unavailable
 * Running on http://0.0.0.0:5000
```

Keep this terminal open while running the app.

---

## Android Setup

1. Open the project in **Android Studio Hedgehog (2023.1.1)** or newer.
2. Click **Sync Now** when prompted to sync Gradle.
3. Start an Android Emulator (API 24+, API 34 recommended).
4. Make sure the Flask backend is running first.
5. Click **Run ▶** or build from terminal:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## How to Use

1. **Splash Screen** — launches briefly, checks existing login session.
2. **Login** — enter username (≥1 char) and password (≥6 chars). Uses local mock authentication.
3. **Sign Up** — fill in username, email, confirm email, password, confirm password, and phone.
4. **Select Interests** — pick up to 10 topics from the available tags. Tap **Next**.
5. **Dashboard** — personalised greeting with 4 learning task cards based on your interests.
6. **Tap a Task Card → Quiz Screen**
   - The app calls `GET /getQuiz?topic=<topic>` and displays 3 multiple-choice questions.
   - Tap **Get a Hint** for an AI hint on the first question (shows prompt + response).
   - Tap **7-Day Study Plan** to generate a personalised study plan (shows prompt + plan).
   - Select your answers and tap **Submit Quiz**.
7. **Results Screen** — shows your score (e.g., 2/3) with correct/wrong badges per question.
8. **Logout** — tap the Logout button on the Dashboard to return to the Login screen.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Health check |
| `GET` | `/getQuiz?topic=<topic>` | Returns 3 quiz questions for the given topic |
| `GET` | `/test` | Returns a test response |

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
- Authentication is mock/local only — no real user database.
- Without Ollama installed, all quiz questions come from the built-in template bank.

---

## Author

**Student:** Vedant Pandya
**Unit:** SIT708 — Mobile Application Development
**Task:** 6.1D — LLM-Enhanced Learning Assistant App
