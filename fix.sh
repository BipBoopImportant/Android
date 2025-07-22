#!/bin/bash
set -e
set -u

# ==============================================================================
# WARNING: THIS IS A DESTRUCTIVE SCRIPT.
# IT WILL REMOVE AND RECONFIGURE YOUR ANDROID SDK SETUP.
# ==============================================================================
echo "This script will:"
echo "  1. REMOVE the system Android SDK (/usr/lib/android-sdk)."
echo "  2. REMOVE any existing SDK in your home directory (~/Android)."
echo "  3. CLEAN your current project's build files."
echo "  4. INSTALL a fresh, user-owned Android SDK in ~/Android/Sdk."
echo "  5. CONFIGURE your environment (.bashrc) to use the new SDK."
echo ""
read -p "Are you sure you want to continue? (yes/no) " -r
echo
if [[ ! "$REPLY" =~ ^[Yy]es$ ]]
then
    echo "Operation cancelled."
    exit 1
fi

# ==============================================================================
# PHASE 1: NUKE - REMOVE OLD CONFIGURATIONS
# ==============================================================================
echo "--- Phase 1: Removing old configurations ---"
echo "Removing system SDK (requires sudo)..."
sudo rm -rf /usr/lib/android-sdk

echo "Removing any user SDK..."
rm -rf "$HOME/Android"

echo "Removing leftover command-line tools downloads..."
rm -rf "$HOME/commandlinetools-linux-*.zip" "$HOME/cmdline-tools"

echo "Cleaning project files..."
rm -f /workspaces/Android/local.properties
rm -rf /workspaces/Android/build
rm -rf /workspaces/Android/app/build

echo "Cleaning up old environment variables from .bashrc..."
sed -i '/ANDROID_HOME/s/^/#/' ~/.bashrc
sed -i '/ANDROID_SDK_ROOT/s/^/#/' ~/.bashrc
sed -i '/cmdline-tools/s/^/#/' ~/.bashrc
sed -i '/platform-tools/s/^/#/' ~/.bashrc
echo "Cleanup complete."
echo ""


# ==============================================================================
# PHASE 2: PAVE - INSTALL AND CONFIGURE A FRESH SDK
# ==============================================================================
echo "--- Phase 2: Installing and configuring a fresh SDK ---"
SDK_PATH="$HOME/Android/Sdk"
mkdir -p "$SDK_PATH"

echo "Downloading Android command-line tools..."
cd "$HOME"
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q commandlinetools-linux-11076708_latest.zip

echo "Setting up command-line tools..."
# The new tools must be in Sdk/cmdline-tools/latest
mkdir -p "$SDK_PATH/cmdline-tools"
mv cmdline-tools "$SDK_PATH/cmdline-tools/latest"
rm commandlinetools-linux-11076708_latest.zip

echo "Configuring environment variables in ~/.bashrc..."
echo '' >> ~/.bashrc
echo '# Android SDK Configuration' >> ~/.bashrc
echo "export ANDROID_HOME=$SDK_PATH" >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc

# Temporarily export for the rest of this script
export ANDROID_HOME=$SDK_PATH
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

echo "Installing required SDK packages (platform-tools, build-tools, platform)..."
# Install essential packages your project needs
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "Accepting all SDK licenses..."
yes | sdkmanager --licenses

echo "Creating project's local.properties file..."
echo "sdk.dir=$SDK_PATH" > /workspaces/Android/local.properties

echo ""
# ==============================================================================
# SUCCESS!
# ==============================================================================
echo "✅ All done! Your Android development environment has been reset."
echo ""
echo "IMPORTANT: To apply the new environment variables, do ONE of the following:"
echo "  1. Run this command: source ~/.bashrc"
echo "  2. Close and reopen your terminal."
echo ""
echo "After that, you can finally build your project from '/workspaces/Android' with:"
echo "  ./gradlew assembleDebug"
echo ""