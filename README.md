<h1 align="center">Trevello - Your Travel Journal Companion</h1>

Trevello is a travel journaling Android application built with Kotlin. It allows users to document their travel experiences and visualize them on a map. With Trevello, you can add entries for each location you visit, complete with title, description, and images. These entries are then marked on a Google Map, allowing you to see your travel journey unfold. Whether you're a casual traveler or a seasoned adventurer, Trevello is designed to make your travel journaling experience seamless and enjoyable.

## Screenshots

<p>
  <img src="https://github.com/ManethSW/trevello/assets/112096694/0c4d8fd4-ff99-448c-bc53-0d6e87486891" />
  <img src="https://github.com/ManethSW/trevello/assets/112096694/5b946551-dfc4-41ea-91de-64843a336583" />
  <img src="https://github.com/ManethSW/trevello/assets/112096694/3c932836-0a66-4687-843d-85110aed08b1" />
  <img src="https://github.com/ManethSW/trevello/assets/112096694/2c2322fb-02d2-493d-9267-0dd18c84e02c" />
</p>

## Features

* <b>User Authentication:</b>
  Allows users to create an account and log in to access their personal travel journals via phone number and otp.

* <b>Travel Journaling:</b>
  Enables users to add entries for each location they visit. Each entry includes a title, description, and images.

* <b>Map Visualization:</b>
  Marks each entry on a Google Map, allowing users to visualize their travel journey.

* <b>Location Services:</b>
  Uses the device's location services to provide real-time location updates.

* <b>Custom Map Markers:</b>
  Uses custom map markers to represent different locations on the map.

* <b>Navigation:</b>
  Includes a bottom navigation bar for easy access to different sections of the app.

* <b>Permissions Handling:</b>
  Handles location permissions, requesting them from the user if not already granted.

* <b>Snackbar Notifications:</b>
  Uses Snackbar notifications to provide feedback to the user.

* <b>Profile Viewing:</b>
  Allows users to view their profile information, including their avatar, full name, and email.

* <b>Profile Editing: </b>
  Enables users to edit their profile information, including their avatar, full name, and email. The application also validates the input data to ensure it is in the correct format.

* <b>Theme Switching: </b>
  Allows users to switch between light and dark themes. The application saves the user's theme preference and applies it when the user reopens the application.

* <b>Logout: </b>
  Allows users to log out of their account. After logging out, they are redirected to the startup page.

* <b>Image Handling: </b>
  Allows users to change their profile picture by either taking a new photo or choosing one from the gallery. The application handles image picking and camera access.

* <b>Firebase Integration:</b>
  Uses Firebase for user authentication. User profile data is stored in a Firestore database, and profile pictures are stored in Firebase Storage.

## Demo credentials

Phone Number: `0770123456`

OTP Code: `135246`


## Development Setup

Before you begin, you should have already downloaded the Android Studio SDK and set it up correctly. You can find a guide on how to do this here: [Setting up Android Studio](http://developer.android.com/sdk/installing/index.html?pkg=studio).

## Building the App

1. Clone the repository using HTTP: git clone https://github.com/ManethSW/trevello.git

2. Open Android Studio.

3. Click on 'Open an existing Android Studio project'

4. Browse to the directory where you cloned the trevello repo and click OK.

5. Let Android Studio import the project.

6. Build the application in your device by clicking run button.