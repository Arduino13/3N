def passHash(passWord):
    from Crypto.Hash import SHA512
    h = SHA512.new()
    h.update(passWord.encode('utf-8'))
    return h.hexdigest()

def generatePass():
    import random
    import string
    alphabet = string.ascii_letters + string.digits
    password = ''.join(random.choice(alphabet) for i in range(7))

    #print(password)

    return password
