package space.celestia.mobilecelestia.common

import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.radiobutton.MaterialRadioButton
import space.celestia.mobilecelestia.R

class RadioButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView
        get() = itemView.findViewById(R.id.title)
    val buttonGroup: RadioGroup
        get() = itemView.findViewById(R.id.button_group)

    fun configure(text: String, showTitle: Boolean, options: List<String>, checkedIndex: Int, stateChangeCallback: (Int) -> Unit) {
        title.text = text
        title.visibility = if (showTitle) View.VISIBLE else View.GONE
        buttonGroup.setOnCheckedChangeListener(null)
        buttonGroup.clearCheck()
        buttonGroup.removeAllViews()
        for (option in options.withIndex()) {
            val button = MaterialRadioButton(itemView.context)
            button.id = option.index
            button.text = option.value
            buttonGroup.addView(button)
        }
        buttonGroup.check(checkedIndex)
        buttonGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == -1) return@setOnCheckedChangeListener
            stateChangeCallback(checkedId)
        }
    }
}