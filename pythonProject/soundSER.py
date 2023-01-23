#importing libraries
import soundfile as sf    #to read audio file
import librosa            #to feature extraction
import os                 #to obtain the file
import glob               #to obtain filename having the same pattern
import pickle             #to save the model
import numpy as np
from sklearn.model_selection import train_test_split#to split train and test data
from sklearn.neural_network import MLPClassifier #multi layer perceptron classifier model
from sklearn.metrics import accuracy_score #to measure the accuracy

#extracting features mfcc,chroma,mel from sound file
def feature_extraction(fileName,mfcc,chroma,mel):
    with sf.SoundFile(fileName) as file:
        sound = file.read(dtype='float32')#reading the sound file
        sample_rate = file.samplerate     #finding sample rate of sound
        if chroma:          #if chroma is true then finding stft
            stft = np.abs(librosa.stft(sound))
        feature = np.array([])                #initializing feature array
        if mfcc:
            mfcc = np.mean(librosa.feature.mfcc(y=sound,sr=sample_rate,n_mfcc=40).T,axis=0)
            feature =np.hstack((feature,mfcc))
        if chroma:
            chroma =  np.mean(librosa.feature.chroma_stft(S=stft,sr=sample_rate).T,axis=0)
            feature = np.hstack((feature,chroma))
        if mel:
            mel = np.mean(librosa.feature.melspectrogram(y=sound,sr=sample_rate).T,axis=0)
            feature =np.hstack((feature,mel))
        return feature  #return feature extracted from audio


#All available emotion in dataset
int_emotion = {
    "01": "neutral",
    "02": "calm",
    "03": "happy",
    "04": "sad",
    "05": "angry",
    "06": "fearful",
    "07": "disgust",
    "08": "surprised"
}
#Emotions we want to observe
EMOTIONS = {"happy","sad","neutral","angry"}

#making and spliting train and test data
def train_test_data(test_size=0.3):
    features, emotions = [],[] #intializing features and emotions
    for file in glob.glob("sounddata/Actor_*/*.wav"):
        fileName = os.path.basename(file)   #obtaining the file name
        emotion = int_emotion[fileName.split("-")[2]] #getting the emotion
        if emotion not in EMOTIONS:
            continue
        feature=feature_extraction(file,mfcc=True,chroma=True,mel=True,) #extracting feature from audio
        features.append(feature)
        emotions.append(emotion)
    return train_test_split(np.array(features),emotions, test_size=test_size, random_state=7) #returning the data splited into train and test set
#we are obtaining train and test data from train_test_data(). Here, the test size is 30%.

#dataset
X_train,X_test,y_train,y_test=train_test_data(test_size=0.3)
print("Total number of training sample: ",X_train.shape[0])
print("Total number of testing example: ",X_test.shape[0])
print("Feature extracted",X_train.shape[1])

#initializing the multi layer perceptron model
model=MLPClassifier(alpha=0.01, batch_size=256, epsilon=1e-08, hidden_layer_sizes=(400,), learning_rate='adaptive', max_iter=1000)

#fitting the training data into model
print("__________Training the model__________")
model.fit(X_train,y_train)

#Predicting the output value for testing data
y_pred = model.predict(X_test)

#calculating accuracy
accuracy = accuracy_score(y_true=y_test,y_pred=y_pred)
accuracy*=100
print("accuracy: {:.4f}%".format(accuracy))

#saving the model
if not os.path.isdir("model"):
   os.mkdir("model")
pickle.dump(model, open("model/mlp_classifier.model", "wb"))

