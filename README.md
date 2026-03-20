# Pingz AI — Advanced Self-Learning Assistant

Pingz AI is a production-ready AI assistant similar to Gemini, featuring a FastAPI backend with RAG (Retrieval-Augmented Generation) memory and a modern Android application.

## 🚀 Key Features
- **Intelligent Chat**: Expert-level reasoning with memory.
- **RAG Memory**: Uses FAISS vector database to learn from provided information.
- **Custom Training**: Support for URLs, YouTube transcripts, and PDF/Text file uploads.
- **Modern UI**: Clean Material 3 chat interface.
- **CI/CD Built-in**: Auto-generates APK on every push.

## ━━━━━━━━━━━━━━ 1. BACKEND (AI ENGINE) ━━━━━━━━━━━━━━
The backend is built with Python/FastAPI and uses the Gemini API for intelligence.

### Setup:
1. `cd backend`
2. `pip install -r requirements.txt`
3. Create a `.env` file from `.env.template` and set your `GEMINI_API_KEY`.
4. Customize your `PINGZ_USER` and `PINGZ_PASS` for Basic Authentication.
5. `python main.py`

### API Endpoints:
- `POST /chat`: Interact with the AI Brain.
- `POST /feedback`: Provide feedback for self-improvement.
- `POST /train/url`: Absorb knowledge from a webpage.
- `POST /train/youtube` (or `/youtube-learn`): Learn from a YouTube transcript.
- `POST /train/upload` (or `/upload`): Upload PDF/Text files to long-term memory.

## ━━━━━━━━━━━━━━ 2. ANDROID APP (APK) ━━━━━━━━━━━━━━
The app provides a direct interface to the Pingz AI Brain.

### Build instructions:
1. Open the project in Android Studio.
2. Update `BASE_URL` in `MainActivity.kt` and `TrainingActivity.kt` to point to your hosted backend.
3. If you changed the backend credentials, update `Credentials.basic("admin", "pingz123")` in both activities.
4. Build the APK using `./gradlew assembleDebug` or via GitHub Actions.

## ━━━━━━━━━━━━━━ 3. MEMORY & LEARNING ━━━━━━━━━━━━━━
Pingz AI uses FAISS (vector database) and Sentence Transformers for RAG:
- **Long-term storage**: Knowledge is stored in `backend/memory_db` and persists across restarts.
- **Step-by-step reasoning**: The AI is prompted to think deeply and reason through problems.
- **Adaptive learning**: User feedback is logged and can be used to fine-tune responses.

## ━━━━━━━━━━━━━━ 4. SECURITY ━━━━━━━━━━━━━━
- **Basic Auth**: Protects all endpoints from unauthorized access.
- **Env Vars**: Keeps sensitive keys out of the codebase.
- **HTTPS Ready**: Designed to be deployed with SSL for production.

Developed by Ashwin.
━━━━━━━━━━━━━━━━━━━━━━━ GOAL ━━━━━━━━━━━━━━━━━━━━━━━
To create a highly intelligent, self-learning, and scalable AI system.
