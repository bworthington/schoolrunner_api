# schoolrunner_api
The purpose of this project is to use SchoolRunner's API and do stuff with data that is not possible or practical through the app.  Source code is written in Java and SQLite.

Here's the general idea:
1. Connect one or more of SR's API endpoints (students, assessments, courses, etc.).
2. Pasrse the JSON results and save the data to a SQLite database.
3. Use SQLite to query the database to get the specific information we want.

Possible applications:
*Identifying assessments that are missing objectives and the teachers who created them (by linking the assessments and staff tables) and automatically emailing those staff with the assessments that need to be fixed.

### Requirements
This code requires a few .jar files be installed first:
Two files related to actually connecting to the API are: okhttp-2.5.0.jar and okio-1.6.0.jar
Find out more and download them from ([http://square.github.io/okhttp/])

One file is for JSON parsing: json-simple.1.1.1.jar
Find out more and download it from ([https://code.google.com/p/json-simple/])

One file is for the SQLite database: sqlite-jdbc-3.8.11.2.jar
Find out more and download it from ([http://www.tutorialspoint.com/sqlite/sqlite_java.htm])

### How It's Organized
**schoolrunner/api_processing** is a Java package that contains the core building blocks of this project:

**ConnectToSRAPI** is the class that handles the connection to a given API endpoint.
**Login** in the class to reads in a username and password for basic authentication in the API connection.
**CreateDatabase** is the class that creates a database for saving data return from the API.
**CreateTable** is the class that creates specific tables within the database related certain endpoints.
**AssessmentsAPI" is the class that connects to the Assessments endpoint (using ConnectToSRAPI).  It can use a default URL or receive a specified URL through an overloaded constructor.  
