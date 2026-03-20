import requests
from bs4 import BeautifulSoup
from youtube_transcript_api import YouTubeTranscriptApi
from pypdf import PdfReader
import io
from fastapi import UploadFile

class WebScraper:
    @staticmethod
    def scrape(url: str) -> str:
        try:
            res = requests.get(url, timeout=10)
            res.raise_for_status()
            soup = BeautifulSoup(res.text, 'html.parser')
            # Remove script and style elements
            for script_or_style in soup(["script", "style"]):
                script_or_style.decompose()
            # Extract text from main tags
            texts = [p.get_text().strip() for p in soup.find_all(['p', 'h1', 'h2', 'h3', 'li'])]
            return "\n".join([t for t in texts if t])
        except Exception as e:
            return f"Error scraping {url}: {str(e)}"

class YouTubeScraper:
    @staticmethod
    def get_transcript(url: str) -> str:
        try:
            # Extract video ID from URL
            if "v=" in url:
                video_id = url.split("v=")[1].split("&")[0]
            else:
                video_id = url.split("/")[-1]
            
            transcript = YouTubeTranscriptApi.get_transcript(video_id)
            return " ".join([t['text'] for t in transcript])
        except Exception as e:
            return f"Error fetching YouTube transcript: {str(e)}"

class PDFProcessor:
    @staticmethod
    async def process(file: UploadFile) -> str:
        try:
            content = await file.read()
            pdf_reader = PdfReader(io.BytesIO(content))
            text = ""
            for page in pdf_reader.pages:
                text += page.extract_text() + "\n"
            return text
        except Exception as e:
            return f"Error processing PDF: {str(e)}"