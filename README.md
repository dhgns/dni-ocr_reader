# DNI OCR Reader

This ANDROID library is created to read DNI and DNIe by reading the OCR section of the documents.
The library uses the google play service for visual recognition.

The library has been implemented creating a textRecognition, provided of a continuous source of frames captured by the camera using a CameraSource.

The project includes the library and a explample of integration of the library in an application

###Features
This library allow the user to pick between 4 levels of exploration:
    Level 0: The search will return (at least) the document number
    Level 1: The search will return (at least) the document number and the expiration date
    Level 2: The search will return (at least) the document number, the expiration date and the birthDate
    Level 3: The search will return (at least) the document number, the expiration date, the birthDate and the name

###Performance

Level 0

          Field      |  Time  |  Match Average
    ---------------- | ------ | ---------------
    Document number  | [1,2]s |      10/10
    Expiration date  |   ''   |      09/10
    BirthDate        |   ''   |      09/10
    Document name    |   ''   |      00/10

Level 1
          Field      |  Time  |  Match Average
    ---------------- | ------ | ---------------
    Document number  | [1,2]s |      10/10
    Expiration date  |   ''   |      10/10
    BirthDate        |   ''   |      09/10
    Document name    |   ''   |      00/10


Level 2
          Field      |  Time  |  Match Average
    ---------------- | ------ | ---------------
    Document number  | [1,2]s |      10/10
    Expiration date  |   ''   |      10/10
    BirthDate        |   ''   |      10/10
    Document name    |   ''   |      01/10


Level 3
          Field      |  Time  |  Match Average
    ---------------- | ------ | ---------------
    Document number  | [1,2]s |      10/10
    Expiration date  |   ''   |      09/10
    BirthDate        |   ''   |      09/10
    Document name    |   ''   |      00/10

### Improvements

Create a custom Camera Source so it only detect the specific area of the OCR, not all the document itself like it is currently developed

License
----
AT
