package com.sonicflow.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sonicflow.R

/**
 * Composant pour afficher l'image de fond de l'application
 * 
 * Pour utiliser ce composant:
 * 1. Placez votre image 'fond.jpg' dans app/src/main/res/drawable/
 * 2. Utilisez BackgroundImage() dans un Box avant votre contenu
 * 
 * @param modifier Modifier pour personnaliser l'apparence
 * @param alpha Transparence de l'image (0.0 = invisible, 1.0 = opaque). Par défaut 0.3
 */
@Composable
fun BackgroundImage(
    modifier: Modifier = Modifier,
    alpha: Float = 0.3f
) {
    Box(modifier = modifier.fillMaxSize()) {
        // NOTE: Pour utiliser cette image, vous devez:
        // 1. Avoir un fichier fond.jpg dans res/drawable/
        // 2. Décommenter le code ci-dessous
        
        /* Décommentez ce bloc une fois que fond.jpg est dans drawable/
        Image(
            painter = painterResource(id = R.drawable.fond),
            contentDescription = "Background image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop, // Remplit tout l'écran
            alpha = alpha // Transparence pour garder le contenu lisible
        )
        */
        
        // ALTERNATIVE: Si vous n'avez pas encore l'image, utilisez une couleur unie
        // Commentez cette ligne une fois que vous avez ajouté fond.jpg
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Variante avec effet de flou pour un look plus moderne
 * Nécessite: import androidx.compose.ui.draw.blur
 */
@Composable
fun BackgroundImageWithBlur(
    modifier: Modifier = Modifier,
    alpha: Float = 0.4f
) {
    Box(modifier = modifier.fillMaxSize()) {
        /* Décommentez une fois fond.jpg ajouté
        Image(
            painter = painterResource(id = R.drawable.fond),
            contentDescription = "Background image",
            modifier = Modifier
                .fillMaxSize()
                .blur(10.dp), // Effet de flou
            contentScale = ContentScale.Crop,
            alpha = alpha
        )
        */
    }
}

/**
 * Variante avec gradient par-dessus pour améliorer la lisibilité
 */
@Composable
fun BackgroundImageWithGradient(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        /* Décommentez une fois fond.jpg ajouté
        // Image de fond
        Image(
            painter = painterResource(id = R.drawable.fond),
            contentDescription = "Background image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient sombre par-dessus pour la lisibilité
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
        */
    }
}
