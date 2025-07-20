bash
#!/bin/bash
set -e # Exit immediately if a command exits with a non-zero status.

echo "### STEP 1: Setting up Android Build Environment ###"

# --- Check for dependencies ---
echo "[1/6] Checking for dependencies (Java, wget, unzip)..."
if ! command -v java &> /dev/null || ! command -v wget &> /dev/null || ! command -v unzip &> /dev/null; then
    echo "ERROR: Java, wget, or unzip is not installed."
    echo "Please install them using your system's package manager (e.g., 'sudo apt-get install openjdk-17-jdk wget unzip') and try again."
    exit 1
fi
echo "Dependencies found."

# --- Define paths and tool versions ---
export ANDROID_HOME=$HOME/android_sdk
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
PLATFORM_VERSION="android-34"
BUILD_TOOLS_VERSION="34.0.0"

# --- Create SDK directories ---
echo "[2/6] Creating Android SDK directories at $ANDROID_HOME..."
mkdir -p "$ANDROID_HOME/cmdline-tools"

# --- Download and extract command line tools ---
echo "[3/6] Downloading Android command line tools..."
wget -q -O cmdline-tools.zip "$CMDLINE_TOOLS_URL"
unzip -q -d "$ANDROID_HOME/cmdline-tools" cmdline-tools.zip
# The tools are extracted into a folder like 'cmdline-tools'. We rename it to 'latest'.
mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
rm cmdline-tools.zip
echo "Command line tools installed."

# --- Set up environment variables ---
echo "[4/6] Setting up environment variables..."
# Detect shell profile file
if [ -n "$BASH_VERSION" ]; then
    PROFILE_FILE=~/.bashrc
elif [ -n "$ZSH_VERSION" ]; then
    PROFILE_FILE=~/.zshrc
else
    PROFILE_FILE=~/.profile
fi

echo "Adding variables to $PROFILE_FILE..."
echo "" >> $PROFILE_FILE
echo "# Android SDK" >> $PROFILE_FILE
echo "export ANDROID_HOME=$HOME/android_sdk" >> $PROFILE_FILE
echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin" >> $PROFILE_FILE
echo "export PATH=\$PATH:\$ANDROID_HOME/platform-tools" >> $PROFILE_FILE
echo "Environment variables configured. You MUST reload your shell (e.g., 'source $PROFILE_FILE' or open a new terminal) after this script completes."

# --- Export for current session and install SDK packages ---
echo "[5/6] Installing SDK platform-tools, build-tools, and platforms..."
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Use sdkmanager to install required packages
# The 'yes' command automatically accepts the licenses.
yes | sdkmanager "platform-tools" "platforms;$PLATFORM_VERSION" "build-tools;$BUILD_TOOLS_VERSION" > /dev/null
echo "SDK packages installed."

# --- Accept any remaining licenses ---
echo "[6/6] Accepting all SDK licenses..."
yes | sdkmanager --licenses > /dev/null
echo "Licenses accepted."

echo ""
echo "### ENVIRONMENT SETUP COMPLETE ###"
echo "IMPORTANT: Open a new terminal or run 'source $PROFILE_FILE' to load the new environment."
