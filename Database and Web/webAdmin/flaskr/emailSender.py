import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

def sendPassword(name, password, email):
    print(name, password, email)
    s = smtplib.SMTP(host='smtp.gmail.com', port=587)
    s.starttls()
    s.login('YOUR EMAIL', 'YOUR PASSWORD')

    msg = MIMEMultipart()
    message = 'Vaše heslo je: ' + password

    msg['From'] = 'YOUR EMAIL'
    msg['To'] = email
    msg['Subject']='Password change'

    msg.attach(MIMEText(message, 'plain'))

    s.send_message(msg)

    del msg
