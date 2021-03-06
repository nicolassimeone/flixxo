package com.masterwok.simplevlcplayer.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.simplevlcplayer.R
import com.masterwok.simplevlcplayer.adapters.SelectionListAdapter
import com.masterwok.simplevlcplayer.common.AndroidJob
import com.masterwok.simplevlcplayer.common.extensions.getCompatColor
import com.masterwok.simplevlcplayer.common.extensions.setColor
import com.masterwok.simplevlcplayer.models.SelectionItem
import com.masterwok.simplevlcplayer.viewmodels.SubtitlesDialogFragmentViewModel
import kotlinx.android.synthetic.main.dialog_subtitles.*
import java.io.Serializable
import javax.inject.Inject

data class Subtitle(var lang: String? = "", val url: String? = "", var langName: String? = "", var isSelected: Boolean? = false): Serializable

data class SerieInteractivePlayer (
    val name: String?,
    val nextEpisodeRight: String? = "",
    val nextEpisodeLeft: String? = "",
    val episodeUrl: String? = "",
    val default: String? = "",
    val showAt: Int?,
    val status: Int?,
    val subtitle: ArrayList<Subtitle>? = arrayListOf()

): Serializable

    class SubtitlesDialogFragment : MediaPlayerServiceDialogFragment() {

    companion object {
        const val Tag = "tag.subtitlesdialogfragment"
        const val OpenSubtitlesUserAgentKey = "key.opensubtitlesuseragent"
        const val SubtitleLanguageCodeKey = "key.subtitlelanguagecode"
        const val MediaNameKey = "key.medianame"
        const val SubtitleUriKey = "key.subtitleuri"
        const val SubtitlesListKey = "key.subtitlelist"
        const val NoneSubtitleIndex = 0
        const val ProvidedSubtitleIndex = 1

        private const val DimAmount = 0.6F

        @JvmStatic
        fun createInstance(
                mediaName: String
                , subtitleUri: Uri?
                , openSubtitlesUserAgent: String?
                , subtitleLanguageCode: String?
                , subtitlesList: ArrayList<Subtitle>?
        ): SubtitlesDialogFragment =
                SubtitlesDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(MediaNameKey, mediaName)
                        putParcelable(SubtitleUriKey, subtitleUri)
                        putString(OpenSubtitlesUserAgentKey, openSubtitlesUserAgent)
                        putString(SubtitleLanguageCodeKey, subtitleLanguageCode)
                        putSerializable(SubtitlesListKey, subtitlesList)
                    }
                }
    }

    @Inject
    lateinit var viewModel: SubtitlesDialogFragmentViewModel

    private lateinit var adapterSubtitles: SelectionListAdapter<OpenSubtitleItem>
    private lateinit var dialogView: View
    private lateinit var mediaName: String
    private lateinit var subtitles: ArrayList<Subtitle>

    private var openSubtitlesUserAgent: String? = null
    private var subtitleLanguageCode: String? = null
    private var subtitleUri: Uri? = null

    private val rootJob: AndroidJob = AndroidJob(lifecycle)

    override fun onServiceConnected() {
        // Intentionally left blank..
    }

    private fun inflateView(): View = requireActivity()
            .layoutInflater
            .inflate(R.layout.dialog_subtitles, null)

    private fun createDialog(view: View): Dialog =
            AlertDialog.Builder(
                    requireActivity()
                    , R.style.AppTheme_DialogDark
            ).apply {
                setView(view)
                setTitle(R.string.dialog_select_subtitles)
                setNegativeButton(R.string.dialog_button_subtitles_negative, null)
            }.create().apply {
                setCanceledOnTouchOutside(false)
                window.setDimAmount(DimAmount)
            }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        dialogView = inflateView()

        return createDialog(dialogView)
    }

    private fun subscribeToViewComponents() {
        listViewSubtitles.onItemClickListener = AdapterView
                .OnItemClickListener { parent: AdapterView<*>
                                       , itemView: View
                                       , position: Int
                                       , id: Long ->
                    onSubtitleSelected(position)
                }
    }

    private fun setLoadingViewState() {
        progressBarSubtitles.visibility = View.VISIBLE
        listViewSubtitles.visibility = View.GONE
        linearLayoutSubtitleError.visibility = View.GONE
    }

    private fun setLoadedViewState() {
        listViewSubtitles.visibility = View.VISIBLE
        progressBarSubtitles.visibility = View.GONE
        linearLayoutSubtitleError.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        progressBarSubtitles.setColor(R.color.progress_bar_spinner)

        mediaName = arguments!!.getString(MediaNameKey)
        subtitleUri = arguments!!.getParcelable(SubtitleUriKey)
        subtitles = arguments!!.getSerializable(SubtitlesListKey) as ArrayList<Subtitle>
        openSubtitlesUserAgent = arguments!!.getString(OpenSubtitlesUserAgentKey)
        subtitleLanguageCode = arguments!!.getString(SubtitleLanguageCodeKey)

        configureListViewAndAdapter()
        subscribeToViewComponents()
//        querySubtitles(
//                mediaName
//                , openSubtitlesUserAgent
//                , subtitleLanguageCode
//        )
    }

    private fun configureListViewAndAdapter() {
        adapterSubtitles = SelectionListAdapter(
                context
                , R.drawable.ic_check_black
                , R.color.player_dialog_check
        )

        listViewSubtitles.adapter = adapterSubtitles

        adapterSubtitles.configure(subtitles)
        setLoadedViewState()
    }

    override fun onCreateView(
            inflater: LayoutInflater
            , container: ViewGroup?
            , savedInstanceState: Bundle?
    ): View? = dialogView

