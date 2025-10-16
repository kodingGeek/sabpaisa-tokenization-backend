# 🚀 Deployment Guide - SabPaisa Tokenization Platform

This guide explains how to deploy the SabPaisa tokenization platform with fixed DNS names and proper database connectivity.

## 📋 Overview

The deployment is now organized with:
- **Fixed DNS names** for each environment
- **Automatic database connectivity** 
- **Environment-based deployment** (dev, stage, prod)

### DNS Structure:
- **Backend**: `tokenization-{env}.api.sabpaisa.in`
- **Frontend**: `tokenization-{env}.sabpaisa.in`

Where `{env}` is: `dev`, `stage`, or `prod`

## 🔧 Prerequisites

1. **AWS Secrets to Configure**:
   ```
   AWS_ACCESS_KEY_ID
   AWS_SECRET_ACCESS_KEY
   HOSTED_ZONE_ID  # Route53 hosted zone ID for sabpaisa.in domain
   ```

2. **Route53 Hosted Zone**: You need a Route53 hosted zone for `sabpaisa.in` domain

## 📦 Deployment Process

### Step 1: Infrastructure Setup (One-time per environment)

Run the **Infrastructure Setup** workflow first:

1. Go to GitHub Actions
2. Select **"Infrastructure Setup"**
3. Choose:
   - Environment: `dev` (or `stage`/`prod`)
   - Action: `create`
4. Run workflow

This will:
- ✅ Create RDS PostgreSQL database
- ✅ Create Application Load Balancer (ALB)
- ✅ Configure Route53 DNS records
- ✅ Set up target groups for backend and frontend
- ✅ Store all configuration in AWS Parameter Store

**Note**: This step takes about 10-15 minutes as it creates the RDS instance.

### Step 2: Deploy Backend

After infrastructure is ready, deploy the backend:

1. Go to GitHub Actions
2. Select **"Deploy Backend V2"**
3. Choose:
   - Environment: `dev` (same as infrastructure)
4. Run workflow

This will:
- ✅ Fetch database connection details automatically
- ✅ Test database connectivity
- ✅ Build and deploy backend with proper database configuration
- ✅ Register with ALB target group
- ✅ Backend will be accessible at: `http://tokenization-dev.api.sabpaisa.in`

### Step 3: Deploy Frontend

Finally, deploy the frontend:

1. Go to GitHub Actions
2. Select **"Deploy Frontend V2"**
3. Choose:
   - Environment: `dev` (same as infrastructure)
4. Run workflow

This will:
- ✅ Fetch backend URL automatically
- ✅ Build frontend with correct API endpoint
- ✅ Deploy to ECS
- ✅ Register with ALB target group
- ✅ Frontend will be accessible at: `http://tokenization-dev.sabpaisa.in`

## 🌐 Access URLs

### Development Environment:
- Frontend: `http://tokenization-dev.sabpaisa.in`
- Backend API: `http://tokenization-dev.api.sabpaisa.in`
- Swagger UI: `http://tokenization-dev.api.sabpaisa.in/swagger-ui.html`

### Staging Environment:
- Frontend: `http://tokenization-stage.sabpaisa.in`
- Backend API: `http://tokenization-stage.api.sabpaisa.in`
- Swagger UI: `http://tokenization-stage.api.sabpaisa.in/swagger-ui.html`

### Production Environment:
- Frontend: `http://tokenization.sabpaisa.in`
- Backend API: `http://tokenization.api.sabpaisa.in`
- Swagger UI: `http://tokenization.api.sabpaisa.in/swagger-ui.html`

## 🗄️ Database Details

- **Engine**: PostgreSQL
- **Instance Class**: db.t3.micro (can be upgraded for production)
- **Database Name**: `sabpaisa_tokenization`
- **Username**: `postgres`
- **Password**: Stored in AWS Secrets Manager as `sabpaisa-tokenization-{env}-password`

## 🔍 Monitoring

### CloudWatch Logs:
- Backend: `/ecs/backend-service-{env}`
- Frontend: `/ecs/frontend-service-{env}`

### ECS Services:
- Backend: `backend-service-{env}`
- Frontend: `frontend-service-{env}`

## 🛠️ Troubleshooting

### DNS Not Resolving:
- Check if Route53 hosted zone ID is configured in GitHub secrets
- DNS propagation can take 5-10 minutes
- Verify ALB is healthy in AWS console

### Backend Can't Connect to Database:
- Check CloudWatch logs for connection errors
- Verify security group allows PostgreSQL (port 5432)
- Ensure database is in "available" state

### Services Not Starting:
- Check ECS task logs in CloudWatch
- Verify IAM roles have proper permissions
- Check if container health checks are passing

## 🗑️ Cleanup

To destroy infrastructure for an environment:

1. Run **Infrastructure Setup** workflow
2. Choose:
   - Environment: `dev` (environment to destroy)
   - Action: `destroy`

**Warning**: This will delete the database and all data!

## 📝 Important Notes

1. **HTTPS Setup**: Currently using HTTP. For production, you'll need to:
   - Add SSL certificates to ALB
   - Create HTTPS listeners
   - Update security groups

2. **Database Backups**: RDS is configured with 7-day backup retention

3. **Scaling**: Services are set to 1 task. Increase for production.

4. **Environment Isolation**: Each environment has separate:
   - Database
   - ECS services
   - Security groups
   - DNS records

## 🔐 Security Considerations

1. Update security groups to restrict access
2. Use VPN or bastion host for database access
3. Enable AWS WAF on ALB for production
4. Implement proper authentication and authorization
5. Enable encryption at rest and in transit

---

For any issues, check the GitHub Actions logs and AWS CloudWatch logs for detailed error messages.