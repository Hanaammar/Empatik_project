import cv2
import numpy as np
from keras.models import load_model
import os

from sklearn.svm import SVC
import sounddevice as sd
import wave
import pyaudio
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
import speech_recognition as sr
import threading
import pickle
import librosa
import librosa.display
import soundfile
import matplotlib.pyplot as plt
#from IPython.display import Audio
import warnings
import tkinter as tk
import time

warnings.filterwarnings('ignore')

recognizer=sr.Recognizer()

model = load_model('model_file.h5')

video = cv2.VideoCapture(0)

faceDetect = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

labels_dict = {0: 'Angry', 1: 'Disgust', 2: 'Fear', 3: 'Happy', 4: 'Sad', 5: 'Surprise'}

id = 0

sr.Microphone()

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


def waveplot(data, sr, emotion):
    plt.figure().clear()
    plt.figure(figsize=(10, 4))
    plt.title(emotion, size=20)
    librosa.display.waveshow(data, sr=sr)
    plt.show()


def spectogram(data, sr, emotion):
    x = librosa.stft(data)
    xdb = librosa.amplitude_to_db(abs(x))
    plt.figure(figsize=(11, 4))
    plt.title(emotion, size=20)
    librosa.display.specshow(xdb, sr=sr, x_axis='time', y_axis='hz')
    plt.colorbar()


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
    record_seconds = 5
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
    print("Recording...")
    for i in range(int(sample_rate / chunk * record_seconds)):
        data = stream.read(chunk)
        # if you want to hear your voice while recording
        #stream.write(data)
        frames.append(data)
    print("Finished recording.")
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
    #filename = "dataset/dataset/Happy/08a02Fe.wav"
    feature = extract_feature(filename, mfcc=True, chroma=True, mel=True)
    feature = feature.reshape(1, -1)
    prediction = loaded_model.predict(feature)
    global sound_prediction
    sound_prediction = prediction
    sound_prediction = "Sound : "+str(sound_prediction).replace('[','').replace(']','').replace('\'','').replace('\"','')
    print(sound_prediction)
    threading.Timer(1.0, get_sound).start()
    # time.sleep(1)
    '''
    emotion = prediction
    path = filename
    data, sampling_rate = librosa.load(path)
    cv2.namedWindow("Secondimage", cv2.WINDOW_NORMAL)
    cv2.namedWindow('RGB' + str(prediction))
    simg = cv2.imread('plots.PNG', 21)
    #cv2.imwrite('grayscale.jpg', simg)
    cv2.imshow('Secondimage', simg)
    cv2.waitKey(21)
    signal, sample_rate = librosa.load(filename)
    plt.plot(signal)
    plt.xlabel('sample')
    plt.ylabel('amplitude')
    greeting = tk.Label(text="Hello, Tkinter")
    greeting.pack()
'''

    #waveplot(data, sampling_rate, emotion)
    #spectogram(data, sampling_rate, emotion)
    #Audio(path)


Thread1 = threading.Thread(target=get_sound)
Thread1.start()

i: int = 0

while True:
    ret, frame = video.read()
    if frame is not None:
     gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
     faces = faceDetect.detectMultiScale(gray, 1.3, 3)
     cv2.namedWindow("Frame", cv2.WINDOW_NORMAL)
     i = 1
     for x, y, w, h in faces:
        sub_face_img = gray[y:y + h, x:x + w]
        resized = cv2.resize(sub_face_img, (48, 48))
        normalize = resized / 255.0
        reshaped = np.reshape(normalize, (1, 48, 48, 1))
        result = model.predict(reshaped)
        label = np.argmax(result, axis=1)[0]
        print(label)
        print(result)
        confidence = 'Confidence: {}'.format(str(np.round(np.max(result[0]) * 100, 1)) + "%")
        print(confidence)
        # cv2.putText(frame, str(confidence), (x + 5, y + h - 5), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
        cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 1)
        cv2.rectangle(frame, (x, y), (x + w, y + h), (50, 50, 255), 2)
        cv2.rectangle(frame, (x, y - 40), (x + w, y), (50, 50, 255), -1)
        cv2.putText(frame, labels_dict[label], (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
        cv2.putText(frame, str(sound_prediction), (x, y + 20), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
        if i == 1:
            Thread1.join()
        i = 0
     cv2.imshow("Frame", frame)
     k = cv2.waitKey(1)
     if k == ord('q'):
       break

video.release()
cv2.destroyAllWindows()

