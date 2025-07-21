#!/bin/bash
set -e

echo "### Starting Post-Create Setup for Android Codespace ###"

# The Android SDK is installed by the feature to /opt/android-sdk
# We need to ensure the environment variables are set for interactive shells.

echo "[1/3] Setting ANDROID_HOME and updating PATH in .bashrc..."

# Check if the variables are already in .bashrc to avoid duplicates
if ! grep -q "ANDROID_HOME" ~/.bashrc; then
    echo '' >> ~/.bashrc
    echo '# Android SDK Environment Variables' >> ~/.bashrc
    echo 'export ANDROID_HOME=/opt/android-sdk' >> ~/.bashrc
    echo 'export ANDROID_SDK_ROOT=/opt/android-sdk' >> ~/.bashrc
    echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.bashrc
    echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc
    echo 'export PATH=$PATH:$ANDROID_HOME/build-tools/34.0.0' >> ~/.bashrc
    echo "Environment variables added to .bashrc."
else
    echo "Environment variables already exist in .bashrc."
fi

# Export for the current script session
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=/opt/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/build-tools/34.0.0

echo "[2/3] Accepting all SDK licenses..."
yes | sdkmanager --licenses

echo "[3/3] Setting correct permissions for the workspace user..."
# The SDK is owned by root, but the vscode user needs to access it.
# The user is already in the 'sdk-users' group which has permissions, but we'll ensure it.
sudo chown -R vscode:vscode /opt/android-sdk

echo ""
echo "#####################################################"
echo "### Codespace Environment Setup is Complete!      ###"
echo "### You can now run the project generation scripts. ###"
echo "#####################################################"
echo ""
