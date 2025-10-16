#!/bin/bash

# Script to check backend deployment status and logs
echo "🔍 Checking Backend Deployment Status"
echo "===================================="

AWS_REGION="ap-south-1"
ECS_CLUSTER="sabpaisa-tokenization-cluster"
ECS_SERVICE="sabpaisa-tokenization-backend"

# Get service details
echo "📊 Service Status:"
aws ecs describe-services \
  --cluster $ECS_CLUSTER \
  --services $ECS_SERVICE \
  --region $AWS_REGION \
  --query 'services[0].{status:status,desiredCount:desiredCount,runningCount:runningCount,pendingCount:pendingCount,deployments:deployments[0].{status:status,taskDefinition:taskDefinition,desiredCount:desiredCount,runningCount:runningCount}}' \
  --output json | jq .

# Get stopped tasks
echo -e "\n❌ Failed Tasks:"
STOPPED_TASKS=$(aws ecs list-tasks \
  --cluster $ECS_CLUSTER \
  --service-name $ECS_SERVICE \
  --desired-status STOPPED \
  --region $AWS_REGION \
  --query 'taskArns' \
  --output json)

if [ "$STOPPED_TASKS" != "[]" ] && [ "$STOPPED_TASKS" != "null" ]; then
  # Get the most recent stopped task
  LATEST_STOPPED=$(echo $STOPPED_TASKS | jq -r '.[0]')
  
  if [ ! -z "$LATEST_STOPPED" ] && [ "$LATEST_STOPPED" != "null" ]; then
    echo "Analyzing task: $LATEST_STOPPED"
    
    # Get task details
    aws ecs describe-tasks \
      --cluster $ECS_CLUSTER \
      --tasks "$LATEST_STOPPED" \
      --region $AWS_REGION \
      --query 'tasks[0].{stopCode:stopCode,stoppedReason:stoppedReason,containers:containers[0].{exitCode:exitCode,reason:reason}}' | jq .
  fi
fi

# Get CloudWatch log streams
echo -e "\n📝 Recent CloudWatch Logs:"
LOG_GROUP="/ecs/$ECS_SERVICE"

# Check if log group exists
aws logs describe-log-groups --log-group-name-prefix $LOG_GROUP --region $AWS_REGION --query 'logGroups[0].logGroupName' --output text 2>/dev/null

# Get latest log stream
LATEST_STREAM=$(aws logs describe-log-streams \
  --log-group-name $LOG_GROUP \
  --order-by LastEventTime \
  --descending \
  --max-items 1 \
  --region $AWS_REGION \
  --query 'logStreams[0].logStreamName' \
  --output text 2>/dev/null)

if [ ! -z "$LATEST_STREAM" ] && [ "$LATEST_STREAM" != "None" ]; then
  echo "Latest log stream: $LATEST_STREAM"
  
  # Get logs from the latest stream
  aws logs get-log-events \
    --log-group-name $LOG_GROUP \
    --log-stream-name "$LATEST_STREAM" \
    --limit 50 \
    --region $AWS_REGION \
    --query 'events[*].message' \
    --output text 2>/dev/null | head -30
else
  echo "No log streams found"
fi

# Check task definition
echo -e "\n🔧 Task Definition Check:"
TASK_DEF=$(aws ecs describe-task-definition \
  --task-definition $ECS_SERVICE \
  --region $AWS_REGION \
  --query 'taskDefinition.{family:family,revision:revision,cpu:cpu,memory:memory,containerDefinitions:containerDefinitions[0].{name:name,image:image,portMappings:portMappings,environment:environment}}' | jq .)

echo "$TASK_DEF"

# Check ECR image
echo -e "\n🐳 ECR Image Status:"
aws ecr describe-images \
  --repository-name sabpaisa-tokenization-backend \
  --region $AWS_REGION \
  --query 'sort_by(imageDetails,&imagePushedAt)[-3:].{digest:imageDigest,tags:imageTags,pushedAt:imagePushedAt,size:imageSizeInBytes}' \
  --output table

echo -e "\n💡 Troubleshooting Tips:"
echo "1. Check if the container is failing health checks"
echo "2. Verify database connectivity from ECS"
echo "3. Ensure all required environment variables are set"
echo "4. Check if the application starts locally with the same Docker image"