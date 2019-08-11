# KeralaRescue.in Relief Camps App 

A simple app for rescue camp coordinators to enter information about accomodated individuals and supply requirements. Data is synced to [keralarescue.in](https://keralarescue.in/relief_camps)

# Testing

### Local Server

- Setup the [backend Django application](https://github.com/IEEEKeralaSection/rescuekerala/) and run it.
- Make sure your mobile is connected to same WiFi as your computer, and find out your local IP. (Example: 192.168.0.20)
- Import this project to Android studio.
- Edit API base URL (with your local IP and server port) in `app/src/main/java/xyz/appmaker/keralarescue/Tools/Config.java`.
   
   Example:
   ```java
   public class Config {
      public static String BASE_URL = "http://192.168.0.20:8000";
   }
   ```

- Create a user for the backend (using `python3 manage.py createsuperuser`). Create a relief camp.

### Using Test Server
We have hosted a test server of the Django application at [http://35.202.24.11/](http://35.202.24.11/find_people/). 
This can be used for testing the app from the pre-built apk [Link to APK in repo](https://github.com/Appmaker-xyz/KeralaRescueRefugeeDB/raw/master/app-debug.apk).
You can use the default username:password (admin:admin) combination for the app for testing. 

### App Testings Steps

- Run the app. Login using username and password for the user (admin:admin for test server settings).

    <img src="https://i.imgur.com/PpVC7aC.jpg" alt="Login Page" width="300">
  
- Select the district that you created relief camp for. The camp should appear below (can be filtered by searching).

    <img src="https://i.imgur.com/BNGQbiB.jpg" alt="Camps Page" width="300">

- Click on the camp to open Person Registration.

    <img src="https://i.imgur.com/LwvECEb.jpg" alt="![Person Registration Page" width="300">

- Enter information for a person and submit it. You should see the count on top of the screen updating.
- Submitting information should work manually (it will be shown as pending, which will be updated when internet is back)
- Clicking Sync button should sync any pending items instantly.
- Data should appear on your local application in Person Finder
  - Test Server: [http://35.202.24.11/](http://35.202.24.11/find_people/)
  - Local: [http://localhost:8000/find_people/](http://localhost:8000/find_people/)
  
  # Releasing new versions
  - Use the keystore file located [here](app/keystore)
  - alias - key0
  - password - keralarescue
  
  # License
  MIT License
