# 📚 StudyFlow - Learning Management System (LMS) Backend

StudyFlow is a robust, modular backend for a Learning Management System. Built with **Java 21** and **Spring Boot 3.5**, it provides a scalable and secure foundation for managing courses, quizzes, student enrollments, and performance analytics.

> **Note:** This repository contains the **Backend API** in this branch. The **Android Frontend** (Jetpack Compose) is available in a separate branch (e.g., `master`). The system is designed to interface via a stateless RESTful API.

---

## 🚀 Key Modules & Features

### 🔐 Authentication & Security
- **JWT-Based Security:** Stateless authentication using JSON Web Tokens.
- **RBAC:** Role-Based Access Control (Student, Teacher, Admin).
- **Profile Management:** Unified user entity with specialized Student and Teacher profiles using Class Table Inheritance.

### 📚 Course Management
- **Course Lifecycle:** Create, update, and manage course materials.
- **Material Distribution:** Secure file storage and distribution for course resources.
- **Enrollment System:** Logic-driven enrollment with capacity limits and conflict prevention.

### 📝 Quiz Engine
- **Dynamic Quizzes:** Multiple-choice question support with flexible scoring.
- **Submission Validation:** Prevents duplicate submissions and ensures data integrity.
- **Instant Grading:** Automated result calculation and feedback.

### 📊 Analytics & Reporting
- **Teacher Dashboard:** Real-time class averages and enrollment trends.
- **Leaderboards:** Top performer tracking using optimized database aggregation.
- **Student Performance:** Personal progress tracking and quiz history.

### 💬 Engagement
- **Discussion Threads:** Course-specific forums for peer-to-peer and teacher-student interaction.
- **Comment System:** Nested comments for granular discussions.

---

## 🛠️ Tech Stack

- **Framework:** [Spring Boot 3.5.x](https://spring.io/projects/spring-boot)
- **Language:** Java 21
- **Security:** Spring Security 6.x (JJWT 0.12.6)
- **Database:** PostgreSQL (Production), H2 (Testing)
- **ORM:** Hibernate / Spring Data JPA
- **Documentation:** SpringDoc OpenAPI / Swagger UI
- **Utilities:** Lombok, Apache Commons Lang3
- **Task Scheduling:** Spring `@Async` for background processing
- **Mailing:** Spring Boot Starter Mail

---

## 🔧 Setup & Installation

### Prerequisites
- **JDK 21** or higher
- **Maven 3.x**
- **PostgreSQL** instance
- **Python 3.x** (for development log scripts)

### Environment Variables
Before running the application, ensure the following environment variables are set (or update them in `.env` / `application.properties`):

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `DB_USERNAME` | PostgreSQL Username | `postgres` |
| `DB_PASSWORD` | PostgreSQL Password | `password` |
| `JWT_SECRET_KEY` | HS256 Signing Key (min 256-bit) | `your_secure_random_key_here` |

### Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/studyflow.git
   cd studyflow
   ```

2. **Configure Database:**
   Ensure a database named `studyflow_db` exists in your PostgreSQL instance.

3. **Run the Application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access Swagger UI:**
   Explore and test the API at: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧪 Testing

The project includes a comprehensive testing suite focusing on integration and security:
- **Unit & Integration Tests:** Run using `./mvnw test`.
- **In-Memory Testing:** Uses H2 database for fast, isolated test cycles.
- **Security Validation:** Tests for RBAC, JWT validation, and edge-case prevention.

---

## 🛠️ Development Tools

### Automatic Development Log
The project includes a Python script `generate_log.py` that automatically updates `DEVELOPMENT_LOG.md` based on your Git commit history. This is triggered automatically during the Maven `compile` phase via the `exec-maven-plugin`.

To run it manually:
```bash
python generate_log.py
```

---

## 🐳 Docker Support

Build and run the application as a container:

1. **Build the JAR:**
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Build the Docker Image:**
   ```bash
   docker build -t studyflow-backend .
   ```

3. **Run the Container:**
   ```bash
   docker run -p 8080:8080 studyflow-backend
   ```

---

## 📌 Roadmap
- [ ] Real-time Notifications (WebSockets/Firebase)
- [ ] Redis Caching for Analytics Dashboards
- [ ] Decoupling into Microservices architecture
- [ ] Expanded Attendance Tracking module
- [ ] Multi-language support (i18n)
