from flask import (Blueprint, flash, redirect, render_template, request, session, url_for)
from flaskr.auth import login_required
from database.database import Database

bp = Blueprint('classes', __name__, url_prefix='/classes')

from database.classTeacher import ClassTeacher
import uuid

def updateClassS(studentList, Class, studentListToAdd=[], className=""):
    for student in studentList:
        if student.id in studentListToAdd and Class.id != student.Class.id:
            index = Class.id.find('_')
            idNumber = Class.id[index+1:]

            student.Class = ClassTeacher(className+'_'+idNumber, className)
        elif student.id not in studentListToAdd and Class.id == student.Class.id:
            student.Class = ClassTeacher('','')

def updateClassT(teacherList, Class, teacherID="", className=""):
    for teacher in teacherList:
        isPresented = False
        for cls in teacher.classes:
            if cls.id == Class.id:
                isPresented = True

        if teacher.id == teacherID:
            if not isPresented:
                index = Class.id.find('_')
                idNumber = Class.id[index+1:]

                teacher.classes.append(ClassTeacher(className+'_'+idNumber, className))

        elif teacher.id != teacherID:
            if isPresented:
                for (index,cls) in enumerate(teacher.classes):
                    if cls.id == Class.id:
                        del teacher.classes[index]
                        break

@bp.route('/', methods=('GET',))
@login_required
def classes():
    db = Database(session.get('id'), session.get('hash'))

    studentList = db.getStudent()
    teacherList = db.getTeacher()
    classList = {}

    for teacher in teacherList:
        for cls in teacher.classes:
            classList[cls] = teacher

    return render_template('classes.html', classList=classList, session=session)

@bp.route('/edit', methods=('POST',))
@login_required
def edit():
    db = Database(session.get('id'), session.get('hash'))

    studentList = db.getStudent()
    teacherList = db.getTeacher()

    id = request.form['id']
    index = id.find('_')
    name = id[:index]
    Class = ClassTeacher(id, name)

    save = request.form['save']

    if save == 'true':
        name = request.form['name']
        teacherID = request.form['teacher']
        selectedStudents = []
        for (key,value) in request.form.items():
            if 'student' in key:
                selectedStudents.append(value)

        if len(selectedStudents) != 0:
            updateClassS(studentList, Class, selectedStudents, name)
            updateClassT(teacherList, Class, teacherID, name)

            for student in studentList:
                db.saveStudent(student)
            for teacher in teacherList:
                db.saveTeacher(teacher)
        
        else:
            flash('Musíte přidat alespoň jednoho studenta')

        return redirect(url_for('classes.classes'))
    else:
        studentClass = []
        for student in studentList:
            if student.Class.id == id:
                studentClass.append(student)

        for teacher in teacherList:
            for cls in teacher.classes:
                if cls.id == id:
                    teacherSelected = teacher
                    break

        return render_template('editTables/classes.html', Class=Class, studentList=studentList, teacherList=teacherList, studentClass=studentClass, teacherSelected=teacherSelected)

@bp.route('/new', methods=('POST','GET'))
@login_required
def new():
    db = Database(session.get('id'), session.get('hash'))

    studentList = db.getStudent()
    teacherList = db.getTeacher()

    if request.method == 'POST':
        id = str(uuid.uuid4())
        name = request.form['name']
        teacherID = request.form['teacher']
        selectedStudents = []
        for (key, value) in request.form.items():
            if 'student' in key:
                selectedStudents.append(value)

        Class = ClassTeacher(name+'_'+id, name)

        if len(selectedStudents) != 0:
            updateClassS(studentList, Class, selectedStudents, name)
            updateClassT(teacherList, Class, teacherID, name)

            for student in studentList:
                db.saveStudent(student)
            for teacher in teacherList:
                db.saveTeacher(teacher)

        else:
            flash('Musíte přidat alespoň jednoho studenta')

        return redirect(url_for('classes.classes'))
    else:
        return render_template('newTables/classes.html', studentList=studentList, teacherList=teacherList)

@bp.route('/delete', methods=('POST',))
@login_required
def delete():
    db = Database(session.get('id'), session.get('hash'))

    studentList = db.getStudent()
    teacherList = db.getTeacher()

    id = request.form['id']
    Class = ClassTeacher(id,'')

    updateClassS(studentList, Class)
    updateClassT(teacherList, Class)

    for student in studentList:
        db.saveStudent(student)
    for teacher in teacherList:
        db.saveTeacher(teacher)

    return redirect(url_for('classes.classes'))


