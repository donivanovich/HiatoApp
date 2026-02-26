from flask import Flask, request, jsonify
from flask_jwt_extended import JWTManager, create_access_token
from pymongo import MongoClient
import os, urllib.parse
from dotenv import load_dotenv

app = Flask(__name__)
load_dotenv()

MONGO_URI = f"mongodb+srv://{urllib.parse.quote_plus(os.getenv('MONGO_USER'))}:{urllib.parse.quote_plus(os.getenv('MONGO_PASS'))}@{os.getenv('MONGO_HOST')}/hiato"
client = MongoClient(MONGO_URI)
db = client.hiato

app.config['JWT_SECRET_KEY'] = os.getenv('JWT_SECRET_KEY', 'super-secret')
jwt = JWTManager(app)

# 🛠️ FUNCIÓN MÁGICA: Elimina _id de TODOS los docs
def remove_id_fields(docs):
    result = []
    for doc in docs:
        clean_doc = {k: v for k, v in doc.items() if k != '_id'}
        result.append(clean_doc)
    return result

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    user = db.users.find_one({'email': data['email']})
    if user and user['password'] == data['password']:
        return jsonify({'token': create_access_token(identity=user['id'])})
    return jsonify({'msg': 'Error login'}), 401

@app.route('/users', methods=['GET'])
def get_users():
    users = db.users.find()  # ✅ TODO incluido (password visible)
    clean_users = remove_id_fields(list(users))  # Solo elimina _id
    return jsonify(clean_users)

@app.route('/grupos', methods=['GET'])
def get_grupos():
    grupos = db.grupos.find()
    clean_grupos = remove_id_fields(list(grupos))
    return jsonify(clean_grupos)

@app.route('/gastos', methods=['GET'])
def get_gastos():
    gastos = db.gastos.find()
    clean_gastos = remove_id_fields(list(gastos))
    return jsonify(clean_gastos)

@app.route('/gastos_users', methods=['GET'])
def get_gastos_users():
    junctions = db.gastos_users.find()
    clean_junctions = remove_id_fields(list(junctions))
    return jsonify(clean_junctions)

@app.route('/users/<int:user_id>', methods=['PUT'])
def update_user(user_id):
    data = request.get_json()
    
    # ✅ Validación básica (campos requeridos)
    if not data.get('email') or not data.get('password') or not data.get('nombre'):
        return jsonify({'msg': 'Faltan campos obligatorios: email, password, nombre'}), 400
    
    # ✅ Actualiza SOLO los campos enviados (id NO se toca)
    update_data = {k: v for k, v in data.items() if k in ['email', 'password', 'nombre']}
    
    result = db.users.update_one(
        {'id': user_id},  # ← Busca por id (NO _id)
        {'$set': update_data}  # ← Actualiza parcial
    )
    
    if result.modified_count == 0:
        return jsonify({'msg': 'Usuario no encontrado o sin cambios'}), 404
    
    # ✅ Devuelve usuario actualizado (sin _id)
    updated_user = db.users.find_one({'id': user_id})
    clean_user = {k: v for k, v in updated_user.items() if k != '_id'}
    return jsonify(clean_user), 200

@app.route('/gastos', methods=['POST'])
def create_gasto():
    data = request.get_json()
    
    # ✅ Validación básica (campos requeridos)
    if not all(key in data for key in ['grupoId', 'nombre', 'precio']):
        return jsonify({'msg': 'Faltan campos obligatorios: grupoId, nombre, precio'}), 400
    
    if not isinstance(data['grupoId'], int) or data['grupoId'] <= 0:
        return jsonify({'msg': 'grupoId debe ser un entero positivo'}), 400
    
    if not isinstance(data['nombre'], str) or len(data['nombre'].strip()) == 0:
        return jsonify({'msg': 'nombre debe ser un string no vacío'}), 400
    
    if not isinstance(data['precio'], (int, float)) or data['precio'] <= 0:
        return jsonify({'msg': 'precio debe ser un número positivo'}), 400
    
    # ✅ Genera nuevo ID autoincremental
    last_gasto = db.gastos.find_one(sort=[('id', -1)])
    new_id = (last_gasto['id'] + 1) if last_gasto else 1
    
    # ✅ Crea el gasto
    nuevo_gasto = {
        'id': new_id,
        'grupoId': data['grupoId'],
        'nombre': data['nombre'].strip(),
        'precio': float(data['precio'])
    }
    
    result = db.gastos.insert_one(nuevo_gasto)
    
    if result.acknowledged:
        # ✅ Limpia _id y devuelve el gasto creado
        clean_gasto = {k: v for k, v in nuevo_gasto.items() if k != '_id'}
        return jsonify(clean_gasto), 201  # 201 = Created
    else:
        return jsonify({'msg': 'Error al crear gasto'}), 500

