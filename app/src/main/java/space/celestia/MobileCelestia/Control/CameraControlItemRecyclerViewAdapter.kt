package space.celestia.MobileCelestia.Control

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import kotlinx.android.synthetic.main.fragment_camera_control_item.view.*
import space.celestia.MobileCelestia.Common.*
import space.celestia.MobileCelestia.Info.Model.InfoActionItem
import space.celestia.MobileCelestia.R

class CameraControlStepperItem(val title: String, val left: CameraControlAction, val right: CameraControlAction): RecyclerViewItem {
    override val clickable: Boolean
        get() = false

    companion object {
        val staticItems: List<CameraControlStepperItem> by lazy { listOf(
            CameraControlStepperItem("Pitch", CameraControlAction.Pitch0, CameraControlAction.Pitch1),
            CameraControlStepperItem("Yaw", CameraControlAction.Yaw0, CameraControlAction.Yaw1),
            CameraControlStepperItem("Roll", CameraControlAction.Roll0, CameraControlAction.Roll1)
        ) }
    }
}

class CameraControlReverseDirectionItem : RecyclerViewItem {
    val title: String
        get() = "Reverse Direction"
}

private val controlSections by lazy {
    listOf(
        CommonSectionV2(CameraControlStepperItem.staticItems, "", "Long press on stepper to change orientation."),
        CommonSectionV2(listOf(CameraControlReverseDirectionItem()))
    )
}

class CameraControlItemRecyclerViewAdapter(
    private val listener: CameraControlFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(controlSections), SteppeView.Listener {

    override fun stepperTouchUp(view: SteppeView, left: Boolean) {
        (view.tag as? CameraControlStepperItem)?.let {
            listener?.onCameraActionStepperTouchUp(if (left) it.left else it.right)
        }
    }

    override fun stepperTouchDown(view: SteppeView, left: Boolean) {
        (view.tag as? CameraControlStepperItem)?.let {
            listener?.onCameraActionStepperTouchDown(if (left) it.left else it.right)
        }
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is CameraControlReverseDirectionItem) {
            listener?.onCameraActionClicked(CameraControlAction.Reverse)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is CameraControlStepperItem) {
            return STEPPER
        }
        if (item is CameraControlReverseDirectionItem) {
            return TEXT
        }
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == STEPPER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_camera_control_item, parent, false)
            return StepperViewHolder(view)
        }
        if (viewType == TEXT) {
            return CommonTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is CameraControlStepperItem && holder is StepperViewHolder) {
            holder.contentView.text = item.title
            holder.stepper.tag = item
            holder.stepper.listener = this
            return
        }
        if (item is CameraControlReverseDirectionItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            return
        }
        super.bindVH(holder, item)
    }

    inner class StepperViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.title
        var stepper: SteppeView = view.stepper
    }

    companion object {
        const val STEPPER = 0
        const val TEXT = 1
    }
}
