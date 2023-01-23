import cv2
import numpy as np
from keras.models import load_model
import os

import wave
import pyaudio
import threading
import pickle
import librosa
import librosa.display
import soundfile
import matplotlib.pyplot as plt
#from IPython.display import Audio
import warnings
from tkinter import *
import time
import statistics

window=Tk()

warnings.filterwarnings('ignore')

model = load_model('model_file.h5')

faceDetect = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

labels_dict = {0: 'Angry', 1: 'Disgust', 2: 'Fear', 3: 'Happy', 4: 'Sad', 5: 'Surprise'}


sound_prediction = "New"
video_start = 0



sound_model_filename = 'mlp_classifier.model'
loaded_model = pickle.load(open(sound_model_filename, 'rb')) # loading the model file from the storage


#Extract features (mfcc, chroma, mel) from a sound file
def extract_feature(file_name, mfcc, chroma, mel):
    with soundfile.SoundFile(file_name) as sound_file:
        X = sound_file.read(dtype="float32")
        sample_rate = sound_file.samplerate
        if chroma:
            stft = np.abs(librosa.stft(X))
        result = np.array([])
        if mfcc:
            mfccs = np.mean(librosa.feature.mfcc(y=X, sr=sample_rate, n_mfcc=40).T, axis=0)
            result = np.hstack((result, mfccs))
        if chroma:
            chroma = np.mean(librosa.feature.chroma_stft(S=stft, sr=sample_rate).T,axis=0)
            result = np.hstack((result, chroma))
        if mel:
            #mel = np.mean(librosa.feature.melspectrogram(X, sr=sample_rate).T,axis=0)
            mel = np.mean(librosa.feature.melspectrogram(y=X, sr=sample_rate).T, axis=0)
            result = np.hstack((result, mel))
    return result




# Emotions in the RAVDESS dataset
emotions = {
  '01':'neutral',
  '02':'calm',
  '03':'happy',
  '04':'sad',
  '05':'angry',
  '06':'fearful',
  '07':'disgust',
  '08':'surprised'
}


def get_video():
    capture = cv2.VideoCapture(0)
    fourcc = cv2.VideoWriter_fourcc('X', 'V', 'I', 'D')
    videoWriter = cv2.VideoWriter('outputvideo.avi', fourcc, 30.0, (640, 480))
    start_time = time.time()
    while (True):
      end_time = time.time()
      total_time = end_time - start_time
      #print(total_time)
      ret, frame = capture.read()
      # video recoed seconds
      if total_time > 5 :
          break
      if ret:
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = faceDetect.detectMultiScale(gray, 1.3, 3)
        cv2.namedWindow("video", cv2.WINDOW_NORMAL)
        videoWriter.write(frame)
        for x, y, w, h in faces:
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 1)
            cv2.rectangle(frame, (x, y), (x + w, y + h), (50, 50, 255), 2)
        cv2.imshow('video', frame)


      if cv2.waitKey(1) == 27:
        break

    capture.release()
    videoWriter.release()
    cv2.destroyAllWindows()





def get_sound():
    # the file name output you want to record into
    filename = "recorded.wav"
    # set the chunk size of 1024 samples
    chunk = 1024
    # sample format
    FORMAT = pyaudio.paInt16
    # mono, change to 2 if you want stereo
    channels = 1
    # 44100 samples per second
    sample_rate = 44100
    record_seconds = 5  # sound record seconds
    # initialize PyAudio object
    p = pyaudio.PyAudio()
    # open stream object as input & output
    stream = p.open(format=FORMAT,
                    channels=channels,
                    rate=sample_rate,
                    input=True,
                    output=True,
                    frames_per_buffer=chunk)
    frames = []
    #print("Recording...")
    for i in range(int(sample_rate / chunk * record_seconds)):
        data = stream.read(chunk)
        # if you want to hear your voice while recording
        #stream.write(data)
        frames.append(data)
    #print("Finished recording.")
    # stop and close stream
    stream.stop_stream()
    stream.close()
    # terminate pyaudio object
    p.terminate()
    # save audio file
    # open the file in 'write bytes' mode
    wf = wave.open(filename, "wb")
    # set the channels
    wf.setnchannels(channels)
    # set the sample format
    wf.setsampwidth(p.get_sample_size(FORMAT))
    # set the sample rate
    wf.setframerate(sample_rate)
    # write the frames as bytes
    wf.writeframes(b"".join(frames))
    # close the file
    wf.close()


videopredection = []
def detectemotion():
    filename = "recorded.wav"
    feature = extract_feature(filename, mfcc=True, chroma=True, mel=True)
    feature = feature.reshape(1, -1)
    prediction = loaded_model.predict(feature)
    global sound_prediction
    sound_prediction = str(prediction).replace('[', '').replace(']', '').replace('\'', '').replace(
        '\"', '')
    #sound_prediction = "Sound : " + str(sound_prediction)


    video = cv2.VideoCapture('outputvideo.avi')
    while video.isOpened():
        ret, frame = video.read()
        if frame is not None:
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            faces = faceDetect.detectMultiScale(gray, 1.3, 3)
            i = 1
            for x, y, w, h in faces:
                sub_face_img = gray[y:y + h, x:x + w]
                resized = cv2.resize(sub_face_img, (48, 48))
                normalize = resized / 255.0
                reshaped = np.reshape(normalize, (1, 48, 48, 1))
                result = model.predict(reshaped)
                label = np.argmax(result, axis=1)[0]
                #print(label)
                #print(result)
                confidence = 'Confidence: {}'.format(str(np.round(np.max(result[0]) * 100, 1)) + "%")
                #print(confidence)
                # cv2.putText(frame, str(confidence), (x + 5, y + h - 5), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 1)
                cv2.rectangle(frame, (x, y), (x + w, y + h), (50, 50, 255), 2)
                cv2.rectangle(frame, (x, y - 40), (x + w, y), (50, 50, 255), -1)
                cv2.putText(frame, labels_dict[label], (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
                cv2.putText(frame, str(sound_prediction), (x, y + 20), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
                videopredection.append(label)
            #cv2.imshow("Frame", frame)
        else:
           break

    video.release()
    cv2.destroyAllWindows()
    videoresult = max(set(videopredection), key=videopredection.count, default=0)
    video_prediction = labels_dict[videoresult]
    print("V "+video_prediction)
    print("S "+sound_prediction)
    allprediction = [video_prediction,sound_prediction,sound_prediction]
    #allprediction = [video_prediction, sound_prediction,sound_prediction,video_prediction]
    finalresult = max(set(allprediction), key=allprediction.count, default=0)
    res = statistics.mode(allprediction)
    print("&&&&&&&&&&&&&&&&&&&&&&& "+res)
    return res





Thread1 = threading.Thread(target=get_sound)
Thread2 = threading.Thread(target=get_video)


def startrecording(event):
    widget['state'] = DISABLED
    print("Recording started...")
    Thread1.start()
    Thread2.start()
    Thread1.join()
    Thread2.join()
    print("Recording completed...")
    dresult = detectemotion()
    lbl = Label(window, text="Result : "+str(dresult), fg='red', font=("Helvetica", 16))
    lbl.place(x=60, y=150)



lbl=Label(window, text="Record video and audio for 5 second", fg='red', font=("Helvetica", 16))
lbl.place(x=10, y=50)
widget = Button(None, text='Record and Detect emotion')
widget.pack()
widget.place(x=110, y=90)
widget.bind('<Button-1>', startrecording)
window.title('Emotion detector')
window.geometry("400x300+10+10")
window.mainloop()








