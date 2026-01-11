#!/bin/bash

echo "🔧 Removing backend folder from entire Git history..."
echo ""
echo "⚠️  This will rewrite Git history to remove sensitive credentials"
echo ""

cd /Users/ajay/Downloads/my/nofsdotaca

# Method 1: Use git filter-repo (recommended) or filter-branch
echo "🗑️  Removing backend/ from all commits..."

# Using git filter-branch (works on all systems)
git filter-branch --force --index-filter \
  'git rm -rf --cached --ignore-unmatch backend' \
  --prune-empty --tag-name-filter cat -- --all

echo ""
echo "✅ Backend folder removed from Git history!"
echo ""
echo "🧹 Cleaning up..."
rm -rf .git/refs/original/
git reflog expire --expire=now --all
git gc --prune=now --aggressive

echo ""
echo "✅ Git history cleaned!"
echo ""
echo "🚀 Now force push to GitHub:"
echo "   git push origin main --force"
echo ""
echo "⚠️  Note: This is a force push because we rewrote history"
