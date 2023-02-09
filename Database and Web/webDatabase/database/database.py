import mysql.connector
from mysql.connector import Error
from database.query import query
from database.tools.Log import Log

class Database:
    __connection = None
    __cursor = None
    __query = ''

    def __init__(self,db,userName,password):
        try:
            self.__connection = mysql.connector.connect(host='localhost',
                                                    database=db,
                                                    user=userName,
                                                    password=password)
            if self.__connection and self.__connection.is_connected():
                self.__cursor = self.__connection.cursor(prepared=True)

        except Error as e:
            Log.info(str(e))
            Log.error('exception in database.__init__')
            if(self.__connection and self.__connection.is_connected()):
                self.__cursor.close()
                self.__connection.close()

    def __del__(self):
        if(self.__connection.is_connected()):
            self.__cursor.close()
            self.__connection.close()

    def query_f(self,params,query):
        ''' 
        Executes query
        '''
        try:
            values = []

            if(params!=None):
                for param in params:
                    values.append(param)

            self.__cursor.execute(query,tuple(values))
            self.__connection.commit()
            return True
        except Error as error:
            Log.info(str(error))
            Log.error('Exception in database.__query_f')
            if(self.__connection.is_connected()):
                self.__cursor.close()
                self.__connection.close()
            return False

    def remove(self,table,req,params):
        Query = query()
        if(req!=None):
            Query.remove(table).where(req)
        else:
            Query.remove(table)

        return self.query_f(params,Query.getQuery())

    def insert(self,table,params):
        Query = query()
        Query.insert(table).values(params)

        return self.query_f(params,Query.getQuery())

    def update(self,table,columns,req,params):
        Query = query()
        Query.update(table).set(columns).where(req)

        return self.query_f(params,Query.getQuery())

    def __select(self,params,Query):
        '''
        Executes select query and fetches results
        '''
        try:
            values = []

            if(params!=None):
                for param in params:
                    values.append(param)
            self.__cursor.execute(Query,tuple(values))
            results = self.__cursor.fetchall()
            data = []

            for result in results:
                data.append([])
                for param in result:
                    data[-1].append(param.decode('utf-8'))

            return data
        except Error as error:
            Log.info(str(error))
            Log.error('Exception in database.__select')
            if(self.__connection.is_connected()):
                self.__cursor.close()
                self.__connection.close()
            return {}

    def select(self,table,columns,req=None,params=None):
        Query = query()
        if(req!=None):
            Query.select(columns).From(table).where(req)
        else:
            Query.select(columns).From(table)

        return self.__select(params,Query.getQuery())
