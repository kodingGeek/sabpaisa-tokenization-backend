#!/bin/bash

# Script to get backend deployment information
echo "🔍 Getting Backend Deployment Information"
echo "========================================"

# Variables
AWS_REGION="ap-south-1"
ECS_CLUSTER="sabpaisa-tokenization-cluster"
ECS_SERVICE="sabpaisa-tokenization-backend"

# Get service info
echo "📊 ECS Service Status:"
SERVICE_INFO=$(aws ecs describe-services \
  --cluster $ECS_CLUSTER \
  --services $ECS_SERVICE \
  --region $AWS_REGION \
  --query 'services[0]' 2>/dev/null)

if [ ! -z "$SERVICE_INFO" ] && [ "$SERVICE_INFO" != "null" ]; then
  RUNNING=$(echo $SERVICE_INFO | jq -r '.runningCount // 0')
  DESIRED=$(echo $SERVICE_INFO | jq -r '.desiredCount // 0')
  echo "- Running tasks: $RUNNING / $DESIRED"
  
  # Get task info
  TASK_ARN=$(aws ecs list-tasks \
    --cluster $ECS_CLUSTER \
    --service-name $ECS_SERVICE \
    --region $AWS_REGION \
    --query 'taskArns[0]' \
    --output text 2>/dev/null)
  
  if [ ! -z "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ]; then
    echo "- Task ARN: $TASK_ARN"
    
    # Get task details
    TASK_INFO=$(aws ecs describe-tasks \
      --cluster $ECS_CLUSTER \
      --tasks $TASK_ARN \
      --region $AWS_REGION \
      --query 'tasks[0]' 2>/dev/null)
    
    # Get ENI and public IP
    ENI_ID=$(echo $TASK_INFO | jq -r '.attachments[0].details[] | select(.name=="networkInterfaceId") | .value' 2>/dev/null)
    
    if [ ! -z "$ENI_ID" ] && [ "$ENI_ID" != "null" ]; then
      PUBLIC_IP=$(aws ec2 describe-network-interfaces \
        --network-interface-ids $ENI_ID \
        --region $AWS_REGION \
        --query 'NetworkInterfaces[0].Association.PublicIp' \
        --output text 2>/dev/null)
      
      if [ ! -z "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "None" ]; then
        echo ""
        echo "🌐 Backend Access URLs:"
        echo "======================"
        echo "Public IP: $PUBLIC_IP"
        echo ""
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
        echo "4️⃣ API Endpoints:"
        echo "   - Login: http://$PUBLIC_IP:8082/api/v1/auth/login"
        echo "   - Register: http://$PUBLIC_IP:8082/api/v1/auth/register"
        echo "   - Tokenize: http://$PUBLIC_IP:8082/api/v1/tokens/tokenize"
        echo ""
        echo "5️⃣ Actuator Endpoints:"
        echo "   - Info: http://$PUBLIC_IP:8082/actuator/info"
        echo "   - Metrics: http://$PUBLIC_IP:8082/actuator/metrics"
        echo ""
        echo "📝 Default Credentials:"
        echo "   Username: admin"
        echo "   Password: admin123"
      else
        echo "❌ Could not get public IP. Task might still be starting."
      fi
    fi
  fi
else
  echo "❌ Service not found or not running"
fi

echo ""
echo "📋 AWS Console Links:"
echo "===================="
echo "1️⃣ ECS Service (Tasks & Status):"
echo "   https://console.aws.amazon.com/ecs/home?region=$AWS_REGION#/clusters/$ECS_CLUSTER/services/$ECS_SERVICE/tasks"
echo ""
echo "2️⃣ CloudWatch Logs (Application Logs):"
echo "   https://console.aws.amazon.com/cloudwatch/home?region=$AWS_REGION#logStream:group=/ecs/$ECS_SERVICE"
echo ""
echo "3️⃣ ECS Task Details:"
echo "   https://console.aws.amazon.com/ecs/home?region=$AWS_REGION#/clusters/$ECS_CLUSTER/tasks"
echo ""

# Test the endpoints
if [ ! -z "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "None" ]; then
  echo ""
  echo "🧪 Testing Backend Endpoints..."
  echo "=============================="
  
  echo -n "Health Check: "
  if curl -s -f "http://$PUBLIC_IP:8082/actuator/health" > /dev/null 2>&1; then
    echo "✅ Healthy"
  else
    echo "❌ Not responding (might still be starting)"
  fi
  
  echo ""
  echo "💡 Quick Test Commands:"
  echo "======================"
  echo "# Test health:"
  echo "curl http://$PUBLIC_IP:8082/actuator/health | jq ."
  echo ""
  echo "# Login (get JWT token):"
  echo "curl -X POST http://$PUBLIC_IP:8082/api/v1/auth/login \\"
  echo "  -H 'Content-Type: application/json' \\"
  echo "  -d '{\"username\": \"admin\", \"password\": \"admin123\"}' | jq ."
  echo ""
  echo "# View Swagger UI in browser:"
  echo "open http://$PUBLIC_IP:8082/swagger-ui.html"
fi

echo ""
echo "📌 Note: If the backend is not responding:"
echo "- Wait 2-3 minutes for the service to fully start"
echo "- Check CloudWatch logs for any errors"
echo "- Ensure the task is in RUNNING state in ECS console"