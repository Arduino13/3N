from database.databaseObj import DatabaseObj
from database.tools.Bundle import Bundle
from database.tools.Json_parser import JSON_parse

class databaseJson:
    __databaseObj = None

    class RequestNamespace:
        messageText = 'text'
        typeOfObject = 'TYPE'
        classID = 'CLASS_ID'
        fromWord = 'FROM_v'
        idUser = 'id_user'
        hash = 'hash'
        homeworkID = 'HOMEWORK_ID'
        homework = 'homework'
        vocabulary = 'words'
        id = 'ID'
        delete = 'DEL'
        auth = 'AUTH'
        get = 'GET'

    class ResponseNamespace:
        successStudent = 'success/student'
        successVocabulary = 'success/vocabulary'
        successHomework = 'success/homework'
        successTeacher = 'success/teacher'
        successAdmin = 'success/admin'
        errorStudent = 'database_error/student'
        errorVocabulary = 'database_error/vocabulary'
        errorHomework = 'database_error/homework'
        errorTeacher = 'database_error/teacher'
        errorAdmin = 'database_error/admin'
        errorDB = 'database_error'
        errorParser = 'parsing error'
        error = 'error'
        update = 'upd'
        text = 'text'

    def __isSubSet(self, of, to):
        return set(to).issubset(set(of))

    def createMessage(self, message, typeMSG):
        message = Bundle(Bundle.Message, {self.ResponseNamespace.text : message}, spec = typeMSG)
        return JSON_parse.parse_OUT(message)

    def createData(self, data):
        return JSON_parse.parse_OUT(data)

    def __tryDecode(self, data):
        '''
        Tries to parse JSON message
        '''
        try:
            processedData = JSON_parse.parse_IN(data)
            id = processedData.getHeader()[self.RequestNamespace.idUser]
            hash = processedData.getHeader()[self.RequestNamespace.hash]

            self.__databaseObj = DatabaseObj(id, hash)

            return processedData, None
        except Exception as e:
            print(e)
            return None, self.createMessage(self.ResponseNamespace.errorParser, Bundle.spec.error)

    def __saveDataProcessLine(self, data):
        '''
        Saves given data and determinetes which object data represents 
        '''
        if self.__databaseObj.getRing() == DatabaseObj.Ring.Student:
            if self.__databaseObj.saveStudent(data):
                return self.createMessage(self.ResponseNamespace.successStudent, Bundle.spec.success)
            else:
                return self.createMessage(self.ResponseNamespace.errorStudent, Bundle.spec.error)

        elif self.__databaseObj.getRing() == DatabaseObj.Ring.Teacher:
            if self.__isSubSet(data.getHeaders(), [self.RequestNamespace.homework]):
                if self.__databaseObj.addHomework(data):
                    return self.createMessage(self.ResponseNamespace.successHomework, Bundle.spec.success)
                else:
                    return self.createMessage(self.ResponseNamespace.errorHomework, Bundle.spec.error)
            elif self.__isSubSet(data.getHeaders(), [self.RequestNamespace.vocabulary]):
                if self.__databaseObj.addWords(data):
                    return self.createMessage(self.ResponseNamespace.successVocabulary, Bundle.spec.success)
                else:
                    return self.createMessage(self.ResponseNamespace.errorVocabulary, Bundle.spec.error)
        elif self.__databaseObj.getRing() == DatabaseObj.Ring.Admin and self.__isSubSet(data.getHeaders(), [self.RequestNamespace.typeOfObject]):
            typeDATA = data.getData()[self.RequestNamespace.typeOfObject]

            try:
                typeDATA = DatabaseObj.DBType(typeDATA)
            except:
                return createMessage(self.ResponseNamespace.errorParser, Bundle.spec.error)

            if typeDATA == DatabaseObj.DBType.student:
                if self.__databaseObj.saveStudent(data):
                    return self.createMessage(self.ResponseNamespace.successAdmin, Bundle.spec.success)
                else:
                    return self.createMessage(self.ResponseNamespace.errorAdmin, Bundle.spec.error)
            if typeDATA == DatabaseObj.DBType.teacher:
                if self.__databaseObj.saveTeacher(data):
                    return self.createMessage(self.ResponseNamespace.successAdmin, Bundle.spec.success)
                else:
                    return self.createMessage(self.ResponseNamespace.errorAdmin, Bundle.spec.error)
            if typeDATA == DatabaseObj.DBType.admin:
                if self.__databaseObj.saveAdmin(data):
                    return self.createMessage(self.ResponseNamespace.successAdmin, Bundle.spec.success)
                else:
                    return self.createMessage(self.ResponseNamespace.errorAdmin, Bundle.spec.error)


    def __getReturnMessageDelete(self):
        '''
        Returns response about succesfully deleting an object
        '''
        if self.__databaseObj.getRing() == DatabaseObj.Ring.Student:
            return self.createMessage(self.ResponseNamespace.successStudent, Bundle.spec.success)
        elif self.__databaseObj.getRing() == DatabaseObj.Ring.Teacher:
            return self.createMessage(self.ResponseNamespace.successTeacher, Bundle.spec.success)
        elif self.__databaseObj.getRing() == DatabaseObj.Ring.Admin:
            return self.createMessage(self.ResponseNamespace.successAdmin, Bundle.spec.success)

    def __deleteDataProcessLine(self, data):
        '''
        Deletes given data and determinates which object data represents
        '''
        if self.__isSubSet(data.getHeaders(), [self.RequestNamespace.typeOfObject]):
            try:
                database = DatabaseObj.DBType(data.getData()[self.RequestNamespace.typeOfObject])
            except:
                return createMessage(self.ResponseNamespace.errorParser, Bundle.spec.error)

            if self.__databaseObj.getRing() > DatabaseObj.Ring.Guest and self.__isSubSet(data.getHeaders(), [self.RequestNamespace.classID]):
                data = data.getData()[self.RequestNamespace.classID].split(';')
                result= self.__databaseObj.delete(database,
                                                  data,
                                                  [self.RequestNamespace.classID for i in range(0,len(data))])

            elif self.__databaseObj.getRing() > DatabaseObj.Ring.Guest and self.__isSubSet(data.getHeaders(), [self.RequestNamespace.id]):
                data = data.getData()[self.RequestNamespace.id].split(';')
                result = self.__databaseObj.delete(database,
                                                   data,
                                                   [self.RequestNamespace.id for i in range(0,len(data))])

            elif self.__databaseObj.getRing() > DatabaseObj.Ring.Guest and self.__isSubSet(data.getHeaders(), [self.RequestNamespace.fromWord]):
                data = data.getData()[self.RequestNamespace.fromWord].split(';')
                result = self.__databaseObj.delete(database,
                                                   data,
                                                   [self.RequestNamespace.fromWord for i in range(0,len(data))])

            elif self.__databaseObj.getRing() > DatabaseObj.Ring.Student and self.__isSubSet(data.getHeaders(), [self.RequestNamespace.homeworkID]):
                data = data.getData()[self.RequestNamespace.homeworkID].split(';')
                result = self.__databaseObj.delete(database,
                                                   data,
                                                   [self.RequestNamespace.homeworkID for i in range(0,len(data))])
            else:
                return self.createMessage(self.ResponseNamespace.errorDB, Bundle.spec.error)
            

            if result:
                return self.__getReturnMessageDelete()
            else:
                return self.createMessage(self.ResponseNamespace.errorDB, Bundle.spec.error)

    def __getDataProcessLine(self, data):
        '''
        Returns object requested in data

        TODO: get rid of try/except 
        '''
        if self.__databaseObj.getRing() == DatabaseObj.Ring.Student:
            if self.__isSubSet(data.getHeaders(), [self.RequestNamespace.typeOfObject]):
                try:
                    typeDATA = data.getData()[self.RequestNamespace.typeOfObject]
                except:
                    return self.createMessage(self.ResponseNamespace.errorParser, Bundle.spec.error)

                if typeDATA == 'rankList':
                    return self.createData(self.__databaseObj.getRankList())
            
                elif typeDATA == DatabaseObj.DBType.student.value:
                    student = self.__databaseObj.studentData()
                    if student:
                        return self.createData(student)
                    else:
                        return self.createMessage(self.ResponseNamespace.errorStudent, Bundle.spec.error)

        elif self.__databaseObj.getRing() == DatabaseObj.Ring.Teacher:
            if self.__isSubSet(data.getHeaders(), [self.RequestNamespace.typeOfObject]):
                typeDATA = data.getData()[self.RequestNamespace.typeOfObject]
                try:
                    typeDATA = DatabaseObj.DBType(data.getData()[self.RequestNamespace.typeOfObject])
                except:
                    return self.createMessage(self.ResponseNamespace.errorParser, Bundle.spec.error)

                if typeDATA == DatabaseObj.DBType.teacher:
                    teacher = self.__databaseObj.teacherData()
                    if teacher:
                        return self.createData(teacher)
                    else:
                        return self.createMessage(self.ResponseNamespace.errorTeacher, Bundle.spec.error)

                elif typeDATA == DatabaseObj.DBType.student:
                    if self.__isSubSet(data.getHeaders(), [self.RequestNamespace.classID]):
                        classID = data.getData()[self.RequestNamespace.classID]
                        students = self.__databaseObj.studentData(classID, True)
                        if students:
                            return self.createData(students)
                        else:
                            return self.createMessage(self.ResponseNamespace.errorTeacher, Bundle.spec.error)
                    else:
                        return self.createMessage(self.ResponseNamespace.errorTeacher, Bundle.spec.error)
                
                else:
                    return self.createMessage(self.ResponseNamespace.errorDB, Bundle.spec.error)
            else:
                return self.createMessage(self.ResponseNamespace.errorTeacher, Bundle.spec.error)

        elif self.__databaseObj.getRing() == DatabaseObj.Ring.Admin:
            if self.__isSubSet(data.getHeaders(), [self.RequestNamespace.typeOfObject]):
                typeDATA = data.getData()[self.RequestNamespace.typeOfObject]
                try:
                    typeDATA = DatabaseObj.DBType(data.getData()[self.RequestNamespace.typeOfObject])
                except:
                    return self.createMessage(self.ResponseNamespace.errorParser, Bundle.spec.error)

                if typeDATA == DatabaseObj.DBType.admin:
                    try: 
                        idAD = data.getData()[self.RequestNamespace.id]
                    except:
                        idAD = None

                    admin = self.__databaseObj.adminData(idAD)
                    if admin:
                        return self.createData(admin)
                    else:
                        return self.createMessage(self.ResponseNamespace.errorAdmin, Bundle.spec.error)

                elif typeDATA == DatabaseObj.DBType.teacher:
                    try: 
                        idTE = data.getData()[self.RequestNamespace.id]
                    except:
                        idTE = None

                    teacher = self.__databaseObj.teacherData(idTE)
                    if teacher:
                        return self.createData(teacher)
                    else:
                        return self.createMessage(self.ResponseNamespace.errorAdmin, Bundle.spec.error)

                elif typeDATA == DatabaseObj.DBType.student:
                    try:
                        idST = data.getData()[self.RequestNamespace.id]
                    except:
                        idST = None

                    students = self.__databaseObj.studentData(idST)
                    if students:
                        return self.createData(students)
                    else:
                        return self.createMessage(self.ResponseNamespace.errorAdmin, Bundle.spec.error)

                else:
                    return self.createMessage(self.ResponseNamespace.errorDB, Bundle.spec.error)
            else:
                return self.createMessage(self.ResponseNamespace.errorAdmin, Bundle.spec.error)   

        else:
            return self.createMessage(self.ResponseNamespace.errorDB, Bundle.spec.error)

    def __authentication(self):
        '''
        Check creditals
        '''
        if self.__databaseObj.getRing() == DatabaseObj.Ring.Student:
            return self.createMessage(self.ResponseNamespace.successStudent, Bundle.spec.success)
        elif self.__databaseObj.getRing() == DatabaseObj.Ring.Teacher:
            return self.createMessage(self.ResponseNamespace.successTeacher, Bundle.spec.success)
        elif self.__databaseObj.getRing() == DatabaseObj.Ring.Admin:
            return self.createMessage(self.ResponseNamespace.successAdmin, Bundle.spec.success)
        else:
            return self.createMessage(self.ResponseNamespace.errorDB, Bundle.spec.error)

    def process(self, data):
        '''
        Main function for parsing JSON requests
        '''
        processedData, message = self.__tryDecode(data)

        if message:
            return message

        if processedData.getType() == Bundle.Message and self.__isSubSet(processedData.getHeaders(), [self.RequestNamespace.messageText]):
            text = processedData.getData()[self.RequestNamespace.messageText]

            if text == self.RequestNamespace.auth:
                return self.__authentication()
            elif text == self.RequestNamespace.get:
                return self.__getDataProcessLine(processedData)
            elif text == self.RequestNamespace.delete:
                return self.__deleteDataProcessLine(processedData)
            else:
                return self.__createMessage(self.ResponseNamespace.errorDB, Bundle.spec.error)
        elif processedData.getType() == Bundle.Data:
            return self.__saveDataProcessLine(processedData) 





