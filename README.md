# SabPaisa Tokenization Backend

Spring Boot backend service for the tokenization platform.

## Deployment

This repository includes GitHub Actions workflow for automated deployment:

1. Go to Actions tab
2. Select "Deploy Backend Application"
3. Click "Run workflow"
4. Choose environment (dev/staging/prod)

The workflow will:
- Build the application
- Deploy to EC2 instance
- Run health checks

## Local Development

```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

- Health: `/api/health`
- Tokenization: `/api/v1/tokenize`
- Detokenization: `/api/v1/detokenize`