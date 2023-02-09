import enum
import uuid
import time
import json
import operator
from database.database import Database
from database.tools.Bundle import Bundle

class DatabaseObj:
    '''
    Class with methods for saving/loading/deleting 
    homework, web, student etc. objects

    TODO: use JOIN 
    '''
    class Ring(enum.IntEnum):
        Guest = -1
        Student = 0
        Teacher = 1
        Admin = 2

    __Database = None
    __ring = Ring.Guest
    __id = ''
    __hash = ''

    class DBType(enum.Enum):
        student = 'students'
        teacher = 'teachers'
        admin = 'admins'
        webPage = 'web_pages'
        vocabulary = 'vocabulary'
        test = 'tests'
        homework = 'homework'
        rankList = 'rankList'

    class DBRankListNamespace():
        studentID = 'ID'
        numberOfTests = 'number_of_tests'
        numberOfSuccess = 'number_of_success'
        numberOfFail = 'number_of_fail'
        successRation = 'success_ratio'

    class DBStudentNamespace(): 
        id = 'ID'
        name = 'NAME'
        classID = 'CLASS_ID'
        vocabulary = 'words'
        webPages = 'web_pages'
        homework = 'homework'
        email = 'email'
        tests = 'tests'
        hash = 'HASH'
        headers = [id.lower(), name.lower(), classID.lower(), vocabulary.lower(), webPages.lower(), homework.lower(), tests.lower()]

    class DBTeacherNamespace():
        id = 'ID'
        name = 'NAME'
        email = 'email'
        classes = 'CLASSES'
        vocabulary = 'words'
        homework = 'homework'
        hash = 'HASH'
        headers = [id.lower(), name.lower(), classes.lower(), email.lower()]

    class DBAdminNamespace():
        id = 'ID'
        name = 'NAME'
        hash = 'HASH'
        headers = [id.lower(), name.lower()]

    class DBWebPageNamespace():
        id = 'ID'
        address = 'ADDRESS'
        name = 'NAME'
        classID = 'CLASS_ID'
        headers = [address.lower(), name.lower()]

    class DBVocabularyNamespace(): 
        id = 'ID'
        fromWord = 'FROM_v'
        toWord = 'TO_v'
        classID = 'CLASS_ID'

    class DBTestNamespace():
        id = 'ID'
        date = 'DATE'
        words = 'WORDS'
        wrongWords = 'WRONG_WORDS'
        homeworkID = 'HOMEWORK_ID'
        classID = 'CLASS_ID'
        headers = [date.lower(), words.lower(), wrongWords.lower(), homeworkID.lower()]

    class DBHomeworkNamespace():
        id = 'ID'
        name = 'NAME'
        homeworkID = 'HOMEWORK_ID'
        studentID = 'STUDENT_ID'
        classID = 'CLASS_ID'
        date = 'DATE'
        dateCompleted = 'DATE_COMPLETED'
        words = 'WORDS'
        wordsCompleted = 'WORDS_COMPLETED'
        completed = 'COMPLETED'
        problematicWords = 'problematicWords'

        headersStudent = [completed.lower(), wordsCompleted.lower()]
        headersTeacher = [date.lower(), dateCompleted.lower(), classID.lower(), name.lower(), homeworkID.lower(), studentID.lower(), words.lower()]

    def __DBNamespaceByType(self, database):
        mapDB = {
            self.DBType.student : self.DBStudentNamespace,
            self.DBType.teacher : self.DBTeacherNamespace,
            self.DBType.admin : self.DBAdminNamespace,
            self.DBType.webPage : self.DBWebPageNamespace,
            self.DBType.vocabulary : self.DBVocabularyNamespace,
            self.DBType.test : self.DBTestNamespace,
            self.DBType.homework : self.DBHomeworkNamespace
        }

        if database in mapDB:
            return mapDB[database]
        else:
            raise Exception('unsupported type of database') 

    def __paramAdd(self, params):
        '''
        Helper function for building WHERE condition
        '''
        paramString = ''

        for (index, param) in enumerate(params):
            if index == (len(params)-1):
                paramString += param + ' = %s'
            else:
                paramString += param + ' = %s AND '

        return paramString

    def __paramOr(self, params):
        '''
        Helper function for building WHERE condition
        '''
        paramString = ''

        for (index, param) in enumerate(params):
            if index == (len(params)-1):
                paramString += param + ' = %s'
            else:
                paramString += param + ' = %s OR '

        return paramString

    def __isSubSet(self, of, to):
        return set(to).issubset(set(of))

    def __exist(self ,database, ids, params = ["ID"], addCommands = True):
        result = self.__Database.select(database, '*', self.__paramAdd(params) if addCommands else self.__paramOr(params), ids)
        return len(result) != 0

    def __getUUID(self):
        return str(uuid.uuid4())

    def __checkPass(self, database, id, hash):
        '''
        Authentication of user
        '''
        namespace = self.__DBNamespaceByType(database)
        return hash == self.__Database.select(database.value, [namespace.hash], self.__paramAdd([namespace.id]), [id])[0][0]
    
    def getRing(self):
        return self.__ring

    def __init__(self, id, hash):
        '''
        Connects to database and determinates user privileges
        '''
        self.__Database = Database('DB NAME PLACEHOLDER', 'USER PLACEHOLDER', 'PASSWORD PLACEHOLDER')

        if self.__exist(self.DBType.student.value, [id]) and self.__checkPass(self.DBType.student, id, hash):
            self.__ring = self.Ring.Student

        elif self.__exist(self.DBType.teacher.value, [id]) and self.__checkPass(self.DBType.teacher, id, hash):
            self.__ring = self.Ring.Teacher

        elif self.__exist(self.DBType.admin.value, [id]) and self.__checkPass(self.DBType.admin, id, hash):
            self.__ring = self.Ring.Admin

        self.__id = id
        self.__hash = hash

    def __getProblematicWords(self, homework_id):
        '''
        Return five words with lowest success ration for given homework_id
        '''
        if self.__ring > self.Ring.Student:
            tests = self.__Database.select(self.DBType.test.value,
                                            '*',
                                            self.__paramAdd([self.DBTestNamespace.homeworkID]),
                                            [homework_id])
            wrongWordsCounter = {}
            
            for test in tests:
                wrongWords = test[3].split(';')

                for word in wrongWords:
                    if word in wrongWordsCounter:
                        wrongWordsCounter[word] += 1
                    else:
                        wrongWordsCounter[word] = 1
            sortedList = sorted(wrongWordsCounter.items(), key=operator.itemgetter(1))

            return sortedList[:5]
        return None

    def getRankList(self):
        '''
        Return rank list of all students

        TODO: fix to certain number of students
        '''
        if self.__ring == self.Ring.Student:
            listOfRanks = self.__Database.select(self.DBType.rankList.value,
                                                '*')
            students = self.__Database.select(self.DBType.student.value,
                        '*')

            data = {}

            for rank in listOfRanks:
                numTests = rank[1]
                numSuccess = rank[2]
                numFail = rank[3]
                name = ''
                for student in students:
                    if student[0] == rank[0]:
                        name = student[1]

                if numSuccess!='0':
                    successRation = int(float(numSuccess)/(float(numSuccess)+float(numFail))*100)
                else:
                    successRation = 0
                
                data[name] = {
                        self.DBRankListNamespace.numberOfTests : str(numTests),
                        self.DBRankListNamespace.successRation : str(successRation),
                        }

            return Bundle(Bundle.Data, data)
        return None

    def adminData(self, id = __id):
        '''
        Returns admin data, id is by default id of logged user
        If given id is None, than all admins are returned
        '''
        if self.__ring > self.Ring.Teacher:
            if id:
                admins = self.__Database.select(self.DBType.admin.value,
                                            '*',
                                           self.__paramAdd([self.DBAdminNamespace.id]),
                                           [id])
            else:
                admins = self.__Database.select(self.DBType.admin.value,
                                            '*')
            
            data = {}

            for admin in admins:
                id = admin[0]
                name = admin[1]

                data[id] = {self.DBAdminNamespace.name.lower() : name}

            return Bundle(Bundle.Data, data)
    
    def teacherData(self, id = __id):
        '''
        Returns teacher data, id is by default id of logged user
        If given id is None all teachers are returned
        '''
        if self.__ring > self.Ring.Student:
            if not id:
                teachers = self.__Database.select(self.DBType.teacher.value,
                                                  '*')
            else:
                teachers = self.__Database.select(self.DBType.teacher.value,
                                                  '*',
                                                  self.__paramAdd([self.DBTeacherNamespace.id]),
                                                  [id])

            data = {}

            for teacher in teachers:
                id = teacher[0]
                name = teacher[1]
                classes = teacher[2]
                email = teacher[4]

                data.update({
                    id: {
                        self.DBTeacherNamespace.name.lower() : name,
                        self.DBTeacherNamespace.classes.lower() : classes,
                        self.DBTeacherNamespace.email.lower() : email,
                    }
                })

            return Bundle(Bundle.Data, data)
        else:
            return None

    def studentData(self, id = None, byClass = False):
        '''
        Returns student data by default id is set to None
        in that case all students are returned.
        Id can be also class id than switch byClass needs to be True
        '''
        if self.__ring > self.Ring.Guest:

            if byClass and not id:
                raise Exception('incompatible setting - student')

            if byClass:
                students = self.__Database.select(self.DBType.student.value,
                                                  '*',
                                                  self.__paramAdd([self.DBStudentNamespace.classID]),
                                                  [id])

            else:
                if self.__ring != self.Ring.Admin:
                    students = self.__Database.select(self.DBType.student.value,
                                                  '*',
                                                  self.__paramAdd([self.DBStudentNamespace.id]),
                                                  [id if id else self.__id])
                else:
                    if id:
                        students = self.__Database.select(self.DBType.student.value,
                                                '*',
                                                self.__paramAdd([self.DBStudentNamespace.id]),
                                                [id])
                    else:
                        students = self.__Database.select(self.DBType.student.value,
                                                '*')
            
            data = {}
            
            for student in students:
                id = student[0]
                name = student[1]
                classID = student[2]
                email = student[4]

                if not byClass:
                    homework = self.__Database.select(self.DBType.homework.value,
                                                  '*',
                                                  self.__paramAdd([self.DBHomeworkNamespace.classID, self.DBHomeworkNamespace.studentID]),
                                                  [classID, self.__id])
                    webPages = self.__Database.select(self.DBType.webPage.value,
                                                  '*',
                                                  self.__paramAdd([self.DBWebPageNamespace.classID]),
                                                  [self.__id])

                    vocabulary = self.__Database.select(self.DBType.vocabulary.value,
                                                  '*',
                                                  self.__paramAdd([self.DBVocabularyNamespace.classID]),
                                                  [self.__id])

                    vocabulary += self.__Database.select(self.DBType.vocabulary.value,
                                                  '*',
                                                  self.__paramAdd([self.DBVocabularyNamespace.classID]),
                                                  [classID])
                else: 
                '''In case of loading by using class_id, use of students objects
                is for teacher object, and therefore only homework and vocabulary objects
                are needed'''
                    webPages = []

                    homework = self.__Database.select(self.DBType.homework.value,
                                                 '*',
                                                 self.__paramAdd([self.DBHomeworkNamespace.classID, self.DBHomeworkNamespace.studentID]),
                                                 [classID, id])
                    vocabulary = self.__Database.select(self.DBType.vocabulary.value,
                                                 '*',
                                                 self.__paramAdd([self.DBVocabularyNamespace.classID]),
                                                 [classID])
                
                homeworkDict = {}
                for h in homework:
                    idH = h[0]
                    nameH = h[1]
                    date = h[2]
                    dateC = h[3]
                    words = h[4]
                    wordsC = h[5]
                    completed = h[6]
                    homeworkID= h[7]
                   
                    if self.__ring != self.Ring.Student:
                        problematicWords = self.__getProblematicWords(homeworkID)
                        listOfWords = []
                        for (word, value) in problematicWords:
                            listOfWords.append(word)

                        problematicWords = ','.join(listOfWords)
                    else:
                        problematicWords = ""

                    homeworkDict.update({
                        idH:{
                            self.DBHomeworkNamespace.name.lower() : nameH,
                            self.DBHomeworkNamespace.homeworkID.lower() : homeworkID,
                            self.DBHomeworkNamespace.classID.lower() : classID,
                            self.DBHomeworkNamespace.date.lower() : date,
                            self.DBHomeworkNamespace.dateCompleted.lower() : dateC,
                            self.DBHomeworkNamespace.words.lower() : words,
                            self.DBHomeworkNamespace.wordsCompleted.lower() : wordsC,
                            self.DBHomeworkNamespace.completed.lower() : completed,
                            self.DBHomeworkNamespace.problematicWords : problematicWords
                        }
                    })
                    
                webDict = {}
                for w in webPages:
                    idW = w[0]
                    address = w[1]
                    nameW = w[2]

                    webDict.update({
                        idW:{
                            self.DBWebPageNamespace.address.lower() : address,
                            self.DBWebPageNamespace.name.lower() : nameW
                        }
                    })

                vocabularyDict = {}
                for v in vocabulary:
                    fromWord = v[1]
                    toWord = v[2]
                    classIDV = v[3]

                    if classIDV in vocabularyDict:
                        vocabularyDict[classIDV].update({
                            fromWord : toWord
                        })
                    else:
                        vocabularyDict[classIDV] = {
                            fromWord : toWord
                        }

                data.update({
                    id : {
                        self.DBStudentNamespace.name.lower() : name,
                        self.DBStudentNamespace.classID.lower() : classID,
                        self.DBStudentNamespace.email.lower() : email,
                        self.DBStudentNamespace.homework.lower() : homeworkDict,
                        self.DBStudentNamespace.webPages.lower() : webDict,
                        self.DBStudentNamespace.vocabulary.lower() : vocabularyDict
                    }
                })

            return Bundle(Bundle.Data, data)
        else:
            return None

    def saveStudent(self, data):
        result = True

        if data and self.__isSubSet(data.getHeaders(), self.DBStudentNamespace.headers):
            studentData = data.getData()
            print(studentData.keys())

            if self.__ring > self.Ring.Teacher and self.__isSubSet(studentData.keys(), self.DBStudentNamespace.headers) and self.__isSubSet(studentData.keys(), [self.DBStudentNamespace.email]):
                if not self.__exist(self.DBType.student.value, [studentData[self.DBStudentNamespace.id.lower()]]):
                    return self.__Database.insert(self.DBType.student.value,
                                                  [studentData[self.DBStudentNamespace.id.lower()], 
                                                   studentData[self.DBStudentNamespace.name.lower()],
                                                   studentData[self.DBStudentNamespace.classID.lower()],
                                                   studentData[self.DBStudentNamespace.hash.lower()],
                                                   studentData[self.DBStudentNamespace.email.lower()]])
                else:
                    return self.__Database.update(self.DBType.student.value,
                                                  [self.DBStudentNamespace.name,
                                                   self.DBStudentNamespace.classID,
                                                   self.DBStudentNamespace.email],
                                                  self.__paramAdd([self.DBStudentNamespace.id]),
                                                  [studentData[self.DBStudentNamespace.name.lower()],
                                                   studentData[self.DBStudentNamespace.classID.lower()],
                                                   studentData[self.DBStudentNamespace.email.lower()],
                                                   studentData[self.DBStudentNamespace.id.lower()]])


            elif self.__ring == self.Ring.Student and self.__isSubSet(studentData.keys(), self.DBStudentNamespace.headers):
                for (fromWord, toWord) in studentData[self.DBStudentNamespace.vocabulary.lower()].items():
                    if not self.__Database.insert(self.DBType.vocabulary.value,
                                                  [self.__getUUID(),
                                                   fromWord,
                                                   toWord,
                                                   self.__id]):
                        result = False

                for id in studentData[self.DBStudentNamespace.webPages.lower()].keys():
                    page = studentData[self.DBStudentNamespace.webPages.lower()][id]

                    if self.__isSubSet(page.keys(), self.DBWebPageNamespace.headers):
                        if not self.__Database.insert(self.DBType.webPage.value,
                                                      [id,
                                                       page[self.DBWebPageNamespace.address.lower()],
                                                       page[self.DBWebPageNamespace.name.lower()],
                                                       self.__id]):
                            result = False
                
                for id in studentData[self.DBStudentNamespace.homework.lower()].keys():
                    homework = studentData[self.DBStudentNamespace.homework.lower()][id]

                    if self.__exist(self.DBType.homework.value, 
                                    [id]):
                        if self.__isSubSet(homework.keys(), self.DBHomeworkNamespace.headersStudent):
                            if not self.__Database.update(self.DBType.homework.value,
                                                          [self.DBHomeworkNamespace.completed,
                                                           self.DBHomeworkNamespace.wordsCompleted],
                                                           self.__paramAdd([self.DBHomeworkNamespace.id]),
                                                           [homework[self.DBHomeworkNamespace.completed.lower()],
                                                            homework[self.DBHomeworkNamespace.wordsCompleted.lower()],
                                                            id]):
                                result = False
                            else:
                                result = False


                listRank = self.__Database.select(self.DBType.rankList.value,
                                                    '*')

                classID = studentData[self.DBStudentNamespace.classID.lower()]

                numTests = 0
                numSuccess = 0
                numFail = 0
                newRankList = True
                for listR in listRank:
                    newRankList = False
                    if listR[0] == self.__id:
                        numTests = int(listR[1])
                        numSuccess = int(listR[2])
                        numFail = int(listR[3])

                
                for id in studentData[self.DBStudentNamespace.tests.lower()].keys():
                    test = studentData[self.DBStudentNamespace.tests.lower()][id]
                    
                    if self.__isSubSet(test.keys(), self.DBTestNamespace.headers):
                        wrongWordsLen = len(test[self.DBTestNamespace.wrongWords.lower()].split(';'))
                        if test[self.DBTestNamespace.wrongWords.lower()] == '':
                            wrongWordsLen = 0

                        numSuccess += len(test[self.DBTestNamespace.words.lower()].keys()) - wrongWordsLen
                        numFail += wrongWordsLen
                        numTests += 1

                        if not self.__Database.insert(self.DBType.test.value, 
                                                      [id,
                                                       test[self.DBTestNamespace.date.lower()],
                                                       json.dumps(test[self.DBTestNamespace.words.lower()]),
                                                       test[self.DBTestNamespace.wrongWords.lower()],
                                                       classID,
                                                       test[self.DBTestNamespace.homeworkID.lower()]]):
                            result = False
                    else:
                        result = False
                
                
                if newRankList:
                    self.__Database.insert(self.DBType.rankList.value,
                                            [self.__id,
                                            str(numTests),
                                            str(numSuccess),
                                            str(numFail)])
                else:
                    self.__Database.update(self.DBType.rankList.value,
                                            [self.DBRankListNamespace.numberOfTests,
                                            self.DBRankListNamespace.numberOfSuccess,
                                            self.DBRankListNamespace.numberOfFail],
                                            self.__paramAdd([self.DBRankListNamespace.studentID]),
                                            [str(numTests),
                                            str(numSuccess),
                                            str(numFail),
                                            self.__id])
                                            
                return result
            else:
                return False

                                                          
    def saveTeacher(self, data):
        if data and self.__isSubSet(data.getHeaders(), self.DBTeacherNamespace.headers):
            teacherData = data.getData()

            if self.__ring > self.Ring.Teacher:
                if not self.__exist(self.DBType.teacher.value, [teacherData[self.DBTeacherNamespace.id.lower()]]):
                    return self.__Database.insert(self.DBType.teacher.value,
                                                  [teacherData[self.DBTeacherNamespace.id.lower()],
                                                   teacherData[self.DBTeacherNamespace.name.lower()],
                                                   teacherData[self.DBTeacherNamespace.classes.lower()],
                                                   teacherData[self.DBTeacherNamespace.hash.lower()],
                                                   teacherData[self.DBTeacherNamespace.email.lower()]])
                else:
                    return self.__Database.update(self.DBType.teacher.value,
                                                  [self.DBTeacherNamespace.name,
                                                   self.DBTeacherNamespace.classes,
                                                   self.DBTeacherNamespace.email],
                                                   self.__paramAdd([self.DBTeacherNamespace.id]),
                                                   [teacherData[self.DBTeacherNamespace.name.lower()],
                                                    teacherData[self.DBTeacherNamespace.classes.lower()],
                                                    teacherData[self.DBTeacherNamespace.email.lower()],
                                                    teacherData[self.DBTeacherNamespace.id.lower()]]) 
            else:
                return False
        else: 
            return False

    def saveAdmin(self, data):
        if data and self.__isSubSet(data.getHeaders(), self.DBAdminNamespace.headers):
            admin = data.getData()

            if self.__ring > self.Ring.Teacher:
                if not self.__exist(self.DBType.admin.value, [admin[self.DBAdminNamespace.id.lower()]]):
                    return self.__Database.insert(self.DBType.admin.value,
                                                  [admin[self.DBAdminNamespace.id.lower()],
                                                   admin[self.DBAdminNamespace.name.lower()],
                                                   admin[self.DBAdminNamespace.hash.lower()]])
                else:
                    return self.__Database.update(self.DBType.admin.value,
                                                  [self.DBAdminNamespace.name],
                                                  self.__paramAdd([self.DBAdminNamespace.id]),
                                                  [admin[self.DBAdminNamespace.name.lower()],
                                                   admin[self.DBAdminNamespace.id.lower()]])

            else:
                return False
        else: 
            return False

    def addHomework(self, data):
        result = True

        if data and self.__isSubSet(data.getHeaders(), [self.DBTeacherNamespace.homework.lower()]):
            if self.__ring > self.Ring.Student:
                for id in data.getData()[self.DBTeacherNamespace.homework.lower()].keys():
                    homework = data.getData()[self.DBTeacherNamespace.homework.lower()][id]

                    if self.__isSubSet(homework.keys(), self.DBHomeworkNamespace.headersTeacher):
                        if not self.__Database.insert(self.DBType.homework.value,
                                                      [id,
                                                       homework[self.DBHomeworkNamespace.name.lower()],
                                                       homework[self.DBHomeworkNamespace.date.lower()],
                                                       homework[self.DBHomeworkNamespace.dateCompleted.lower()],
                                                       json.dumps(homework[self.DBHomeworkNamespace.words.lower()]),
                                                       '',
                                                       'false',
                                                       homework[self.DBHomeworkNamespace.homeworkID.lower()],
                                                       homework[self.DBHomeworkNamespace.studentID.lower()],
                                                       homework[self.DBHomeworkNamespace.classID.lower()]]):
                            result = False
                    else: 
                        result = False
            else: 
                result = False
        else: 
            result = False

        return result

    def addWords(self, data):
        result  = True

        if data and self.__isSubSet(data.getHeaders(), [self.DBTeacherNamespace.vocabulary.lower()]):
            if self.__ring > self.Ring.Student:
                for id in data.getData()[self.DBTeacherNamespace.vocabulary.lower()].keys():
                    words = data.getData()[self.DBTeacherNamespace.vocabulary.lower()][id]

                    for fromWord, toWord in words.items():
                        if not self.__Database.insert(self.DBType.vocabulary.value,
                                                      [self.__getUUID(),
                                                       fromWord,
                                                       toWord,
                                                       id]):
                            result = False

            else:
                return False
        else:
            return False

        return result

    def __delete(self, typeDB, ids, params):
        '''
        Deletes objects based, with checking if user privileges are sufficient
        params specify if class_id, id, homework_id etc. is used

        TODO: buggy implementation, need complete rewriting, works only if deleting
        data have only one class_id
        '''
        if self.__ring > self.Ring.Teacher:
            return self.__Database.remove(typeDB.value,
                                          self.__paramAdd(params),
                                          ids)
        elif self.__ring > self.Ring.Student:
            #loading class_id of objects specifed by params and ids
            classes = self.__Database.select(self.DBType.teacher.value,
                                             [self.DBTeacherNamespace.classes],
                                             self.__paramAdd([self.DBTeacherNamespace.id]),
                                             [self.__id])[0][0].split(',')


            if typeDB == self.DBType.vocabulary:
                class_ids = self.__Database.select(typeDB.value,
                                                  [self.DBVocabularyNamespace.classID],
                                                  self.__paramOr(params),
                                                  ids)

                for class_id in class_ids:
                    class_id = class_id[0]
                    result = True

                    #checking if privileges are sufficient
                    if class_id in classes:
                        if not self.__Database.remove(typeDB.value,
                                                  self.__paramOr(params),
                                                  ids):
                            result = False
                    else:
                        result = False

                return result
            elif typeDB == self.DBType.homework:
                class_ids = self.__Database.select(typeDB.value,
                                                  [self.DBHomeworkNamespace.classID],
                                                  self.__paramOr(params),
                                                  ids)

                for class_id in class_ids:
                    class_id = class_id[0]
                    result = True
                   
                    if class_id in classes:
                        if not self.__Database.remove(typeDB.value,
                                                     self.__paramOr(params),
                                                     ids):
                            result = False

                    else:
                        result = False

                return result
        elif self.__ring > self.Ring.Guest:
            if typeDB == self.DBType.webPage:
                class_ids = self.__Database.select(typeDB.value,
                                                   [self.DBWebPageNamespace.classID],
                                                   self.__paramOr(params),
                                                   ids)
                for class_idW in class_ids:
                    class_idW = class_idW[0]
                    result = True

                    if class_idW == self.__id:
                        if not self.__Database.remove(typeDB.value,
                                                      self.__paramOr(params),
                                                      ids):
                            result = False
                    else:
                        result = False

                return result

            elif typeDB == self.DBType.vocabulary:
                class_ids = self.__Database.select(typeDB.value,
                                                   [self.DBVocabularyNamespace.classID],
                                                   self.__paramOr(params),
                                                   ids)

                for class_idV in class_ids:
                    class_idV = class_idV[0]
                    result = True

                    if class_idV == self.__id:
                        if not self.__Database.remove(typeDB.value,
                                                      self.__paramOr(params),
                                                      ids):
                            result = False
                    else:
                        result = False
                
                return result

    def delete(self, typeDB, ids, params):
        '''
        Checks if objects exists and then deletes them
        '''
        if self.__exist(typeDB.value, ids, params, False):
            return self.__delete(typeDB, ids, params)
        else:
            return False

    def updatePass(self, hash, id=None, typeDB = None):
        '''
        Updates password of any user if current logged user is admin, otherwise
        uses id of currently logged user
        '''
        if self.__ring > self.Ring.Guest:
            if id and typeDB:
                if self.__ring > self.Ring.Teacher:
                    return self.__Database.update(typeDB.value,
                                                  [self.__DBNamespaceByType(typeDB).hash],
                                                  self.__paramAdd([self.__DBNamespaceByType(typeDB).id]),
                                                  [hash, id])
            else:
                if self.__ring > self.Ring.Guest:
                    return self.__Database.update(self.DBType.student.value,
                                                  [self.DBStudentNamespace.hash],
                                                  self.__paramAdd([self.DBStudentNamespace.id]),
                                                  [hash, self.__id])
                elif self.__ring > self.Ring.Student:
                    return self.__Database.update(self.DBType.teacher.value,
                                                  [self.DBTeacherNamespace.hash],
                                                  self.__paramAdd([self.DBTeacherNamespace.id]),
                                                  [hash, self.__id])





















   



