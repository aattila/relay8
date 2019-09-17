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

<img src="https://github.com/aattila/relay8/blob/master/src/main/resources/docs/login.png?raw=true" alt="Login" width="400">

Use your login credentials that you are set in the file ```application.yml``` and you are done. 
This page will be loaded:

<img src="https://github.com/aattila/relay8/blob/master/src/main/resources/docs/someset.png?raw=true" alt="Login" width="400">

When this page is opening the state of the relays will be fetched from the device and the switches will set properly.
There is also the device name is fetched and used as title. Of course the screenshot has modified labels (label.properties).

At that moment when you switch a relay that is immediately will set also at the device. In case of any failure an error
message will shown and all the switches wil be hidden.

### Ad-hoc Setup

If you are lazy enough to modify the settings in ```application.yml``` file you can start the application in that way to override some default settings. An example if you want to start the application with a specific device IP address and a custom password, just run:

```java -Drly8.host=192.168.1.10 -Dspring.security.user.password=your_password -jar relay8-0.1.0-SNAPSHOT.jar```

## More than a deploy

You can deploy the application also to a RaspberryPI (but be sure you have Java 8 on it) and use different public and free 
services to have access into your application without port forwarding.  

## Features

### Dynamic Properties

The most of the features encountered at the chapters below are using properties from a dedicated YAML file. 
This file can be changed at runtime and any modification will be updated properly.
This dynamic file name is _triggers.yml_ and that needs to be placed in the working directory. An example file is looking like this:
```
sensors:
  irrigation:
    type: yaml
    source: /var/app/environment/environment.yml
    key: outside.irrigation

triggers:
  backyard:
    relay: 1
    cron: "0 */1 * * * ?"
    sensor: irrigation
  goose_lake:
    relay: 1
    cron: "0 0 6,18 * * ?"

switchback:
  relay1: 40
  relay2: 0
  relay3: 0
  relay4: 0
  relay5: 0
  relay6: 0
  relay7: 0
  relay8: 0
```

The following chapters will detail each of those sections.

### Triggers (automated switch on)

You can fully automate your relays switch on by specifying a cron per relay. First you need to switch on this funtionality by setting the ```rly8.relay?.auto``` property (true) in the ```application.yml```. As a second step you can specify a cron string for the ```rly8.relay?.cron``` property.

### Switchback (automated switch off)

In case if you want to automatically switching off a relay after a predefined time please set the ```rly8.relay?.switchback``` property in the ```application.yml```. The value is in minutes and 0 means no switchback.

### Senzor checks


### Headless (dummy device)

The application can be used also in test mode, without the RLY-8 device. For this please start tha applciation with the  parameter ```-Ddummy.device=true```


