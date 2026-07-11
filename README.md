---
title: FieldInspect
emoji: 🔧
colorFrom: green
colorTo: gray
sdk: docker
app_port: 7860
pinned: false
---

# FieldInspect

> **Live demo** (Hugging Face Space): log in with `alan@fieldinspect.com` / `password123`
> (technician) or `siti@fieldinspect.com` / `password123` (supervisor). The demo runs the
> Flutter client as a web build against the same Spring Boot API, on an in-memory H2
> database that reseeds on every restart. Local development uses SQL Server and the
> native Android app.

FieldInspect is a full-stack field inspection management application that allows technicians to perform on-site asset inspections while enabling administrators to manage assets, users, and inspection records.

The project demonstrates the development of a secure REST API using Spring Boot, JWT authentication, JPA/Hibernate, and a relational database, with a mobile client consuming the API.

---

## Features

### Authentication & Authorization

* JWT-based authentication
* User registration and login
* Password hashing with BCrypt
* Role-based access control (Administrator and Inspector)

### Asset Management

* Create, update, and delete assets
* View all assets
* Track asset information such as name, location, and type

### Field Inspections

* Perform inspections on assets
* Record inspection status:

  * PASS
  * FAIL
  * NEEDS_FOLLOW_UP
* Add inspection notes
* Automatically record the inspector and inspection timestamp
* Store multiple measurement readings for each inspection

### Measurement Readings

Each inspection can include multiple readings such as:

* Voltage
* Current
* Temperature
* Pressure
* Humidity

Each reading stores:

* Metric
* Value
* Unit
* Timestamp

---

## Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA (Hibernate)
* JWT Authentication
* Jakarta Validation
* Maven

### Database

* Microsoft SQL Server 2022 (Docker) for development
* H2 in-memory for tests and the hosted demo

### Client

* Flutter mobile application (Android), also compiled to web for the hosted demo
* Provider state management, JWT session persisted in encrypted secure storage

---

## Project Structure

```text
src
└── main
    └── java
        └── com.fieldinspect.backend
            ├── asset
            ├── auth
            ├── inspection
            ├── reading
            ├── user
            └── security
```

---

## API Highlights

### Authentication

* Register user
* Login
* Receive JWT access token

### Assets

* Create asset
* List assets
* Update asset
* Delete asset

### Inspections

* Submit a new inspection
* Retrieve inspection history
* View inspection details

Each inspection records:

* Asset
* Inspector
* Status
* Notes
* Date performed
* Measurement readings

---

## Security

The application uses JWT authentication for stateless API security.

After a successful login:

1. The server issues a JWT.
2. The client stores the token.
3. Every protected request includes:

```http
Authorization: Bearer <JWT>
```

The authenticated user is extracted from the JWT, ensuring clients cannot impersonate another inspector.

---

## Validation

Incoming requests are validated using Jakarta Validation annotations.

Examples include:

* Required fields
* Valid inspection status
* Non-empty measurement units
* Non-null reading values

Invalid requests automatically return a `400 Bad Request` response.

---

## Learning Objectives

This project was built to strengthen knowledge of:

* RESTful API design
* Spring Boot architecture
* Spring Security
* JWT authentication
* Entity relationships with JPA/Hibernate
* Request and response DTOs
* Bean validation
* Database design
* Layered application architecture
* Backend development best practices

---

## Future Improvements

* Photo uploads for inspections
* GPS location tracking
* Offline inspection support with synchronization
* Inspection scheduling
* Search and filtering
* Asset maintenance history
* Dashboard and analytics
* PDF inspection reports
* Email notifications
* Swagger/OpenAPI documentation

---

## License

This project is intended for educational and portfolio purposes.
