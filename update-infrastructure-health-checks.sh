#!/bin/bash

# Safe infrastructure update script
# Updates health check settings without recreating resources

set -e

ENV_NAME="prod"
AWS_REGION="ap-south-1"

echo "🔧 Safe Infrastructure Health Check Update"
echo "=========================================="

# Function to update target group health checks
update_target_group_health_checks() {
    local tg_name=$1
    local health_path=$2
    
    echo "Checking target group: $tg_name"
    
    # Get target group ARN
    TG_ARN=$(aws elbv2 describe-target-groups \
        --region $AWS_REGION \
        --names $tg_name \
        --query 'TargetGroups[0].TargetGroupArn' \
        --output text 2>/dev/null || echo "NOT_FOUND")
    
    if [ "$TG_ARN" = "NOT_FOUND" ]; then
        echo "❌ Target group $tg_name not found"
        return 1
    fi
    
    echo "📍 Found: $TG_ARN"
    
    # Show current settings
    echo "Current health check settings:"
    aws elbv2 describe-target-groups \
        --region $AWS_REGION \
        --target-group-arns $TG_ARN \
        --query 'TargetGroups[0].{Path:HealthCheckPath,Interval:HealthCheckIntervalSeconds,Timeout:HealthCheckTimeoutSeconds,Unhealthy:UnhealthyThresholdCount}' \
        --output table
    
    # Update health check settings
    echo "🛠️ Updating health check settings..."
    aws elbv2 modify-target-group \
        --region $AWS_REGION \
        --target-group-arn $TG_ARN \
        --health-check-path "$health_path" \
        --health-check-interval-seconds 45 \
        --health-check-timeout-seconds 30 \
        --unhealthy-threshold-count 5 \
        --healthy-threshold-count 2
    
    echo "✅ Updated $tg_name health check settings"
    
    # Store in parameter store for future reference
    aws ssm put-parameter \
        --region $AWS_REGION \
        --name "/${ENV_NAME}/infrastructure/health-check-updated" \
        --value "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        --type String \
        --overwrite
}

# Update backend target group
echo ""
echo "📱 Updating Backend Target Group..."
update_target_group_health_checks "${ENV_NAME}-backend-tg-80" "/api/actuator/health"

# Update frontend target group (if needed)
echo ""
echo "🌐 Updating Frontend Target Group..."
update_target_group_health_checks "${ENV_NAME}-frontend-tg-80" "/"

echo ""
echo "🎉 Infrastructure health check settings updated successfully!"
echo "💡 This is now consistent with infrastructure-setup-v3.yml"

# Create a marker file to indicate this was updated manually
cat > /tmp/health-check-update-log.txt << EOF
Infrastructure Health Check Update Log
=====================================
Date: $(date)
Environment: $ENV_NAME
Updated by: Manual script (update-infrastructure-health-checks.sh)
Reason: Align with infrastructure-setup-v3.yml settings
Changes:
- Backend health check path: /api/actuator/health
- Health check interval: 45 seconds
- Health check timeout: 30 seconds
- Unhealthy threshold: 5 attempts
- Healthy threshold: 2 attempts
EOF

echo ""
echo "📋 Update log created at /tmp/health-check-update-log.txt"