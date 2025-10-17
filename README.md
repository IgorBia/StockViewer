# StockViewer
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk)
![Spring](https://img.shields.io/badge/Spring_3.4.5-6DB33F?style=flat-square&logo=spring)
![Go](https://img.shields.io/badge/Go-1.21+-00ADD8?style=flat-square&logo=go)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=flat-square&logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)

Lightweight asset market viewer for visualizing candlestick charts and paper trading.


## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Features](#features)
- [Project Structure](#project-structure)
- [Documentation](#documentation)


## Overview

**StockViewer** is a modular, cross-language application (Java and Go) designed for efficient visualization of market data and strategy prototyping. The project enables users to render candlestick charts and practice trading in a risk-free environment. 

## Installation

### Using Docker Compose

1. Make sure you have Docker & docker-compose installed.

2. Create a .env file with enviromental variables e.g. (or copy .env-example):

    ```
    DB_HOST=postgres
    DB_PORT=5432
    DB_USER=user
    POSTGRES_USER=user
    DB_PASSWORD=password
    POSTGRES_DB=stockviewer
    DB_NAME=stockviewer
    POSTGRES_PASSWORD=password
    JWT_SECRET=esESFSEFESfesfsefSEFSEFSEFSef425245543544ji24j4j242ngK1kn12nfkeafn53342iiSFESfijakkna32532jjsEEEsjesesjjoojimni
    
    KAFKA_ENABLE_KRAFT=yes
    KAFKA_CFG_PROCESS_ROLES=broker,controller
    KAFKA_CFG_NODE_ID=1
    KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@kafka:9094
    KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
    KAFKA_KRAFT_METADATA_DIR=/bitnami/kafka/data/kraft
    
    KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9094
    KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
    KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT
    KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
    
    KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=1
    KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1
    KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR=1
    
    KAFKA_CREATE_TOPICS=indicator_events1:1
    ```

3. Run:
   ```bash
   docker compose up --build -d
   ```

## Features

- **Candlestick Chart Visualization:**  
  Interactive charts for market analysis.

- **Paper Trading:**  
  Execute simulated trades to test strategies without financial risk.

- **Asset watchlist:**  
  Keep your favourite assets close by adding them to the watchlist and seeing them on the main page.

## Project Structure

The project contains following parts:
  
  | Technology | Description |
|-----------|-------------|
| ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ⁺ ![Spring](https://img.shields.io/badge/Spring-6DB33F?style=flat-square&logo=spring&logoColor=white) | REST API |
| ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white) | Relational Database |
| ![Go](https://img.shields.io/badge/Go-00ADD8?style=flat-square&logo=go&logoColor=white) | DB maintenance service |


#### Diagram:

  ![image](https://github.com/user-attachments/assets/54f757fd-a5fc-4d15-994e-2468d11fab6b)

## Documentation

 Full documentation is available in documentation folder, it contains:
  - UML diagrams

  - ERD diagram

  - Requirements

  - Mockup

  - Endpoints

  - SQL schema script
  