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
 * Pour utiliser une image personnalisée:
 * 1. Placez votre image 'fond.jpg' dans app/src/main/res/drawable/
 * 2. Décommentez le bloc Image ci-dessous
 * 3. Commentez le Spacer
 * 
 * @param modifier Modifier pour personnaliser l'apparence
 * @param alpha Transparence de l'image (0.0 = invisible, 1.0 = opaque). Par défaut 0.3
 */
@Composable
fun BackgroundImage(
    modifier: Modifier = Modifier,
    alpha: Float = 0.3f
) {
    // Version active: fonctionne sans image
    // Pour ajouter une vraie image, décommentez le bloc Image et commentez le Spacer
    
    Box(modifier = modifier.fillMaxSize()) {
        /* Pour utiliser une image personnalisée, décommentez ce bloc:
        Image(
            painter = painterResource(id = R.drawable.fond),
            contentDescription = "Background image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = alpha
        )
        */
        
        // Placeholder actif - permet au composant de fonctionner sans image
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.fillMaxSize()
        )
    }
}
