package space.celestia.mobilecelestia.resource

import android.os.Bundle
import android.view.View
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.resource.model.GuideItem
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.utils.commonHandler
import javax.inject.Inject

@AndroidEntryPoint
class GuideListFragment: AsyncListFragment<GuideItem>() {
    @Inject
    lateinit var celestiaLanguage: String
    @Inject
    lateinit var resourceAPI: ResourceAPIService

    private lateinit var type: String
    private lateinit var listTitle: String
    private lateinit var errorMessage: String

    override val defaultErrorMessage: String
        get() = errorMessage

    override val shouldAddItemDecoration: Boolean
        get() = false

    override fun createViewHolder(listener: Listener<GuideItem>?): BaseAsyncListAdapter<GuideItem> {
        return PlainAsyncListAdapter(listener)
    }

    override suspend fun refresh(): List<GuideItem> {
        return resourceAPI.guides(type, celestiaLanguage).commonHandler(object: TypeToken<ArrayList<GuideItem>>() {}.type, ResourceAPI.gson)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = requireArguments().getString(ARG_TYPE, "")
        listTitle = requireArguments().getString(ARG_TITLE, "")
        errorMessage = requireArguments().getString(ARG_ERROR_MESSAGE, "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = listTitle
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val ARG_TITLE = "title"
        private const val ARG_ERROR_MESSAGE = "error_message"

        fun newInstance(type: String, title: String, errorMessage: String) = GuideListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TYPE, type)
                putString(ARG_TITLE, title)
                putString(ARG_ERROR_MESSAGE, errorMessage)
            }
        }
    }
}