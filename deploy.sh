#!/bin/bash

# Deploy script for backend application
set -e

echo "🚀 Starting backend deployment..."

# Configuration
ENVIRONMENT=${1:-dev}
AWS_REGION="ap-south-1"

# Find backend instance
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

# Build application
echo "🔨 Building application..."
mvn clean package -DskipTests

# Upload WAR to S3
WAR_FILE=$(ls target/*.war | head -1)
S3_PATH="s3://sabpaisa-artifacts/$ENVIRONMENT/backend/app-$(date +%Y%m%d-%H%M%S).war"

echo "📤 Uploading WAR to S3..."
aws s3 cp "$WAR_FILE" "$S3_PATH"
aws s3 cp "$WAR_FILE" "s3://sabpaisa-artifacts/$ENVIRONMENT/backend/app-latest.war"

# Deploy via SSM
echo "🚀 Deploying to EC2..."

COMMAND_ID=$(aws ssm send-command \
  --instance-ids $INSTANCE_ID \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=[
    "aws s3 cp s3://sabpaisa-artifacts/'$ENVIRONMENT'/backend/app-latest.war /tmp/app.war",
    "sudo systemctl stop tomcat || true",
    "sudo rm -rf /opt/tomcat/webapps/ROOT*",
    "sudo cp /tmp/app.war /opt/tomcat/webapps/ROOT.war",
    "sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war",
    "sudo systemctl start tomcat",
    "sleep 30",
    "curl -f http://localhost:8080/api/health || exit 1"
  ]' \
  --output text \
  --query "Command.CommandId" \
  --region $AWS_REGION)

echo "⏳ Waiting for deployment to complete..."
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

if [ "$STATUS" = "Success" ]; then
  echo "✅ Deployment successful!"
  echo "🌐 Application URL: http://$PUBLIC_IP:8080/api/health"
else
  echo "❌ Deployment failed!"
  aws ssm get-command-invocation \
    --command-id $COMMAND_ID \
    --instance-id $INSTANCE_ID \
    --query "StandardErrorContent" \
    --output text \
    --region $AWS_REGION
  exit 1
fi