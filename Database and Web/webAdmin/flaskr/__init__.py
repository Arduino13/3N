import os

from flask import (Flask, redirect, url_for)

def create_app(test_config=None):
    # create and configure the app
    app = Flask(__name__, instance_relative_config=True)
    app.config.from_mapping(
            SECRET_KEY='kldkflj*(&*$&(#!!'
    )

    if test_config is None:
        # load the instance config, if it exists, when not testing
        app.config.from_pyfile('config.py', silent=True)
    else:
        # load the test config if passed in
        app.config.from_mapping(test_config)

    # ensure the instance folder exists
    try:
        os.makedirs(app.instance_path)
    except OSError:
        pass

    from . import auth
    from . import students
    from . import teachers
    from . import admins
    from . import classes
    app.register_blueprint(auth.bp)
    app.register_blueprint(students.bp)
    app.register_blueprint(teachers.bp)
    app.register_blueprint(admins.bp)
    app.register_blueprint(classes.bp)

    @app.route('/')
    def root():
        return redirect(url_for('auth.logout'))

    return app


