#!/bin/bash

# Script to fix backend deployment with proper configuration
echo "🔧 Fixing Backend Deployment"
echo "==========================="

AWS_REGION="ap-south-1"
ECS_CLUSTER="sabpaisa-tokenization-cluster"
ECS_SERVICE="sabpaisa-tokenization-backend"
TASK_FAMILY="sabpaisa-tokenization-backend"
CONTAINER_NAME="backend"

# Get account ID
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Get VPC and subnets
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query 'Vpcs[0].VpcId' --output text --region $AWS_REGION)
SUBNETS=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].SubnetId' --output text --region $AWS_REGION)
SUBNET_1=$(echo $SUBNETS | cut -d' ' -f1)
SUBNET_2=$(echo $SUBNETS | cut -d' ' -f2)

# Get latest image from ECR
IMAGE_URI=$(aws ecr describe-repositories --repository-names sabpaisa-tokenization-backend --query 'repositories[0].repositoryUri' --output text --region $AWS_REGION):latest

# Create security group for backend
echo "Creating security group..."
SG_NAME="backend-sg-fixed-$(date +%s)"
SG_ID=$(aws ec2 create-security-group \
  --group-name $SG_NAME \
  --description "Backend security group with proper access" \
  --vpc-id $VPC_ID \
  --region $AWS_REGION \
  --query 'GroupId' \
  --output text)

# Allow traffic on port 8082 and database access
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 8082 \
  --cidr 0.0.0.0/0 \
  --region $AWS_REGION

# Allow all outbound traffic for external API calls
aws ec2 authorize-security-group-egress \
  --group-id $SG_ID \
  --protocol all \
  --cidr 0.0.0.0/0 \
  --region $AWS_REGION 2>/dev/null || true

echo "Security group created: $SG_ID"

# Create updated task definition with proper environment variables
cat > /tmp/backend-task-definition.json <<EOF
{
  "family": "$TASK_FAMILY",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::$ACCOUNT_ID:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "$CONTAINER_NAME",
      "image": "$IMAGE_URI",
      "portMappings": [{
        "containerPort": 8082,
        "protocol": "tcp"
      }],
      "essential": true,
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "docker,dev"
        },
        {
          "name": "SERVER_PORT",
          "value": "8082"
        },
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
        },
        {
          "name": "SPRING_DATASOURCE_USERNAME",
          "value": "sa"
        },
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "value": "password"
        },
        {
          "name": "SPRING_JPA_DATABASE_PLATFORM",
          "value": "org.hibernate.dialect.H2Dialect"
        },
        {
          "name": "SPRING_H2_CONSOLE_ENABLED",
          "value": "true"
        },
        {
          "name": "SPRING_REDIS_HOST",
          "value": "localhost"
        },
        {
          "name": "SPRING_REDIS_PORT",
          "value": "6379"
        },
        {
          "name": "REDIS_ENABLED",
          "value": "false"
        },
        {
          "name": "APP_SCHEDULING_ENABLED",
          "value": "false"
        },
        {
          "name": "JAVA_OPTS",
          "value": "-Xmx512m -Xms256m"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-create-group": "true",
          "awslogs-group": "/ecs/$ECS_SERVICE",
          "awslogs-region": "$AWS_REGION",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8082/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 10,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
EOF

echo "Registering new task definition..."
aws ecs register-task-definition --cli-input-json file:///tmp/backend-task-definition.json --region $AWS_REGION

# Update the service with new task definition
echo "Updating ECS service..."
aws ecs update-service \
  --cluster $ECS_CLUSTER \
  --service $ECS_SERVICE \
  --task-definition $TASK_FAMILY \
  --force-new-deployment \
  --region $AWS_REGION \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_1,$SUBNET_2],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --query 'service.serviceName' \
  --output text

echo "Service updated. Waiting for deployment..."
sleep 30

# Check deployment status
echo -e "\n📊 Checking deployment status..."
aws ecs describe-services \
  --cluster $ECS_CLUSTER \
  --services $ECS_SERVICE \
  --region $AWS_REGION \
  --query 'services[0].{status:status,desiredCount:desiredCount,runningCount:runningCount,pendingCount:pendingCount}' \
  --output table

# Get running task
echo -e "\nWaiting for task to start..."
sleep 60

TASK_ARN=$(aws ecs list-tasks \
  --cluster $ECS_CLUSTER \
  --service-name $ECS_SERVICE \
  --desired-status RUNNING \
  --region $AWS_REGION \
  --query 'taskArns[0]' \
  --output text)

if [ ! -z "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ]; then
  echo "Task running: $TASK_ARN"
  
  # Get task details and public IP
  ENI_ID=$(aws ecs describe-tasks \
    --cluster $ECS_CLUSTER \
    --tasks $TASK_ARN \
    --region $AWS_REGION \
    --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value | [0]' \
    --output text)
  
  if [ ! -z "$ENI_ID" ] && [ "$ENI_ID" != "None" ]; then
    PUBLIC_IP=$(aws ec2 describe-network-interfaces \
      --network-interface-ids $ENI_ID \
      --region $AWS_REGION \
      --query 'NetworkInterfaces[0].Association.PublicIp' \
      --output text)
    
    if [ ! -z "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "None" ]; then
      echo -e "\n✅ Backend Successfully Deployed!"
      echo "================================="
      echo "Public IP: $PUBLIC_IP"
      echo ""
      echo "🌐 Access URLs:"
      echo "=============="
      echo "1️⃣ Health Check:"
      echo "   http://$PUBLIC_IP:8082/actuator/health"
      echo ""
      echo "2️⃣ Swagger UI:"
      echo "   http://$PUBLIC_IP:8082/swagger-ui.html"
      echo "   http://$PUBLIC_IP:8082/swagger-ui/index.html"
      echo ""
      echo "3️⃣ H2 Console (for development):"
      echo "   http://$PUBLIC_IP:8082/h2-console"
      echo "   JDBC URL: jdbc:h2:mem:testdb"
      echo "   Username: sa"
      echo "   Password: password"
      echo ""
      echo "4️⃣ API Endpoints:"
      echo "   Base URL: http://$PUBLIC_IP:8082/api/v1"
      echo "   - Login: POST /api/v1/auth/login"
      echo "   - Register: POST /api/v1/auth/register"
      echo "   - Tokenize: POST /api/v1/tokens/tokenize"
      echo ""
      echo "📋 CloudWatch Logs:"
      echo "   https://console.aws.amazon.com/cloudwatch/home?region=$AWS_REGION#logStream:group=/ecs/$ECS_SERVICE"
      echo ""
      echo "🧪 Test Commands:"
      echo "================"
      echo "# Test health:"
      echo "curl http://$PUBLIC_IP:8082/actuator/health"
      echo ""
      echo "# Register user:"
      echo "curl -X POST http://$PUBLIC_IP:8082/api/v1/auth/register \\"
      echo "  -H 'Content-Type: application/json' \\"
      echo "  -d '{\"username\": \"testuser\", \"password\": \"Test123!\", \"email\": \"test@example.com\"}'"
    fi
  fi
else
  echo "❌ Task not running yet. Check CloudWatch logs for errors."
fi