import os
import json
from flask import Flask
from flask import request
from database.databaseJson import databaseJson

def create_app(test_config=None):
    
    app = Flask(__name__, instance_relative_config=True)

    if test_config is None:
        app.config.from_pyfile('config.py', silent=True)
    else:
        app.config.from_mapping(test_config)

    try:
        os.makedirs(app.instance_path)
    except OSError:
        pass

    @app.route('/run', methods=['POST'])
    def process():
        print('---------------------------------------------------------------------------------------')
        print(request.json)

        db = databaseJson()
        response = db.process(json.dumps(request.json))

        print('---------------------------------------------------------------------------------------')
        return response.encode('utf-8')

    return app

