from fastapi import FastAPI
from src.controller import predict

app = FastAPI()
app.include_router(predict.router)