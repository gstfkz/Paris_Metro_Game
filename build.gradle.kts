// Fichier racine : déclare les plugins utilisés par les sous-modules (ici uniquement :app).
// NB : les numéros de version ci-dessous sont ceux connus au moment de la rédaction
// (début 2026). Ouvre le projet dans Android Studio et laisse l'IDE proposer une mise
// à jour AGP/Kotlin si une version plus récente est disponible : je n'ai pas pu vérifier
// ces numéros contre le dépôt Maven (pas d'accès réseau depuis mon environnement).
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
