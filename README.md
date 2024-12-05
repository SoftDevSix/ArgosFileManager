
# ArgosFileManager
File Manager dedicated to managing and storing unstructured data.

## File Manager API Documentation

This document provides the details of the File Manager API endpoints, including their request formats, responses, and examples.

#### To Run the Api:
With Swagger Environment

1. Set Environment AWS credentials with the secret file provided: run-file-manager.sh
2. chmod +x ./run-file-manager.sh
3. ./run-file-manager.sh


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


## Architectural Design

### 1. Layered Architecture with Repository Pattern
The API uses a **layered architecture** combined with the **repository pattern**.

#### Key Features:
- **Separation of Concerns**: The application is structured into layers such as controllers, services, and repositories, each with a distinct responsibility.
- **Loose Coupling**: Components are independent, making it easy to replace or update them without affecting the rest of the system.
- **Storage Flexibility**: The repository layer abstracts storage operations. For example, if AWS S3 is replaced with another storage solution, only the repository layer needs to be modified.

#### Benefits:
- Enhances **maintainability** and **scalability**.
- Promotes **modularity**, ensuring adaptability to future changes or integrations.

---

### 2. Error Handling with GlobalHandler
The API implements centralized error management using a `GlobalExceptionHandler` class.

#### Implementation:
Errors are handled using a structured class hierarchy:

```java
package org.argos.file.manager.exceptions;

/**
 * Base class for API exceptions.
 * Implements IApiException and extends RuntimeException.
 */
public abstract class ApiException extends RuntimeException implements IApiException {
    private final int statusCode;

    protected ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }
}

```

### Design Pattern: Template Method
The error-handling structure follows the Template Method Pattern.

### Why Template Method?
The Template Method Pattern defines a general algorithm structure in a base class, allowing subclasses to implement specific behaviors for certain steps.

### In the Context of ApiException:
- General Structure: The base class ApiException provides a common structure for handling exceptions, including fields for the error message (message) and HTTP status code (statusCode).
- Extensibility: Subclasses such as BadRequestError and NotFoundError can add specific behaviors or data without modifying the base class.
- Centralized Template: Methods like getStatusCode() and the constructor in ApiException ensure consistency across all API exceptions.
