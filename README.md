# Guide-Me-Navigation-App
Graduation Project for MIU 2020 graduates tackling issues visually impaired people face.

## Project Description
This is an indoor navigation system for the visually impaired that identifies objects in the room and calculates the distance to them using triangle similarity and provide audio directions for
the user, the user is also able to add his own objects to the object recognition dataset, the user is authenticated using facial recognition,
the app notifies the emergency contact if it senses that the user is in danger using accelerometer readings.

## Setup 
The server directory is placed in the server enviroment for customized detection model generation using the images in the directory. <br>
Import the rest to android study and sync the gradle. <br>
Run the app on your android phone. <br>

### Built Uing
Luxand Facial Recognition. <br>
Google Assistant. <br>
TensorFlow Lite. <br>

## Limitations
Distance is measured starting from 30 cm away to 200 cm. <br>
Room lighting is important for object detection range. <br>
Phone must be connected to the internet. <br>

## Acknolowdgements
Dr. Salama Mohamed for providing prespective on the difficulties faced in his everyday life as someone with vision impairment.
