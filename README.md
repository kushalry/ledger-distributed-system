# 🏦 Distributed Ledger API

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-black.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)

A highly concurrent, event-driven financial ledger REST API built to handle race conditions and reliable message delivery in distributed systems.

## 🏗️ Architecture & System Design



This system is engineered to solve two of the hardest problems in financial tech: **Concurrency** and **Reliable Event Streaming**.

### Core Engineering Patterns Implemented:
* **Concurrency Control (Pessimistic Locking):** Utilizes database-level row locking (`SELECT ... FOR UPDATE`) to guarantee absolute data integrity when multiple concurrent requests attempt to modify the same wallet balance simultaneously.
* **Transactional Outbox Pattern:** Ensures zero-data-loss event publishing. Instead of calling external services directly during a transaction (risking dual-write failures), domain events are saved to an `outbox` table within the same ACID transaction as the balance update.
* **Event-Driven Architecture:** A background polling publisher reads the outbox and securely streams payload events to an **Apache Kafka** broker for asynchronous downstream processing (e.g., Notification Services, Fraud Analytics).
* **SRE & Observability:** Fully instrumented with Micrometer, exposing JVM metrics, active threads, and API response times to **Prometheus** and visualizing the health of the matrix via a custom **Grafana** dashboard.

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Hibernate
* **Database:** PostgreSQL 15
* **Message Broker:** Apache Kafka (KRaft mode)
* **Observability:** Prometheus, Grafana
* **Infrastructure:** Docker, Docker Compose

---

## 🚀 Quick Start (Run Locally)

The entire infrastructure (Database, API, Message Broker, and Telemetry) is fully containerized. You do not need to install Postgres or Kafka locally.

### 1. Boot the Matrix
Ensure Docker Desktop is running, then execute:
```bash
docker compose up --build

### 2. Access the Endpoints
Once Tomcat starts on port 8080, navigate to the Swagger UI to test the endpoints:
```bash
Swagger UI: http://localhost:8080/swagger-ui.html

### 3. Verify the Event Stream
To prove the Outbox Pattern is successfully publishing to Kafka, open a new terminal and attach a consumer to the broker:
```bash
docker exec -it ledger-kafka /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ledger-events --from-beginning

Execute a transfer in Swagger, and watch the JSON payload instantly arrive in the terminal.

### 4. System Observability
Monitor the live health of the JVM and application throughput:
```bash
Grafana Dashboard: http://localhost:3000 (Login: admin / admin)
Navigate to Dashboards -> JVM (Micrometer).

🔒 API Endpoints
Method.   Endpoint.              Description 
POST.     /api/v1/transfers.     Executes an ACID-compliant money transfer
GET.      /actuator/prometheus.  Exposes raw telemetry data for scraping


