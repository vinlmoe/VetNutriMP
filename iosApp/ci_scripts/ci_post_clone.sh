#!/bin/sh
# Xcode Cloud : exécuté juste après le clone du dépôt.
# Les machines Xcode Cloud n'ont pas de JDK : sans lui, la phase de build
# « ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode » échoue (exit 65).
set -e

echo "☕ Installation du JDK 17 (Homebrew)..."
export HOMEBREW_NO_AUTO_UPDATE=1
export HOMEBREW_NO_INSTALL_CLEANUP=1
brew install openjdk@17

JDK_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
"$JDK_HOME/bin/java" -version

# Réglages Gradle propres à Xcode Cloud (le gradle.properties utilisateur
# l'emporte sur celui du projet) : JDK explicite et mémoire adaptée à la machine.
mkdir -p "$HOME/.gradle"
cat > "$HOME/.gradle/gradle.properties" <<PROPS
org.gradle.java.home=$JDK_HOME
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC -Dfile.encoding=UTF-8
kotlin.daemon.jvmargs=-Xmx3g -XX:+UseG1GC
org.gradle.workers.max=2
PROPS

echo "✅ JDK prêt : $JDK_HOME"
