# 🔐 FaceVault – Smart Face-Based App Security for Android

FaceVault is an Android application that enhances mobile security by using **face detection and recognition** to monitor and log access to sensitive apps.
It acts as a **privacy guard**, helping users track unauthorized access attempts in real time.

---

## 🚀 Features

* 📷 **Real-Time Face Detection**
  Detects faces using the device camera with low-latency processing

* 🔒 **App Access Monitoring**
  Tracks when selected apps are opened

* 🧠 **On-Device ML Processing**
  Uses TensorFlow Lite for fast and secure inference (no cloud dependency)

* 🕵️ **Intruder Capture System**
  Captures and stores images of users attempting to access protected apps

* 🧾 **Access Logs**
  Maintains timestamped records of app access attempts

* ⚡ **Optimized Performance**
  Efficient handling of camera frames and ML model inference

---

## 🛠️ Tech Stack

* **Language:** Kotlin
* **Framework:** Android SDK
* **Camera:** CameraX
* **Machine Learning:** TensorFlow Lite
* **Image Processing:** Bitmap APIs
* **Architecture:** (Add if you used – MVVM / Clean Architecture)

---

## 📱 How It Works

1. User selects apps to protect
2. When a protected app is opened:

   * Camera activates automatically
   * Face detection runs in real-time
3. If access is unauthorized:

   * Image is captured
   * Entry is stored in logs with timestamp

---

## 🧪 Challenges & Solutions

* **Camera lifecycle issues** → Managed using CameraX lifecycle-aware components
* **TensorFlow Lite interpreter crashes** → Fixed by proper tensor allocation and initialization
* **Performance lag** → Optimized bitmap conversion and reduced inference overhead

---

## 📸 Screenshots

*(Add screenshots here)*

---

## ⚙️ Installation

1. Clone the repository:
```
git clone https://github.com/yourusername/FaceVault.git
```

2. Open in Android Studio

3. Build and run on a physical device (Camera required)

---

## 🔐 Permissions Required

* Camera
* Storage (for saving captured images)
* Usage Access (to monitor app usage)

---

## 📈 Future Improvements

* Face recognition (not just detection)
* Cloud sync for logs
* Alert notifications for intrusions
* App lock integration.

## 👨‍💻 Author

**Samadnya Suryawanshi**

* Android Developer | Machine Learning Enthusiast

---