@app.route('/grupos', methods=['POST'])
def create_grupo():
    data = request.get_json()
    print("🔍 DATA RECIBIDA:", data)
    
    # ✅ Validación básica (campos requeridos)
    if not all(key in data for key in ['nombre', 'user_id']):
        return jsonify({'msg': 'Faltan campos obligatorios: nombre, user_id'}), 400
    
    if not isinstance(data['user_id'], int) or data['user_id'] <= 0:
        return jsonify({'msg': 'user_id debe ser un entero positivo'}), 400
    
    if not isinstance(data['nombre'], str) or len(data['nombre'].strip()) == 0:
        return jsonify({'msg': 'nombre debe ser un string no vacío'}), 400
    
    # ✅ Verifica que el user existe
    user_exists = db.users.find_one({'id': data['user_id']})
    if not user_exists:
        return jsonify({'msg': 'Usuario no encontrado'}), 404
    
    # ✅ Genera nuevo ID autoincremental
    last_grupo = db.grupos.find_one(sort=[('id', -1)])
    new_id = (last_grupo['id'] + 1) if last_grupo else 1
    
    # ✅ Crea el grupo
    nuevo_grupo = {
        'id': new_id,
        'nombre': data['nombre'].strip(),
        'user_id': data['user_id']  # ✅ Corregido: data['user_id']
    }
    
    result = db.grupos.insert_one(nuevo_grupo)
    
    if result.acknowledged:
        # ✅ Limpia _id y devuelve el grupo creado
        clean_grupo = {k: v for k, v in nuevo_grupo.items() if k != '_id'}
        return jsonify(clean_grupo), 201  # 201 = Created
    else:
        return jsonify({'msg': 'Error al crear grupo'}), 500

@app.route('/gastos_users', methods=['POST'])
def create_gasto_user():
    data = request.get_json()
    print("🔍 GASTOS_USERS DATA:", data)
    
    # ✅ Solo snake_case
    gasto_id = data.get('gasto_id')
    user_id = data.get('user_id')
    
    print("🔍 PARSEADO gasto_id={}, user_id={}".format(gasto_id, user_id))
    
    # ✅ VALIDACIONES OBLIGATORIAS
    if not gasto_id or not isinstance(gasto_id, int):
        return jsonify({'error': 'gasto_id requerido (entero)'}), 400
    
    if not user_id or not isinstance(user_id, int):
        return jsonify({'error': 'user_id requerido (entero)'}), 400
    
    # ✅ VERIFICAR QUE EL USUARIO EXISTA
    user_exists = db.users.find_one({'id': user_id})
    if not user_exists:
        return jsonify({'error': f'Usuario con ID {user_id} no existe'}), 404
    
    # ✅ VERIFICAR QUE EL GASTO EXISTA
    gasto_exists = db.gastos.find_one({'id': gasto_id})
    if not gasto_exists:
        return jsonify({'error': f'Gasto con ID {gasto_id} no existe'}), 404
    
    # 🚫 EVITAR DUPLICADOS (snake_case completo)
    duplicado = db.gastos_users.find_one({
        'gasto_id': gasto_id,
        'user_id': user_id
    })
    if duplicado:
        return jsonify({
            'error': f'Usuario {user_id} ya está asignado a este gasto {gasto_id}'
        }), 409
    
    # ✅ Nuevo ID secuencial
    last_id = db.gastos_users.find_one(sort=[('id', -1)])
    new_id = (last_id['id'] + 1) if last_id else 1
    
    # ✅ Todo snake_case en DB
    nuevo_gasto_user = {
        'id': new_id,
        'gasto_id': gasto_id,
        'user_id': user_id
    }
    
    db.gastos_users.insert_one(nuevo_gasto_user)
    
    # ✅ Response snake_case para Android
    response = {
        'id': new_id,
        'gasto_id': gasto_id,
        'user_id': user_id
    }
    
    print("✅ GASTO_USER CREADO:", response)
    return jsonify(response), 201


if __name__ == '__main__':
    app.run(debug=True, port=8000)
