package net.osomahe.whitenoise247

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import net.osomahe.whitenoise247.db.DbFacade
import net.osomahe.whitenoise247.db.Noise
import net.osomahe.whitenoise247.ui.theme.WhiteNoise247Theme

class MainActivity : ComponentActivity() {

    private val TAG = "whitenoise247"

    private var database: Database? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CouchbaseLite.init(this)
        database = Database("whitenoise247")

        Log.i(TAG, "CBL Initialized")
        setContent {
            WhiteNoise247Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoiseList(DbFacade.getInstance().getNoises())
                }
            }
        }
        val intent = Intent(this, SoundService::class.java)
        ContextCompat.startForegroundService(this, intent)
       // finishAffinity()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WhiteNoise247Theme {
        Greeting("Android")
    }
}

@Composable
fun NoiseList(noises: List<Noise>) {
    Column {
        noises.forEach { noise ->
            NoiseRow(noise)
        }
    }
}

@Composable
private fun NoiseRow(noise: Noise) = Unit