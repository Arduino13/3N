class query:
    '''
    Contains methods for putting together SQL query
    '''
    __query=''

    def remove(self,table):
        self.__query+='DELETE FROM '
        self.__query+=table
        return self
    def where(self,req):
        self.__query+=' WHERE '
        self.__query+=req
        return self
    def update(self,table):
        self.__query+='UPDATE '
        self.__query+=table
        return self
    def set(self,columns):
        self.__query+=' SET '

        if(len(columns)==0):
            raise 'Zero len columns -> query'

        for colum in columns:
            self.__query+=colum
            self.__query+=' = '
            self.__query+='%s'
            self.__query+=','

        index = self.__query.rindex(',')
        self.__query=self.__query[0 : index] + self.__query [index+1 : :]

        return self
    def insert(self,table):
        self.__query+='INSERT INTO '
        self.__query+=table
        return self
    def From(self,table):
        self.__query+=' FROM '
        self.__query+=table
        return self
    def select(self,columns=None):
        self.__query+='SELECT '

        if(columns!=None and len(columns)==0):
            raise 'Zero len columns -> query'
        if(columns):
            for colum in columns:
                self.__query+=colum
                self.__query+=','

            index=self.__query.rindex(',')
            self.__query=self.__query[0 : index] + self.__query[index + 1 : :]
        else:
            self.__query+='*'
    
        return self
    def values(self,params):
        self.__query+=' VALUES('
        
        if(len(params)==0):
            raise 'Zero len params -> query'

        for param in params:
            self.__query+='%s,'
        
        index=self.__query.rindex(',')
        self.__query=self.__query[0 : index] + self.__query[index + 1 : :]
        self.__query+=');'

        return self
    def getQuery(self):
        print(self.__query)
        return self.__query

