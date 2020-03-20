package space.celestia.MobileCelestia.Common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import space.celestia.MobileCelestia.R

fun Fragment.push(fragment: Fragment, containerID: Int) = run {
    childFragmentManager.beginTransaction()
        .addToBackStack(childFragmentManager.backStackEntryCount.toString())
        .show(fragment)
        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
        .add(containerID, fragment)
        .commitAllowingStateLoss()
}

fun Fragment.pop() = run {
    childFragmentManager.popBackStack((childFragmentManager.backStackEntryCount - 1).toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE)
}

fun Fragment.replace(fragment: Fragment, containerID: Int) {
    val current = childFragmentManager.findFragmentById(containerID)
    var trans = childFragmentManager.beginTransaction()
    if (current != null) {
        trans = trans.hide(current).remove(current)
    }
    trans.add(containerID, fragment)
    trans.commitAllowingStateLoss()
}

open class TitledFragment : Fragment() {
    open val title: String
        get() = ""
}