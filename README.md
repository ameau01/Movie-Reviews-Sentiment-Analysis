# AI-Powered Movie Sentiment Rating System
### CSCA-5028 Software Architecture for Big Data Applications 

This project aims to convert user-submitted movie reviews into rating scores using sentiment analysis from a Natural Language Processor. Movie ratings and reviews can be sold to online movie platforms, such as Netflix, to provide movie recommendations.

---
1. **What problem is the product aimed at solving?**
  This product aims to address the challenges of generating rating scores for movie review text.
2. **Who is the product geared towards (targeted audience)?**
  The primary target audience is **online movie platforms**, such as **Netflix** or **Amazon Prime**, to use these ratings in their recommender system.
3. **How is the product unique?**
  This product is unique in its application of **transfer learning**, where a pre-trained NLP model is **fine-tuned for the domain of movie reviews**.
---
## Project Components

1. **Frontend Server**  
   A Ktor web application to provide an HTML frontend to allow end-users to submit movie reviews and view sentiment analysis reports.
2. **Movie Review Data Collector**  
   A Ktor service responsible for downloading the Movie critics expert using an external API, and , and then persisting the sentiment analysis results in a NoSQL document store. The persisted data will be used in the report to display the top 10 movies. 
3. **Sentiment Data Analyzer**  
   A Ktor web service that loads a pre-trained Hugging Face NLP model (such as **DistilBERT**) to perform sentiment classification. The sentiment analysis result will be persisted in a collection in the document store. Data analyzer components accept inputs from both **interactive** and **batch jobs**. 
4. **Data Persistence**  
   A **NoSQL Document store** will be used within the entire system. It will store the expert movie reviews collected from the web API and all the sentiment analysis results for reporting purposes.
5. **Message Queue**
   Implemented RabbitMQ service to process data from external web API using publish/subscribe workers.
   
---

## Product Environment
**Google Cloud**### Technology stack

This codebase is written in a language called [Kotlin](https://kotlinlang.org) that is able to run on the JVM with full
Java compatibility.
It uses the [Ktor](https://ktor.io) web framework, and runs on the [Netty](https://netty.io/) web server.
HTML templates are written using [Freemarker](https://freemarker.apache.org).
The codebase is tested with [JUnit](https://junit.org/) and uses [Gradle](https://gradle.org) to build a jarfile.