//    private fun querySubtitles(
//            mediaName: String
//            , openSubtitlesUserAgent: String?
//            , subtitleLanguageCode: String?
//    ): Job  {
//        setLoadingViewState()
//
//        try {
//            val subtitles = viewModel.querySubtitles(
//                    mediaName
//                    , openSubtitlesUserAgent
//                    , subtitleLanguageCode
//            )
//
//            adapterSubtitles.configure(
//                    createSubtitleSelectionItemList(subtitles)
//            )
//
//            setLoadedViewState()
//        } catch (ignored: JobCancellationException) {
//            // Nothing to do..
//        } catch (ex: Exception) {
//            setErrorViewState(R.string.dialog_subtitle_error_querying, View.OnClickListener {
//                hideRetryButton()
//                querySubtitles(
//                        mediaName
//                        , openSubtitlesUserAgent
//                        , subtitleLanguageCode
//                )
//            })
//        }
//    }

    private fun showErrorContainer() {
        progressBarSubtitles.visibility = View.GONE
        listViewSubtitles.visibility = View.GONE
        linearLayoutSubtitleError.visibility = View.VISIBLE
    }

    private fun setErrorViewState(
            @StringRes errorId: Int
            , listener: View.OnClickListener?
    ) {
        showErrorContainer()

        textViewSubtitlesError.text = getString(errorId)

        showRetryButton(listener)
    }

    private fun hideRetryButton() {
        (dialog as AlertDialog)
                .getButton(AlertDialog.BUTTON_NEUTRAL)
                .visibility = View.GONE
    }

    private fun showRetryButton(
            listener: View.OnClickListener?
    ) = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL).apply {
        text = context.getString(R.string.dialog_subtitle_error_retry)
        setTextColor(context.getCompatColor(R.color.dialog_subtitle_error))
        visibility = View.VISIBLE

        setOnClickListener(listener)
    }

//    private fun createSubtitleSelectionItemList(
//            subtitles: List<OpenSubtitleItem>
//    ): ArrayList<SelectionItem<OpenSubtitleItem>> = ArrayList(subtitles.map {
//        val downloadUri = viewModel.getSubtitleItemDownloadUri(
//                requireContext()
//                , it
//                , arguments?.getParcelable(DestinationUriKey)
//        )
//
//        SelectionItem(
//                serviceBinder?.selectedSubtitleUri == downloadUri
//                , it.SubFileName
//                , it
//        )
//    }).apply {
//        add(NoneSubtitleIndex, SelectionItem(
//                serviceBinder?.selectedSubtitleUri == null || this.size == 0
//                , getString(R.string.dialog_none)
//                , null
//        ))
//
//        if (subtitleUri == null) {
//            return@apply
//        }
//
//        add(ProvidedSubtitleIndex, SelectionItem(
//                serviceBinder?.selectedSubtitleUri == subtitleUri
//                , subtitleUri?.lastPathSegment
//                , null
//        ))
//    }

    private fun onSubtitleSelected(position: Int) {
        val sub = subtitles[position]
        serviceBinder?.setSubtitle(Uri.parse(sub.url))
        subtitles.forEach {
            it.isSelected = false
            sub.isSelected = true
        }
        dismiss()
//        val selectedItem = adapterSubtitles.getItem(position)
//        val context = context!!
//        val openSubtitleItem = selectedItem?.value
//
//        setLoadingViewState()
//
//        if (openSubtitleItem == null) {
//            when (position) {
//                NoneSubtitleIndex -> serviceBinder?.setSubtitle(null)
//                ProvidedSubtitleIndex -> serviceBinder?.setSubtitle(subtitleUri)
//            }
//
//            dismiss()
//
//            return@launch
//        }
//
//        try {
////            val subtitleUri = viewModel.downloadSubtitleItem(
////                    context
////                    , openSubtitleItem
////                    , arguments?.getParcelable(DestinationUriKey)
////            )
//
//            dismiss()
//
////            serviceBinder?.setSubtitle(subtitleUri)
//
//        } catch (ignored: CancellationException) {
//            // Nothing to do..
//        } catch (ex: Exception) {
//            setErrorViewState(R.string.dialog_subtitle_error_downloading, View.OnClickListener {
//                hideRetryButton()
////                querySubtitles(
////                        mediaName
////                        , openSubtitlesUserAgent
////                        , subtitleLanguageCode
////                )
//            })
//        }
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        val transaction = manager?.beginTransaction()
        transaction?.add(this, tag)
        transaction?.commitAllowingStateLoss()
    }

}