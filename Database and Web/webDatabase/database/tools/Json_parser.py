import json
from database.tools.Bundle import Bundle
import uuid
from datetime import datetime
import platform

class JSON_parse():
    '''
    Static methods for working with JSON
    '''
    @staticmethod
    def parse_HEADER(data):
        parsed_json = json.loads(data)
        parsed_header = dict(parsed_json)
        del parsed_header['content']

        return (parsed_json['content'],parsed_header)

    @staticmethod
    def make_HEADER():
        header = {'id' : str(hex(uuid.getnode())),
                  'time' : datetime.now().strftime("%d.%m.%Y %H:%M:%S"),
                  'version' : '0.1a-server',
                  'system' : platform.platform()}
        return header

    @staticmethod
    def parse_IN(data):
        '''
        Parses JSON request to bundle
        '''
        parsed_json,parsed_header = JSON_parse.parse_HEADER(data)
        if(parsed_json['type']==str(Bundle.Message)):
            del parsed_json['type']
            spec = parsed_json['spec']
            del parsed_json['spec']
            return Bundle(Bundle.Message,parsed_json,parsed_header,spec)
        elif(parsed_json['type']==str(Bundle.Data)):
            del parsed_json['type']
            return Bundle(Bundle.Data,parsed_json,parsed_header)
        else:
            raise ValueError

    @staticmethod
    def dict_encode(data):
        '''
        Method for encoding dictionary to JSON
        '''
        for(key,value) in dict(data).items():
            if(type(value) is dict):
                data[key] = JSON_parse.dict_encode(value)
        return json.dumps(data)

    @staticmethod
    def parse_OUT(bundle):
        '''
        Parses bundle to JSON request
        '''

        data = JSON_parse.make_HEADER()

        print(bundle.getData())

        data['content'] = bundle.getData()
        data['content'].update({'type':bundle.getType()})
        data['content'] = JSON_parse.dict_encode(data['content'])
        return json.dumps(data)
