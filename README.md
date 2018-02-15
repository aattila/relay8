# DFRobot's RLY-8 Web Application

![RLY-8](https://github.com/aattila/relay8/blob/master/src/main/resources/docs/module.jpg?raw=true)

DFRobot has a very cool network controlled [RLY-8](https://www.dfrobot.com/product-1218.html) relay module that is 
easily manageable by a TCP connection or serial over USB. Using TCP is more powerful because there ie a possibility to 
use simple JSON formatted commands.

In case if you just want to enable some relays it is enough to use telnet and send that JSON commands but in the era of 
smart homes we want more.

So I made this java SpringBoot web app that brings a wery simple webpage where you can switch that relays remotely. 
It also has login page so you can publish this app into the internet and you can control your home stuff from everywhere.

There is a possibility to edit the labels of the relays within this [property](https://github.com/aattila/relay8/blob/master/src/main/resources/label.properties) file 

It is also you can configure some important values within this [application.yml](https://github.com/aattila/relay8/blob/master/src/main/resources/application.yml) file.
It is wery important to change the login password and of course your relay ip address.

To bring up the application you need to proceed the following steps.

## Build

First of all you need to have Java 8 and Gradle installed.

Tha application is using Gradle, so to build you just run ```gradle clean build```

## Deploy

After the successful build the application jar will be ```build/libs/relay8-0.1.0-SNAPSHOT.jar``` 

## Run

To run the application firs copy the jar fle in their final destination go into that folder and run
 ```java -jar relay8-0.1.0-SNAPSHOT.jar```

In your browser go tho the address ```http://localhost:7777``` and you will find this login screen:

![Login](https://github.com/aattila/relay8/blob/master/src/main/resources/docs/login.png?raw=true)

Use your login credentials that you are set in the file ```application.yml``` and you are done. 
This page will be loaded:

![Relays](https://github.com/aattila/relay8/blob/master/src/main/resources/docs/someset.png?raw=true)

When this page is opening the state of the relays will be fetched from the device and the switches will set properly.
There is also the device name is fetched and used as title. Of course the screenshot has modified labels (label.properties).

At that moment when you switch a relay that is immediately will set also at the device. In case of any failure an error
message will shown and all the switches wil be hidden.

## More than a deploy

You can deploy the application also to a RaspberryPI (but be sure you have Java 8 on it) and use different public and free 
services to have access into your application without port forwarding.  

