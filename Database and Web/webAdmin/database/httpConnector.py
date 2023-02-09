import requests
import sys
import json
from datetime import datetime
from database.tools import Filters

class HttpConnector:
    '''
    Class for creating local connection to database server, similar
    to HttpConnector in android application
    '''
    def __init__(self, id, hash):
        self.id = id
        self.hash = hash

        self.address = 'https://127.0.0.1:443/run'

    def initBuffer(self):
        now = datetime.now()
        currentTime = now.strftime('%Y-%m-%d %H:%M')

        self.toSend = {
                'id' : 'server',
                'time' : currentTime,
                'version' : '0.1a-serverRelease',
                'os_v' : sys.platform,
                'type' : 'PC',
                'manu' : 'server',
                'id_user' : self.id,
                'hash' : self.hash
        }

    def openData(self):
        self.initBuffer()
        self.content = {
                'type' : 'data'
        }

    def openMessage(self, msg):
        self.initBuffer()
        self.content = {
                'type' : 'msg',
                'spec' : ''
        }

        #for now always returns true
        if Filters.isValid(msg):
            self.content['text'] = msg
        else:
            raise Exception('you can not use special characters')

        
    def add(self, data):
        for (key, value) in data.items():
            self.content[key] = value

    def sendResponse(self):
        self.toSend['content'] = self.content
       
        re = requests.post(self.address, json=self.toSend, verify=False)
        re.encoding = 'iso-8859-1'
        data = re.text

        data = data.replace('\\', '')
        data = data.replace('"{', '{')
        data = data.replace('}"', '}')

        return json.loads(data)
