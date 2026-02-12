# Guide: Ajouter une image de fond sur toutes les pages de SonicFlow

## Vue d'ensemble
Ce guide vous explique comment ajouter une image de fond (`fond.jpg`) sur toutes les pages de l'application SonicFlow.

## Étape 1: Préparer l'image de fond

1. **Renommer votre image** en `fond.jpg` si ce n'est pas déjà fait
2. **Placer l'image** dans le dossier: `app/src/main/res/drawable/`
   
   Chemin complet: `/home/runner/work/SonicFlow/SonicFlow/app/src/main/res/drawable/fond.jpg`

   **Note**: Si vous avez une image PNG, vous pouvez aussi la nommer `fond.png`

## Étape 2: Créer un composant BackgroundImage

Créez un nouveau fichier `BackgroundImage.kt` dans le package `ui.theme`:

**Chemin**: `app/src/main/java/com/sonicflow/ui/theme/BackgroundImage.kt`

```kotlin
package com.sonicflow.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sonicflow.R

/**
 * Composant pour afficher l'image de fond de l'application
 */
@Composable
fun BackgroundImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.fond),
        contentDescription = "Background",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop, // Pour remplir tout l'écran
        alpha = 0.3f // Transparence pour que le contenu reste lisible (ajustez entre 0.1 et 1.0)
    )
}
```

## Étape 3: Modifier MainActivity.kt

Modifiez le fichier `MainActivity.kt` pour inclure l'image de fond:

**Avant** (lignes 44-54):
```kotlin
setContent {
    SonicFlowTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            SonicFlowNavGraph(navController = navController)
        }
    }
}
```

**Après**:
```kotlin
setContent {
    SonicFlowTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image de fond
            BackgroundImage()
            
            // Contenu de l'application par-dessus
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent // Important: rendre le Surface transparent
            ) {
                val navController = rememberNavController()
                SonicFlowNavGraph(navController = navController)
            }
        }
    }
}
```

**Imports à ajouter** en haut de MainActivity.kt:
```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import com.sonicflow.ui.theme.BackgroundImage
```

## Étape 4: Ajuster la transparence (optionnel)

Si l'image de fond est trop visible ou pas assez:

1. **Ouvrez** `BackgroundImage.kt`
2. **Modifiez** le paramètre `alpha`:
   - `alpha = 0.1f` → Très transparent (image presque invisible)
   - `alpha = 0.3f` → Transparent (recommandé)
   - `alpha = 0.5f` → Semi-transparent
   - `alpha = 1.0f` → Opaque (image complètement visible)

## Étape 5: Alternatives de mise en page

### Option A: Fond avec flou (effet moderne)

```kotlin
@Composable
fun BackgroundImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.fond),
        contentDescription = "Background",
        modifier = modifier
            .fillMaxSize()
            .blur(10.dp), // Ajoute un effet de flou
        contentScale = ContentScale.Crop,
        alpha = 0.4f
    )
}
```

**Import nécessaire**: `import androidx.compose.ui.draw.blur` et `import androidx.compose.ui.unit.dp`

### Option B: Fond avec gradient par-dessus

```kotlin
@Composable
fun BackgroundImage(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Image
        Image(
            painter = painterResource(id = R.drawable.fond),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient sombre par-dessus pour améliorer la lisibilité
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.3f)
                        )
                    )
                )
        )
    }
}
```

**Imports nécessaires**:
```kotlin
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
```

## Structure finale des fichiers

```
app/src/main/
├── java/com/sonicflow/
│   ├── MainActivity.kt (modifié)
│   └── ui/theme/
│       ├── BackgroundImage.kt (nouveau)
│       ├── Theme.kt
│       └── ...
└── res/
    └── drawable/
        └── fond.jpg (nouveau)
```

## Conseils

1. **Format d'image recommandé**: 
   - JPG pour les photos (plus léger)
   - PNG pour les images avec transparence
   - Résolution: 1080x1920 ou plus (format portrait)

2. **Taille de fichier**: 
   - Idéalement < 500 KB pour ne pas alourdir l'application
   - Utilisez un outil de compression d'image si nécessaire

3. **Lisibilité**: 
   - Assurez-vous que le texte reste lisible sur votre fond
   - Ajustez `alpha` ou ajoutez un gradient si nécessaire

4. **Performance**: 
   - L'image est chargée une seule fois dans MainActivity
   - Pas d'impact sur les performances de navigation

## Test

Après avoir appliqué ces modifications:

1. **Compilez** l'application: `./gradlew assembleDebug`
2. **Installez** sur votre appareil/émulateur
3. **Vérifiez** que l'image apparaît sur tous les écrans:
   - Library Screen
   - Player Screen  
   - Playlist Screen
   - Auth Screens

## Résumé des modifications

✅ **Fichiers à créer**:
- `app/src/main/java/com/sonicflow/ui/theme/BackgroundImage.kt`

✅ **Fichiers à modifier**:
- `app/src/main/java/com/sonicflow/MainActivity.kt`

✅ **Ressources à ajouter**:
- `app/src/main/res/drawable/fond.jpg`

Voilà! Votre application aura maintenant une image de fond sur toutes les pages. 🎨
