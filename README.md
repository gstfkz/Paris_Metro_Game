# Metro Game

Jeu Android (Kotlin + Jetpack Compose) : retrouver l'itinéraire le plus rapide en
métro/RER entre deux stations parisiennes tirées au hasard, puis comparaison avec
l'itinéraire officiel calculé en direct par l'API PRIM (Île-de-France Mobilités).

## ⚠️ Important — ce qui a été fait et ce qui reste à vérifier

Ce projet a été écrit sans accès à un SDK Android, un émulateur, ni à Internet
(l'environnement qui l'a généré est isolé). Concrètement :

- **Le code n'a pas été compilé ni exécuté.** Il est structuré et devrait fonctionner,
  mais attends-toi à corriger quelques erreurs de compilation (versions de dépendances,
  API Compose Material3 qui évolue vite, etc.) une fois ouvert dans Android Studio.
- **Les endpoints de l'API PRIM n'ont pas été testés en direct.** Le format utilisé
  (`apikey` en header, `stop_area:IDFM:...`, `journeys?from=&to=&forbidden_uris[]=...`)
  est celui documenté pour Navitia/PRIM, mais vérifie le premier appel réel (Postman,
  `curl`, ou un log Logcat via l'intercepteur déjà en place) avant de te fier à l'appli.
- Les numéros de version Gradle/AGP/Kotlin/Compose dans les `build.gradle.kts` sont ceux
  connus à la rédaction (~janvier 2026) : laisse Android Studio proposer une mise à jour
  s'il en détecte une plus récente.

## Mise en place

1. Ouvre le dossier dans Android Studio (il régénérera le binaire du Gradle wrapper
   manquant, ou lance `gradle wrapper` toi-même si tu as Gradle installé).
2. Crée un compte gratuit sur https://prim.iledefrance-mobilites.fr/, puis dans
   l'espace développeur, abonne-toi à l'API **Navitia** pour obtenir une clé.
3. Dans `local.properties` (à la racine, non versionné), ajoute :
   ```
   PRIM_API_KEY=ta_cle_ici
   ```
4. Lance l'appli sur un émulateur ou un appareil (API 26+).

## Choix d'architecture retenus (validés avec toi)

- L'itinéraire "officiel" est recalculé par un appel à l'API PRIM à chaque partie
  (pas de graphe pré-calculé embarqué) — voir `MetroRepository.fetchOfficialJourney`.
- Les lignes 3bis et 7bis sont traitées comme des lignes à part entière (elles
  remontent naturellement comme telles via le filtre `physical_mode:Metro`).
- Le RER n'est filtré aux stations intra-muros qu'en comparant le `zip_code` des
  `administrative_regions` du `stop_area` (préfixe `"75"`) — voir
  `MetroRepository.isInParisIntramuros()`.
- Les lignes et stations (menus déroulants) sont chargées depuis l'API au lancement
  d'une partie puis mises en cache en mémoire (pas de jeu de données statique
  codé en dur, conformément à ta demande d'utiliser l'API aussi pour ces infos).

## Points à valider / limites connues

- **Ordre des stations dans les menus déroulants** : elles viennent de l'endpoint
  `lines/{id}/stop_areas`, qui ne garantit pas l'ordre géographique le long de la
  ligne (elles sont triées par ordre alphabétique par défaut). Si tu veux l'ordre
  réel des stations, il faudra passer par les `routes` de la ligne et leurs
  `stop_points` ordonnés — non implémenté ici faute de pouvoir tester la structure
  exacte de la réponse.
- **Définition de "même itinéraire"** (`ItineraryComparator`) : exige la séquence
  exacte (ligne, station départ, station arrivée) identique à celle renvoyée par
  l'API. Si plusieurs trajets sont ex-aequo au temps le plus court, seul le premier
  renvoyé par PRIM est comparé — à ajuster si tu veux accepter plusieurs solutions.
  Réfléchis aussi à un éventuel écart de tolérance sur la durée si tu préfères
  comparer par temps plutôt que par tracé exact.
- **Aucun fallback hors-ligne** : sans connexion ou en cas de quota API dépassé,
  l'appli affiche un écran d'erreur avec retour au menu (`GameScreen.Error`).
- **Sécurité de la clé API** : elle est lue depuis `local.properties` (non commitée)
  et injectée via `BuildConfig`, ce qui reste extractible de l'APK par rétro-ingénierie
  si tu distribues l'appli publiquement. Pour une diffusion large, mets un petit
  backend relais qui porte la clé côté serveur.
- **Tirage aléatoire des stations** (`pickRandomStationPair`) : ne vérifie pas à
  l'avance qu'un chemin purement métro/RER existe entre les deux stations. C'est
  quasi toujours le cas à Paris, mais si `fetchOfficialJourney` renvoie `null`
  (aucune solution sans marche imposée entre deux gares), l'appli affiche une
  erreur plutôt que de planter — à améliorer en relançant un tirage automatique
  si tu préfères une meilleure UX.

## Structure

```
app/src/main/java/com/example/metrogame/
├── MainActivity.kt              # assemblage + routage entre écrans
├── data/model/Models.kt         # modèles de domaine + état de l'écran
├── data/network/                # DTOs + Retrofit + client HTTP (API PRIM)
├── data/repository/             # chargement réseau + itinéraire officiel
├── ui/screen/                   # Menu / Jeu / Résultat (Compose)
├── ui/viewmodel/GameViewModel.kt
└── util/ItineraryComparator.kt  # logique de comparaison gagné/perdu
```
