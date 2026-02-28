# AI-Powered Movie Review → Rating Engine (Fine-Tuned NLP + Production-Grade ML Web Service)
### CSCA-5028 Software Architecture for Big Data Applications Project

A graduate software-architecture project that **fine-tunes a transformer-based NLP model for movie review sentiment / rating prediction**, then **serves the trained model behind a production-style microservice stack** to power a simple web UI and reporting workflow.

This repository is designed to demonstrate the **full ML-to-production path**:
- **Model adaptation (transfer learning / fine-tuning)** for the movie-review domain
- **Inference service** (predict + batch jobs)
- **Frontend web app** for interactive review scoring
- **Data ingestion** from an external movie review API
- **Persistence + async processing** (MongoDB + RabbitMQ)
- **Observability** (Prometheus + Grafana)
- **Containerized deployment** (Docker + Compose)

---
### Project Overview

This project aims to convert user-submitted movie reviews into rating scores using sentiment analysis from a Natural Language Processor. Movie ratings and reviews can be sold to online movie platforms, such as Netflix, to provide movie recommendations.

* **NLP Fine-Tuning:** Custom fine-tuning of **DistilBERT** (via Hugging Face) on movie review datasets to achieve state-of-the-art sentiment classification.
* **Production ML Engine:** A dedicated **Sentiment Data Analyzer** service that hosts the trained model and serves predictions via a RESTful API.
* **High-Concurrency Backend:** Built with **Ktor (Kotlin)** to ensure asynchronous, non-blocking request handling.
* **Hybrid Workflow Architecture:**
    * **Interactive:** Low-latency inference path utilizing **Redis** for caching frequent sentiment results.
    * **Scheduled:** Message-driven retraining and data ingestion pipeline powered by **RabbitMQ**.
  
---
### Tech Stack

| Category | Tools & Technologies |
| :--- | :--- |
| **Machine Learning** | Python, Hugging Face Transformers, PyTorch/TensorFlow, DistilBERT |
| **Backend / API** | Kotlin, Ktor (JVM), RESTful APIs |
| **Data & Cache** | Google Cloud Firestore (NoSQL), Redis |
| **Messaging** | RabbitMQ (Asynchronous Task Processing) |
| **Infrastructure** | Docker, Google Cloud Platform (GCP), GitHub Actions (CI/CD) |
| **Observability** | Prometheus, Grafana |

This codebase is written in a language called [Kotlin](https://kotlinlang.org) that is able to run on the JVM with full
Java compatibility.
It uses the [Ktor](https://ktor.io) web framework, and runs on the [Netty](https://netty.io/) web server.
HTML templates are written using [Freemarker](https://freemarker.apache.org).
The codebase is tested with [JUnit](https://junit.org/) and uses [Gradle](https://gradle.org) to build a jarfile.


---

### Architecture
Fine-tune a **DistilBERT** model on movie reviews using **transfer learning**, convert 5-class sentiment into **1–5 star ratings**, then **deploy the trained model as a scalable, production-ready ML inference engine** behind a modern web application.

The system powers real-time user reviews and batch critic analysis — exactly the kind of AI service used by Netflix, Amazon Prime, or Rotten Tomatoes for better recommendations.

- **End-to-end ML Engineering**: From domain-specific fine-tuning -> model export -> production serving in a JVM microservices stack
- **Real production architecture**: Async processing with RabbitMQ, Dockerized services, monitoring, multi-environment deployment
- **Not just a notebook** — a fully hosted, web-accessible ML engine

| Layer              | Technology                                      |
|--------------------|-------------------------------------------------|
| **ML Model**       | DistilBERT (Hugging Face Transformers) + Transfer Learning |
| **Dataset**        | SST-5 (Stanford Sentiment Treebank 5) — movie reviews |
| **Backend**        | Kotlin + **Ktor 2.x** + Netty                   |
| **ML Inference**   | Fine-tuned DistilBERT served inside Ktor service |
| **Messaging**      | **RabbitMQ** (publish/subscribe for batch & interactive jobs) |
| **Frontend**       | FreeMarker HTML templates                       |
| **Persistence**    | NoSQL Document Store (MongoDB-compatible)       |
| **Containerization**| Docker + docker-compose                         |
| **Monitoring**     | Prometheus + Grafana                            |
| **Build & CI**     | Gradle (Kotlin DSL), JUnit                      |
| **Cloud**          | Google Cloud + Heroku ready (Procfile)          |

---

### Model Development – Fine-Tuning Pipeline

- **Dataset**: `data/SST-5/` — 11,855 movie review sentences/phrases with 5 fine-grained sentiment labels
- **Model**: `distilbert-base-uncased` fine-tuned for **5-class sequence classification**
- **Technique**: Transfer learning (freeze lower layers → fine-tune classifier head on movie domain)
- **Output Mapping**: 
  - Very Negative → 1 star  
  - Negative → 2 stars  
  - Neutral → 3 stars  
  - Positive → 4 stars  
  - Very Positive → 5 stars
- **Why it matters**: Generic sentiment models fail on nuanced movie language; domain fine-tuning delivers production-quality ratings.
*(Fine-tuning performed with Hugging Face in Python; exported model integrated into the Kotlin inference service. Training scripts/notebooks available on request.)*

---
### System Architecture (Production ML Engine)

**Microservices Design** (all in Ktor):

1. **Frontend Server** – Clean web UI to submit reviews and see instant rating + explanation
2. **Sentiment Analyzer** – **Core ML service**: loads fine-tuned DistilBERT and returns rating in <100ms
3. **Movie Review Data Collector** – Pulls critic reviews from external APIs → publishes to queue
4. **RabbitMQ Broker** – Decouples services, supports batch processing and high throughput
5. **Persistence Layer** – Stores raw reviews + predicted ratings for reporting (Top 10 movies dashboard)

* Default ports (from docker-compose)
* Frontend UI: http://localhost/ (host port 80 → container 8080)
* Analyzer service: http://localhost:8881/
* Collector service: http://localhost:8882/
* RabbitMQ UI: http://localhost:15672/
* Prometheus: http://localhost:9090/
* Grafana: http://localhost:3000/
* MongoDB: mongodb://localhost:27017

Health checks
- Each service exposes a /health endpoint used by Compose healthchecks.
---

### Quick Start (Local)

Fully containerized — one `docker-compose up` starts everything (app + RabbitMQ + DB + monitoring).
```bash
git clone https://github.com/ameau01/Movie-Reviews-Sentiment-Analysis.git
cd Movie-Reviews-Sentiment-Analysis

# Start the entire production stack
docker-compose up --build
