#!/bin/bash

# Simple backend deployment script
echo "🚀 Deploying Backend (Simplified)"
echo "================================"

AWS_REGION="ap-south-1"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Create log group first
echo "Creating CloudWatch log group..."
aws logs create-log-group --log-group-name /ecs/backend-simple --region $AWS_REGION 2>/dev/null || echo "Log group exists"

# Get VPC and subnets
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query 'Vpcs[0].VpcId' --output text --region $AWS_REGION)
SUBNETS=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].SubnetId' --output text --region $AWS_REGION)
SUBNET_1=$(echo $SUBNETS | cut -d' ' -f1)
SUBNET_2=$(echo $SUBNETS | cut -d' ' -f2)

# Create security group
SG_ID=$(aws ec2 create-security-group \
  --group-name backend-simple-$(date +%s) \
  --description "Simple backend SG" \
  --vpc-id $VPC_ID \
  --region $AWS_REGION \
  --query 'GroupId' \
  --output text)

# Allow all inbound traffic for testing
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 0-65535 \
  --cidr 0.0.0.0/0 \
  --region $AWS_REGION

# Create minimal task definition
cat > /tmp/simple-task.json <<EOF
{
  "family": "backend-simple",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::$ACCOUNT_ID:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "backend",
      "image": "893445410174.dkr.ecr.ap-south-1.amazonaws.com/sabpaisa-tokenization-backend:latest",
      "portMappings": [{
        "containerPort": 8082,
        "protocol": "tcp"
      }],
      "essential": true,
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "default"
        },
        {
          "name": "SERVER_PORT",
          "value": "8082"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/backend-simple",
          "awslogs-region": "$AWS_REGION",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
EOF

# Register task
aws ecs register-task-definition --cli-input-json file:///tmp/simple-task.json --region $AWS_REGION

# Delete old service
aws ecs delete-service --cluster sabpaisa-tokenization-cluster --service sabpaisa-tokenization-backend --force --region $AWS_REGION 2>/dev/null || true
echo "Waiting for old service cleanup..."
sleep 30

# Create new service
aws ecs create-service \
  --cluster sabpaisa-tokenization-cluster \
  --service-name backend-simple \
  --task-definition backend-simple \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_1],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --region $AWS_REGION

echo "Service created. Waiting for deployment..."
sleep 60

# Get task info
TASK_ARN=$(aws ecs list-tasks \
  --cluster sabpaisa-tokenization-cluster \
  --service-name backend-simple \
  --region $AWS_REGION \
  --query 'taskArns[0]' \
  --output text)

if [ ! -z "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ]; then
  echo "Task: $TASK_ARN"
  
  # Get logs
  echo -e "\n📝 Recent Logs:"
  STREAM=$(aws logs describe-log-streams \
    --log-group-name /ecs/backend-simple \
    --order-by LastEventTime \
    --descending \
    --max-items 1 \
    --region $AWS_REGION \
    --query 'logStreams[0].logStreamName' \
    --output text 2>/dev/null)
  
  if [ ! -z "$STREAM" ] && [ "$STREAM" != "None" ]; then
    aws logs get-log-events \
      --log-group-name /ecs/backend-simple \
      --log-stream-name "$STREAM" \
      --limit 20 \
      --region $AWS_REGION \
      --query 'events[*].message' \
      --output text | head -20
  fi
  
  # Try to get public IP
  ENI_ID=$(aws ecs describe-tasks \
    --cluster sabpaisa-tokenization-cluster \
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
      echo -e "\n✅ Backend IP: $PUBLIC_IP"
      echo "Test URL: http://$PUBLIC_IP:8082/actuator/health"
    fi
  fi
fi