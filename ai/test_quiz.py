# test_quiz.py
import requests

data = {"spotName": "경복궁 근정전", "grade": 3, "problemCnt": 2}

response = requests.post("http://localhost:8000/generate-problem", json=data)
print(f"Status: {response.status_code}")
print(f"Response: {response.json()}")
