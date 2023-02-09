import enum

class Bundle:
    '''
    Class for encapsulating data and messages

    TODO: rename variables

    type: defines if bundle handles data or message
    spec: used in case of message to specify its kind
    headers: keys in 'content' section
    headers_header: keys in Header of the request
    Header: JSON request without 'content' key
    '''
    Message = "msg"
    Data = "data"
    Nondef = " "

    class spec(enum.Enum):
        info = "inf"
        error = "err"
        success = "suc"
        update = "upd"
        defa = ""

    type = Nondef
    spec = spec.defa
    headers = []
    headers_header = []
    data = {}
    Header = {}

    def __init__(self,type_l,data,spec):
        self.__init__(type_l,data,"",spec)

    def __init__(self,type_l,data,header="",spec=""):
        self.type=type_l
        self.data = data;
        self.Header = header;
        self.headers = [header for header in data.keys()]

        if(spec==Bundle.spec.info.value):
            self.spec = Bundle.spec.info
        elif(spec==Bundle.spec.error.value):
            self.spec = Bundle.spec.error
        elif(spec==Bundle.spec.success.value):
            self.spec = Bundle.spec.success
        elif(spec==Bundle.spec.update.value):
            self.spec = Bundle.spec.update
        else:
            self.spec = Bundle.spec.defa

    def set_header(self,header):
        self.Header = header
        self.headers_header = [header for header in header.keys()]

    def getHeaders(self):
        return self.headers;
    def getData(self):
        return self.data
    def getType(self):
        return self.type
    def getHeader(self):
        return self.Header
    def getHeadersHeader(self):
        return self.header_headers
