from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from pymongo import MongoClient
import numpy as np
from dotenv import load_dotenv
import os

load_dotenv()

app = FastAPI()

MONGO_URL = os.getenv("MONGO_URL")
DB_NAME = os.getenv("DB_NAME")
COLLECTION_NAME = os.getenv("COLLECTION_NAME")


# MongoDB connection
client = MongoClient(MONGO_URL)
db = client[DB_NAME]
collection = db[COLLECTION_NAME]

if not MONGO_URL:
    raise ValueError("MONGO_URL not set in .env")

# Request model
class FaceData(BaseModel):
    user_id: str
    embedding: List[float]

# 🔐 REGISTER API
@app.post("/register")
def register(data: FaceData):
    collection.insert_one({
        "user_id": data.user_id,
        "embedding": data.embedding
    })
    return {"message": "User registered successfully"}

# 🔍 Euclidean Distance
def calculate_distance(e1, e2):
    return np.linalg.norm(np.array(e1) - np.array(e2))

# 🔓 LOGIN API
@app.post("/login")
def login(data: FaceData):
    users = collection.find()

    for user in users:
        dist = calculate_distance(data.embedding, user["embedding"])

        if dist < 1.0:  # threshold
            return {
                "message": "Login Success",
                "user_id": user["user_id"],
                "distance": float(dist)
            }

    return {"message": "No Match Found"}