#!/bin/bash
set -e

echo "### STEP 3: Building the Procedurally Generated APK ###"

# --- Check if we are in the project directory ---
if [ ! -f "gradlew" ]; then
    echo "ERROR: 'gradlew' script not found."
    echo "Please run this script from the root of the 'DynamicApp' project directory."
    exit 1
fi

echo "[1/4] IMPORTANT: Have you run 'supabase_schema.sql' in your Supabase project?"
read -p "Press [Enter] to continue..."

echo "[2/4] IMPORTANT: Have you added your Supabase credentials to the AppModule?"
read -p "Press [Enter] to continue..."

# --- Clean and Build ---
echo "[3/4] Cleaning the project..."
./gradlew clean

echo "[4/4] Building the unsigned release APK... This may take a few minutes."
./gradlew assembleRelease

echo ""
echo "##################################"
echo "### BUILD SUCCESSFUL ###"
echo "##################################"
echo ""
echo "The unsigned APK is located at:"
echo "  app/build/outputs/apk/release/app-release-unsigned.apk"
echo ""
echo "You can now sign this APK for release using the instructions from the README."
