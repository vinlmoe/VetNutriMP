#!/bin/sh
# Xcode Cloud : exécuté juste après le clone du dépôt.
# Les machines Xcode Cloud n'ont pas de JDK : sans lui, la phase de build
# « ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode » échoue (exit 65).
set -e

export HOMEBREW_NO_AUTO_UPDATE=1
export HOMEBREW_NO_INSTALL_CLEANUP=1

# Les images (icônes de l'app, ressources Compose) sont dans Git LFS : Xcode Cloud
# ne clone que les pointeurs texte, ce qui donne une app sans icône que
# App Store Connect refuse (« Preparing build for App Store Connect failed »).
echo "🖼️ Récupération des fichiers Git LFS..."
brew install git-lfs
cd "${CI_PRIMARY_REPOSITORY_PATH:-$(dirname "$0")/../..}"
git lfs install --local
git lfs pull
head -c 8 iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/1024.png | grep -q PNG \
  || { echo "❌ L'icône 1024.png n'est pas un PNG après git lfs pull"; exit 1; }

echo "☕ Installation du JDK 17 (Homebrew)..."
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
