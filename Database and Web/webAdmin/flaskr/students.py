from flask import (Blueprint, flash, g, redirect, render_template, request, session, url_for)
from flaskr.auth import login_required
from database.database import Database
from database.tools import Filters
from database.tools import gen

from database.classTeacher import ClassTeacher
from database.student import Student
import uuid

#TODO correct filters, controlling of inpout from forms

bp = Blueprint('students', __name__, url_prefix='/students')

@bp.route('/', methods=('GET',))
@login_required
def students():
    db = Database(session.get('id'), session.get('hash'))

    studentList = db.getStudent()
    return render_template('students.html', studentList=studentList, session=session)

@bp.route('/edit', methods=('POST',))
@login_required
def edit():
    db = Database(session.get('id'), session.get('hash'))
    student = db.getStudent(request.form['id'])[0]

    if request.method == 'POST':
        save = request.form['save']
        
        if save == 'true':
            id = request.form['id']
            name = request.form['name']
            email = request.form['email']

            if Filters.isValid(name) and Filters.isValid(email):
                    student.name = name
                    student.email = email
                
                    db.saveStudent(student)
                    return redirect(url_for('students.students'))

        else:
            return render_template('editTables/students.html', student=student)

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
            Class = ClassTeacher("","")

            password = gen.generatePass()

            if Filters.isValid(name) and Filters.isValid(email):
                db.saveStudent(Student(id, name, email, Class, gen.passHash(password)))
                emailSender.sendPassword(name, password, email)

                return redirect(url_for('students.students'))

    else:
        return render_template('newTables/students.html')

@bp.route('/delete', methods=('POST',))
@login_required
def delete():
    db = Database(session.get('id'), session.get('hash'))

    if request.method == 'POST':
        id = request.form['id']
        student = db.getStudent(id)[0]

        db.deleteStudents([student,])

        return redirect(url_for('students.students'))

import flaskr.emailSender as emailSender

@bp.route('/chgPass', methods=('POST',))
@login_required
def chgPass():
    db = Database(session.get('id'), session.get('hash'))

    id = request.form['id']
    password = gen.generatePass()
    
    student = db.getStudent(id)[0]

    print(student)

    student.hash = gen.passHash(password)

    db.saveStudent(student)
    emailSender.sendPassword(student.name, password, student.email)

    return redirect(url_for('students.students'))

