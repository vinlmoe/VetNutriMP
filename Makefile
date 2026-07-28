# Makefile pour VetNutri MP - Gestion des icônes et du projet

.PHONY: help icons test-icons clean-icons build test-all

# Variables
ICON_SOURCE ?= assets/icon_example.svg
DESKTOP_RESOURCES = composeApp/src/desktopMain/resources

# Aide
help:
	@echo "=== VetNutri MP - Commandes disponibles ==="
	@echo ""
	@echo "Icônes :"
	@echo "  make icons              # Générer toutes les icônes desktop"
	@echo "  make icons SOURCE=path  # Générer depuis une image source spécifique"
	@echo "  make test-icons         # Tester la configuration des icônes"
	@echo "  make clean-icons        # Nettoyer les icônes desktop"
	@echo ""
	@echo "Build :"
	@echo "  make build              # Construire le projet"
	@echo "  make test-all           # Tester et construire"
	@echo ""
	@echo "Exemples :"
	@echo "  make icons SOURCE=assets/logo.png"
	@echo "  make test-icons"

# Génération des icônes
icons:
	@echo "Génération des icônes depuis : $(ICON_SOURCE)"
	@if [ ! -f "$(ICON_SOURCE)" ]; then \
		echo "Erreur: Image source non trouvée: $(ICON_SOURCE)"; \
		echo "Utilisez: make icons SOURCE=chemin/vers/image"; \
		exit 1; \
	fi
	@chmod +x scripts/generate_icons.sh
	@./scripts/generate_icons.sh "$(ICON_SOURCE)"
	@echo "✓ Icônes générées avec succès !"

# Test de la configuration des icônes
test-icons:
	@echo "Test de la configuration des icônes..."
	@chmod +x scripts/test_icons.sh
	@./scripts/test_icons.sh

# Nettoyage des icônes desktop
clean-icons:
	@echo "Nettoyage des icônes desktop..."
	@rm -rf $(DESKTOP_RESOURCES)/icon.*
	@echo "✓ Icônes desktop supprimées"

# Build du projet
build:
	@echo "Construction du projet..."
	@./gradlew :composeApp:build

# Test complet et build
test-all: test-icons build
	@echo "✓ Tests et build terminés avec succès !"

# Installation des dépendances (macOS)
install-deps:
	@echo "Installation des dépendances pour la génération d'icônes..."
	@if command -v brew &> /dev/null; then \
		brew install imagemagick; \
	else \
		echo "Homebrew non trouvé. Installez ImageMagick manuellement."; \
	fi
	@echo "✓ Dépendances installées"

# Génération d'icônes depuis SVG (nécessite ImageMagick)
svg-to-icons:
	@echo "Conversion SVG vers icônes..."
	@if command -v convert &> /dev/null; then \
		convert -background transparent -resize 1024x1024 assets/icon_example.svg assets/icon_example.png; \
		make icons SOURCE=assets/icon_example.png; \
	else \
		echo "ImageMagick non installé. Utilisez: make install-deps"; \
	fi

# Vérification rapide
check:
	@echo "Vérification rapide de la configuration..."
	@if [ -f "composeApp/build.gradle.kts" ]; then \
		echo "✓ build.gradle.kts trouvé"; \
	else \
		echo "✗ build.gradle.kts manquant"; \
	fi
	@if [ -d "composeApp/src/androidMain/res" ]; then \
		echo "✓ Dossier Android trouvé"; \
	else \
		echo "✗ Dossier Android manquant"; \
	fi
	@if [ -d "iosApp/iosApp/Assets.xcassets" ]; then \
		echo "✓ Dossier iOS trouvé"; \
	else \
		echo "✗ Dossier iOS manquant"; \
	fi
	@if [ -d "$(DESKTOP_RESOURCES)" ]; then \
		echo "✓ Dossier Desktop trouvé"; \
	else \
		echo "✗ Dossier Desktop manquant"; \
	fi
