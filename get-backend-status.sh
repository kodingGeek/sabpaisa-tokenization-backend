#!/bin/bash

# Get current backend deployment status and access URLs
echo "🔍 Backend Deployment Status"
echo "==========================="

AWS_REGION="ap-south-1"
ECS_CLUSTER="sabpaisa-tokenization-cluster"
ECS_SERVICE="backend-service-final"

# Get service status
echo "📊 Service Status:"
aws ecs describe-services \
  --cluster $ECS_CLUSTER \
  --services $ECS_SERVICE \
  --region $AWS_REGION \
  --query 'services[0].{status:status,desiredCount:desiredCount,runningCount:runningCount,pendingCount:pendingCount}' \
  --output table

# Get current task
TASK_ARN=$(aws ecs list-tasks \
  --cluster $ECS_CLUSTER \
  --service-name $ECS_SERVICE \
  --region $AWS_REGION \
  --query 'taskArns[0]' \
  --output text)

if [ ! -z "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ]; then
  echo -e "\n📦 Current Task:"
  echo "Task ARN: $TASK_ARN"
  
  # Get task status
  TASK_STATUS=$(aws ecs describe-tasks \
    --cluster $ECS_CLUSTER \
    --tasks $TASK_ARN \
    --region $AWS_REGION \
    --query 'tasks[0].lastStatus' \
    --output text)
  echo "Status: $TASK_STATUS"
  
  # Get public IP
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
      echo -e "\n🌐 Backend Access Information:"
      echo "=============================="
      echo "Public IP: $PUBLIC_IP"
      echo ""
      echo "📍 Endpoints:"
      echo "- Health: http://$PUBLIC_IP:8082/actuator/health"
      echo "- Swagger UI: http://$PUBLIC_IP:8082/swagger-ui.html"
      echo "- API Docs: http://$PUBLIC_IP:8082/v3/api-docs"
      echo "- H2 Console: http://$PUBLIC_IP:8082/h2-console"
      echo ""
      echo "🔧 API Endpoints:"
      echo "- Register: POST http://$PUBLIC_IP:8082/api/v1/auth/register"
      echo "- Login: POST http://$PUBLIC_IP:8082/api/v1/auth/login"
      echo "- Tokenize: POST http://$PUBLIC_IP:8082/api/v1/tokens/tokenize"
      echo ""
      echo "🧪 Test Health Endpoint:"
      echo "curl http://$PUBLIC_IP:8082/actuator/health"
      echo ""
      echo "📝 Note: The backend may take 2-3 minutes to fully start."
      echo "If endpoints are not accessible, check CloudWatch logs."
    fi
  fi
fi

echo -e "\n📋 AWS Console Links:"
echo "===================="
echo "CloudWatch Logs:"
echo "https://console.aws.amazon.com/cloudwatch/home?region=$AWS_REGION#logStream:group=/ecs/backend-final"
echo ""
echo "ECS Service:"
echo "https://console.aws.amazon.com/ecs/home?region=$AWS_REGION#/clusters/$ECS_CLUSTER/services/$ECS_SERVICE/tasks"