#  EduScan – Secure QR-based PDF Sharing App for Teachers and Students

EduScan is an Android application that enables teachers to securely upload and share PDF documents with students via QR codes. Students can scan the QR code to view the document directly in the app, with access control and security features to ensure a smooth and safe experience.

> 🌐 **Download the apk**: [EduScan.apk](https://drive.google.com/file/d/1KbUiYOP7SOftaQa2uJwdKlW6BF5w3XVC/view?usp=drive_link)

---

##  Features

###  For Teachers:
-  Login with email & password
-  Select and upload PDF securely to Firebase
-  Automatically generate QR code for each uploaded PDF
-  Prevent screenshots or screen recordings for PDF content
-  PDF access is disabled once teacher logs out

###  For Students:
-  Login with email & password
-  Scan QR code using in-app scanner
-  View the PDF inside the app (no external app access)
-  Prevents saving or screenshotting of PDF content

---

##  Tech Stack

| Component            | Technology                        |
|----------------------|------------------------------------|
| Language             | Kotlin                             |
| UI Framework         | Android XML Layouts                |
| Authentication       | Firebase Authentication            |
| Storage              | Firebase Storage                   |
| Database             | Firebase Firestore (for PDF mapping) |
| QR Code Generation   | ZXing QR Code Generator            |
| QR Code Scanner      | ML Kit or ZXing Barcode Scanner    |
| PDF Viewer           | `com.github.barteksc:android-pdf-viewer` |
| Permissions Handling | Android Runtime Permissions (Camera & Storage) |

---

##  Permissions Used

| Permission               | Purpose                        |
|--------------------------|--------------------------------|
| `CAMERA`                 | QR code scanning               |
| `READ_EXTERNAL_STORAGE`  | Teacher selects local PDFs     |
| `INTERNET`               | Firebase services              |

---

##  Screenshots

<img width="664" alt="Screenshot 2025-04-17 at 7 21 47 PM" src="https://github.com/user-attachments/assets/f30a8765-0afd-44b0-9079-89f2196c3c9b" />
<img width="560" alt="Screenshot 2025-04-17 at 7 22 35 PM" src="https://github.com/user-attachments/assets/81380fc7-c9aa-4e39-82a5-284eb31a46ea" />
<img width="665" alt="Screenshot 2025-04-17 at 7 21 30 PM" src="https://github.com/user-attachments/assets/457b936b-407b-48c0-ae78-0881522ead1e" />
<img width="565" alt="Screenshot 2025-04-17 at 7 22 22 PM" src="https://github.com/user-attachments/assets/a0544295-cd19-4c6f-9606-f7b46c8a2431" />
<img width="460" alt="Screenshot 2025-04-17 at 7 22 50 PM" src="https://github.com/user-attachments/assets/7204d171-58e0-4ad4-8570-e254f532b2d3" />
<img width="561" alt="Screenshot 2025-04-17 at 7 22 07 PM" src="https://github.com/user-attachments/assets/9731cb38-d381-4647-9acd-d3e678d22cb3" />

---
