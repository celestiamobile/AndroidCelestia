package space.celestia.mobilecelestia.common

import android.view.View
import android.widget.RadioGroup
import android.widget.Space
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.radiobutton.MaterialRadioButton
import space.celestia.mobilecelestia.R

class RadioButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val title: TextView
        get() = itemView.findViewById(R.id.title)
    private val subtitle: TextView
        get() = itemView.findViewById(R.id.subtitle)
    private val buttonGroup: RadioGroup
        get() = itemView.findViewById(R.id.button_group)

    private val gap: Space
        get() = itemView.findViewById(R.id.gap)

    fun configure(text: String, description: String? = null, showTitle: Boolean, options: List<String>, checkedIndex: Int, stateChangeCallback: (Int) -> Unit) {
        title.text = text
        title.visibility = if (showTitle) View.VISIBLE else View.GONE
        subtitle.visibility = if (description != null) View.VISIBLE else View.GONE
        subtitle.text = description
        gap.visibility = if (showTitle || description != null) View.VISIBLE else View.GONE
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