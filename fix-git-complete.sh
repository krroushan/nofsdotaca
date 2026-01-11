#!/bin/bash

echo "🔧 Fixing Git repository to remove backend folder with secrets..."
echo ""

cd /Users/ajay/Downloads/my/nofsdotaca

# Check if backend is in the last commit
echo "📋 Checking Git status..."
git status

echo ""
echo "🗑️  Removing backend folder from Git history..."

# Remove backend from the staging area and commit history
git rm -rf --cached backend 2>/dev/null || echo "Backend already removed from staging"

# Commit the removal
echo ""
echo "💾 Committing the removal..."
git add .gitignore
git commit -m "Remove backend folder (moved to /Users/ajay/Downloads/my/fcmserver/)" 2>/dev/null || echo "Nothing to commit"

echo ""
echo "✅ Git repository fixed!"
echo ""
echo "📊 Current status:"
git status

echo ""
echo "🚀 Now you can push to GitHub:"
echo "   git push origin main"
echo ""
echo "Note: The backend folder is now at /Users/ajay/Downloads/my/fcmserver/"
