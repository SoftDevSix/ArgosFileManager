
# ArgosFileManager
File Manager dedicated to managing and storing unstructured data.

## File Manager API Documentation

This document provides the details of the File Manager API endpoints, including their request formats, responses, and examples.

## Endpoints

### 1. List Files

#### Request
- **Method**: `GET`
- **URL**: `/fileManager/files`

#### Request Parameters
- **projectId**: The unique identifier of the project.

#### Response
- **Status Code**: 200 OK
- **Body**: A JSON array of file names (strings).

#### Example Response
```json
[
    "projects/{projectId}/projectFiles/dirctoryFirst/class1.java",
    "projects/{projectId}/projectFiles/sec/classDirSec.java",
    "projects/{projectId}/projectFiles/subdirector/subClass1.java"
]
```

### 2. Get File Content

#### Request
- **Method**: `GET`
- **URL**: `/fileManager/file`

#### Request Parameters
- **projectId**: The unique identifier of the project.
- **filePath**: The path of the file.

#### Response
- **Status Code**: 200 OK
- **Body**: The content of the file as a string.

#### Example Request
```http
GET /fileManager/file?projectId=test-project-id&filePath=file1.java
```

#### Example Response
```Java
public class Main {
    public static void main(String[] args) {
        System.out.println("Jefferson Viejo");
    }
}
```

### 3. Upload Directory

#### Request
- **Method**: `POST`
- **URL**: `/fileManager/upload`

#### Request Parameters
- **localDir**: The local directory path that contains the files to be uploaded.

#### Response
- **Status Code**: 200 OK
- **Body**: A JSON object that contains the status of each uploaded file.

#### Example Request
```http
POST /fileManager/upload?localDir=test/
```

#### Example Response
```json
{
  "uploadResults": {
    "projects/e011bad2-0b57-4ed3-a278-29b255d25621/projectFiles/dirctoryFirst/class1.java": "Uploaded",
    "projects/e011bad2-0b57-4ed3-a278-29b255d25621/projectFiles/subdirector/subClass1.java": "Uploaded",
    "projects/e011bad2-0b57-4ed3-a278-29b255d25621/projectFiles/sec/classDirSec.java": "Uploaded"
  },
  "projectId": "e011bad2-0b57-4ed3-a278-29b255d25621"
}
```


### 4. Upload ZIP File

#### Request
- **Method**: `POST`
- **URL**: `/fileManager/uploadZip`
- 
#### Request Body
- **Content Type**: `multipart/form-data`
- **Form Data**:
    - **zipFile**: The ZIP file to be uploaded.

#### Response
- **Status Code**: 200 OK
- **Body**: A JSON object containing the status of each file in the ZIP after extraction and upload.

#### Example Response
```json
{
  "uploadResults": {
    "projects/e011bad2-0b57-4ed3-a278-29b255d25621/projectFiles/dirctoryFirst/class1.java": "Uploaded",
    "projects/e011bad2-0b57-4ed3-a278-29b255d25621/projectFiles/subdirector/subClass1.java": "Uploaded",
    "projects/e011bad2-0b57-4ed3-a278-29b255d25621/projectFiles/sec/classDirSec.java": "Uploaded"
  },
  "projectId": "e011bad2-0b57-4ed3-a278-29b255d25621"
}
```