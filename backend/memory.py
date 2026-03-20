import faiss
import numpy as np
from sentence_transformers import SentenceTransformer
from typing import List
import os
import pickle

class PingzMemory:
    def __init__(self, model_name: str = "all-MiniLM-L6-v2", db_path: str = "memory_db"):
        self.model = SentenceTransformer(model_name)
        self.dimension = 384
        self.db_path = db_path
        self.index_path = f"{db_path}/faiss.index"
        self.texts_path = f"{db_path}/texts.pkl"
        
        if not os.path.exists(db_path):
            os.makedirs(db_path)
            
        if os.path.exists(self.index_path) and os.path.exists(self.texts_path):
            self.index = faiss.read_index(self.index_path)
            with open(self.texts_path, "rb") as f:
                self.texts = pickle.load(f)
        else:
            self.index = faiss.IndexFlatL2(self.dimension)
            self.texts: List[str] = []

    def _save(self):
        faiss.write_index(self.index, self.index_path)
        with open(self.texts_path, "wb") as f:
            pickle.dump(self.texts, f)

    def add_text(self, text: str):
        if not text.strip(): return
        # Simple chunking by 1000 characters
        chunks = [text[i:i+1000] for i in range(0, len(text), 1000)]
        for chunk in chunks:
            embedding = self.model.encode([chunk])
            self.index.add(np.array(embedding).astype('float32'))
            self.texts.append(chunk)
        self._save()

    def search(self, query: str, k: int = 3) -> str:
        if not self.texts:
            return ""
        embedding = self.model.encode([query])
        D, I = self.index.search(np.array(embedding).astype('float32'), min(k, len(self.texts)))
        results = [self.texts[i] for i in I[0] if i != -1]
        return "\n---\n".join(results)