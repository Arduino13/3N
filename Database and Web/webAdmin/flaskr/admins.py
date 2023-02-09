from flask import (Blueprint, flash, redirect, render_template, request, session, url_for)
from flaskr.auth import login_required
from database.database import Database

import uuid

from database.tools import Filters
from database.tools import gen

from database.admin import Admin

bp = Blueprint('admins', __name__, url_prefix='/admins')

@bp.route('/', methods=('GET', 'POST'))
@login_required
def admins():
    db = Database(session.get('id'), session.get('hash'))

    adminList = db.getAdmin()
    return render_template('admins.html', adminList=adminList, session=session)

@bp.route('edit', methods=('POST',))
@login_required
def edit():
    db = Database(session.get('id'), session.get('hash'))
    admin = db.getAdmin(request.form['id'])[0]

    if request.method == 'POST':
        save = request.form['save']

        if save == 'true':
            id = request.form['id']
            name = request.form['name']

            if Filters.isValid(name):
                admin.name = name

                db.saveAdmin(admin)
                return redirect(url_for('admins.admins'))
        else:
            return render_template('editTables/admins.html', admin=admin)

@bp.route('/new/', methods=('POST','GET'))
@login_required
def new():
    db = Database(session.get('id'), session.get('hash'))

    if request.method == 'POST':
        save = request.form['save']

        if save == 'true':
            name = request.form['name']
            id = name

            if Filters.isValid(name):
                unHashed = gen.generatePass(hashed=False)
                flash('Heslo nově přidaného administrátora je: ' + unHashed)

                db.saveAdmin(Admin(id, name, gen.passHash(unHashed)))
                return redirect(url_for('admins.admins'))

    else:
        return render_template('newTables/admins.html')

@bp.route('/delete', methods=('POST',))
@login_required
def delete():
    db = Database(session.get('id'), session.get('hash'))

    if request.method == 'POST':
        id = request.form['id']
        admin = db.getAdmin(id)[0]

        db.deleteAdmins([admin,])

        return redirect(url_for('admins.admins'))

@bp.route('/chgPass', methods=('POST',))
@login_required
def chgPass():
    pass
