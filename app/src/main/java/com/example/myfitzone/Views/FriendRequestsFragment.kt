package com.example.myfitzone.Views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfitzone.Callbacks.FirestoreGetCompleteAny
import com.example.myfitzone.DataModels.Friend
import com.example.myfitzone.Models.DashboardModel
import com.example.myfitzone.Models.FriendsModel
import com.example.myfitzone.R
import com.example.myfitzone.databinding.FragmentFriendsBinding
import com.example.myfitzone.databinding.FriendItemViewBinding
import java.text.SimpleDateFormat

class FriendRequestsFragment: Fragment() {
    private val TAG = "FriendRequestsFragment"

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardModel: DashboardModel
    private lateinit var friendsModel: FriendsModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardModel = ViewModelProvider(requireActivity())[DashboardModel::class.java]
        friendsModel = ViewModelProvider(requireActivity())[FriendsModel::class.java]
        binding.backButtonFriends.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        binding.searchViewFriends.visibility = View.GONE
        binding.titleFriends.text = "Friend Requests"
        dashboardModel.getFriendRequestLiveList().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.noFriendsTextFriends.visibility = View.GONE
                binding.recyclerViewFriends.apply {
                    visibility = View.VISIBLE
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = FriendsAdapter(it)
                }
            }
            else{
                binding.recyclerViewFriends.visibility = View.GONE
                binding.noFriendsTextFriends.visibility = View.VISIBLE
                binding.noFriendsTextFriends.text = "No Friend Requests"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class FriendsAdapter(friend: ArrayList<Friend>) : RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>(){

        var friend = friend

        inner class FriendsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val binding = FriendItemViewBinding.bind(itemView)
            fun bind(){
                val sdf = SimpleDateFormat("MM/dd/yyyy")
                binding.apply {
                    nameFriendItemView.text = friend[bindingAdapterPosition].name
                    usernameFriendItemView.text = friend[bindingAdapterPosition].username
                    when (friend[bindingAdapterPosition].status) {
                        "pendingFROM" ->{
                            dateFriendItemView.visibility = View.VISIBLE
                            dateFriendItemView.text =
                                "Request Received on: ${sdf.format(friend[bindingAdapterPosition].dateAdded)}"
                            declineButtonFriendItemView.apply {
                                visibility = View.VISIBLE
                                background =
                                    resources.getDrawable(R.drawable.baseline_close_24)
                                setOnClickListener {
                                    declineFriendRequest()
                                }
                            }
                            addRemoveCancelButtonFriendItemView.apply {
                                background =
                                    resources.getDrawable(R.drawable.baseline_check_24)
                                setOnClickListener {
                                    acceptFriendRequest()
                                }
                            }
                        }
                    }
                }
            }

            private fun declineFriendRequest() {
                friendsModel.declineFriendRequest(friend[bindingAdapterPosition], object :
                    FirestoreGetCompleteAny {
                    override fun onGetComplete(result: Any) {
                        Toast.makeText(requireContext(), "Friend Request Declined", Toast.LENGTH_SHORT).show()
                    }

                    override fun onGetFailure(string: String) {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }

                })
            }

            private fun acceptFriendRequest() {
                friendsModel.acceptFriendRequest(friend[bindingAdapterPosition], object :
                    FirestoreGetCompleteAny {
                    override fun onGetComplete(result: Any) {
                        Toast.makeText(requireContext(), "Friend Request Accepted", Toast.LENGTH_SHORT).show()
                    }

                    override fun onGetFailure(string: String) {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
            FriendItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                return FriendsViewHolder(root)
            }
        }

        override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {

            holder.bind()
        }

        override fun getItemCount(): Int {
            return 1
        }

    }
}