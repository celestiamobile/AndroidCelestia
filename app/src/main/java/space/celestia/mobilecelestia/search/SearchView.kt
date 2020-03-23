package space.celestia.mobilecelestia.search

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import space.celestia.mobilecelestia.R

class SearchView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), TextWatcher {
    private val textView: EditText
    private val cancelView: ImageView

    var listener: SearchView.Listener? = null

    interface Listener {
        fun onTextChanged(newText: String)
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutResID = R.layout.search_view
        inflater.inflate(layoutResID, this, true)

        textView = findViewById<EditText>(R.id.edt_search_text)
        cancelView = findViewById<ImageView>(R.id.iv_clear_text)

        textView.addTextChangedListener(this)
        cancelView.setOnClickListener {
            textView.setText("")
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (p0 == null || p0.isEmpty()) {
            cancelView.visibility = View.GONE
        } else {
            cancelView.visibility = View.VISIBLE
        }
        listener?.onTextChanged(p0.toString())
    }

    override fun afterTextChanged(p0: Editable?) {
    }
}