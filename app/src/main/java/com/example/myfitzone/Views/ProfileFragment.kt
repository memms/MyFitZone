package com.example.myfitzone.Views

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.marginEnd
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.DashboardRecyclerData
import com.example.myfitzone.DataModels.PublicSocialData
import com.example.myfitzone.DataModels.User
import com.example.myfitzone.Models.AuthenticationModel
import com.example.myfitzone.Models.FriendsModel
import com.example.myfitzone.Models.SocialDataModel
import com.example.myfitzone.R
import com.example.myfitzone.Utils.dpToPx
import com.example.myfitzone.databinding.DashboardCardItemViewBinding
import com.example.myfitzone.databinding.DashboardCardWrapContentItemViewBinding
import com.example.myfitzone.databinding.FragmentProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar
import kotlin.math.roundToInt

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authmodel: AuthenticationModel
    private lateinit var socialDataModel: SocialDataModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socialDataModel = ViewModelProvider(requireActivity())[SocialDataModel::class.java]
        authmodel = ViewModelProvider(requireActivity())[AuthenticationModel::class.java]
        socialDataModel.getMyUserInfo(callback = object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                Log.d("ProfileFragment", "onGetComplete: $result")
                result.let { data ->
                    val userData = data as User
                    binding.nameProfile.text = "${userData.name["first"]} ${userData.name["last"]}"
                    binding.usernameProfile.text = userData.username
                    binding.bioProfile.text = userData.bio
                }
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
            }

        })
        binding.editButtonProfile.setOnClickListener {
            showBottomSheetDialog()
        }
        binding.backButtonProfile.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val hzLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        socialDataModel.getCurrentUserProfile(callback = object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                Log.d("ProfileFragment", "onGetComplete: $result")
                result.let { list ->
                    val data = list as ArrayList<PublicSocialData>
                    val bodyData = data.filter { it.type == "bodyMeasure" }
                    val exerciseData = data.filter { it.type == "exercise" }
                    if(bodyData.isEmpty()){
                        binding.myCardsBodyProfile.visibility = View.GONE
                        binding.cardTopReyclerViewProfile.visibility = View.GONE
                        binding.leftRecyclerViewButtonProfile.visibility = View.GONE
                        binding.rightRecyclerViewButtonProfile.visibility = View.GONE
                    }
                    if(exerciseData.isEmpty()){
                        binding.myCardsExerciseProfile.visibility = View.GONE
                        binding.cardBottomReyclerViewProfile.visibility = View.GONE
                    }
                    binding.cardTopReyclerViewProfile.apply {
                        layoutManager = hzLayoutManager
                        adapter = ProfileCardCustomRecyclerAdapter(bodyData)
                    }
                    binding.cardBottomReyclerViewProfile.apply {
                        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        adapter = ProfileCardRecyclerAdapter(exerciseData)
                    }

                }
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
            }

        })
        binding.leftRecyclerViewButtonProfile.setOnClickListener {
            if(hzLayoutManager.findFirstVisibleItemPosition() > 0){
                binding.cardTopReyclerViewProfile.smoothScrollToPosition(hzLayoutManager.findFirstVisibleItemPosition() - 1)
            }
            else{
                binding.cardTopReyclerViewProfile.smoothScrollToPosition(0)
            }
        }
        binding.rightRecyclerViewButtonProfile.setOnClickListener {
            binding.cardTopReyclerViewProfile.smoothScrollToPosition(hzLayoutManager.findLastVisibleItemPosition() + 1)
        }
    }

