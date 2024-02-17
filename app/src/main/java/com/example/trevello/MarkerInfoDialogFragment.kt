package com.example.trevello// com.example.trevello.MarkerInfoDialogFragment.kt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MarkerInfoDialogFragment : BottomSheetDialogFragment() {
    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    private lateinit var tvEntryTitle: TextView
    private lateinit var tvEntryLocation: TextView
    private lateinit var tvEntryDescription: TextView
    private lateinit var vpEntryImages: ViewPager2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_marker_info, container, false)

        tvEntryTitle = view.findViewById(R.id.tvEntryTitle)
        tvEntryLocation = view.findViewById(R.id.tvEntryLocation)
        tvEntryDescription = view.findViewById(R.id.tvEntryDescription)
        vpEntryImages = view.findViewById(R.id.vpEntryImages)

        val title = arguments?.getString("title")
        var location = arguments?.getString("location")
        if (location != null && location.length > 30) {
            location = location.substring(0, 30) + "..."
        }
        val description = arguments?.getString("description")
        val images = arguments?.getStringArrayList("images")

        // Update the ui with the values
        tvEntryTitle.text = title
        tvEntryLocation.text = location
        tvEntryDescription.text = description

        // Create an instance of UrlImageCarouselAdapter and set it to the ViewPager2
        if (images != null) {
            val imageUrls = images.toMutableList()
            val urlImageCarouselAdapter = UrlImageCarouselAdapter(imageUrls)
            vpEntryImages.adapter = urlImageCarouselAdapter
        }

        return view
    }

    companion object {
        fun newInstance(title: String, location: String, description: String, images: ArrayList<String>): MarkerInfoDialogFragment {
            val args = Bundle()
            args.putString("title", title)
            args.putString("location", location)
            args.putString("description", description)
            args.putStringArrayList("images", images)
            val fragment = MarkerInfoDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}