# VisualAttendanceApp
[![Ask DeepWiki](https://devin.ai/assets/askdeepwiki.png)](https://deepwiki.com/vky342/VisualAttendanceApp)

VisualAttendanceApp is a mobile-first attendance tracking system that leverages computer vision and facial recognition. It consists of an Android application for the user interface and a Python-based backend server for processing. The system allows for easy registration of individuals and streamlined attendance marking from a single group photograph.

## Architecture

The project is divided into two main components:

1.  **Android Application (`/app`):** A client application built with Jetpack Compose that provides a user-friendly interface for student registration and attendance taking. It communicates with the backend server via a REST API.
2.  **Backend Server (`/server`):** A FastAPI server that handles all the heavy lifting of facial recognition. It uses the `insightface` library to create facial embeddings for registration and to recognize faces in group photos for attendance.

## Features

-   **Student Registration:** Register a student by providing an enrollment number and three facial photos (front and side profiles). The backend processes these images to create a robust facial embedding for the student.
-   **Automated Attendance:** Capture or upload a single group photo of the audience. The system detects all faces, compares them against the registered database, and generates a list of present individuals.
-   **Visual Feedback:** The backend returns annotated images highlighting all detected faces and a separate image showing only unrecognized faces, making it easy to review the results.
-   **Manual Entry & Export:** The app allows for manual addition of enrollment numbers to the attendance list and provides an option to save the final attendance list as a PDF.
-   **Modern Android UI:** The app is built entirely with Jetpack Compose, offering a modern, reactive, and intuitive user experience.

## Technology Stack

-   **Android Application:**
    -   **Language:** Kotlin
    -   **UI:** Jetpack Compose
    -   **Networking:** Retrofit & OkHttp
    -   **Asynchronous Operations:** Kotlin Coroutines
    -   **Image Loading:** Coil

-   **Backend Server:**
    -   **Framework:** FastAPI
    -   **Face Recognition:** `insightface`
    -   **Machine Learning Runtime:** ONNX Runtime
    -   **Image Processing:** OpenCV, NumPy
    -   **Deployment:** Docker

## How It Works

1.  **Registration (`Register` Screen):**
    -   The user enters a student's enrollment number and uploads/captures three clear photos of their face.
    -   The Android app sends these images and the enrollment number to the `POST /register` endpoint on the server.
    -   The server uses `insightface` to detect the face in each image, generate a 512-dimension facial embedding, and calculates the mean of the three embeddings.
    -   This final embedding is stored along with the enrollment number in a local database (`students_db.npz`).

2.  **Attendance (`Home` Screen):**
    -   The user uploads or captures a group photo of the attendees.
    -   The app sends this photo to the `POST /recognize` endpoint.
    -   The server detects all faces in the image, generates an embedding for each one, and computes the similarity score against every registered embedding in the database.
    -   If a similarity score exceeds a predefined threshold (`0.4`), the face is matched to the corresponding enrollment number.
    -   The server returns a JSON response containing the list of recognized enrollment numbers and base64-encoded strings for the annotated images.
    -   The Android app displays the list of recognized students and the annotated images.

## Setup & Installation

### Backend Server

1.  Navigate to the `server/` directory.
    ```bash
    cd server
    ```
2.  Install the required Python packages.
    ```bash
    pip install -r requirements.txt
    ```
3.  Run the FastAPI server. By default, it runs on port 7860, but the Android app expects port 8000. You can run it on port 8000 as follows:
    ```bash
    uvicorn model_api:app --host 0.0.0.0 --port 8000
    ```
    Alternatively, you can build and run the provided `Dockerfile`.

### Android Application

1.  Open the project's root folder in Android Studio.
2.  Navigate to `app/src/main/java/com/example/visualattendanceapp/data/models.kt`.
3.  In the `RetrofitInstance` object, update the `baseUrl` to match the IP address and port of your running backend server.
    ```kotlin
    object RetrofitInstance {
        // ...
        val api: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl("http://YOUR_SERVER_IP:8000/") // <-- Update this line
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
    ```
4.  Build and run the app on an Android emulator or a physical device.

## API Endpoints

-   `POST /register`
    -   Registers a new student.
    -   **Form Data:**
        -   `enroll_no` (string): The student's enrollment number.
        -   `images` (files): Exactly three image files.
-   `POST /recognize`
    -   Recognizes students in a group photo.
    -   **Form Data:**
        -   `file` (file): A single image file containing the audience.

## License

This project is licensed under the **Creative Commons Attribution-NonCommercial 4.0 International License**. You are free to view, share, and adapt the code for personal or educational purposes with proper attribution. Commercial use is strictly prohibited. See the [LICENSE](LICENSE) file for more details.