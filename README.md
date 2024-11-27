# ArgosFileManager
File Manager dedicated manage and store unstructured data.

## File Manager API Documentation

This document provides the details of the File Manager API endpoints, including their request formats, responses, and examples.

## Endpoints

### 1. List Files

#### Request
- **Method**: `GET`
- **URL**: `/api/files`

#### Request Parameters
- None

#### Response
- **Status Code**: 200 OK
- **Body**: A JSON array of file names (strings).

#### Example Response
```json
[
    "projectFiles/dirctoryFirst/class1.java",
    "projectFiles/sec/classDirSec.java",
    "projectFiles/subdirector/subClass1.java"
]
```

### 2. Get File Content

#### Request
- **Method**: GET
- **URL**: `/api/file`
  
#### Request Parameters
- **key**: The unique identifier of the file (e.g., file name).

#### Response
- **Status Code**: 200 OK
- **Body**: The content of the file as a string.

#### Example Request
```http
GET /api/file?key=file1.java
```

#### Example Response
```Java
public class Main {
    public static void main(String[] args) {
        System.out.println("Jefferson Viejo");
    }
}
```

---

### 3. Upload Directory

#### Request
- **Method**: POST
- **URL**: `/api/upload`

#### Request Parameters
- **localDir**: The local directory path that contains the files to be uploaded.

#### Response
- **Status Code**: 200 OK
- **Body**: A JSON object that contains the status of each uploaded file.

#### Example Request
```http
POST /api/upload?localDir=test/
```

#### Example Response
```json
{
    "projectFiles/subdirector/subClass1.java": "Uploaded",
    "projectFiles/sec/classDirSec.java": "Uploaded",
    "projectFiles/dirctoryFirst/class1.java": "Uploaded"
}
```

---

