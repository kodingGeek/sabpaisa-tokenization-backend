#!/bin/bash

# Check what infrastructure was actually deployed for production
set -e

ENV_NAME="prod"
AWS_REGION="ap-south-1"

echo "🔍 Checking Production Infrastructure Details"
echo "============================================="

echo ""
echo "📋 Parameter Store Values:"
echo "------------------------"

# Check all production parameters
echo "Database endpoint:"
aws ssm get-parameter --region $AWS_REGION --name /${ENV_NAME}/database/endpoint --query 'Parameter.Value' --output text 2>/dev/null || echo "NOT_FOUND"

echo ""
echo "Backend target group ARN:"
TARGET_GROUP_ARN=$(aws ssm get-parameter --region $AWS_REGION --name /${ENV_NAME}/backend/target-group-arn --query 'Parameter.Value' --output text 2>/dev/null || echo "NOT_FOUND")
echo $TARGET_GROUP_ARN

echo ""
echo "Frontend target group ARN:"
aws ssm get-parameter --region $AWS_REGION --name /${ENV_NAME}/frontend/target-group-arn --query 'Parameter.Value' --output text 2>/dev/null || echo "NOT_FOUND"

echo ""
echo "ALB DNS:"
aws ssm get-parameter --region $AWS_REGION --name /${ENV_NAME}/alb/dns --query 'Parameter.Value' --output text 2>/dev/null || echo "NOT_FOUND"

echo ""
echo "VPC ID (v3 only parameter):"
aws ssm get-parameter --region $AWS_REGION --name /${ENV_NAME}/vpc/id --query 'Parameter.Value' --output text 2>/dev/null || echo "NOT_FOUND - indicates v2 infrastructure"

if [ "$TARGET_GROUP_ARN" != "NOT_FOUND" ]; then
    echo ""
    echo "🎯 Backend Target Group Health Check Settings:"
    echo "--------------------------------------------"
    aws elbv2 describe-target-groups \
        --region $AWS_REGION \
        --target-group-arns $TARGET_GROUP_ARN \
        --query 'TargetGroups[0].{HealthCheckPath:HealthCheckPath,HealthCheckIntervalSeconds:HealthCheckIntervalSeconds,HealthCheckTimeoutSeconds:HealthCheckTimeoutSeconds,UnhealthyThresholdCount:UnhealthyThresholdCount,HealthyThresholdCount:HealthyThresholdCount}' \
        --output table
        
    echo ""
    echo "📊 Target Group Details:"
    echo "----------------------"
    aws elbv2 describe-target-groups \
        --region $AWS_REGION \
        --target-group-arns $TARGET_GROUP_ARN \
        --query 'TargetGroups[0].{Name:TargetGroupName,Port:Port,Protocol:Protocol,VpcId:VpcId}' \
        --output table
fi

echo ""
echo "🏥 Current Target Health:"
echo "------------------------"
if [ "$TARGET_GROUP_ARN" != "NOT_FOUND" ]; then
    aws elbv2 describe-target-health \
        --region $AWS_REGION \
        --target-group-arn $TARGET_GROUP_ARN \
        --output table 2>/dev/null || echo "No targets registered"
fi