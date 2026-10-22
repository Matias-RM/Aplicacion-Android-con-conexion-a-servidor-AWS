from flask import Flask, request, jsonify

app = Flask(__name__)

USERS_DB = {
    "matias": {
        "password": "perazzz",
        "name": "Matías Reyes",
        "email": "m.reyes22@ufromail.cl",
        "rut": "22.318.500-2"
    },
    "admin": {
        "password": "1234",
        "name": "admin",
        "email": "admin@gmail.com",
        "rut": "12.345.678-9"
    },
    "marta": {
        "password": "0000",
        "name": "Marta Silva",
        "email": "marta.silva@gmail.com",
        "rut": "18.765.432-1"
    },
    "carlos": {
        "password": "5960",
        "name": "Carlos Pérez",
        "email": "carlos.perez@gmail.com",
        "rut": "15.987.654-K"
    },
    "sofia": {
        "password": "sofiM",
        "name": "Sofía Morales",
        "email": "sofia.morales@gmail.com",
        "rut": "20.123.456-7"
    }
}

@app.route('/api/login', methods=['POST'])
def login():
    data = request.get_json()
    
    if not data or 'username' not in data or 'password' not in data:
        return jsonify({
            "status": "error",
            "message": "Faltan parámetros 'username' o 'password'"
        }), 400

    username = data['username'].strip().lower()
    password = data['password'].strip()

    if username in USERS_DB and USERS_DB[username]["password"] == password:
        user_info = USERS_DB[username]
        return jsonify({
            "status": "success",
            "message": "Autenticación exitosa",
            "user": {
                "name": user_info["name"],
                "email": user_info["email"],
                "rut": user_info["rut"]
            }
        }), 200
    else:
        return jsonify({
            "status": "error",
            "message": "Usuario o contraseña incorrectos"
        }), 401


@app.route('/api/user', methods=['GET'])
def get_user():

    username = request.args.get('username', '').strip().lower()

    if username and username in USERS_DB:
        user_info = USERS_DB[username]
    else:
        user_info = USERS_DB["admin"] # Usuario por defecto
        
    return jsonify({
        "status": "success",
        "data": {
            "name": user_info["name"],
            "email": user_info["email"],
            "rut": user_info["rut"]
        }
    }), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
