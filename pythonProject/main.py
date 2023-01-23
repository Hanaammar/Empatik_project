from keras.preprocessing.image import ImageDataGenerator
from keras.models import Sequential
from keras.layers import Dense, Dropout, Flatten
from keras.layers import Conv2D, MaxPooling2D
import os
import matplotlib.pyplot as plt
import numpy as np

train_data_dir = 'data/train/'
validation_data_dir = 'data/test/'

train_datagen = ImageDataGenerator(
    rescale=1. / 255,
    rotation_range=30,
    shear_range=0.3,
    zoom_range=0.3,
    horizontal_flip=True,
    fill_mode='nearest')

validation_datagen = ImageDataGenerator(rescale=1. / 255)

train_generator = train_datagen.flow_from_directory(
    train_data_dir,
    color_mode='grayscale',
    target_size=(48, 48),
    batch_size=32,
    class_mode='categorical',
    shuffle=True)

validation_generator = validation_datagen.flow_from_directory(
    validation_data_dir,
    color_mode='grayscale',
    target_size=(48, 48),
    batch_size=32,
    class_mode='categorical',
    shuffle=True)


class_labels = ['angry', 'disgust', 'fear', 'happy', 'sad', 'surprise']

img, label = train_generator.__next__()

model = Sequential()
# This are the layers
model.add(Conv2D(32, kernel_size=(3, 3), activation='relu', input_shape=(48, 48, 1)))

model.add(Conv2D(64, kernel_size=(3, 3), activation='relu'))
model.add(MaxPooling2D(pool_size=(2, 2)))
model.add(Dropout(0.1))

model.add(Conv2D(128, kernel_size=(3, 3), activation='relu'))
model.add(MaxPooling2D(pool_size=(2, 2)))
model.add(Dropout(0.1))

model.add(Conv2D(256, kernel_size=(3, 3), activation='relu'))
model.add(MaxPooling2D(pool_size=(2, 2)))
model.add(Dropout(0.1))

model.add(Flatten())
model.add(Dense(512, activation='relu'))
model.add(Dropout(0.2))
# activation='softmax' means we have multiple classes like : angry , happy, sad,fear,etc
# Dense(6 means 6 classes)
model.add(Dense(6, activation='softmax'))
# Here we are compiling our model
# categorical_crossentropy means it will gives the loss data in category
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
print(model.summary())
# we are giving our image datasets here
train_path = "data/train/"
test_path = "data/test/"
# here we using for loopch all image dataset
num_train_imgs = 0
for root, dirs, files in os.walk(train_path):
    num_train_imgs += len(files)

num_test_imgs = 0
for root, dirs, files in os.walk(test_path):
    num_test_imgs += len(files)

print(num_train_imgs)
print(num_test_imgs)
# epochs means how many batch you wanted to run the program to trained our model
# above 30 to 50  epochs is always good
epochs = 40
# here we finally producing our model
history = model.fit(train_generator,
                    steps_per_epoch=num_train_imgs // 32,
                    epochs=epochs,
                    validation_data=validation_generator,
                    validation_steps=num_test_imgs // 32)
# here we save our model in files
model.save('model_file.h5')

# here we are displaying the result and graph of perormance of the model
# plot the training loss and accuracy
plot_path=os.getcwd()+"//plot"
N = epochs
H = history
plt.style.use("ggplot")
plt.figure()
plt.plot(np.arange(0, N), H.history["loss"], label="train_loss")
plt.plot(np.arange(0, N), H.history["val_loss"], label="val_loss")
plt.plot(np.arange(0, N), H.history["accuracy"], label="train_acc")
plt.plot(np.arange(0, N), H.history["val_accuracy"], label="val_acc")
plt.title("Training Loss and Accuracy")
plt.xlabel("Epoch #")
plt.ylabel("Loss/Accuracy")
plt.legend(loc="lower left")
plt.savefig(plot_path)
print("Training Completed")