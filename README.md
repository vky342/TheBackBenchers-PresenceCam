# The-Backbenchers
Visual Attendance system allowing coordinator to extract attendance via clicked picture of the audience 

## Overview
This project offers a modern and efficient solution for event coordinators to manage attendance for large audiences.  
By leveraging **computer vision** and **facial recognition**, the application automates the process of taking attendance from a single group photograph, eliminating the need for manual check-ins.  

It is ideal for **conferences, workshops, and large gatherings** where a quick and accurate attendance record is crucial.

---

## Features
- **Automated Face Detection**: Automatically identify and count individual faces in a group photograph, even in crowded settings.  
- **Attendance Matching**: Cross-reference detected faces with a pre-registered list of attendees (e.g., from a CSV file) to accurately mark who is present.  
- **Detailed Reporting**: Generates a comprehensive report including:  
  - List of attendees who were successfully matched  
  - Count of detected faces  
  - List of registered individuals who were not found in the photo  
- **Simple User Interface**: A clean and user-friendly web interface makes it easy for coordinators to upload a photo and get an attendance report in just a few clicks.  

---

## How It Works
The process is straightforward and fast:

1. **Registration Data Upload**: The coordinator uploads a CSV file containing the list of all registered attendees.  
2. **Group Photo Upload**: A group picture of the audience is taken and uploaded to the application.  
3. **Processing**: The facial recognition model scans the photo, locates each face, and then matches these faces to the data from the registration file.  
4. **Report Generation**: A final attendance report is generated, showing who was present and who was not.  

---

## License
This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.

