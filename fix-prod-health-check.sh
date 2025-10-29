#!/bin/bash

# Quick fix for production target group health check settings
# This updates the existing target group without redeploying infrastructure

set -e

ENV_NAME="prod"
AWS_REGION="ap-south-1"

echo "🔧 Fixing Production Target Group Health Check Settings"
echo "======================================================"

# Get the target group ARN from parameter store
TARGET_GROUP_ARN=$(aws ssm get-parameter \
  --region $AWS_REGION \
  --name /${ENV_NAME}/backend/target-group-arn \
  --query 'Parameter.Value' \
  --output text)

if [[ -z "$TARGET_GROUP_ARN" ]]; then
  echo "❌ Target group ARN not found in parameter store"
  exit 1
fi

echo "📍 Found target group: $TARGET_GROUP_ARN"

# Show current health check settings
echo ""
echo "🔍 Current Health Check Settings:"
aws elbv2 describe-target-groups \
  --region $AWS_REGION \
  --target-group-arns $TARGET_GROUP_ARN \
  --query 'TargetGroups[0].{HealthCheckPath:HealthCheckPath,HealthCheckIntervalSeconds:HealthCheckIntervalSeconds,HealthCheckTimeoutSeconds:HealthCheckTimeoutSeconds,UnhealthyThresholdCount:UnhealthyThresholdCount}' \
  --output table

echo ""
echo "🛠️ Updating health check settings..."

# Update health check settings to match infrastructure-setup-v3.yml
aws elbv2 modify-target-group \
  --region $AWS_REGION \
  --target-group-arn $TARGET_GROUP_ARN \
  --health-check-path "/api/actuator/health" \
  --health-check-interval-seconds 45 \
  --health-check-timeout-seconds 30 \
  --unhealthy-threshold-count 5 \
  --healthy-threshold-count 2

echo ""
echo "✅ Health check settings updated successfully!"

# Show new settings
echo ""
echo "🔍 New Health Check Settings:"
aws elbv2 describe-target-groups \
  --region $AWS_REGION \
  --target-group-arns $TARGET_GROUP_ARN \
  --query 'TargetGroups[0].{HealthCheckPath:HealthCheckPath,HealthCheckIntervalSeconds:HealthCheckIntervalSeconds,HealthCheckTimeoutSeconds:HealthCheckTimeoutSeconds,UnhealthyThresholdCount:UnhealthyThresholdCount,HealthyThresholdCount:HealthyThresholdCount}' \
  --output table

echo ""
echo "🎉 Production health checks should now work properly!"
echo "💡 The running ECS tasks should become healthy within 2-3 minutes."