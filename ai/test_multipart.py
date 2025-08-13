import requests

files = {
    "file": ("7.jpg", open(r"C:\Users\SSAFY\S13P11A301\ai\7.jpg", "rb"), "image/jpeg")
}
data = {"pose_select": "front"}

response = requests.post("http://localhost:8000/pose/full", files=files, data=data)
print(f"Status: {response.status_code}")
print(f"Response: {response.json()}")
