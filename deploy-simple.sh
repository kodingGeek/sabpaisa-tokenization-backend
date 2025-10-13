#!/bin/bash

# Simple deployment script for backend application
set -e

echo "🚀 Starting backend deployment..."

ENVIRONMENT=${1:-dev}
INSTANCE_IP=${2:-}

if [ -z "$INSTANCE_IP" ]; then
  echo "Usage: $0 <environment> <instance-ip>"
  echo "Example: $0 dev 13.232.45.67"
  exit 1
fi

# Build application
echo "🔨 Building application..."
mvn clean package -DskipTests

WAR_FILE=$(ls target/*.war | head -1)
echo "📦 Built: $WAR_FILE"

# Copy WAR to server
echo "📤 Copying WAR to server..."
scp -o StrictHostKeyChecking=no "$WAR_FILE" ec2-user@$INSTANCE_IP:/tmp/app.war

# Deploy on server
echo "🚀 Deploying on server..."
ssh -o StrictHostKeyChecking=no ec2-user@$INSTANCE_IP << 'EOF'
  sudo systemctl stop tomcat || true
  sudo rm -rf /opt/tomcat/webapps/ROOT*
  sudo cp /tmp/app.war /opt/tomcat/webapps/ROOT.war
  sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war
  sudo systemctl start tomcat
  echo "✅ Deployment complete!"
EOF

echo "🌐 Application should be available at: http://$INSTANCE_IP:8080/api/health"