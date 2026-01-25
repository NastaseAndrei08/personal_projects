# Energy Management System (Microservices)

A distributed energy management system built with Spring Boot, React, Docker, and RabbitMQ. This project demonstrates a microservices architecture for tracking smart meter data, featuring event-driven synchronization and real-time monitoring.

## 🏗 Architecture

The system consists of 4 isolated microservices, a frontend, and a simulator, orchestrated via Docker Compose:

* **M1 - Auth Service:** Handles JWT authentication and registration.
* **M2 - User Service:** Manages user profiles (Admin/Client) and broadcasts synchronization events via RabbitMQ.
* **M3 - Device Service:** Manages smart devices. It consumes user events to maintain a local consistency table, allowing decoupled validation.
* **M4 - Monitoring Service:** Consumes sensor data from RabbitMQ, aggregates hourly consumption, and provides visualization APIs.
* **Simulator:** A standalone Java app that generates realistic energy data (random walk algorithm) and pushes it to RabbitMQ.
* **Frontend:** A React application with an Admin Dashboard (CRUD operations) and Client Dashboard (Historical Charts).

## 🚀 Getting Started

### Prerequisites
* Docker & Docker Compose
* (Optional) Java 21 & Maven for local development

### Installation & Run
1.  **Clone the repository:**
    ```bash
    git clone <your-repo-url>
    cd ds2025_30243_nastase_andrei_assig1
    ```

2.  **Start with Docker Compose:**
    This command builds all images (Frontend, M1-M4, Simulator) and starts the infrastructure (Postgres, RabbitMQ, Traefik).
    ```bash
    docker-compose up -d --build
    ```

3.  **Access the Application:**
    * **Frontend:** [http://localhost:3000](http://localhost:3000)
    * **Traefik Dashboard:** [http://localhost:8081](http://localhost:8081)
    * **RabbitMQ Dashboard:** [http://localhost:15672](http://localhost:15672) (User: `guest`, Pass: `guest`)

### Default Credentials
* **Admin:** Register a new user with role `ADMIN`.
* **Client:** Register a new user with role `CLIENT`.

## 🧪 Testing the Flow

1.  **Create Data:** Log in as Admin. Create a **User** and a **Device**.
2.  **Assign:** Assign the Device to the User. (This triggers M3 validation via its synchronized local database).
3.  **Simulate:**
    * Copy the Device ID from the Admin panel.
    * Update `simulator/src/main/resources/config.properties`.
    * Run `docker-compose restart simulator`.
4.  **Visualize:** Log in as the Client. Click the device card to view the real-time energy chart.

## 🛠 Technology Stack
* **Backend:** Java 21, Spring Boot 3.3, Hibernate/JPA
* **Frontend:** React 18, Reactstrap, Recharts
* **Messaging:** RabbitMQ (Topic Exchange `sync_exchange`)
* **Database:** PostgreSQL (4 isolated instances)
* **Gateway:** Traefik Reverse Proxy
* **Security:** JWT (JSON Web Tokens) with ForwardAuth