package org.fnives.keepass.android.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.fnives.keepass.android.manager.ui.theme.KeePassAndroidManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KeePassAndroidManagerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    LazyColumn {
        this.item { Text(text = "Hello0 $name!") }
        this.item { Text(text = "Hello1 $name!") }
        this.item { Text(text = "Hello2 $name!") }
        this.item { Text(text = "Hello3 $name!") }
        this.item { Text(text = "Hello4 $name!") }
        this.item { Text(text = "Hello5 $name!") }
        this.item { Text(text = "Hello6 $name!") }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KeePassAndroidManagerTheme {
        Greeting("Android")
    }
}
