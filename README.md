# Ottero

Invoicing and business management platform.

## Structure

```
ottero/
├── frontend/    # React app (TypeScript, Vite, Tailwind)
├── backend/     # Spring Boot API (Java 21)
└── infra/       # Infrastructure (K8s, Docker Compose)
```

## Quick Start

### Prerequisites
- Node.js 20+
- Java 21+
- Docker

### Local Development

1. Start MySQL:
```bash
cd infra/local
docker-compose up -d
```

2. Start backend:
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

3. Start frontend:
```bash
cd frontend
npm install
npm run dev
```

Frontend: http://localhost:5173
Backend: http://localhost:8080/api

## Deployment

### Manual Deploy
```bash
# Apply K8s manifests
kubectl apply -f infra/k8s/
```

### CI/CD
- Push to `main` triggers builds
- Use "Deploy to Kubernetes" workflow for production deploy

## Environment

- **Production**: ottero.com.au
- **K8s Context**: ottero-prod
- **Auth**: Auth0 (ottero.au.auth0.com)
