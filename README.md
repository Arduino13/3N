# 3N - Application for learning words
This repository contains small project which composes of android application, web server for database,
administrator interface and scripts for training of a neural network used for speech recognition on android devices. 

# Android application
User can add new words from photos, web pages or by directly writing them. These words can be learned with help of
using inbuild tests. Application is split to two parts student's and teacher's part. Teacher's part is used to assign homework
or words to class. Data are stored on server but connection to server is only neccesary when logging in to the application. 

News feed             |  Adding new words from page
:-------------------------:|:-------------------------:
<img src="https://user-images.githubusercontent.com/26491801/217811241-839f2830-c81d-4666-9476-d7d3702affb2.jpg" alt="drawing" width="300"/>|<img src="https://user-images.githubusercontent.com/26491801/217811268-69dfac77-a891-452a-82a7-17af12fc609a.jpg" alt="drawing" width="300"/>

Tests            |   Tests
:-------------------------:|:-------------------------:
<img src="https://user-images.githubusercontent.com/26491801/217811374-b94367b8-0c61-410c-bf2e-27ac2b32a689.jpg" alt="drawing" width="300"/>|<img src="https://user-images.githubusercontent.com/26491801/217811406-fcb4c55e-2d89-4941-b697-66f5f3770a6b.jpg" alt="drawing" width="300"/>

Vocabulary lists            |   Automatic translation
:-------------------------:|:-------------------------:
<img src="https://user-images.githubusercontent.com/26491801/217811429-3af8f6fe-a7d5-48b1-9bdb-a469cd7cf242.jpg" alt="drawing" width="300"/>|<img src="https://user-images.githubusercontent.com/26491801/217811443-080672ce-392e-4fa7-803a-a0cb13ed9e9c.jpg" alt="drawing" width="300"/>

# Database and administrator interface
Administrator interface is for adding new students, teachers and classes to the database. It's only basic flask application.

![Screenshot from 2023-02-09 13-17-08](https://user-images.githubusercontent.com/26491801/217811495-55edd658-be08-489f-8f60-fa579d134f39.png)

Database is build using MySQL with custom REST API interface for application.

# Speech recognition
One test tests pronounciation of the given word, as backend for this test is a small neural network based 
on DeepSpeech2, which can be runned on android device. Neural network was trained using Keras and PyTorch,
but for now only PyTorch version is used. In scripts folder there are both versions. 

For now it can only process english.

# Set up of the project
You will need to set up SQL database and change login creditals in "Database and Web/webDatabase/database/databaseObj.py" if you want to send renewed passwords by email to users you also will need to update "Database and Web/webAdmin/flaskr/emailSender.py", altough passwords can be for now only renew from administrator interface. To set up the database you can use DBschema in root folder.
