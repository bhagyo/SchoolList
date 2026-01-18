🏫 School Connect - Android Application

## 📱 Project Overview
**School Connect** is an Android application built with Kotlin and Jetpack Compose designed to facilitate school management, information sharing, and emergency communication. The app provides a comprehensive platform for accessing school details, statistics, and emergency protocols.

## 🏗️ Project Structure

### 📁 src/main/java/com.example.appdeeps/

#### 🧩 components/
UI components reused across the application

- **InfoCard.kt** - Generic card component for displaying information with consistent styling
- **SchoolCard.kt** - Specialized card for displaying school information with image, name, and quick stats
- **TeacherCard.kt** - Component for displaying teacher profiles with contact information and subjects

#### 📱 screens/
Application screens and their associated components

##### components/dialogs/
- **AboutDialog.kt** - Modal dialog displaying application information, version, and developer details
- **EmergencyDialog.kt** - Critical alert dialog for emergency situations with action buttons
- **extra.kt** - Additional utility components and extensions for screens

##### Screen Components
- **SchoolListContent.kt** - Main content area for school listings with scrollable layout
- **SchoolListHeader.kt** - Header section with title, filters, and action buttons
- **SchoolSearchBar.kt** - Search functionality with real-time filtering of schools
- **StatisticsDashboard.kt** - Visual dashboard displaying metrics and analytics
- **ThreeDotMenu.kt** - Context menu component for additional actions

##### Main Screens
- **SchoolDetailsScreen.kt** - Detailed view of individual school with complete information
- **SchoolListScreen.kt** - Primary screen displaying all schools in list/grid format

#### 🎨 ui.theme/
Material Design 3 theming and styling

- **Color.kt** - Color palette definition (primary, secondary, error, background colors)
- **Theme.kt** - Main theme configuration including dark/light mode support
- **Type.kt** - Typography system (font families, sizes, weights)

#### 🛠️ utils/
Utility classes and helper functions

##### Calculations/
- Mathematical utilities for statistics, averages, and data processing

##### MapUtils/
- **MapUtils.kt** - Geographic utilities for location services, distance calculation, and mapping integration

##### Shared Utilities
- **ShareUtils.kt** - Functions for sharing content via other apps (email, messaging, social media)

#### 🔥 FirebaseManager/
Cloud integration and backend services

- Handles authentication, real-time database, Firestore, and cloud storage
- Manages school data synchronization and user management

#### 🚀 MainActivity.kt
Application entry point and main activity
- Sets up navigation graph and global app configuration
- Initializes Firebase and other essential services

## 📁 res/
Resources directory containing non-code assets

### AndroidManifest.xml
Application configuration file
- Defines permissions (internet, location, storage)
- Declares activities, services, and broadcast receivers
- Sets application metadata and intent filters

## 🧪 Testing

### test/ (Unit Tests)
- JUnit tests for business logic, ViewModels, and utility functions
- Mocking with Mockito for dependencies

### src/androidTest/ (Instrumentation Tests)
- UI tests with Espresso and Compose testing
- Integration tests for full user flows

## 🔧 Key Features

### 1. School Directory
- Browse schools with filtering and search capabilities
- Detailed school profiles with contact information, facilities, and staff

### 2. Emergency Communication
- Quick access to emergency protocols
- Direct communication channels for urgent situations

### 3. Statistics Dashboard
- Visual representation of school metrics and performance indicators
- Data analytics for informed decision-making

### 4. Sharing Capabilities
- Share school information via multiple platforms
- Export data in various formats

### 5. Location Services
- Integrated mapping for school locations
- Distance calculation and navigation assistance

## 🛠️ Technical Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase (Authentication, Firestore, Storage)
- **Navigation:** Jetpack Navigation Component
- **Dependency Injection:** Hilt/Dagger (implied by typical architecture)
- **Testing:** JUnit, Espresso, Compose UI Testing

## 📦 Dependencies (Implied from structure)

- **Firebase BOM** - Authentication, Firestore, Storage
- **Jetpack Compose** - UI toolkit
- **Coil/Glide** - Image loading
- **Maps SDK** - Location services
- **Coroutines & Flow** - Asynchronous programming
- **Room Database** - Local caching (if implemented)

## 🚀 Setup Instructions

1. **Clone the repository**
   git clone <repository-url>

2. **Add Firebase Configuration**
   - Download google-services.json from Firebase Console
   - Place in app/ directory

3. **Update API Keys**
   - Add Google Maps API key in local.properties:
     MAPS_API_KEY=your_key_here

4. **Build and Run**
   ./gradlew assembleDebug

## 🔐 Permissions Required

- INTERNET - For API calls and Firebase
- ACCESS_FINE_LOCATION - For mapping features
- WRITE_EXTERNAL_STORAGE - For sharing and data export

## 📝 Development Notes

### Architecture Decisions
- Used Compose for modern declarative UI
- Separated concerns with clean package structure
- Implemented reusable components for consistency
- Firebase provides real-time sync and scalability

### Future Enhancements
- Push notifications for updates
- Parent-teacher communication module
- Event calendar integration
- Offline mode with Room database
- Multi-language support

## 📊 Data Models (Implied)

1. School - Name, address, contact, facilities, staff
2. Teacher - Name, subject, contact, qualifications
3. EmergencyContact - Protocol, contacts, procedures
4. Statistics - Metrics, analytics, trends

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes with descriptive messages
4. Submit a pull request

## 📄 License
This project is for educational purposes. All school data should be used in compliance with privacy regulations.

## 🏆 Acknowledgments

- Jetpack Compose team for the excellent UI framework
- Firebase for backend services
- Material Design 3 for design system
- Android Developer Community for resources and support

---
Last Updated: January 2026
Project Status: Completed
Target SDK: Android 14 (API 34)
Minimum SDK: Android 8.0 (API 26)
---

