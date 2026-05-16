import os
import re
import json
import random
import hashlib
import requests
from datetime import datetime
from flask import Flask, request, jsonify

app = Flask(__name__)

OLLAMA_URL = "http://localhost:11434/api/generate"
OLLAMA_MODEL = "llama3"          # fallback tries llama2 if llama3 not found

# ---------------------------------------------------------------------------
# MongoDB Atlas
# ---------------------------------------------------------------------------

# Set MONGO_URI as an environment variable, or replace the placeholder below.
# Format: mongodb+srv://<user>:<password>@<cluster>.mongodb.net/<dbname>
MONGO_URI = "mongodb+srv://learnai:Y0ZVwcEvEhZaGDDF@cluster0.8rtyvz6.mongodb.net/?appName=Cluster0"

mongo_available = False
db = None
users_col = None
quiz_results_col = None

try:
    import ssl
    import certifi
    from pymongo import MongoClient
    from pymongo.errors import ConnectionFailure, ServerSelectionTimeoutError
    from bson import ObjectId

    def _make_client(uri, timeout_ms=5000):
        """Try standard TLS first, fall back to relaxed TLS for Windows SSL inspection."""
        try:
            c = MongoClient(uri, serverSelectionTimeoutMS=timeout_ms, tlsCAFile=certifi.where())
            c.admin.command("ping")
            return c
        except Exception:
            # Windows antivirus / corporate proxy can intercept TLS — retry with system trust store
            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE
            c = MongoClient(uri, serverSelectionTimeoutMS=timeout_ms, tlsClientCertificateKeyFile=None,
                            tls=True, tlsAllowInvalidCertificates=True)
            c.admin.command("ping")
            return c

    _client = _make_client(MONGO_URI)
    db = _client["learnai"]
    users_col = db["users"]
    quiz_results_col = db["quiz_results"]

    # Unique index on username
    users_col.create_index("username", unique=True)

    mongo_available = True
    print("[MongoDB] Connected to Atlas successfully")
except Exception as _e:
    print(f"[MongoDB] Not available — running without database: {_e}")


