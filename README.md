# 🐾 Adopet
**Mobile Pet Adoption and Tracking Platform**

Adopet is a modern Android application that brings pet adoption processes to mobile platforms, featuring a **Firebase-based architecture** with **multiple API and sensor integrations**.  
The application is developed using the Kotlin programming language and up-to-date Android architecture principles.

This project was developed by a **single developer** as a **personal portfolio project** focused on **academic coursework and internship applications**.

---

## 🧠 What Does the Application Do?

The main purpose of Adopet is:

> **To manage pet adoption listings in a secure, filterable, and location-based manner**

The application primarily focuses on the following functionalities:

- 📋 Listing and filtering pet adoption posts  
- ❤️ Adding listings to favorites  
- ➕ Creating new listings (GPS-supported)  
- 🗺️ Displaying listing locations on a map  
- ☁️ Performing CRUD operations via Firebase Cloud Firestore  

---

## 🚀 Features

- 📱 Android application developed with Kotlin  
- ☁️ Firebase Cloud Firestore (NoSQL – CRUD)  
- 🔌 Integration of 2 Web APIs  
- 📍 Location acquisition via GPS sensor  
- 🗺️ Google Maps integration  
- 🔄 Real-time data management  
- 📦 High-performance listing with RecyclerView  
- 🎨 Modern UI based on Material Design  

---

## 🛠️ Technologies Used

| Component | Technology |
|------|---------|
| Language | Kotlin |
| Platform | Android |
| Database | Firebase Cloud Firestore |
| API | OpenWeatherMap, Google Maps / Geocoding |
| Networking | Retrofit 2, Gson |
| Asynchronous | Kotlin Coroutines |
| Sensor | GPS |
| UI | AndroidX, Material Design |
| Image Loading | Glide / Picasso |

---

## 🔌 API & Sensor Integrations

- 🌦️ **OpenWeatherMap API** – Provides weather data based on the user’s location  
- 🗺️ **Google Maps / Geocoding API** – Displays listing locations on an interactive map  
- 📍 **GPS Sensor** – Retrieves real-time coordinates while creating listings  

📡 All API calls are executed **asynchronously** to ensure a smooth user experience.

---

<img width="526" height="835" alt="image" src="https://github.com/user-attachments/assets/f25d18e4-c8f6-44c1-83c1-d30503845997" />


📂 Project Structure
  Adopet/
  │
  ├── app/
  │   ├── src/
  │   └── build.gradle.kts
  │
  ├── firestore.rules
  ├── build.gradle.kts
  ├── settings.gradle.kts
  └── gradle.properties

⚙️ Installation
  git clone https://github.com/username/adopet.git
  Open the project with Android Studio

  Create a project in Firebase Console
  
  Add the google-services.json file to the app/ directory
  
  Enable Firestore
  
  Run the application

🎯 Project Purpose
  This project aims to:

  Gain hands-on experience with modern Android development

  Implement Firebase and API integrations

  Present a strong portfolio project for internships and academic evaluations

👤 Developer
  Batuhan Gürsoy
  Information Systems Engineering Student

📄 License
  This project is intended for educational and personal use only.

