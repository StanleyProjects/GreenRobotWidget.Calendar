package sp.service.sample

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "sp.service.sample"
        val v = LinearLayout(this).also {
            it.addView(textView)
        }
        setContentView(v)
    }
}