def _hash(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()


def _to_quiz_result(doc: dict) -> dict:
    """Convert a MongoDB quiz_results document to Android camelCase format."""
    if doc is None:
        return {}
    ts = doc.get("timestamp", 0)
    if isinstance(ts, str):
        try:
            ts = int(ts)
        except (ValueError, TypeError):
            ts = 0
    return {
        "id":             str(doc.get("_id", "")),
        "topic":          doc.get("topic", ""),
        "taskTitle":      doc.get("task_title", ""),
        "timestamp":      ts,
        "totalQuestions": doc.get("total_questions", 0),
        "correctCount":   doc.get("correct_count", 0),
        "incorrectCount": doc.get("incorrect_count", 0),
        "questions":      doc.get("questions", []),
    }


# ---------------------------------------------------------------------------
# Ollama LLM helpers
# ---------------------------------------------------------------------------

def call_ollama(prompt: str, model: str = OLLAMA_MODEL) -> str | None:
    """Call Ollama local LLM.  Returns generated text or None on any error."""
    try:
        resp = requests.post(
            OLLAMA_URL,
            json={"model": model, "prompt": prompt, "stream": False},
            timeout=120,
        )
        if resp.status_code == 200:
            return resp.json().get("response", "")
        if model == OLLAMA_MODEL:
            return call_ollama(prompt, "llama2")
    except Exception:
        pass
    return None


QUIZ_PROMPT = """Generate a quiz with exactly 3 multiple-choice questions about: {topic}

For EACH question use this EXACT format (no extra lines between fields):

QUESTION: <question text ending with ?>
OPTION A: <option text>
OPTION B: <option text>
OPTION C: <option text>
OPTION D: <option text>
ANS: <letter of correct option, e.g. B>

Repeat this block 3 times. Do not add any other text."""


def parse_quiz(text: str) -> list[dict]:
    """Parse the structured quiz text into a list of question dicts."""
    questions = []
    blocks = re.split(r'(?=QUESTION:)', text, flags=re.IGNORECASE)

    for block in blocks:
        block = block.strip()
        if not block:
            continue

        q_match  = re.search(r'QUESTION:\s*(.+?)(?=OPTION A:)',  block, re.IGNORECASE | re.DOTALL)
        a_match  = re.search(r'OPTION A:\s*(.+?)(?=OPTION B:)',  block, re.IGNORECASE | re.DOTALL)
        b_match  = re.search(r'OPTION B:\s*(.+?)(?=OPTION C:)',  block, re.IGNORECASE | re.DOTALL)
        c_match  = re.search(r'OPTION C:\s*(.+?)(?=OPTION D:)',  block, re.IGNORECASE | re.DOTALL)
        d_match  = re.search(r'OPTION D:\s*(.+?)(?=ANS:)',       block, re.IGNORECASE | re.DOTALL)
        ans_match = re.search(r'ANS:\s*([ABCD])',                 block, re.IGNORECASE)

        if not all([q_match, a_match, b_match, c_match, d_match, ans_match]):
            continue

        options = [
            a_match.group(1).strip(),
            b_match.group(1).strip(),
            c_match.group(1).strip(),
            d_match.group(1).strip(),
        ]
        ans_letter = ans_match.group(1).strip().upper()
        letter_map = {"A": 0, "B": 1, "C": 2, "D": 3}
        correct_answer = options[letter_map.get(ans_letter, 0)]

        questions.append({
            "question": q_match.group(1).strip(),
            "options": options,
            "correct_answer": correct_answer,
        })

    return questions


# ---------------------------------------------------------------------------
# Quiz templates
# ---------------------------------------------------------------------------

TEMPLATES: dict[str, list[dict]] = {
    "default": [
        {
            "question": "What is the primary purpose of version control systems like Git?",
            "options": [
                "To compile source code faster",
                "To track changes and collaborate on code",
                "To deploy applications to production",
                "To optimise database queries",
            ],
            "correct_answer": "To track changes and collaborate on code",
        },
        {
            "question": "Which data structure uses LIFO (Last In, First Out) ordering?",
            "options": ["Queue", "Stack", "Linked List", "Binary Tree"],
            "correct_answer": "Stack",
        },
        {
            "question": "What does API stand for?",
            "options": [
                "Application Programming Interface",
                "Automated Process Integration",
                "Advanced Program Input",
                "Application Protocol Instruction",
            ],
            "correct_answer": "Application Programming Interface",
        },
    ],
    "android": [
        {
            "question": "Which method is called when an Android Activity first becomes visible to the user?",
            "options": ["onCreate()", "onStart()", "onResume()", "onVisible()"],
            "correct_answer": "onStart()",
        },
        {
            "question": "What is the correct way to start a new Activity in Android?",
            "options": [
                "activity.launch()",
                "startActivity(new Intent(this, Target.class))",
                "Intent.start(Target.class)",
                "Activity.open(Target.class)",
            ],
            "correct_answer": "startActivity(new Intent(this, Target.class))",
        },
        {
            "question": "Which layout arranges its children in a single row or column?",
            "options": [
                "ConstraintLayout",
                "RelativeLayout",
                "LinearLayout",
                "FrameLayout",
            ],
            "correct_answer": "LinearLayout",
        },
    ],
    "python": [
        {
            "question": "Which keyword is used to define a function in Python?",
            "options": ["function", "def", "fun", "define"],
            "correct_answer": "def",
        },
        {
            "question": "What is the output of len([1, 2, 3, 4]) in Python?",
            "options": ["3", "4", "5", "Error"],
            "correct_answer": "4",
        },
        {
            "question": "Which Python data structure stores key-value pairs?",
            "options": ["List", "Tuple", "Set", "Dictionary"],
            "correct_answer": "Dictionary",
        },
    ],
    "java": [
        {
            "question": "Which keyword prevents a class from being subclassed in Java?",
            "options": ["static", "abstract", "final", "private"],
            "correct_answer": "final",
        },
        {
            "question": "What is the default value of an int field in a Java class?",
            "options": ["null", "undefined", "0", "-1"],
            "correct_answer": "0",
        },
        {
            "question": "Which collection class provides O(1) average-case access by key?",
            "options": ["ArrayList", "LinkedList", "HashMap", "TreeSet"],
            "correct_answer": "HashMap",
        },
    ],
    "machine learning": [
        {
            "question": "Which technique reduces overfitting by randomly disabling neurons during training?",
            "options": ["Batch Normalisation", "Dropout", "L1 Regularisation", "Pooling"],
            "correct_answer": "Dropout",
        },
        {
            "question": "What does the term 'epoch' mean in neural network training?",
            "options": [
                "A single forward pass",
                "One complete pass through the entire training dataset",
                "A learning rate adjustment step",
                "A single neuron update",
            ],
            "correct_answer": "One complete pass through the entire training dataset",
        },
        {
            "question": "Which algorithm is commonly used for classification and regression using decision boundaries?",
            "options": [
                "K-Means Clustering",
                "PCA",
                "Support Vector Machine",
                "DBSCAN",
            ],
            "correct_answer": "Support Vector Machine",
        },
    ],
    "data structures": [
        {
            "question": "What is the time complexity of searching in a balanced Binary Search Tree?",
            "options": ["O(1)", "O(n)", "O(log n)", "O(n²)"],
            "correct_answer": "O(log n)",
        },
        {
            "question": "Which data structure is best suited for implementing a priority queue?",
            "options": ["Stack", "Array", "Heap", "Linked List"],
            "correct_answer": "Heap",
        },
        {
            "question": "What does BFS stand for in graph traversal?",
            "options": [
                "Binary First Search",
                "Breadth First Search",
                "Branch Function Search",
                "Block File System",
            ],
            "correct_answer": "Breadth First Search",
        },
    ],
    "algorithms": [
        {
            "question": "Which sorting algorithm has the best average-case time complexity of O(n log n)?",
            "options": ["Bubble Sort", "Insertion Sort", "Merge Sort", "Selection Sort"],
            "correct_answer": "Merge Sort",
        },
        {
            "question": "What algorithm technique breaks a problem into overlapping sub-problems and stores their results?",
            "options": [
                "Greedy Algorithm",
                "Divide and Conquer",
                "Dynamic Programming",
                "Backtracking",
            ],
            "correct_answer": "Dynamic Programming",
        },
        {
            "question": "What is the time complexity of binary search?",
            "options": ["O(1)", "O(n)", "O(log n)", "O(n log n)"],
            "correct_answer": "O(log n)",
        },
    ],
    "databases": [
        {
            "question": "Which SQL clause is used to filter results after aggregation?",
            "options": ["WHERE", "FILTER", "HAVING", "GROUP"],
            "correct_answer": "HAVING",
        },
        {
            "question": "What does ACID stand for in database transactions?",
            "options": [
                "Atomicity, Consistency, Isolation, Durability",
                "Access, Control, Index, Data",
                "Async, Cache, Input, Delete",
                "Atomicity, Concurrency, Integrity, Distribution",
            ],
            "correct_answer": "Atomicity, Consistency, Isolation, Durability",
        },
        {
            "question": "Which type of join returns all rows from both tables, matching where possible?",
            "options": ["INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL OUTER JOIN"],
            "correct_answer": "FULL OUTER JOIN",
        },
    ],
}


def get_template_quiz(topic: str) -> list[dict]:
    topic_lower = topic.lower()
    for key in TEMPLATES:
        if key in topic_lower or topic_lower in key:
            qs = TEMPLATES[key].copy()
            random.shuffle(qs)
            return qs[:3]
    return TEMPLATES["default"]


# ---------------------------------------------------------------------------
# Flask routes — existing
# ---------------------------------------------------------------------------

@app.route('/')
def home():
    return jsonify({"status": "LearnAI backend running", "mongo": mongo_available})


@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    topic = request.args.get('topic', '').strip()
    if not topic:
        return jsonify({'error': 'Missing topic parameter'}), 400

    print(f"[getQuiz] topic='{topic}'")

    prompt = QUIZ_PROMPT.format(topic=topic)
    llm_output = call_ollama(prompt)

    questions = []
    if llm_output:
        print("[getQuiz] Ollama responded, parsing…")
        questions = parse_quiz(llm_output)
        print(f"[getQuiz] Parsed {len(questions)} question(s) from LLM")

    if len(questions) < 3:
        print(f"[getQuiz] Topping up from {len(questions)} to 3 with templates")
        template_qs = get_template_quiz(topic)
        for tq in template_qs:
            if len(questions) >= 3:
                break
            questions.append(tq)

    return jsonify({'quiz': questions[:3]}), 200


@app.route('/test', methods=['GET'])
def run_test():
    return jsonify({'quiz': "test"}), 200


# ---------------------------------------------------------------------------
# Flask routes — MongoDB Atlas
# ---------------------------------------------------------------------------

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json(silent=True) or {}
    username = data.get("username", "").strip()
    email    = data.get("email", "").strip()
    password = data.get("password", "")
    phone    = data.get("phone", "").strip()

    if not username or not password:
        return jsonify({"success": False, "message": "Username and password required"}), 400

    if not mongo_available:
        return jsonify({"success": True, "message": "Registered (offline mode)",
                        "user": {"username": username, "email": email, "phone": phone}}), 200

    try:
        if users_col.find_one({"username": username}):
            return jsonify({"success": False, "message": "Username already exists"}), 409

        users_col.insert_one({
            "username": username,
            "email":    email,
            "password": _hash(password),
            "phone":    phone,
            "interests": [],
            "upgrade_tier": "",
            "created_at": datetime.utcnow().isoformat(),
        })
        return jsonify({"success": True, "message": "Registration successful",
                        "user": {"username": username, "email": email, "phone": phone}}), 201
    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500


@app.route('/login', methods=['POST'])
def login():
    data = request.get_json(silent=True) or {}
    username = data.get("username", "").strip()
    password = data.get("password", "")

    if not username or not password:
        return jsonify({"success": False, "message": "Username and password required"}), 400

    if not mongo_available:
        return jsonify({"success": True, "message": "Logged in (offline mode)",
                        "user": {"username": username, "email": "", "phone": "",
                                 "interests": [], "upgrade_tier": ""}}), 200

    user = users_col.find_one({"username": username})
    if not user or user.get("password") != _hash(password):
        return jsonify({"success": False, "message": "Invalid username or password"}), 401

    return jsonify({"success": True, "message": "Login successful",
                    "user": {
                        "username":    user["username"],
                        "email":       user.get("email", ""),
                        "phone":       user.get("phone", ""),
                        "interests":   user.get("interests", []),
                        "upgrade_tier": user.get("upgrade_tier", ""),
                    }}), 200


@app.route('/saveInterests', methods=['POST'])
def save_interests():
    data = request.get_json(silent=True) or {}
    username  = data.get("username", "").strip()
    interests = data.get("interests", [])

    if not username:
        return jsonify({"success": False, "message": "Username required"}), 400

    if not mongo_available:
        return jsonify({"success": True, "message": "Interests saved (offline mode)"}), 200

    users_col.update_one({"username": username}, {"$set": {"interests": interests}})
    return jsonify({"success": True, "message": "Interests saved"}), 200


@app.route('/saveQuizResult', methods=['POST'])
def save_quiz_result():
    data = request.get_json(silent=True) or {}
    username        = data.get("username", "").strip()
    topic           = data.get("topic", "").strip()
    task_title      = data.get("taskTitle", data.get("task_title", topic)).strip()
    questions       = data.get("questions", [])
    correct_count   = int(data.get("correctCount", data.get("correct_count", 0)))
    total_questions = int(data.get("totalQuestions", data.get("total_questions", len(questions))))
    # timestamp sent as long (ms since epoch)
    ts = data.get("timestamp", 0)
    try:
        ts = int(ts)
    except (ValueError, TypeError):
        ts = 0

    if not username:
        return jsonify({"success": False, "message": "Username required"}), 400

    if not mongo_available:
        return jsonify({"success": True, "message": "Quiz result saved (offline mode)"}), 200

    quiz_results_col.insert_one({
        "username":        username,
        "topic":           topic,
        "task_title":      task_title,
        "questions":       questions,
        "correct_count":   correct_count,
        "incorrect_count": total_questions - correct_count,
        "total_questions": total_questions,
        "timestamp":       ts,
    })
    return jsonify({"success": True, "message": "Quiz result saved"}), 201


@app.route('/getHistory', methods=['GET'])
def get_history():
    username = request.args.get("username", "").strip()
    if not username:
        return jsonify({"success": False, "message": "Username required"}), 400

    if not mongo_available:
        return jsonify({"success": True, "history": []}), 200

    results = list(
        quiz_results_col.find({"username": username}).sort("timestamp", -1).limit(50)
    )
    serialized = [_to_quiz_result(r) for r in results]
    return jsonify({"success": True, "history": serialized}), 200


@app.route('/getProfile', methods=['GET'])
def get_profile():
    username = request.args.get("username", "").strip()
    if not username:
        return jsonify({"success": False, "message": "Username required"}), 400

    if not mongo_available:
        return jsonify({"success": True, "profile": {
            "username": username, "email": "", "phone": "",
            "interests": [], "upgrade_tier": "",
            "total_questions": 0, "total_correct": 0, "total_incorrect": 0,
        }}), 200

    user = users_col.find_one({"username": username})
    if not user:
        return jsonify({"success": False, "message": "User not found"}), 404

    # Aggregate stats from quiz_results
    pipeline = [
        {"$match": {"username": username}},
        {"$group": {
            "_id": None,
            "total_questions": {"$sum": "$total_questions"},
            "total_correct":   {"$sum": "$correct_count"},
            "total_incorrect": {"$sum": "$incorrect_count"},
        }}
    ]
    agg = list(quiz_results_col.aggregate(pipeline))
    stats = agg[0] if agg else {"total_questions": 0, "total_correct": 0, "total_incorrect": 0}

    return jsonify({"success": True, "profile": {
        "username":        user["username"],
        "email":           user.get("email", ""),
        "phone":           user.get("phone", ""),
        "interests":       user.get("interests", []),
        "upgrade_tier":    user.get("upgrade_tier", ""),
        "total_questions": stats.get("total_questions", 0),
        "total_correct":   stats.get("total_correct", 0),
        "total_incorrect": stats.get("total_incorrect", 0),
    }}), 200


@app.route('/saveUpgradeTier', methods=['POST'])
def save_upgrade_tier():
    data     = request.get_json(silent=True) or {}
    username = data.get("username", "").strip()
    tier     = data.get("tier", "").strip()

    if not username:
        return jsonify({"success": False, "message": "Username required"}), 400

    if not mongo_available:
        return jsonify({"success": True, "message": "Tier saved (offline mode)"}), 200

    users_col.update_one({"username": username}, {"$set": {"upgrade_tier": tier}})
    return jsonify({"success": True, "message": f"Tier '{tier}' saved"}), 200


if __name__ == '__main__':
    port_num = 5000
    print(f"App running on port {port_num}")
    print(f"MongoDB Atlas: {'connected' if mongo_available else 'not available (offline mode)'}")
    print("LLM backend: Ollama (http://localhost:11434) — falls back to templates if unavailable")
    app.run(port=port_num, host="0.0.0.0")
