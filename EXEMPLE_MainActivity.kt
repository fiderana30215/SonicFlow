// EXEMPLE DE MODIFICATION POUR MainActivity.kt
// Ce fichier montre comment modifier MainActivity.kt pour ajouter l'image de fond

package com.sonicflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.sonicflow.ui.navigation.SonicFlowNavGraph
import com.sonicflow.ui.theme.BackgroundImage // NOUVEAU IMPORT
import com.sonicflow.ui.theme.SonicFlowTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for SonicFlow application
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted - user can now scan their library
        } else {
            // Permission denied - show a message to the user
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request permissions at startup
        requestAudioPermission()
        
        setContent {
            SonicFlowTheme {
                // MODIFICATION: Utiliser Box au lieu de Surface directement
                Box(modifier = Modifier.fillMaxSize()) {
                    // Image de fond (en arrière-plan)
                    BackgroundImage()
                    
                    // Contenu de l'application (au premier plan)
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent // IMPORTANT: Rendre transparent pour voir le fond
                    ) {
                        val navController = rememberNavController()
                        SonicFlowNavGraph(navController = navController)
                    }
                }
            }
        }
    }
    
    private fun requestAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

/*
RÉSUMÉ DES MODIFICATIONS:

1. IMPORTS AJOUTÉS:
   - import androidx.compose.foundation.layout.Box
   - import androidx.compose.ui.graphics.Color
   - import com.sonicflow.ui.theme.BackgroundImage

2. CHANGEMENTS DANS setContent:
   AVANT:
   Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
       val navController = rememberNavController()
       SonicFlowNavGraph(navController = navController)
   }
   
   APRÈS:
   Box(modifier = Modifier.fillMaxSize()) {
       BackgroundImage()
       Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
           val navController = rememberNavController()
           SonicFlowNavGraph(navController = navController)
       }
   }

3. ÉTAPES POUR ACTIVER LE FOND:
   a. Placez fond.jpg dans app/src/main/res/drawable/
   b. Ouvrez BackgroundImage.kt
   c. Décommentez le code Image() dans la fonction BackgroundImage()
   d. Recompilez l'application

VARIANTES POSSIBLES:
- BackgroundImage() → Fond simple avec transparence
- BackgroundImageWithBlur() → Fond avec effet de flou
- BackgroundImageWithGradient() → Fond avec gradient sombre par-dessus

Pour changer de variante, remplacez simplement:
  BackgroundImage()
par:
  BackgroundImageWithBlur()
ou:
  BackgroundImageWithGradient()
*/
