import cv2
import soundfile
import pickle
import librosa
import numpy as np
import statistics
from keras.models import load_model
import moviepy.editor
import os
from pydub import AudioSegment
from flask import Flask
import urllib.request


app = Flask(__name__)

model = load_model('model_file.h5')
faceDetect = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
sound_model_filename = 'mlp_classifier.model'
loaded_model = pickle.load(open(sound_model_filename, 'rb'))  # loading the model file from the storage
labels_dict = {0: 'Angry', 1: 'Disgust', 2: 'Fear', 3: 'Happy', 4: 'Sad', 5: 'Surprise'}

# Extract features (mfcc, chroma, mel) from a sound file
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
            chroma = np.mean(librosa.feature.chroma_stft(S=stft, sr=sample_rate).T, axis=0)
            result = np.hstack((result, chroma))
        if mel:
            # mel = np.mean(librosa.feature.melspectrogram(X, sr=sample_rate).T,axis=0)
            mel = np.mean(librosa.feature.melspectrogram(y=X, sr=sample_rate).T, axis=0)
            result = np.hstack((result, mel))
    return result

videopredection = []

def detectemotion(video_path):
    res = convert_video_to_audio_moviepy(video_path)
    filename, ext = os.path.splitext(video_path)
    feature = extract_feature(filename + '.wav', mfcc=True, chroma=True, mel=True)
    feature = feature.reshape(1, -1)
    prediction = loaded_model.predict(feature)
    global sound_prediction
    sound_prediction = str(prediction).replace('[', '').replace(']', '').replace('\'', '').replace('\"', '')
    print(sound_prediction)
    #confidence2 = 'Confidence sound: {}'.format(str(np.round(np.max(sound_prediction[0]) * 100, 1)) + "%")
    #print(confidence2)
    # sound_prediction = "Sound : " + str(sound_prediction)
    
    video = cv2.VideoCapture(filename + '.mp4')
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
                # print(label)
                print(result)
                confidence = 'Confidence: {}'.format(str(np.round(np.max(result[0]) * 100, 1)) + "%")
                confidence1 = 'Confidence video: {}'.format(str(np.round(np.max(result[0]) * 100, 1)) + "%")
                confidence2 = np.round(np.max(result[0]) * 100, 1)
                # print(confidence)
                print(confidence1)
                # cv2.putText(frame, str(confidence), (x + 5, y + h - 5), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 1)
                cv2.rectangle(frame, (x, y), (x + w, y + h), (50, 50, 255), 2)
                cv2.rectangle(frame, (x, y - 40), (x + w, y), (50, 50, 255), -1)
                cv2.putText(frame, labels_dict[label], (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
                cv2.putText(frame, str(sound_prediction), (x, y + 20), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255),
                            2)
                videopredection.append(label)
            # cv2.imshow("Frame", frame)
        else:
            break

    video.release()
    cv2.destroyAllWindows()
    videoresult = max(set(videopredection), key=videopredection.count, default=0)
    video_prediction = labels_dict[videoresult]
    allprediction = [video_prediction, sound_prediction, video_prediction]
    finalresult = max(set(allprediction), key=allprediction.count, default=0)
    res = statistics.mode(allprediction)
    if(confidence2 > 60.697):
        res = video_prediction
    else:
        res = sound_prediction
    return res

def convert_video_to_audio_moviepy(video_file, output_ext="wav"):    
    filename, ext = os.path.splitext(video_file)
    video = moviepy.editor.VideoFileClip(video_file)
    audio = video.audio
    if audio != None:
        audio.write_audiofile(filename + ".wav")
        sound = AudioSegment.from_wav(filename + ".wav")
        sound = sound.set_channels(1)
        sound.export(filename + ".wav", format="wav")
    else:
        print('No audio detected for the given video...')


@app.route('/detectEmotion/<string:video_file_name>')
def detectEmotion(video_file_name: str):
    video_file = 'https://firebasestorage.googleapis.com/v0/b/empatik-1.appspot.com/o/videos%2F' + video_file_name + '?alt=media&token=e9053a4d-66fe-45fd-a472-30396b621dc7'
    urllib.request.urlretrieve(video_file, 'asset/' + video_file_name) 
    dresult = detectemotion('asset/' + video_file_name)
    return (str(dresult))
    
@app.route('/')
def index():
    return "Hello, World!"

if __name__ == "__main__":
    #app.run(host="172.20.10.11")
    app.run(host = "10.0.0.15")