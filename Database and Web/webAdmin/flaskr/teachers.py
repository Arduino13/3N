from flask import (Blueprint, flash, redirect, render_template, request, session, url_for)
from flaskr.auth import login_required
from database.database import Database

import uuid

from database.tools import Filters
from database.tools import gen

from database.classTeacher import ClassTeacher
from database.teacher import Teacher

bp = Blueprint('teachers', __name__, url_prefix='/teachres')

@bp.route('/', methods=('GET',))
@login_required
def teachers():
    db = Database(session.get('id'), session.get('hash'))

    teacherList = db.getTeacher()
    return render_template('teachers.html', teacherList=teacherList, session=session)

@bp.route('edit', methods=('POST',))
@login_required
def edit():
    db = Database(session.get('id'), session.get('hash'))
    teacher = db.getTeacher(request.form['id'])[0]

    if request.method == 'POST':
        save = request.form['save']

        if save == 'true':
            id = request.form['id']
            name = request.form['name']
            email = request.form['email']

            if Filters.isValid(name) and Filters.isValid(email):
                teacher.name = name
                teacher.email = email

                db.saveTeacher(teacher)
                return redirect(url_for('teachers.teachers'))
        else:
            classNames = ""
            for cls in teacher.classes:
                classNames += cls.name
                if not cls == teacher.classes[-1]:
                    classNames += ','

            return render_template('editTables/teachers.html', teacher=teacher, classNames=classNames)

@bp.route('/new', methods=('POST','GET'))
@login_required
def new():
    db = Database(session.get('id'), session.get('hash'))

    if request.method == 'POST':
        save = request.form['save']

        if save == 'true':
            name = request.form['name']
            id = name
            email = request.form['email']
            classes = [ClassTeacher("","")]

            password = gen.generatePass()

            if Filters.isValid(name) and Filters.isValid(email):
                db.saveTeacher(Teacher(id, name, email, classes, gen.passHash(password)))
                emailSender.sendPassword(name, password, email)

                return redirect(url_for('teachers.teachers'))

    else:
        return render_template('newTables/teachers.html')

@bp.route('/delete', methods=('POST',))
@login_required
def delete():
    db = Database(session.get('id'), session.get('hash'))

    if request.method == 'POST':
        id = request.form['id']
        teacher = db.getTeacher(id)[0]

        db.deleteTeachers([teacher,])

        return redirect(url_for('teachers.teachers'))

import flaskr.emailSender as emailSender

@bp.route('/chgPass', methods=('POST',))
@login_required
def chgPass():
    db = Database(session.get('id'), session.get('hash'))

    id = request.form['id']
    password = gen.generatePass()

    teacher = db.getTeacher(id)[0]

    teacher.hash = gen.passHash(password)

    db.saveTeacher(teacher)
    emailSender.sendPassword(teacher.name, password, teacher.email)

    return redirect(url_for('teachers.teachers'))
