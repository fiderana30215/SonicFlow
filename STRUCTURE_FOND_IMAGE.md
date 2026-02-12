# Structure de l'image de fond dans SonicFlow

## Architecture visuelle

```
┌─────────────────────────────────────────────────────┐
│ MainActivity (ComponentActivity)                    │
│                                                      │
│  setContent {                                        │
│    SonicFlowTheme {                                  │
│      ┌──────────────────────────────────────────┐   │
│      │ Box (Conteneur principal)               │   │
│      │                                          │   │
│      │  ┌────────────────────────────────────┐ │   │
│      │  │ BackgroundImage()                  │ │   │
│      │  │ (Couche 1 - Arrière-plan)         │ │   │
│      │  │                                    │ │   │
│      │  │  [Image: fond.jpg]                │ │   │
│      │  │  - ContentScale.Crop              │ │   │
│      │  │  - alpha = 0.3f                   │ │   │
│      │  └────────────────────────────────────┘ │   │
│      │                                          │   │
│      │  ┌────────────────────────────────────┐ │   │
│      │  │ Surface (transparent)              │ │   │
│      │  │ (Couche 2 - Premier plan)         │ │   │
│      │  │                                    │ │   │
│      │  │  NavHost {                        │ │   │
│      │  │    • LibraryScreen                │ │   │
│      │  │    • PlayerScreen                 │ │   │
│      │  │    • PlaylistScreen               │ │   │
│      │  │    • SignInScreen                 │ │   │
│      │  │    • etc...                       │ │   │
│      │  │  }                                │ │   │
│      │  └────────────────────────────────────┘ │   │
│      └──────────────────────────────────────────┘   │
│    }                                                 │
│  }                                                   │
└─────────────────────────────────────────────────────┘
```

## Flux de navigation avec fond d'image

```
LibraryScreen          PlayerScreen         PlaylistScreen
┌──────────┐          ┌──────────┐          ┌──────────┐
│ [FOND]   │          │ [FOND]   │          │ [FOND]   │
│          │  ────→   │          │  ────→   │          │
│ Contenu  │          │ Contenu  │          │ Contenu  │
│ Library  │          │ Player   │          │ Playlist │
└──────────┘          └──────────┘          └──────────┘
     ↑                                            │
     └────────────────────────────────────────────┘

L'image de fond reste visible sur TOUS les écrans
car elle est définie au niveau de MainActivity
```

## Hiérarchie des composants

```
MainActivity.kt
├── SonicFlowTheme
│   └── Box (fillMaxSize)
│       ├── BackgroundImage()      ← IMAGE DE FOND ICI
│       │   └── Image(fond.jpg)
│       └── Surface (transparent)
│           └── NavHost
│               ├── LibraryScreen
│               ├── PlayerScreen
│               ├── PlaylistScreen
│               ├── PlaylistDetailScreen
│               ├── SignInScreen
│               └── SignUpScreen
```

## Pourquoi cette approche?

### ✅ Avantages

1. **Une seule modification**: 
   - Changez uniquement MainActivity.kt
   - Pas besoin de modifier chaque écran individuellement

2. **Performance optimale**:
   - Image chargée une seule fois
   - Pas de rechargement lors de la navigation
   - Mémoire utilisée efficacement

3. **Cohérence visuelle**:
   - Même fond sur tous les écrans
   - Transitions fluides

4. **Maintenance facile**:
   - Pour changer l'image: remplacez fond.jpg
   - Pour ajuster la transparence: modifiez `alpha` dans BackgroundImage.kt
   - Pour désactiver: commentez `BackgroundImage()` dans MainActivity

### Options de personnalisation

#### Option 1: Transparence ajustable
```kotlin
BackgroundImage(alpha = 0.3f)  // 30% visible
```

#### Option 2: Effet de flou
```kotlin
BackgroundImageWithBlur(alpha = 0.4f)
```

#### Option 3: Gradient par-dessus
```kotlin
BackgroundImageWithGradient()
```

## Exemple de code complet

### 1. Créer BackgroundImage.kt
```kotlin
@Composable
fun BackgroundImage(alpha: Float = 0.3f) {
    Image(
        painter = painterResource(id = R.drawable.fond),
        contentDescription = "Background",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = alpha
    )
}
```

### 2. Modifier MainActivity.kt
```kotlin
setContent {
    SonicFlowTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundImage()  // ← Image de fond
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent  // ← Important!
            ) {
                val navController = rememberNavController()
                SonicFlowNavGraph(navController = navController)
            }
        }
    }
}
```

### 3. Ajouter l'image
```
Placez fond.jpg dans:
app/src/main/res/drawable/fond.jpg
```

## Résultat attendu

```
┌────────────────────────────┐
│  ╔══════════════════════╗  │
│  ║                      ║  │
│  ║   [Votre image de   ║  │
│  ║    fond visible en  ║  │
│  ║    semi-transparent]║  │
│  ║                      ║  │
│  ║   Par-dessus:        ║  │
│  ║   • Boutons          ║  │
│  ║   • Texte            ║  │
│  ║   • Listes           ║  │
│  ║   • etc.             ║  │
│  ║                      ║  │
│  ╚══════════════════════╝  │
└────────────────────────────┘
```

## Checklist d'implémentation

- [ ] Préparer l'image fond.jpg (format portrait, < 500KB)
- [ ] Placer fond.jpg dans app/src/main/res/drawable/
- [ ] Créer BackgroundImage.kt dans ui/theme/
- [ ] Décommenter le code Image() dans BackgroundImage.kt
- [ ] Modifier MainActivity.kt:
  - [ ] Ajouter imports (Box, Color, BackgroundImage)
  - [ ] Remplacer Surface par Box
  - [ ] Ajouter BackgroundImage()
  - [ ] Rendre Surface transparent
- [ ] Compiler: ./gradlew assembleDebug
- [ ] Tester sur toutes les pages
- [ ] Ajuster alpha si nécessaire

## Dépannage

### L'image n'apparaît pas?
1. Vérifiez que fond.jpg est dans drawable/
2. Vérifiez que le code Image() est décommenté
3. Vérifiez que Surface est transparent (Color.Transparent)
4. Recompilez complètement (./gradlew clean assembleDebug)

### L'image est trop visible?
- Augmentez la transparence: `alpha = 0.2f` ou moins

### L'image est trop floue/invisible?
- Diminuez la transparence: `alpha = 0.5f` ou plus

### Le texte n'est pas lisible?
- Ajoutez un gradient: utilisez `BackgroundImageWithGradient()`
- Ou diminuez alpha: `alpha = 0.2f`
