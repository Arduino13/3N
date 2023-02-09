import time
from datetime import datetime

class Log():
    @staticmethod
    def open():
        return open('Log_system.log','a')

    @staticmethod
    def time():
        return datetime.now().strftime('%d/%m/%Y %H:%M:%S')

    @staticmethod
    def info(text):
        temp = Log.time()+' info: '+text+'\n\r'
        file = Log.open()
        file.write(temp)
        print(temp)
        file.close()

    @staticmethod
    def warning(text):
        temp = Log.time()+' warning: '+text+'\n\r'
        file = Log.open()
        file.write(temp)
        print(temp)
        file.close()

    @staticmethod
    def error(text):
        temp = Log.time()+' error: '+text+'\n\r'
        file = Log.open()
        file.write(temp)
        print(temp)
        file.close()


