# PresenceCam

**A visual attendance app using face recognition**

PresenceCam is a mobile-first attendance tracking system that leverages computer vision and facial recognition. It consists of an Android application for the user interface and a Python-based backend server for processing. The system allows for easy registration of individuals and streamlined attendance marking from a single group photograph.

## Team Details
- **Team Name:** The BackBenchers  
- **Team Members:**
  - Ankit Agarwal
  - Kunal Sahu
  - Sarwang Sinha

## Architecture

The project is divided into two main components:

1. **Android Application (`/app`):**  
   A client application built with Jetpack Compose that provides a user-friendly interface for student registration and attendance taking. It communicates with the backend server via a REST API.

2. **Backend Server (`/server`):**  
   A FastAPI server that handles all the heavy lifting of facial recognition. It uses the `insightface` library (and ONNX runtime) to create facial embeddings for registration and to recognize faces in group photos for attendance.

## Features

- **Student Registration:** Register a student by providing an enrollment number and three facial photos (front and side profiles). The backend processes these images to create a robust facial embedding for the student.  
- **Automated Attendance:** Capture or upload a single group photo of the audience. The system detects all faces, compares them against the registered database, and generates a list of present individuals.  
- **Visual Feedback:** The backend returns annotated images highlighting all detected faces and a separate image showing only unrecognized faces, making it easy to review the results.  
- **Manual Entry & Export:** The app allows for manual addition of enrollment numbers to the attendance list and provides an option to save the final attendance list as a PDF.  
- **Modern Android UI:** The app is built entirely with Jetpack Compose, offering a modern, reactive, and intuitive user experience.

## Technology Stack

- **Android Application:**
  - **Language:** Kotlin
  - **UI:** Jetpack Compose
  - **Networking:** Retrofit & OkHttp
  - **Asynchronous Operations:** Kotlin Coroutines
  - **Image Loading:** Coil

- **Backend Server:**
  - **Framework:** FastAPI
  - **Face Recognition:** `insightface`
  - **Machine Learning Runtime:** ONNX Runtime
  - **Image Processing:** OpenCV, NumPy
  - **Deployment:** Docker (optional)

## How It Works

1. **Registration (`Register` Screen):**
   - The user enters a student's enrollment number and uploads/captures three clear photos of their face.
   - The Android app sends these images and the enrollment number to the `POST /register` endpoint on the server.
   - The server uses `insightface` to detect the face in each image, generate a facial embedding for each image, and calculates the mean embedding across the three images.
   - This final embedding is stored along with the enrollment number in a local database (`students_db.npz`) or other storage.

2. **Attendance (`Home` Screen):**
   - The user uploads or captures a group photo of the attendees.
   - The app sends this photo to the `POST /recognize` endpoint.
   - The server detects all faces in the image, generates an embedding for each detected face, and computes similarity scores against every registered embedding in the database.
   - If a similarity score exceeds a predefined threshold (example `0.4`), the face is matched to the corresponding enrollment number.
   - The server returns a JSON response containing the list of recognized enrollment numbers and base64-encoded annotated images.
   - The Android app displays the list of recognized students and the annotated images for review.

## Setup & Installation

### Backend Server

1. Navigate to the `server/` directory:
```bash
cd server
```

2. Install the required Python packages:
```bash
pip install -r requirements.txt
```

3. Run the FastAPI server. By default the app may use port 7860 — run it on port `8000` (recommended for the Android app) like this:
```bash
uvicorn model_api:app --host 0.0.0.0 --port 8000
```
Alternatively you can build and run via Docker if a `Dockerfile` is supplied.

### Android Application

1. Open the project in Android Studio.  
2. Update your backend base URL in the Retrofit instance (example file: `app/src/main/java/.../RetrofitInstance.kt`) to match the server IP and port:
```kotlin
.baseUrl("http://YOUR_SERVER_IP:8000/") // update this to your server
```
3. Build and run on a device/emulator.

## API Endpoints

- `POST /register`  
  Registers a new student.  
  **Form Data:**
  - `enroll_no` (string): The student's enrollment number.
  - `images` (files): Exactly three image files.

- `POST /recognize`  
  Recognizes students in a group photo.  
  **Form Data:**
  - `file` (file): A single image file containing the audience.

## UI / Screenshots

Screenshots used in the app are included in the `assets/` folder:

![Register Screen](assets/register_screen.jpeg)

![Home / Recognize Screen](assets/home_screen.jpeg)

## Future Enhancements

- Replace the local embeddings store with a scalable DB (DynamoDB / PostgreSQL).  
- Add live camera attendance streaming for real-time marking.  
- Improve matching accuracy with larger multi-view registration and better threshold tuning.  
- Add role-based access, attendance analytics, and export formats (CSV / Excel).

## AlgoStorm Submission Notes

- Repository name format: `TeamName-ProjectName` — e.g. `TheBackBenchers-PresenceCam`  
- Add your team name & project title in the repo description before submission.  
- Include `requirements.txt` (backend) and build files for the Android app.  
- Upload screenshots in `assets/` (already referenced).  
- Ensure README and project folders are present and the repo is public for submission.

## License

This project is licensed under the **Creative Commons Attribution-NonCommercial 4.0 International License**. You are free to view, share, and adapt the code for personal or educational purposes with proper attribution. Commercial use is prohibited. See the [LICENSE](LICENSE) file for more details.
