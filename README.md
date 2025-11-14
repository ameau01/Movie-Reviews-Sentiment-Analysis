# AI-Powered Movie Sentiment Rating System
## CSCA-5028 Software Architecture for Big Data Applications

**CSCA-5028: Software Architecture for Big Data Applications**

This project aims to convert user-submitted movie reviews into rating scores using sentiment analysis from a Natural Language Processor. Movie ratings and reviews can be sold to online movie platforms, such as Netflix, to provide movie recommendations.

---
1. What problem is the product aimed at solving?
This product aims to address the challenges of generating accurate rating scores for movie review text.
2. Who is the product geared towards (targeted audience)?
The primary target audience is **online movie platforms**, such as **Netflix** or **Amazon Prime**, to use these ratings in their recommender system.
3. How is the product unique?
This product is unique in its application of **transfer learning**, where a pre-trained NLP model is **fine-tuned for the domain of movie reviews**.
---
## Project Components

1. **Frontend Server**  
   A Ktor web application to provide an HTML frontend to allow end-users to submit movie reviews and view sentiment analysis reports.
2. **Movie Review Data Collector**  
   A Ktor service responsible for downloading the raw movie review dataset online, ingesting the data into a simple JSON format, and then persisting it in a NoSQL document store. The persisted data will be used in both fine-tuning training and the evaluation of the prediction from the data analyzer model.
3. **Sentiment Data Analyzer**  
   A Ktor web service that loads a pre-trained Hugging Face NLP model (such as **DistilBERT**) to perform sentiment classification. The sentiment analysis result will be persisted in a collection in the document store. Data analyzer components accept inputs from both **interactive** and **batch jobs**. Priority will be given to the `/analyze` RESTful API endpoints over the Pub/Sub batch job.
4. **Data Persistence**  
   A **NoSQL Document store** will be used within the entire system. It will store the ingested dataset collected from **Kaggle**, as well as all the sentiment analysis results for reporting purposes.
---
## Product Environment
**Google Cloud**### Technology stack

This codebase is written in a language called [Kotlin](https://kotlinlang.org) that is able to run on the JVM with full
Java compatibility.
It uses the [Ktor](https://ktor.io) web framework, and runs on the [Netty](https://netty.io/) web server.
HTML templates are written using [Freemarker](https://freemarker.apache.org).
The codebase is tested with [JUnit](https://junit.org/) and uses [Gradle](https://gradle.org) to build a jarfile.

## Getting Started

1.  Build a Java Archive (jar) file.
    ```bash
    ./gradlew clean build
    ```

1.  Configure the port that each server runs on.
    ```bash
    export PORT=8881
    ```

1.  Run the servers locally using the below examples.

    ```bash
    java -jar applications/basic-server/build/libs/basic-server-1.0-SNAPSHOT.jar
    ```

    Data collector

    ```bash
    java -jar applications/data-collector-server/build/libs/data-collector-server-1.0-SNAPSHOT.jar
    ```

    Data analyzer
    
    ```bash
    java -jar applications/data-analyzer-server/build/libs/data-analyzer-server-1.0-SNAPSHOT.jar
    ```
    
## Running with Docker

1. Build with Docker.

    ```bash
    docker build -t kotlin-ktor-starter . --platform linux/amd64
    ```

1.  Run with docker.

    ```bash
    docker run -e PORT=8881 -p 8881:8881 kotlin-ktor-starter
    ```

That's a wrap for now.
