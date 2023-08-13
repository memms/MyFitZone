package com.example.myfitzone.Views

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.Friend
import com.example.myfitzone.DataModels.PublicSocialData
import com.example.myfitzone.Models.DashboardModel
import com.example.myfitzone.Models.LeaderboardModel
import com.example.myfitzone.Models.SocialDataModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentLeaderboardBinding
import com.example.myfitzone.databinding.LeaderboardItemViewBinding

class LeaderboardFragment : Fragment() {

    private val TAG = "LeaderboardFragment"
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var leaderboardModel: LeaderboardModel
    private lateinit var dashboardModel: DashboardModel
    private lateinit var socialDataModel: SocialDataModel
    private var currentLeaderboardName = ""
    private var friendsList: ArrayList<Friend>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding  = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaderboardModel = ViewModelProvider(this)[LeaderboardModel::class.java]
        dashboardModel = ViewModelProvider(requireActivity())[DashboardModel::class.java]
        socialDataModel = ViewModelProvider(requireActivity())[SocialDataModel::class.java]
        friendsList = dashboardModel.getAllFriendsList().clone() as ArrayList<Friend>
        Log.d(TAG, "onViewCreated: ${friendsList.toString()}")
        binding.backButtonLeaderboard.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        if(friendsList.isNullOrEmpty()){
            binding.progressBarLeaderboard.root.visibility = View.GONE
            Toast.makeText(context, "No friends found", Toast.LENGTH_LONG).show()
        }
        else {
            friendsList!!.add(0, Friend("Me", "0", "", "", 0, ""))
            //TODO: ProfilePic
            leaderboardModel.getLeaderboardLiveData().observe(viewLifecycleOwner) { result ->
                //HashMap<String, HashMap<Double, ArrayList<PublicSocialData>>>
                //Leaderboard name -> Hashmap < Score, ArrayList<PublicSocialData>>
                Log.d(TAG, "onViewCreated: $result")
                if (!result.isNullOrEmpty()) {
                    binding.progressBarLeaderboard.root.visibility = View.GONE
                    val data =
                        result as HashMap<String, HashMap<Double, ArrayList<PublicSocialData>>>
                    val map = hashMapOf<Double, ArrayList<PublicSocialData>>()
                    data[currentLeaderboardName]?.forEach {
                        map[it.key] = it.value
                    }
                    val sortedMap = map.toSortedMap(reverseOrder()).toMap()
                    val list = ArrayList<PublicSocialData>()
                    sortedMap.forEach {
                        list.addAll(it.value)
                    }
                    val adapter = LeaderboardAdapter(list, friendsList!!)
                    binding.recyclerViewLeaderboard.visibility = View.VISIBLE
                    binding.recyclerViewLeaderboard.layoutManager =
                        androidx.recyclerview.widget.LinearLayoutManager(
                            context,
                            RecyclerView.VERTICAL,
                            false
                        )
                    binding.recyclerViewLeaderboard.adapter = adapter

                } else {
                    Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
            setupSpinner()
        }
    }

    private fun setupSpinner(){
        socialDataModel.getCurrentUserProfile(callback = object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                val list = result as ArrayList<PublicSocialData>
                friendsList?.get(0)?.uid = list[0].uid
                val spinnerList = ArrayList<String>()
                spinnerList.add("Select a leaderboard")//0
                list.forEach {
                    spinnerList.add(it.name)
                }
                val adapter = object : ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    spinnerList){
                    override fun isEnabled(position: Int): Boolean {
                        return position != 0
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view: TextView =
                            super.getDropDownView(position, convertView, parent) as TextView
                        //set the color of first item in the drop down list to gray
                        if (position == 0) {
                            view.setTextColor(Color.GRAY)
                        } else {
                            //here it is possible to define color for other items by
                            //view.setTextColor(Color.RED)
                        }
                        return view
                    }
                }
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerLeaderboard.adapter = context?.let { adapter }

                binding.spinnerLeaderboard.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val value = parent!!.getItemAtPosition(position).toString()
                        if(value == spinnerList[0]){
                            (view as TextView).setTextColor(Color.GRAY)
                        }
                        else{
                            currentLeaderboardName = value
                            getLeaderboardData(currentLeaderboardName, list[position-1])
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //Do nothing
                    }

                }
            }

            override fun onGetFailure(string: String) {
                Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getLeaderboardData(
        currentLeaderboardName: String,
        publicSocialData: PublicSocialData
    ) {
        binding.progressBarLeaderboard.root.visibility = View.VISIBLE
        binding.progressBarLeaderboard.textViewLoading.visibility = View.VISIBLE
        binding.progressBarLeaderboard.textViewLoading.text = "Loading leaderboard data"
        leaderboardModel.getLeaderboardData(friendsList!!, publicSocialData, currentLeaderboardName, callback = object: FirestoreGetCompleteAny{
            override fun onGetComplete(result: Any) {
                binding.progressBarLeaderboard.root.visibility = View.GONE
            }

            override fun onGetFailure(string: String) {
                binding.progressBarLeaderboard.root.visibility = View.GONE
                Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
            }

        })
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    inner class LeaderboardAdapter(private val list: ArrayList<PublicSocialData>, private val friendsList: ArrayList<Friend>) :
        RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            LeaderboardItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                return ViewHolder(root)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val name = friendsList.find { it.uid == list[position].uid }?.name
            //TODO: Add profile pic
            val profilePic = friendsList.find { it.uid == list[position].uid }?.profilePic
            val userVal = list[position].value.split('\n').toList()
            val userUnits = list[position].unit.split('\n').toList()
            val stringBuilder = StringBuilder()
            for(i in userVal.indices){
                stringBuilder.append(userVal[i])
                stringBuilder.append(" ")
                stringBuilder.append(userUnits[i])
                stringBuilder.append("\n")
            }
            holder.binding.apply {
                root.background = when(position){
                    0%2 -> ContextCompat.getColor(requireContext(), R.color.white).toDrawable()
                    1%2 -> ContextCompat.getColor(requireContext(), R.color.light_grey).toDrawable()
                    else -> ContextCompat.getColor(requireContext(), R.color.white).toDrawable()
                }
                leaderboardRank.text = "${position+1}."
                leaderboardName.text = name
                leaderboardValue.text = list[position].value
                leaderboardValue.text = stringBuilder.toString().trim('\n')
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val binding = LeaderboardItemViewBinding.bind(itemView)
        }
    }


}