//    <LinearLayout
//    android:id="@+id/first_row_profile_diag"
//    android:layout_width="match_parent"
//    android:layout_height="wrap_content"
//    android:orientation="horizontal"
//    android:padding="8dp">
//
//    <ImageView
//    android:layout_width="24dp"
//    android:layout_height="24dp"
//    android:layout_gravity="center_vertical"
//    android:src="@drawable/placeholder_banner"
//    android:layout_marginEnd="8dp"/>
//
//    <TextView
//    android:layout_width="wrap_content"
//    android:layout_height="wrap_content"
//    android:text="Edit Profile"
//    android:layout_gravity="center_vertical"
//    android:padding="8dp"/>
//
//    </LinearLayout>
    private fun showBottomSheetDialog() {
        val bottomSheetFragment: BottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetFragment.setContentView(R.layout.dialog_profile)
        bottomSheetFragment.show()
        val arrayListTitles = arrayListOf("Edit Username", "Edit Name", "Edit Bio", "Edit Profile Picture","Edit Email",  "Edit Password", "Send Email Verification")
        //TODO: add appropriate image
        val arrayListImages = arrayListOf(R.drawable.placeholder_banner,R.drawable.placeholder_banner,R.drawable.placeholder_banner,R.drawable.placeholder_banner
            ,R.drawable.placeholder_banner,R.drawable.placeholder_banner,R.drawable.placeholder_banner)
        for(arrayItem in arrayListTitles){
            val linearLayout = LinearLayout(requireContext())
            linearLayout.apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.HORIZONTAL
                setPadding(8,8,8,8)
                addView(ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(24.dpToPx(), 24.dpToPx()).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        marginEnd = 8.dpToPx()
                    }
                    setImageResource(arrayListImages[arrayListTitles.indexOf(arrayItem)])
                })
                addView(TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    text = arrayItem
                    setPadding(8,8,8,8)
                    gravity = View.TEXT_ALIGNMENT_CENTER
                })
                setOnClickListener {
                    bottomSheetFragment.dismiss()
                    view?.findNavController()
                        ?.navigate(R.id.action_profileFragment_to_editInfoFragment, bundleOf("editType" to arrayItem))
                }
            }
            bottomSheetFragment.findViewById<LinearLayout>(R.id.dialog_profile)!!.addView(linearLayout)
        }

        bottomSheetFragment.findViewById<LinearLayout>(R.id.logout_row_profile_diag)?.let {
            bottomSheetFragment.findViewById<LinearLayout>(R.id.dialog_profile)!!.removeView(it)
            bottomSheetFragment.findViewById<LinearLayout>(R.id.dialog_profile)!!.addView(it)
        }
        bottomSheetFragment.findViewById<LinearLayout>(R.id.logout_row_profile_diag)?.setOnClickListener {
            bottomSheetFragment.dismiss()
            logout()
        }

    }


    fun logout(){
        authmodel.logout()
    }

    fun editProfile(){
        view?.findNavController()?.navigate(R.id.action_profileFragment_to_editInfoFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }


    inner class ProfileCardRecyclerAdapter(cardList: List<PublicSocialData>) :
        RecyclerView.Adapter<ProfileCardRecyclerAdapter.ProfileCardViewHolder>() {

        private var profileCardList: List<PublicSocialData> = cardList

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ProfileCardViewHolder {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.dashboard_card_item_view, parent, false)
            val binding =
                DashboardCardItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ProfileCardViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: ProfileCardViewHolder,
            position: Int
        ) {
            Log.d("ProfileFragment", "onBindViewHolder: ${profileCardList[position]}")
            holder.view.cardName.text = profileCardList[position].name
            holder.view.cardValue.text = profileCardList[position].value
            holder.view.cardUnit.text = profileCardList[position].unit
            val calendarNow = Calendar.getInstance()
            val diff = calendarNow.timeInMillis - profileCardList[position].updated
            val days = diff / (24 * 60 * 60 * 1000)
            val hours = diff / (60 * 60 * 1000) % 24
            val minutes = diff / (60 * 1000) % 60
            val seconds = diff / 1000 % 60
            holder.view.cardUpdated.text = when {
                days > 0 ->  "From: $days days ago"
                hours > 0 ->  "From: $hours hours ago"
                minutes > 0 ->  "From: $minutes minutes ago"
                seconds > 0 -> "From: $seconds seconds ago"
                else -> "From: Just now"
            }
//            TODO: Uncomment this when the images are added to the project
//            holder.view.cardLogo.setImageResource(dashCardList[position].cardLogo.toInt())


        }

        override fun getItemCount(): Int {
            return profileCardList.size
        }

        inner class ProfileCardViewHolder(val view: DashboardCardItemViewBinding) :
            RecyclerView.ViewHolder(view.root), View.OnClickListener {
            init {
                view.cardViewDashboard.setOnClickListener(this)
                view.cardMore.setOnClickListener(this)
            }
            override fun onClick(view: View?) {
                when (view?.id) {
                    R.id.card_more -> {
                        Log.d("DashCardViewHolder", "onClick: More")
//                        inflateMoreDialog(dashCardList[bindingAdapterPosition])
                    }
                    R.id.cardView_dashboard -> {
                        Log.d("DashCardViewHolder", "onClick: Card")
                    }
                }
            }

        }
    }

    inner class ProfileCardCustomRecyclerAdapter(cardList: List<PublicSocialData>) :
        RecyclerView.Adapter<ProfileCardCustomRecyclerAdapter.ProfileCardCustomViewHolder>() {

        private var profileCardList: List<PublicSocialData> = cardList

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ProfileCardCustomViewHolder {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.dashboard_card_item_view, parent, false)
            val binding =
                DashboardCardWrapContentItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ProfileCardCustomViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: ProfileCardCustomViewHolder,
            position: Int
        ) {
            Log.d("ProfileFragment", "onBindViewHolder: ${profileCardList[position]}")
            holder.view.cardName.text = profileCardList[position].name
            holder.view.cardValue.text = profileCardList[position].value
            holder.view.cardUnit.text = profileCardList[position].unit
            val calendarNow = Calendar.getInstance()
            val diff = calendarNow.timeInMillis - profileCardList[position].updated
            val days = diff / (24 * 60 * 60 * 1000)
            val hours = diff / (60 * 60 * 1000) % 24
            val minutes = diff / (60 * 1000) % 60
            val seconds = diff / 1000 % 60
            holder.view.cardUpdated.text = when {
                days > 0 ->  "From: $days days ago"
                hours > 0 ->  "From: $hours hours ago"
                minutes > 0 ->  "From: $minutes minutes ago"
                seconds > 0 -> "From: $seconds seconds ago"
                else -> "From: Just now"
            }
//            TODO: Uncomment this when the images are added to the project
//            holder.view.cardLogo.setImageResource(dashCardList[position].cardLogo.toInt())


        }

        override fun getItemCount(): Int {
            return profileCardList.size
        }

        inner class ProfileCardCustomViewHolder(val view: DashboardCardWrapContentItemViewBinding) :
            RecyclerView.ViewHolder(view.root), View.OnClickListener {
            init {
                view.cardViewDashboard.setOnClickListener(this)
                view.cardMore.setOnClickListener(this)
            }
            override fun onClick(view: View?) {
                when (view?.id) {
                    R.id.card_more -> {
                        Log.d("DashCardViewHolder", "onClick: More")
//                        inflateMoreDialog(dashCardList[bindingAdapterPosition])
                    }
                    R.id.cardView_dashboard -> {
                        Log.d("DashCardViewHolder", "onClick: Card")
                    }
                }
            }

        }
    }

//    private fun inflateMoreDialog(dashboardRecyclerData: DashboardRecyclerData){
//        val bottomSheetFragment: BottomSheetDialog = BottomSheetDialog(requireContext())
//        val moreView = DialogDashboardMoreBinding.inflate(layoutInflater)
//        bottomSheetFragment.setContentView(moreView.root)
//        bottomSheetFragment.show()
//        moreView.deleteRowDashMoreDiag.setOnClickListener {
//            dashboardModel.deleteDashboard(dashboardRecyclerData, callback = object :
//                FirestoreGetCompleteAny {
//                override fun onGetComplete(result: Any) {
//                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
//                    bottomSheetFragment.dismiss()
//                }
//
//                override fun onGetFailure(string: String) {
//                    Toast.makeText(requireContext(), "Error: $string", Toast.LENGTH_SHORT).show()
//                }
//
//            })
//        }
//    }


}