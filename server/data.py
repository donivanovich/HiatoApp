from pymongo import MongoClient
import os, urllib.parse
from dotenv import load_dotenv

load_dotenv()
user = os.getenv('MONGO_USER')
passw = os.getenv('MONGO_PASS')
host = os.getenv('MONGO_HOST')
MONGO_URI = f"mongodb+srv://{urllib.parse.quote_plus(user)}:{urllib.parse.quote_plus(passw)}@{host}/hiato"
client = MongoClient(MONGO_URI)
db = client.hiato

# LIMPIAR
db.users.drop()
db.grupos.drop()
db.gastos.drop()
db.gastos_users.drop()

# DATOS CON ID ENTEROS
db.users.insert_many([
    {"id": 1, "email": "donnie@gmail.com", "password": "1234", "nombre": "Donnie"},
    {"id": 2, "email": "ivan@gmail.com", "password": "1234", "nombre": "Ivan"},
    {"id": 3, "email": "ana@gmail.com", "password": "1234", "nombre": "Ana"}
])

db.grupos.insert_many([
    {"id": 1, "nombre": "Verano 2025", "user_id": 1},
    {"id": 2, "nombre": "Ibiza 2026", "user_id": 3}
])

db.gastos.insert_many([
    {"id": 1, "grupoId": 1, "nombre": "Mojitos", "precio": 12.0},
    {"id": 2, "grupoId": 1, "nombre": "Hotel", "precio": 200.0},
    {"id": 3, "grupoId": 2, "nombre": "Cena Ana", "precio": 45.0}
])

db.gastos_users.insert_many([
    {"id": 1, "gasto_id": 1, "user_id": 1},
    {"id": 2, "gasto_id": 1, "user_id": 2},
    {"id": 3, "gasto_id": 2, "user_id": 1},
    {"id": 4, "gasto_id": 3, "user_id": 3}
])

client.close()
print("✅ ¡DB lista con IDs 1,2,3...!")
