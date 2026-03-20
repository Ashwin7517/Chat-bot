import os
import google.generativeai as genai
from typing import Optional

class PingzAI:
    def __init__(self, api_key: Optional[str] = None):
        # Use provided key or from environment
        self.api_key = api_key or os.getenv("GEMINI_API_KEY")
        if not self.api_key:
             # Default fallback message if no key is present
             print("Warning: GEMINI_API_KEY not found in environment.")
        else:
            genai.configure(api_key=self.api_key)
            self.model = genai.GenerativeModel('gemini-1.5-pro')
            self.system_prompt = """
            You are Pingz AI, a highly advanced, expert-level intelligent assistant.
            Your purpose is to provide precise, well-reasoned, and helpful responses based on the provided context and your extensive knowledge.
            
            Key Behaviors:
            1. Deep Thinker: Always analyze questions thoroughly. If a problem is complex, break it down step-by-step.
            2. Adaptive Learner: Utilize the provided context (which may include information from PDFs, websites, or YouTube transcripts) to provide the most relevant answer.
            3. Memory-Aware: You are aware that you have a long-term memory system. Use it to maintain consistency across interactions.
            4. Precise & Professional: Your tone is professional, intelligent, and helpful.
            5. Step-by-Step Reasoning: For technical or logical questions, explicitly show your reasoning process.
            
            If the context is irrelevant to the question, state that you are relying on your general knowledge.
            """

    def generate_response(self, message: str, context: str = "") -> str:
        if not self.api_key:
            return "Pingz AI is currently in offline mode (API key missing). How can I assist you locally?"
        
        prompt = f"{self.system_prompt}\n\nContext for reference:\n{context}\n\nUser Question: {message}\n\nResponse:"
        try:
            response = self.model.generate_content(prompt)
            return response.text
        except Exception as e:
            return f"Pingz AI encountered an error: {str(e)}"
