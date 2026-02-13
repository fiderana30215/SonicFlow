package com.sonicflow.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonicflow.MainActivity
import com.sonicflow.R
import com.sonicflow.ui.theme.SonicFlowTheme
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    companion object {
        private const val SPLASH_DURATION = 2000L // 2 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ❌ SUPPRIMER cette ligne: installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SonicFlowTheme {
                SplashScreen()
            }
        }
    }

    @Composable
    private fun SplashScreen() {
        var startAnimation by remember { mutableStateOf(false) }
        val alpha by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec = tween(durationMillis = 1000),
            label = "splash_fade_in"
        )

        LaunchedEffect(key1 = true) {
            startAnimation = true
            delay(SPLASH_DURATION)
            if (!isFinishing) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black // Fond noir
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier.size(250.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}