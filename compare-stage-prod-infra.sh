#!/bin/bash

# Compare stage and prod infrastructure configurations
set -e

AWS_REGION="ap-south-1"

echo "🔍 Comparing Stage vs Prod Infrastructure"
echo "========================================"

compare_target_group() {
    local env=$1
    echo ""
    echo "📊 $env Environment:"
    echo "-------------------"
    
    # Get target group ARN
    TG_ARN=$(aws ssm get-parameter \
        --region $AWS_REGION \
        --name /${env}/backend/target-group-arn \
        --query 'Parameter.Value' \
        --output text 2>/dev/null || echo "NOT_FOUND")
    
    if [ "$TG_ARN" = "NOT_FOUND" ]; then
        echo "❌ Target group not found for $env"
        return
    fi
    
    echo "Target Group ARN: $TG_ARN"
    
    # Get health check settings
    echo ""
    echo "Health Check Settings:"
    aws elbv2 describe-target-groups \
        --region $AWS_REGION \
        --target-group-arns $TG_ARN \
        --query 'TargetGroups[0].{HealthCheckPath:HealthCheckPath,HealthCheckIntervalSeconds:HealthCheckIntervalSeconds,HealthCheckTimeoutSeconds:HealthCheckTimeoutSeconds,UnhealthyThresholdCount:UnhealthyThresholdCount,HealthyThresholdCount:HealthyThresholdCount}' \
        --output table
    
    # Check if VPC parameter exists (indicates v3 infrastructure)
    VPC_PARAM=$(aws ssm get-parameter \
        --region $AWS_REGION \
        --name /${env}/vpc/id \
        --query 'Parameter.Value' \
        --output text 2>/dev/null || echo "NOT_FOUND")
    
    if [ "$VPC_PARAM" != "NOT_FOUND" ]; then
        echo "Infrastructure Version: v3 (has VPC parameter)"
    else
        echo "Infrastructure Version: v2 or v1 (no VPC parameter)"
    fi
}

# Compare both environments
compare_target_group "stage"
compare_target_group "prod"

echo ""
echo "🎯 Analysis:"
echo "============"
echo "If health check settings are different, prod needs to be updated to match stage."
echo "If infrastructure versions are different, that explains the mismatch."