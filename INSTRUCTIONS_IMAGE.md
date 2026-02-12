# Instructions pour ajouter votre image fond.jpg

## Où placer l'image

```
SonicFlow/
└── app/
    └── src/
        └── main/
            └── res/
                └── drawable/
                    └── fond.jpg  ← PLACEZ VOTRE IMAGE ICI
```

**Chemin complet**: `/home/runner/work/SonicFlow/SonicFlow/app/src/main/res/drawable/fond.jpg`

## Spécifications recommandées

### Format
- **JPG** pour les photos (recommandé, plus léger)
- **PNG** si vous avez besoin de transparence
- **WebP** pour une compression optimale (Android supporte)

### Dimensions
- **Portrait**: 1080 × 1920 pixels (ratio 9:16)
- **Ou plus grand**: 1440 × 2560 pixels
- L'image sera automatiquement redimensionnée pour remplir l'écran

### Taille de fichier
- **Idéal**: < 500 KB
- **Maximum recommandé**: < 1 MB
- Utilisez un outil de compression si nécessaire

### Qualité
- **Qualité JPG**: 80-85% (bon compromis qualité/taille)
- Évitez les images trop détaillées (elles peuvent distraire)
- Préférez des images avec des zones calmes pour la lisibilité du texte

## Outils de compression

### En ligne
- **TinyJPG/TinyPNG**: https://tinyjpg.com/
- **Compressor.io**: https://compressor.io/
- **Squoosh**: https://squoosh.app/

### Ligne de commande
```bash
# Avec ImageMagick
convert fond_original.jpg -quality 85 -resize 1080x1920^ fond.jpg

# Avec cwebp (pour WebP)
cwebp -q 80 fond.jpg -o fond.webp
```

## Types d'images recommandées

### ✅ Bonnes pour le fond
- Dégradés de couleurs
- Motifs abstraits
- Paysages flous
- Textures subtiles
- Images avec beaucoup d'espaces unis

### ❌ À éviter
- Photos très détaillées et contrastées
- Images avec beaucoup de texte
- Motifs répétitifs trop marqués
- Couleurs trop vives qui fatiguent les yeux

## Exemple de nommage

Si vous avez plusieurs variations:

```
drawable/
├── fond.jpg              ← Version par défaut
├── fond_dark.jpg         ← Version sombre
├── fond_light.jpg        ← Version claire
└── fond_gradient.jpg     ← Version gradient
```

Puis dans `BackgroundImage.kt`, changez:
```kotlin
painter = painterResource(id = R.drawable.fond)
// ou
painter = painterResource(id = R.drawable.fond_dark)
```

## Checklist avant d'ajouter l'image

- [ ] Image au format JPG, PNG ou WebP
- [ ] Dimensions minimum: 1080 × 1920 pixels
- [ ] Taille de fichier < 500 KB
- [ ] Image compressée avec un outil
- [ ] Image testée visuellement (contraste, lisibilité)
- [ ] Fichier nommé exactement `fond.jpg` (ou .png, .webp)
- [ ] Fichier placé dans `app/src/main/res/drawable/`

## Après avoir ajouté l'image

1. **Ouvrez** `BackgroundImage.kt`
2. **Décommentez** le bloc Image():
   ```kotlin
   Image(
       painter = painterResource(id = R.drawable.fond),
       contentDescription = "Background image",
       modifier = Modifier.fillMaxSize(),
       contentScale = ContentScale.Crop,
       alpha = 0.3f
   )
   ```
3. **Recompilez** l'application:
   ```bash
   ./gradlew clean assembleDebug
   ```
4. **Testez** sur votre appareil

## Problèmes courants

### Erreur: "Unresolved reference: fond"
- ✓ Vérifiez que le fichier est bien dans drawable/
- ✓ Vérifiez le nom: `fond.jpg` (pas `Fond.jpg` ou `fond.jpeg`)
- ✓ Nettoyez et recompilez: `./gradlew clean build`

### L'image ne s'affiche pas
- ✓ Vérifiez que le code est décommenté dans BackgroundImage.kt
- ✓ Vérifiez que MainActivity utilise BackgroundImage()
- ✓ Vérifiez que Surface est transparent (Color.Transparent)

### L'image est déformée
- ✓ Utilisez `ContentScale.Crop` pour remplir l'écran
- ✓ Ou `ContentScale.FillBounds` pour étirer
- ✓ Ou `ContentScale.Fit` pour voir l'image entière

### L'application est lente
- ✓ Réduisez la taille de l'image (< 500 KB)
- ✓ Diminuez les dimensions (1080×1920 suffit)
- ✓ Compressez avec un outil

## Alternative: Utiliser une couleur dégradée

Si vous n'avez pas d'image, vous pouvez utiliser un dégradé:

```kotlin
@Composable
fun BackgroundGradient() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    )
}
```

Puis dans MainActivity:
```kotlin
BackgroundGradient()  // au lieu de BackgroundImage()
```

---

**Note**: Une fois que vous avez placé `fond.jpg` et décommenté le code, l'image apparaîtra automatiquement sur tous les écrans de l'application! 🎨
