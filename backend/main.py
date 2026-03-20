import os
from fastapi import FastAPI, UploadFile, File, Form, HTTPException, Depends
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from pydantic import BaseModel
from typing import List, Optional
from memory import PingzMemory
from ai_brain import PingzAI
from scrapers import WebScraper, YouTubeScraper, PDFProcessor
import uvicorn
import secrets

app = FastAPI(title="Pingz AI API")
security = HTTPBasic()

# For demonstration: In production, use environment variables and hashed passwords
API_USER = os.getenv("PINGZ_USER", "admin")
API_PASS = os.getenv("PINGZ_PASS", "pingz123")

def get_current_user(credentials: HTTPBasicCredentials = Depends(security)):
    is_user_ok = secrets.compare_digest(credentials.username, API_USER)
    is_pass_ok = secrets.compare_digest(credentials.password, API_PASS)
    if not (is_user_ok and is_pass_ok):
        raise HTTPException(
            status_code=401,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Basic"},
        )
    return credentials.username

memory = PingzMemory()
ai = PingzAI()

class ChatRequest(BaseModel):
    message: str
    user_id: str = "default_user"

class FeedbackRequest(BaseModel):
    message: str
    response: str
    rating: int  # 1 for like, -1 for dislike

@app.post("/chat", dependencies=[Depends(get_current_user)])
async def chat(request: ChatRequest):
    # Retrieve context from long-term memory
    context = memory.search(request.message)
    # Generate response using AI brain with context
    response = ai.generate_response(request.message, context)
    # Save interaction to memory
    memory.add_text(f"User: {request.message}\nAI: {response}")
    return {"response": response}

@app.post("/feedback", dependencies=[Depends(get_current_user)])
async def feedback(request: FeedbackRequest):
    # Log feedback for self-improvement
    with open("feedback.log", "a") as f:
        f.write(f"Query: {request.message} | Response: {request.response} | Rating: {request.rating}\n")
    return {"status": "Feedback received. Thank you!"}

@app.post("/train/url", dependencies=[Depends(get_current_user)])
async def train_url(url: str = Form(...)):
    content = WebScraper.scrape(url)
    memory.add_text(content)
    return {"status": "Knowledge absorbed from URL"}

@app.post("/youtube-learn", dependencies=[Depends(get_current_user)])
@app.post("/train/youtube", dependencies=[Depends(get_current_user)])
async def train_youtube(url: str = Form(...)):
    transcript = YouTubeScraper.get_transcript(url)
    memory.add_text(transcript)
    return {"status": "YouTube knowledge integrated"}

@app.post("/upload", dependencies=[Depends(get_current_user)])
@app.post("/train/upload", dependencies=[Depends(get_current_user)])
async def train_upload(file: UploadFile = File(...)):
    if file.filename.endswith(".pdf"):
        content = await PDFProcessor.process(file)
        memory.add_text(content)
        return {"status": f"PDF {file.filename} processed"}
    elif file.filename.endswith(".txt"):
        content = await file.read()
        memory.add_text(content.decode())
        return {"status": f"Text file {file.filename} learned"}
    else:
        raise HTTPException(status_code=400, detail="Unsupported file format")

@app.get("/health")
async def health():
    return {"status": "Pingz AI is alive"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
