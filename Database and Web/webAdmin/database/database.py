from database.httpConnector import HttpConnector
from database.admin import Admin
from database.teacher import Teacher
from database.student import Student
from database.classTeacher import ClassTeacher
import sys

class Database:
    '''
    Class for handling database operations
    '''

    def __init__(self, id, hash):
        self.id = id
        self.hash = hash

    def __wasSuccesful(self, data):
        return data == 'success/student' or data == 'success/teacher' or data == 'success/admin'

    def getAdmin(self, id = None):
        connector = HttpConnector(self.id, self.hash)

        connector.openMessage('GET')
        if id:
            connector.add({
                'TYPE' : 'admins',
                'ID' : id
            })
        else:
            connector.add({
                'TYPE' : 'admins'
            })

        admins = []
        data = connector.sendResponse()['content']
        del data['type']

        for (id, dataAD) in data.items():
            try:
                admins.append(Admin(id, data[id]['name']))
            except:
                print(sys.exc_info()[0])
                return []
        
        return admins

    def getTeacher(self, id = None):
        connector = HttpConnector(self.id, self.hash)

        connector.openMessage('GET')
        if id: 
            connector.add({
                'TYPE' : 'teachers',
                'ID' : id
            })
        else:
            connector.add({
                'TYPE' : 'teachers'
            })

        data = connector.sendResponse()['content']
        del data['type']

        teachers=[]
        for (id, dataTE) in data.items():
            try:
                classes = []
                for cls in data[id]['classes'].split(','):
                    index = cls.find('_')
            
                    nameCls = cls[:index]
                    idC = cls

                    if idC != '':
                        classes.append(ClassTeacher(idC, nameCls))


                teachers.append(Teacher(id, data[id]['name'], data[id]['email'], classes))
            except:
                raise
                return []


        return teachers

    def getStudent(self, id = None):
        connector = HttpConnector(self.id, self.hash)

        connector.openMessage('GET')
        if id:
            connector.add({
                'TYPE' : 'students',
                'ID' : id
            })
        else:
            connector.add({
                'TYPE' : 'students'
            })

        data = connector.sendResponse()['content']
        del data['type']

        students=[]
        for (id, dataST) in data.items():
            try:
                class_id = dataST['class_id']
                index = class_id.find('_')
                idCls = class_id
                nameCls = class_id[:index]

                students.append(Student(id, dataST['name'], dataST['email'], ClassTeacher(idCls, nameCls)))
            except:
                raise
                return []

        return students

    def saveAdmin(self, admin):
        connector = HttpConnector(self.id, self.hash)

        connector.openData()

        connector.add({
            'TYPE' : 'admins',
            'id' : admin.id,
            'name' : admin.name
        })

        if admin.hash:
            connector.add({
                'hash' : admin.hash
            })

        data = connector.sendResponse()
        
        return self.__wasSuccesful(data)

    def saveTeacher(self, teacher):
        connector = HttpConnector(self.id, self.hash)

        connector.openData()

        classes = ""
        for cls in teacher.classes:
            classes += cls.id + ","

        classes = classes[:-1]

        connector.add({
            'TYPE' : 'teachers',
            'id' : teacher.id,
            'name' : teacher.name,
            'classes' : classes,
            'email' : teacher.email
        })

        if teacher.hash:
            connector.add({
                'hash' : teacher.hash
            })

        data = connector.sendResponse()

        return self.__wasSuccesful(data)

    def saveStudent(self, student):
        connector = HttpConnector(self.id, self.hash)

        connector.openData()

        connector.add({
            'TYPE' : 'students',
            'id' : student.id,
            'name' : student.name,
            'class_id' : student.Class.id,
            'email' : student.email,
            'words' : dict(),
            'web_pages' : dict(),
            'homework' : dict(),
            'tests' : dict()
        })

        if student.hash:
            connector.add({
                'hash' : student.hash
            })

        data = connector.sendResponse()

        return self.__wasSuccesful(data)

    def deleteStudents(self, students):
        connector = HttpConnector(self.id, self.hash)

        connector.openMessage('DEL')

        idsToDelete = ""
        for (index,student) in enumerate(students):
            idsToDelete += student.id + ","

        idsToDelete = idsToDelete[:-1]

        connector.add({
            'TYPE' : 'students',
            'ID' : idsToDelete
        })

        data = connector.sendResponse()

        return self.__wasSuccesful(data)

    def deleteTeachers(self, teachers):
        connector = HttpConnector(self.id, self.hash)

        connector.openMessage('DEL')

        idsToDelete = ""
        for (index, teacher) in enumerate(teachers):
            idsToDelete += teacher.id + ","

        idsToDelete = idsToDelete[:-1]

        connector.add({
            'TYPE' : 'teachers',
            'ID' : idsToDelete
        })

        data = connector.sendResponse()

        return self.__wasSuccesful(data)

    def deleteAdmins(self, admins):
        connector = HttpConnector(self.id, self.hash)

        connector.openMessage('DEL')

        idsToDelete = ""
        for (index, admin) in enumerate(admins):
            idsToDelete += admin.id + ","

        idsToDelete = idsToDelete[:-1]

        connector.add({
            'TYPE' : 'admins',
            'ID' : idsToDelete
        })

        data = connector.sendResponse()

        return self.__wasSuccesful(data)

    def __isClassInList(self, classes, cls):
        for clsList in classes:
            if clsList.id == cls.id:
                return True

        return False

    def __removeClassesFromList(self, classes, clsList):
        toReturn = []
        idsToRemove = []
        for cls in clsList:
            idsToRemove.append(cls.id)


        for classT in classes:
            if classT.id not in idsToRemove:
                toReturn.append(classT)

        return toReturn
        
    
    def deleteClasses(self, classes, students, teachers):
        connector = HttpConnector(self.id, self.hash)

        connector.openMessage('DEL')

        idsToDelete = ""
        for (index, student) in enumerate(students):
            if self.__isClassInList(classes, student.classID):
                idsToDelete += student.id + ","

        idsToDelete = idsToDelete[:-1]

        connector.add({
            'TYPE' : 'students',
            'ID' : idsToDelete
        })

        data = connector.sendResponse()

        if self.__wasSuccesful(data):
            for teacher in teachers:
                teacher.classes = self.__removeClassFromList(teacher.classes, classes)
            for student in students:
                if student.id in idsToDelete:
                    students.remove(student)

            return True
        else:
            return False
