#!/bin/bash

echo "🔧 Fixing Git repository..."
echo ""

# Remove backend folder from Git (it's already moved to /Users/ajay/Downloads/my/fcmserver/)
echo "📁 Removing backend folder from Git tracking..."
git rm -rf backend

# Add the removal to .gitignore to prevent accidental re-adding
echo "📝 Adding backend to .gitignore..."
echo "" >> .gitignore
echo "# Backend folder (moved to separate location)" >> .gitignore
echo "backend/" >> .gitignore

# Commit the changes
echo "💾 Committing changes..."
git add .gitignore
git commit -m "Remove backend folder (moved to /Users/ajay/Downloads/my/fcmserver/)"

echo ""
echo "✅ Git repository fixed!"
echo ""
echo "Now you can push to GitHub:"
echo "git push origin main"
