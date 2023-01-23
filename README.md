# Empatik_project
An Android application that detects a person's mood based on his voice waves and facial features.
And according to this, it presents to the user a list of songs and a sentence that suit his mood.
The user has many things that he can do in the application like:   
mark songs, unmark song, pause song ,view the history of the moods he had and many other things.     
The application works with a remote server that written in Python, and the application itself is in Android.

Running instructions:
1. Download the Python files and run the main.py, soundSER.py (these for training the model) on PyCharm.
File -> New project -> Copy all the files in pythonProject folder to the new project folder and then run the files we mentioned.
2. In line No. 131 in the server.py file, the IP number must be changed to the IP number of the computer on which the code is running.
3. The app-debug.apk file must be downloaded to a phone with an Android operating system.
4. After running the server file and after downloading the application, as soon as you enter the application, click on the settings icon, and change the IP there to the IP written on the server and click back.
5. From there you can use the attached demo video to see the features in the application.
