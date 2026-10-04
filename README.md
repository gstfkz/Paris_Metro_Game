# Paris Metro Game

Jeu Android (Kotlin + Jetpack Compose) : retrouver l'itinéraire le plus rapide en
métro/RER entre deux stations parisiennes tirées au hasard, puis comparaison avec
l'itinéraire officiel calculé en direct par l'API PRIM (Île-de-France Mobilités).

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
