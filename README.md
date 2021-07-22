# transfer_azimuth_android
Establish TCP/IP connection between Android devices to transfer the azimuth (the horizontal direction of the device)

## Application Info
### SocketServer
The app accepts Socket connections and return the current azimuth (0 <= theta < 360, clockwise).

e.g. 3.2, 245.7, 145.0 etc.

- Run a Socket connection server
- Continuously calculate the current azimuth by using position sensors in the device.
- Accept Socket connections from other devices and return the current azimuth

This app will accept Socket connections from any device.

### SocketTest
- Test 
- Establish Socket connection to the device runnning SocketServer.
- Send a string data (the current time) and receive a response (the current azimuth) from the server.

## Requirements
- API 19: Android 4.4 (KitKat)

## Installation
Install Android Studio on your PC and run app on real device or Android Virturl Device.