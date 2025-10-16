#!/bin/bash

# Final backend deployment with AWS profile
echo "🚀 Deploying Backend Service (Final)"
echo "===================================="

AWS_REGION="ap-south-1"
ECS_CLUSTER="sabpaisa-tokenization-cluster"
ECS_SERVICE="backend-service-final"
TASK_FAMILY="backend-final"
CONTAINER_NAME="backend"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Get VPC and subnets
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query 'Vpcs[0].VpcId' --output text --region $AWS_REGION)
SUBNETS=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].SubnetId' --output text --region $AWS_REGION)
SUBNET_1=$(echo $SUBNETS | cut -d' ' -f1)
SUBNET_2=$(echo $SUBNETS | cut -d' ' -f2)

# Create log group
aws logs create-log-group --log-group-name /ecs/backend-final --region $AWS_REGION 2>/dev/null || echo "Log group exists"

# Create security group
SG_ID=$(aws ec2 create-security-group \
  --group-name backend-final-sg-$(date +%s) \
  --description "Backend final security group" \
  --vpc-id $VPC_ID \
  --region $AWS_REGION \
  --query 'GroupId' \
  --output text)

# Allow all inbound traffic
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 0-65535 \
  --cidr 0.0.0.0/0 \
  --region $AWS_REGION

echo "Security group: $SG_ID"

# Create task definition with AWS profile
cat > /tmp/backend-final-task.json <<EOF
{
  "family": "$TASK_FAMILY",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::$ACCOUNT_ID:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "$CONTAINER_NAME",
      "image": "893445410174.dkr.ecr.ap-south-1.amazonaws.com/sabpaisa-tokenization-backend:aws-profile",
      "portMappings": [{
        "containerPort": 8082,
        "protocol": "tcp"
      }],
      "essential": true,
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "aws"
        },
        {
          "name": "SERVER_PORT",
          "value": "8082"
        },
        {
          "name": "MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE",
          "value": "health,info,metrics"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/backend-final",
          "awslogs-region": "$AWS_REGION",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
EOF

# Register task definition
aws ecs register-task-definition --cli-input-json file:///tmp/backend-final-task.json --region $AWS_REGION

# Delete any old services
aws ecs delete-service --cluster $ECS_CLUSTER --service sabpaisa-tokenization-backend --force --region $AWS_REGION 2>/dev/null || true
aws ecs delete-service --cluster $ECS_CLUSTER --service backend-simple --force --region $AWS_REGION 2>/dev/null || true
echo "Waiting for old services to terminate..."
sleep 30

# Create the service
aws ecs create-service \
  --cluster $ECS_CLUSTER \
  --service-name $ECS_SERVICE \
  --task-definition $TASK_FAMILY \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_1,$SUBNET_2],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --region $AWS_REGION

echo "Service created. Waiting for deployment..."
sleep 60

# Check status
echo -e "\n📊 Service Status:"
aws ecs describe-services \
  --cluster $ECS_CLUSTER \
  --services $ECS_SERVICE \
  --region $AWS_REGION \
  --query 'services[0].{status:status,desiredCount:desiredCount,runningCount:runningCount,pendingCount:pendingCount}' \
  --output table

# Get task info
TASK_ARN=$(aws ecs list-tasks \
  --cluster $ECS_CLUSTER \
  --service-name $ECS_SERVICE \
  --region $AWS_REGION \
  --query 'taskArns[0]' \
  --output text)

if [ ! -z "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ]; then
  echo -e "\nTask ARN: $TASK_ARN"
  
  # Get logs
  echo -e "\n📝 Recent Logs:"
  STREAM=$(aws logs describe-log-streams \
    --log-group-name /ecs/backend-final \
    --order-by LastEventTime \
    --descending \
    --max-items 1 \
    --region $AWS_REGION \
    --query 'logStreams[0].logStreamName' \
    --output text 2>/dev/null)
  
  if [ ! -z "$STREAM" ] && [ "$STREAM" != "None" ]; then
    aws logs get-log-events \
      --log-group-name /ecs/backend-final \
      --log-stream-name "$STREAM" \
      --limit 30 \
      --region $AWS_REGION \
      --query 'events[*].message' \
      --output text | tail -20
  fi
  
  # Try to get public IP
  sleep 30
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
      echo "🌐 Backend Access URLs:"
      echo "======================"
      echo "1️⃣ Health Check:"
      echo "   http://$PUBLIC_IP:8082/actuator/health"
      echo ""
      echo "2️⃣ Swagger UI:"
      echo "   http://$PUBLIC_IP:8082/swagger-ui.html"
      echo "   http://$PUBLIC_IP:8082/swagger-ui/index.html"
      echo ""
      echo "3️⃣ API Documentation:"
      echo "   http://$PUBLIC_IP:8082/v3/api-docs"
      echo ""
      echo "4️⃣ H2 Console (Development):"
      echo "   http://$PUBLIC_IP:8082/h2-console"
      echo ""
      echo "5️⃣ API Endpoints:"
      echo "   - Register: POST http://$PUBLIC_IP:8082/api/v1/auth/register"
      echo "   - Login: POST http://$PUBLIC_IP:8082/api/v1/auth/login"
      echo "   - Tokenize: POST http://$PUBLIC_IP:8082/api/v1/tokens/tokenize"
      echo ""
      echo "📋 CloudWatch Logs:"
      echo "https://console.aws.amazon.com/cloudwatch/home?region=$AWS_REGION#logStream:group=/ecs/backend-final"
      echo ""
      echo "📊 ECS Service Console:"
      echo "https://console.aws.amazon.com/ecs/home?region=$AWS_REGION#/clusters/$ECS_CLUSTER/services/$ECS_SERVICE/tasks"
    fi
  fi
fi