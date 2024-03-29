package com.example.myfitzone.Views.MainViews

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.Callbacks.FirestoreGetCompleteCallbackArrayList
import com.example.myfitzone.DataModels.DashboardRecyclerData
import com.example.myfitzone.Models.DashboardModel
import com.example.myfitzone.Models.UserDetailModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.DashboardCardItemViewBinding
import com.example.myfitzone.databinding.DialogDashboardMoreBinding
import com.example.myfitzone.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar


class HomeFragment : Fragment() {



    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardCardAdapter: DashCardRecyclerAdapter
    private lateinit var recyclerView: RecyclerView
    private var dataList = mutableListOf<DashboardRecyclerData>()
    private lateinit var dashboardModel: DashboardModel
    private val TAG = "HomeFragment"
    private lateinit var loggedInUser : UserDetailModel
    private var isFabOpen = false
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.d(TAG, "handleOnBackPressed: ")
            if (isFabOpen) {
                hideFabMenu()
                Log.d(TAG, "handleOnBackPressed: FAB Open")
            } else {
                requireActivity().finish()
                Log.d(TAG, "handleOnBackPressed: FAB Closed")
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loggedInUser = ViewModelProvider(requireActivity())[UserDetailModel::class.java]

        Log.d(TAG, "onViewCreated: ${loggedInUser.getUser().toString()}")
        binding.DprofileName.text = "Welcome ${loggedInUser.getUser().name["first"]}"
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a")
        binding.datetime.text = dateFormat.format(calendar.time)

        //TODO: Remove Test Data
        //default
//        dataList.add(DashboardRecyclerData("Weight", "", "00", "kg", 0, 0))
//        dataList.add(DashboardRecyclerData("Height", "", "00", "cm", 0, 0))
//        dataList.add(DashboardRecyclerData("Steps", "", "00", "steps", 0, 0))
//        dataList.add(DashboardRecyclerData("Calories", "", "00", "kcal", 0, 0))
//        dataList.add(DashboardRecyclerData("Distance Travelled", "", "0.0", "m", 0, 0))
//
//        //user enabled
//        dataList.add(DashboardRecyclerData("BMI", "", "24.2", "", 0, 0))
//        dataList.add(DashboardRecyclerData("Water", "", "2", "L", 0, 0))
//        dataList.add(DashboardRecyclerData("Sleep", "", "8", "hours", 0, 0))
//        dataList.add(DashboardRecyclerData("Heart Rate", "", "70", "bpm", 0, 0))
//        dataList.add(DashboardRecyclerData("Body Fat", "", "20", "%", 0, 0))
//        dataList.add(DashboardRecyclerData("Blood Pressure", "", "120/80", "mmHg", 0, 0))
//        dataList.add(DashboardRecyclerData("Blood Sugar", "", "100", "mg/dL", 0, 0))
//        dataList.add(DashboardRecyclerData("Body Temperature", "", "98.6", "°F", 0, 0))
//        dataList.add(DashboardRecyclerData("Oxygen Saturation", "", "98", "%", 0, 0))
//        dataList.add(DashboardRecyclerData("Respiratory Rate", "", "16", "breaths/min", 0, 0))
        dashboardModel = ViewModelProvider(requireActivity())[DashboardModel::class.java]
        dashboardModel.getDashLiveData().observe(viewLifecycleOwner) {
            Log.d(TAG, "onViewCreated: ${it.toString()}")
            dataList.clear()
            dataList.addAll(it)
            dashboardCardAdapter.setDashCardList(dataList)
            dashboardCardAdapter.notifyDataSetChanged()
        }
        dashboardModel.getFriendRequestLiveList().observe(viewLifecycleOwner) {
            Log.d(TAG, "onViewCreated: ${it.toString()}")
            if (it.isNotEmpty()) {
                binding.notificationsButtonHome.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.baseline_notifications_active_24, null)
            }
            else{
                binding.notificationsButtonHome.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.baseline_notifications_none_24, null)
            }
        }
        binding.notificationsButtonHome.setOnClickListener {
            Log.d(TAG, "onViewCreated: Notifications Button Clicked")
            onNotificationsButtonClicked()
            dashboardModel.setNotificationsOpened(value = true)
            binding.notificationsButtonHome.background =
                ResourcesCompat.getDrawable(resources, R.drawable.baseline_notifications_24, null)
        }
        recyclerView = binding.recyclerViewHome
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        dashboardCardAdapter = DashCardRecyclerAdapter()
        dashboardCardAdapter.setDashCardList(dataList)
        recyclerView.adapter = dashboardCardAdapter
        getDashboards(Source.CACHE)
        binding.imageProfile.setOnClickListener {
            Log.d(TAG, "onViewCreated: Profile Image Clicked")
            onProfileImageClicked()
        }

        binding.fabHome.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB Clicked")
            onMainFabClicked()
        }

        binding.reloadDashboardButtonHome.setOnClickListener {
            Log.d(TAG, "onViewCreated: Reload Dashboard Button Clicked")
            getDashboards(Source.SERVER)
        }

        setFabClicks()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

    }

    private fun onNotificationsButtonClicked() {
        view?.let {
            it.findNavController().navigate(R.id.action_homeFragment_to_friendRequestsFragment)
        }
    }

    private fun getDashboards(source: Source){
        if(source == Source.CACHE) {
            dashboardModel.getHomePageDashboard(callback = object :
                FirestoreGetCompleteCallbackArrayList {
                override fun onGetComplete(result: ArrayList<String>) {
                }

                override fun onGetFailure(string: String) {
                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                }

            })
        }
        else{
            dashboardModel.updateDashboardValuesSensors(object : FirestoreGetCompleteCallbackArrayList {
                override fun onGetComplete(result: ArrayList<String>) {
                    Log.d(TAG, "onGetComplete: HOMEPAGE updateDashboardValuesSensors")

                }

                override fun onGetFailure(string: String) {
                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onGetFailure: HOMEPAGE updateDashboardValuesSensors")
                }

            })
            dashboardModel.updateDashboardValuesBodyMeasure(object : FirestoreGetCompleteCallbackArrayList {
                override fun onGetComplete(result: ArrayList<String>) {
                    Log.d(TAG, "onGetComplete: HOMEPAGE updateDashboardValuesBodyMeasure")
                    getupdatedDashboard()
                }

                override fun onGetFailure(string: String) {
                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                }

            })
            dashboardModel.updateDashboardValuesExercise(object : FirestoreGetCompleteCallbackArrayList {
                override fun onGetComplete(result: ArrayList<String>) {
                    Log.d(TAG, "onGetComplete: HOMEPAGE updateDashboardValuesExercise")
                    getupdatedDashboard()
                }

                override fun onGetFailure(string: String) {
                    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    private fun getupdatedDashboard(){
        dashboardModel.getHomePageDashboardFromServer(object : FirestoreGetCompleteCallbackArrayList {
            override fun onGetComplete(result: ArrayList<String>) {
                Log.d(TAG, "onGetComplete: $result")
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setFabClicks() {
        binding.viewHomeFABOverlay.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB Overlay Clicked")
            onMainFabClicked()
        }
        binding.linearLayoutHomeFAB1.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB 1 Clicked")
            onFab1Clicked()
        }
        binding.linearLayoutHomeFAB2.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB 2 Clicked")
            onFab2Clicked()
        }
        binding.linearLayoutHomeFAB3.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB 3 Clicked")
            onFab3Clicked()
        }
        binding.linearLayoutHomeFAB4.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB 4 Clicked")
            onFab4Clicked()
        }
        binding.fabHomeFAB1.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB 1 Clicked")
            onFab1Clicked()
        }
        binding.fabHomeFAB2.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB 2 Clicked")
            onFab2Clicked()
        }
        binding.fabHomeFAB3.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB 3 Clicked")
            onFab3Clicked()
        }
        binding.fabHomeFAB4.setOnClickListener {
            Log.d(TAG, "onViewCreated: FAB 4 Clicked")
            onFab4Clicked()
        }
    }

    //Start Exercise
    private fun onFab4Clicked() {
        TODO("Not yet implemented")
    }

    //Add Body Measure
    private fun onFab3Clicked() {
        Log.d(TAG, "onFab3Clicked: ")
        view?.let {
            it.findNavController().navigate(R.id.action_homeFragment_to_bodyMeasureSelectorFragment)
        }
    }

    //Add Exercise
    private fun onFab2Clicked() {
        Log.d(TAG, "onFab2Clicked: ")
        view?.let {
            it.findNavController().navigate(R.id.action_homeFragment_to_exerciseGroupFragment)
        }
    }

    //Add Dashboard Item
    private fun onFab1Clicked() {
        Log.d(TAG, "onFab1Clicked: ")
        view?.let {
            it.findNavController().navigate(R.id.action_homeFragment_to_dashboardTypeSelectorFragment)
        }
    }

    private fun onMainFabClicked() {
        Log.d(TAG, "onFabClicked: ")
        if (isFabOpen) {
            hideFabMenu()
        } else {
            showFabMenu()
        }
    }

    private fun showFabMenu() {
        isFabOpen = true
        binding.viewHomeFABOverlay.visibility = View.VISIBLE
        binding.linearLayoutHomeFAB1.visibility = View.VISIBLE
        binding.linearLayoutHomeFAB2.visibility = View.VISIBLE
        binding.linearLayoutHomeFAB3.visibility = View.VISIBLE
        binding.linearLayoutHomeFAB4.visibility = View.VISIBLE
        binding.fabHome.animate().rotation(135f)
        binding.linearLayoutHomeFAB1.animate().translationY(-resources.getDimension(R.dimen.standard_75))
        binding.linearLayoutHomeFAB2.animate().translationY(-resources.getDimension(R.dimen.standard_145))
        binding.linearLayoutHomeFAB3.animate().translationY(-resources.getDimension(R.dimen.standard_215))
        binding.linearLayoutHomeFAB4.animate().translationY(-resources.getDimension(R.dimen.standard_285))

    }

    private fun hideFabMenu() {
        isFabOpen = false
        binding.viewHomeFABOverlay.visibility = View.GONE
        binding.fabHome.animate().rotation(0f)
        binding.linearLayoutHomeFAB1.animate().translationY(0f)
        binding.linearLayoutHomeFAB2.animate().translationY(0f)
        binding.linearLayoutHomeFAB3.animate().translationY(0f)
        binding.linearLayoutHomeFAB4.animate().translationY(0f).withEndAction {
            binding.linearLayoutHomeFAB1.visibility = View.GONE
            binding.linearLayoutHomeFAB2.visibility = View.GONE
            binding.linearLayoutHomeFAB3.visibility = View.GONE
            binding.linearLayoutHomeFAB4.visibility = View.GONE
        }

    }




    private fun onProfileImageClicked() {
//        TODO("Not yet implemented")
        Log.d(TAG, "onProfileImageClicked: ")
        view?.let {
            it.findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isFabOpen = false
        _binding = null
    }

    inner class DashCardRecyclerAdapter() :
        RecyclerView.Adapter<DashCardRecyclerAdapter.DashCardViewHolder>() {

        private var dashCardList = emptyList<DashboardRecyclerData>()

        internal fun setDashCardList(dashCardList: List<DashboardRecyclerData>) {
            this.dashCardList = dashCardList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DashCardViewHolder {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.dashboard_card_item_view, parent, false)
            val binding =
                DashboardCardItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DashCardViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: DashCardViewHolder,
            position: Int
        ) {

            holder.view.cardName.text = dashCardList[position].cardName
            holder.view.cardValue.text = dashCardList[position].cardValue
            holder.view.cardUnit.text = dashCardList[position].cardUnit
            val calendarNow = Calendar.getInstance()
            val diff = calendarNow.timeInMillis - dashCardList[position].cardUpdated
            val days = diff / (24 * 60 * 60 * 1000)
            val hours = diff / (60 * 60 * 1000) % 24
            val minutes = diff / (60 * 1000) % 60
            val seconds = diff / 1000 % 60
            holder.view.cardUpdated.text = when {
                days > 0 ->  "Updated: $days days ago"
                hours > 0 ->  "Updated: $hours hours ago"
                minutes > 0 ->  "Updated: $minutes minutes ago"
                seconds > 0 -> "Updated: $seconds seconds ago"
                else -> "Updated: Just now"
            }
//            TODO: Uncomment this when the images are added to the project
//            holder.view.cardLogo.setImageResource(dashCardList[position].cardLogo.toInt())


        }

        override fun getItemCount(): Int {

            return dashCardList.size
//            TODO("Not yet implemented")
        }

        inner class DashCardViewHolder(val view: DashboardCardItemViewBinding) :
            RecyclerView.ViewHolder(view.root), View.OnClickListener {
            init {
                view.cardViewDashboard.setOnClickListener(this)
                view.cardMore.setOnClickListener(this)
            }
            override fun onClick(view: View?) {
                when (view?.id) {
                    R.id.card_more -> {
                        Log.d("DashCardViewHolder", "onClick: More")
                        inflateMoreDialog(dashCardList[bindingAdapterPosition])
                    }
                    R.id.cardView_dashboard -> {
                        Log.d("DashCardViewHolder", "onClick: Card")
                        val bundle = Bundle()
                        bundle.putString("name", dashCardList[bindingAdapterPosition].cardName)
                        bundle.putString("type", dashCardList[bindingAdapterPosition].cardType)
                        view?.let {
                            it.findNavController().navigate(R.id.action_homeFragment_to_chartsFragment, bundle)
                        }
                    }
                }
            }

        }
    }

    private fun inflateMoreDialog(dashboardRecyclerData: DashboardRecyclerData){
        val bottomSheetFragment: BottomSheetDialog = BottomSheetDialog(requireContext())
        val moreView = DialogDashboardMoreBinding.inflate(layoutInflater)
        bottomSheetFragment.setContentView(moreView.root)
        bottomSheetFragment.show()
        moreView.deleteRowDashMoreDiag.setOnClickListener {
            dashboardModel.deleteDashboard(dashboardRecyclerData, callback = object : FirestoreGetCompleteAny{
                override fun onGetComplete(result: Any) {
                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                    bottomSheetFragment.dismiss()
                }

                override fun onGetFailure(string: String) {
                    Toast.makeText(requireContext(), "Error: $string", Toast.LENGTH_SHORT).show()
                }

            })
        }
//        bottomSheetFragment.findViewById<LinearLayout>(R.id.first_row_profile_diag)?.setOnClickListener {
//            bottomSheetFragment.dismiss()
//
//        }
    }
}