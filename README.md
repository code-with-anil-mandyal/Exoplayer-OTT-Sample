# Android OTT Video Player (Media3 ExoPlayer)

A modern Android video player built using **Media3 ExoPlayer** demonstrating core OTT platform features such as video streaming, custom player controls, caching, offline downloads, and DRM playback.

This project demonstrates how OTT platforms like Netflix, Amazon Prime Video, Disney+, and YouTube implement video playback architecture on Android using Media3 ExoPlayer.

The goal of this project is to showcase **production-style Android video streaming architecture**.

---

# Features

## Video Streaming
• HLS video streaming (.m3u8)  
• DASH video playback support  
• Adaptive bitrate streaming  

## Custom Video Player UI
• Custom Play / Pause button  
• Custom Seekbar with video progress  
• Fullscreen mode (portrait ↔ landscape)  
• Auto hide player controls  
• Forward / Replay controls (±10 seconds)

## Playback Controls
• Playback speed control  
• Video quality selection  
• Player state handling (Buffering, Ready, Error)

## Video Caching
• Implemented using **SimpleCache**
• Reduces buffering and improves playback performance
• Efficient segment storage

## Offline Video Download
• Implemented using **DownloadManager**
• Uses **DownloadService** for background downloads
• Foreground notification download progress
• Supports offline playback without internet connection

## DRM Support
• Widevine DRM playback support
• Secure encrypted video streaming

## Player Lifecycle Handling
• Player maintained using ViewModel
• Handles orientation changes without restarting playback

---

# Tech Stack

## Language
Kotlin

## Video Player
Media3 ExoPlayer

## Architecture
MVVM Architecture

## Android Libraries
Android Jetpack ViewModel  
Media3 UI  
Media3 ExoPlayer  

## Streaming Technologies
HLS Streaming  
DASH Streaming  

## Caching
SimpleCache  
CacheDataSource  

## Offline Download
DownloadManager  
DownloadService  

## DRM
Widevine DRM

---

# Architecture Overview

The project follows a clean architecture for player lifecycle and streaming logic.

Activity (UI)  
↓  
ViewModel (Player State Management)  
↓  
ExoPlayer (Media3)  
↓  
MediaSource  
↓  
CacheDataSource  
↓  
SimpleCache  
↓  
Network  

This architecture ensures:

• lifecycle-safe player management  
• separation of UI and player logic  
• efficient caching and offline playback  

---

# Player Features Implemented

## Custom Player Controls

The default ExoPlayer controls are replaced with a custom player UI including:

• Play / Pause  
• Forward 10 seconds  
• Replay 10 seconds  
• Seekbar with time indicator  
• Fullscreen toggle  
• Settings menu  

---

# Adaptive Bitrate Streaming

The player automatically switches video quality depending on network speed.

Example:

Fast Network → 720p / 1080p  
Slow Network → 240p / 360p  

This helps reduce buffering and improves playback experience.

---

# Offline Download Implementation

Offline video download is implemented using:

DownloadManager  
DownloadService  
SimpleCache  

### Download Flow

1. User taps Download  
2. DownloadManager starts downloading video segments  
3. Segments are stored in SimpleCache  
4. Foreground notification shows download progress  
5. Video becomes available for offline playback  

Offline playback works even when:

• WiFi is OFF  
• Mobile Data is OFF  

---

# DRM Playback

This project demonstrates **Widevine DRM playback**.

Widevine is the DRM technology used by most Android OTT platforms.

### Widevine Security Levels

Level | Description  
L1 | Hardware secure (supports HD / 4K playback)  
L2 | Partial hardware security  
L3 | Software-based security  

Most Android emulators support **Widevine L3 only**.

---

# Foreground Service Support (Android 14+)

Android 14 introduced strict foreground service requirements.

The app includes required permissions:

android.permission.FOREGROUND_SERVICE  
android.permission.FOREGROUND_SERVICE_DATA_SYNC  

These permissions allow video downloads to run in the background using a foreground service.

---

# Sample Test Streams

## HLS Stream

https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8

## DRM DASH Stream

https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd

License Server

https://proxy.uat.widevine.com/proxy?provider=widevine_test

---

## Screenshots

### Player Screen
![Player](screenshots/player_screen.png)

### Settings Screen
![Player](screenshots/setting_screen.png)

### Select Video Quality Screen
![Player](screenshots/video_quality_screen.png)

### Fullscreen Mode
![Fullscreen](screenshots/fullscreen_mode.png)

### Download Notification
![Download](screenshots/download_notification.png)

---

# Future Improvements

Possible improvements for this project:

• Chromecast support  
• Picture-in-Picture mode  
• Subtitle support  
• DRM offline license support  
• Live streaming support  
• Video analytics integration  

---

# What This Project Demonstrates

This project demonstrates core OTT concepts such as:

• Video streaming architecture  
• Adaptive bitrate streaming  
• Offline download system  
• Video caching strategies  
• DRM video playback  

---

# Author

Anil Kumar  

Android Developer with **6+ years of experience** building scalable Android applications using Kotlin and modern Android architecture.

---

# License

This project is intended for educational and demonstration purposes.
