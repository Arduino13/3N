import re

def isValid(msg):
    '''
    TODO: debug pattern, for now filter always returns true
    '''
    return True

    pattern = '[@_#$%^&*()<>/|}{~:]' 
    
    if re.search(pattern, msg):
        return False
    else:
        return True
