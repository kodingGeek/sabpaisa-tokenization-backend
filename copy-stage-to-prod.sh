#!/bin/bash

# Copy stage target group settings exactly to prod
set -e

AWS_REGION="ap-south-1"

echo "🔄 Copying Stage Settings to Prod"
echo "================================="

echo ""
echo "🔍 Step 1: Getting Stage Target Group Settings..."

# Get stage target group settings
STAGE_SETTINGS=$(aws elbv2 describe-target-groups \
  --region $AWS_REGION \
  --names stage-backend-tg-80 \
  --query 'TargetGroups[0].{HealthCheckPath:HealthCheckPath,HealthCheckIntervalSeconds:HealthCheckIntervalSeconds,HealthCheckTimeoutSeconds:HealthCheckTimeoutSeconds,UnhealthyThresholdCount:UnhealthyThresholdCount,HealthyThresholdCount:HealthyThresholdCount}' \
  --output json)

if [ "$STAGE_SETTINGS" = "null" ] || [ -z "$STAGE_SETTINGS" ]; then
    echo "❌ Stage target group not found!"
    exit 1
fi

echo "✅ Stage settings found:"
echo "$STAGE_SETTINGS" | jq '.'

# Extract individual values
HEALTH_PATH=$(echo "$STAGE_SETTINGS" | jq -r '.HealthCheckPath')
HEALTH_INTERVAL=$(echo "$STAGE_SETTINGS" | jq -r '.HealthCheckIntervalSeconds')
HEALTH_TIMEOUT=$(echo "$STAGE_SETTINGS" | jq -r '.HealthCheckTimeoutSeconds')
UNHEALTHY_THRESHOLD=$(echo "$STAGE_SETTINGS" | jq -r '.UnhealthyThresholdCount')
HEALTHY_THRESHOLD=$(echo "$STAGE_SETTINGS" | jq -r '.HealthyThresholdCount')

echo ""
echo "📋 Stage Configuration:"
echo "Health Check Path: $HEALTH_PATH"
echo "Health Check Interval: $HEALTH_INTERVAL seconds"
echo "Health Check Timeout: $HEALTH_TIMEOUT seconds"
echo "Unhealthy Threshold: $UNHEALTHY_THRESHOLD"
echo "Healthy Threshold: $HEALTHY_THRESHOLD"

echo ""
echo "🔍 Step 2: Current Prod Settings..."

# Show current prod settings
aws elbv2 describe-target-groups \
  --region $AWS_REGION \
  --names prod-backend-tg-80 \
  --query 'TargetGroups[0].{HealthCheckPath:HealthCheckPath,HealthCheckIntervalSeconds:HealthCheckIntervalSeconds,HealthCheckTimeoutSeconds:HealthCheckTimeoutSeconds,UnhealthyThresholdCount:UnhealthyThresholdCount,HealthyThresholdCount:HealthyThresholdCount}' \
  --output table

echo ""
echo "🔧 Step 3: Applying Stage Settings to Prod..."

# Apply stage settings to prod
aws elbv2 modify-target-group \
  --region $AWS_REGION \
  --target-group-arn arn:aws:elasticloadbalancing:ap-south-1:152271395950:targetgroup/prod-backend-tg-80/c4d2893ae7a31e80 \
  --health-check-path "$HEALTH_PATH" \
  --health-check-interval-seconds $HEALTH_INTERVAL \
  --health-check-timeout-seconds $HEALTH_TIMEOUT \
  --unhealthy-threshold-count $UNHEALTHY_THRESHOLD \
  --healthy-threshold-count $HEALTHY_THRESHOLD

echo ""
echo "✅ Step 4: Verifying New Prod Settings..."

# Verify the changes
aws elbv2 describe-target-groups \
  --region $AWS_REGION \
  --names prod-backend-tg-80 \
  --query 'TargetGroups[0].{HealthCheckPath:HealthCheckPath,HealthCheckIntervalSeconds:HealthCheckIntervalSeconds,HealthCheckTimeoutSeconds:HealthCheckTimeoutSeconds,UnhealthyThresholdCount:UnhealthyThresholdCount,HealthyThresholdCount:HealthyThresholdCount}' \
  --output table

echo ""
echo "🎉 SUCCESS! Prod now has EXACTLY the same settings as Stage!"
echo ""
echo "⏰ Your deployment should stabilize within 5 minutes."
echo "🔍 Monitor ECS service: backend-service-prod"