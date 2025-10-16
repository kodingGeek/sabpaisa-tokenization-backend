# 🚀 Deployment Guide - SabPaisa Tokenization Platform

This guide explains how to deploy the SabPaisa tokenization platform with fixed AWS ALB DNS names and proper database connectivity.

## 📋 Overview

The deployment is now organized with:
- **Fixed ALB DNS names** that don't change between deployments
- **Automatic database connectivity** 
- **Environment-based deployment** (dev, stage, prod)
- **Path-based routing** through Application Load Balancer

### URL Structure:
- **Frontend**: `http://{alb-dns-name}/`
- **Backend API**: `http://{alb-dns-name}/api`

Where ALB DNS name follows the pattern: `sabpaisa-token-api-{env}-alb.{region}.elb.amazonaws.com`

## 🔧 Prerequisites

1. **AWS Secrets to Configure**:
   ```
   AWS_ACCESS_KEY_ID
   AWS_SECRET_ACCESS_KEY
   ```

2. **No Domain Required**: The platform uses AWS ALB DNS names, so you don't need to own any domain

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
- ✅ Create Application Load Balancer (ALB) with fixed DNS name
- ✅ Configure path-based routing (/api/* → backend, /* → frontend)
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
- ✅ Backend will be accessible at: `http://{alb-dns}/api`

### Step 3: Deploy Frontend

Finally, deploy the frontend:

1. Go to GitHub Actions
2. Select **"Deploy Frontend V2"**
3. Choose:
   - Environment: `dev` (same as infrastructure)
4. Run workflow

This will:
- ✅ Fetch ALB DNS automatically
- ✅ Build frontend with correct API endpoint
- ✅ Deploy to ECS
- ✅ Register with ALB target group
- ✅ Frontend will be accessible at: `http://{alb-dns}/`

## 🌐 Access URLs

After infrastructure setup completes, you'll get a fixed ALB DNS name like:
- Development: `sabpaisa-token-api-dev-alb.ap-south-1.elb.amazonaws.com`
- Staging: `sabpaisa-token-api-stage-alb.ap-south-1.elb.amazonaws.com`
- Production: `sabpaisa-token-api-prod-alb.ap-south-1.elb.amazonaws.com`

### Access Points:
- Frontend: `http://{alb-dns}/`
- Backend API: `http://{alb-dns}/api`
- Swagger UI: `http://{alb-dns}/api/swagger-ui.html`
- Health Check: `http://{alb-dns}/api/actuator/health`

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

### ALB Not Accessible:
- Check if ALB is healthy in AWS console
- Verify security groups allow HTTP traffic on port 80
- Check if target groups have healthy targets

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
   - Add SSL certificates to ALB (can use AWS Certificate Manager)
   - Create HTTPS listeners
   - Update security groups

2. **Fixed URLs**: The ALB DNS name remains constant across deployments, providing stable endpoints for your team and customers

3. **Database Backups**: RDS is configured with 7-day backup retention

4. **Scaling**: Services are set to 1 task. Increase for production.

5. **Environment Isolation**: Each environment has separate:
   - Database
   - ECS services
   - Security groups
   - ALB with unique DNS name

## 🔐 Security Considerations

1. Update security groups to restrict access
2. Use VPN or bastion host for database access
3. Enable AWS WAF on ALB for production
4. Implement proper authentication and authorization
5. Enable encryption at rest and in transit

---

For any issues, check the GitHub Actions logs and AWS CloudWatch logs for detailed error messages.