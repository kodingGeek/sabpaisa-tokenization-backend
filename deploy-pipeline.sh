#!/bin/bash

# Deploy script for backend application (runs in GitHub Actions)
set -e

echo "🚀 Starting backend deployment from pipeline..."

# Configuration
ENVIRONMENT=${1:-dev}
AWS_REGION="ap-south-1"

# Find backend instance
echo "🔍 Finding backend instance for environment: $ENVIRONMENT"
INSTANCE_ID=$(aws ec2 describe-instances \
  --filters \
    "Name=tag:Name,Values=sabpaisa-backend-$ENVIRONMENT" \
    "Name=tag:Type,Values=backend" \
    "Name=instance-state-name,Values=running" \
  --query "Reservations[0].Instances[0].InstanceId" \
  --output text \
  --region $AWS_REGION)

if [ "$INSTANCE_ID" = "None" ] || [ -z "$INSTANCE_ID" ]; then
  echo "❌ Backend instance not found!"
  echo "Please ensure the backend EC2 instance is running"
  exit 1
fi

echo "✅ Found instance: $INSTANCE_ID"

# Get instance IP
PUBLIC_IP=$(aws ec2 describe-instances \
  --instance-ids $INSTANCE_ID \
  --query "Reservations[0].Instances[0].PublicIpAddress" \
  --output text \
  --region $AWS_REGION)

echo "📍 Instance IP: $PUBLIC_IP"

# Check if WAR file exists (should be built by previous step)
WAR_FILE=$(ls target/*.war 2>/dev/null | head -1 || echo "")
if [ -z "$WAR_FILE" ]; then
  echo "❌ WAR file not found in target directory!"
  echo "Please ensure the build step completed successfully"
  exit 1
fi

echo "📦 Found WAR file: $WAR_FILE"

# Upload WAR to S3
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
S3_PATH="s3://sabpaisa-artifacts/$ENVIRONMENT/backend/app-$TIMESTAMP.war"

echo "📤 Uploading WAR to S3..."
aws s3 cp "$WAR_FILE" "$S3_PATH"
aws s3 cp "$WAR_FILE" "s3://sabpaisa-artifacts/$ENVIRONMENT/backend/app-latest.war"

echo "✅ WAR uploaded to S3"

# Deploy via SSM
echo "🚀 Deploying to EC2 instance..."

COMMAND_ID=$(aws ssm send-command \
  --instance-ids $INSTANCE_ID \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=[
    "echo \"Starting deployment...\"",
    "aws s3 cp s3://sabpaisa-artifacts/'$ENVIRONMENT'/backend/app-latest.war /tmp/app.war",
    "sudo systemctl stop tomcat || true",
    "sleep 5",
    "sudo rm -rf /opt/tomcat/webapps/ROOT*",
    "sudo cp /tmp/app.war /opt/tomcat/webapps/ROOT.war",
    "sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war",
    "sudo systemctl start tomcat",
    "echo \"Waiting for application to start...\"",
    "sleep 40",
    "curl -f http://localhost:8080/api/health || (sudo journalctl -u tomcat -n 50 && exit 1)",
    "echo \"Deployment completed successfully!\""
  ]' \
  --output text \
  --query "Command.CommandId" \
  --region $AWS_REGION)

echo "📋 SSM Command ID: $COMMAND_ID"
echo "⏳ Waiting for deployment to complete (this may take 2-3 minutes)..."

# Wait for command with timeout
aws ssm wait command-executed \
  --command-id $COMMAND_ID \
  --instance-id $INSTANCE_ID \
  --region $AWS_REGION || true

# Check status
STATUS=$(aws ssm get-command-invocation \
  --command-id $COMMAND_ID \
  --instance-id $INSTANCE_ID \
  --query "Status" \
  --output text \
  --region $AWS_REGION)

echo "📊 Deployment status: $STATUS"

if [ "$STATUS" = "Success" ]; then
  echo "✅ Deployment successful!"
  echo ""
  echo "🎉 Backend Application Deployed Successfully!"
  echo "📍 Instance: $INSTANCE_ID"
  echo "🌐 Public IP: $PUBLIC_IP"
  echo ""
  echo "🔗 Application URLs:"
  echo "   Health Check: http://$PUBLIC_IP:8080/api/health"
  echo "   API Base: http://$PUBLIC_IP:8080/api"
  echo ""
  echo "📝 To test the deployment:"
  echo "   curl http://$PUBLIC_IP:8080/api/health"
else
  echo "❌ Deployment failed!"
  echo "Getting error details..."
  
  ERROR_OUTPUT=$(aws ssm get-command-invocation \
    --command-id $COMMAND_ID \
    --instance-id $INSTANCE_ID \
    --query "StandardErrorContent" \
    --output text \
    --region $AWS_REGION)
    
  echo "Error output:"
  echo "$ERROR_OUTPUT"
  
  STANDARD_OUTPUT=$(aws ssm get-command-invocation \
    --command-id $COMMAND_ID \
    --instance-id $INSTANCE_ID \
    --query "StandardOutputContent" \
    --output text \
    --region $AWS_REGION)
    
  echo "Standard output:"
  echo "$STANDARD_OUTPUT"
  
  exit 1
fi