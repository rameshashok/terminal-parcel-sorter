# Terminal Parcel Sorting System

Full-stack parcel sorting application with AI-powered RAG sorting decisions.

## Stack
- **Backend**: Quarkus 3.9 (REST, Hibernate ORM Panache, Kafka, OpenAI RAG)
- **Frontend**: Angular 19 + Angular Material
- **Database**: PostgreSQL 16
- **Messaging**: Apache Kafka
- **AI**: OpenAI GPT-4o-mini with RAG (sorting rules as context)

## Architecture

```
Angular UI → Quarkus REST API → PostgreSQL
                    ↓
              Kafka Producer → parcel-events topic
                    ↓
              Kafka Consumer → SortingService
                    ↓
              RAG Service → OpenAI API (with sorting rules context)
```

## Quick Start

### 1. Start infrastructure
```bash
docker-compose up -d
```

### 2. Set OpenAI API key
```bash
# Windows
set OPENAI_API_KEY=sk-your-key-here

# Linux/Mac
export OPENAI_API_KEY=sk-your-key-here
```

### 3. Start backend
```bash
cd backend/parcel-sorter
mvn quarkus:dev
```

### 4. Start frontend
```bash
cd frontend/parcel-sorter-ui
npm install
npm start
```

Open http://localhost:4200

## Features
- **Dashboard**: Real-time belt status and parcel counts by status
- **Parcel Registration**: Register incoming parcels with tracking info
- **Parcel List**: View all parcels, trigger sorting, dispatch
- **Sorting Rules**: Manage postal-code-based belt assignment rules (RAG knowledge base)
- **AI Assistant**: Chat with RAG-powered assistant about sorting decisions

## RAG Flow
1. Sorting rules stored in PostgreSQL
2. On sort request: rules fetched and injected as context into OpenAI prompt
3. AI decides belt assignment when no exact rule matches
4. Decision + reasoning stored on parcel record
