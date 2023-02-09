import functools

from Crypto.Hash import SHA512

from database.database import Database
from database.tools import Filters

from flask import (
    Blueprint, flash, g, redirect, render_template, request, session, url_for
)

bp = Blueprint('auth', __name__, url_prefix='/auth')

def __passHash(passWord):
    h = SHA512.new()
    h.update(passWord.encode('utf-8'))
    return h.hexdigest()

@bp.route('/login', methods=('GET', 'POST'))
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        
        if Filters.isValid(username):
            db = Database(username, __passHash(password))
            
            admins = db.getAdmin(username)
            if len(admins) != 0:
                session.clear()
                session['id'] = username
                session['hash'] = __passHash(password)
                return redirect(url_for('students.students'))

    return render_template('auth/login.html')

@bp.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('auth.login'))

def login_required(view):
    @functools.wraps(view)
    def wrapped_view(**kwargs):
        if session.get('id') is None:
            return redirect(url_for('auth.login'))

        return view(**kwargs)
    
    return wrapped_view
