package com.example.myfitzone.Views.MainViews

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
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

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var friendsModel: FriendsModel
    private lateinit var adapter: FriendsAdapter
    private lateinit var dashboardModel: DashboardModel

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
        friendsModel = ViewModelProvider(requireActivity())[FriendsModel::class.java]
        dashboardModel = ViewModelProvider(requireActivity())[DashboardModel::class.java]

        binding.searchViewFriends.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    friendsModel.getFriendSearchResult(it, object : FirestoreGetCompleteAny{
                        override fun onGetComplete(result: Any) {
                            binding.backSearchButton.visibility = View.VISIBLE
                            if(result == false){
                                Toast.makeText(requireContext(), "No user found", Toast.LENGTH_SHORT).show()
                                Log.d("FriendsFragment", "No user found")
                                binding.recyclerViewFriends.visibility = View.GONE
                                binding.noFriendsTextFriends.visibility = View.VISIBLE
                                binding.noFriendsTextFriends.text = "No user found"
                                binding.searchViewFriends.clearFocus()
                            }
                            else{
                                Log.d("FriendsFragment", "User found $result")
                                result as Friend
                                binding.noFriendsTextFriends.visibility = View.GONE
                                adapter = FriendsAdapter(arrayListOf(result))
                                binding.recyclerViewFriends.apply {
                                    visibility = View.VISIBLE
                                    adapter = this@FriendsFragment.adapter
                                    layoutManager = LinearLayoutManager(requireContext())
                                }
                                adapter.notifyDataSetChanged()
                                binding.searchViewFriends.clearFocus()
                            }
                        }

                        override fun onGetFailure(string: String) {
                            Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                            Log.d("FriendsFragment", "onGetFailure: $string")
                            binding.searchViewFriends.clearFocus()
                        }
                    })
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        binding.backSearchButton.setOnClickListener {
            binding.searchViewFriends.clearFocus()
            getMyFriendsList()
            binding.backSearchButton.visibility = View.GONE
        }
        getMyFriendsList()
    }

    private fun getMyFriendsList(){

        val arrayList = dashboardModel.getAllFriendsList()
        if(arrayList.isEmpty()){
            Toast.makeText(requireContext(), "No user found", Toast.LENGTH_SHORT).show()
            Log.d("FriendsFragment", "No user found")
            binding.recyclerViewFriends.visibility = View.GONE
            binding.noFriendsTextFriends.visibility = View.VISIBLE
        }
        else{
            Log.d("FriendsFragment", "User found $arrayList")
            binding.noFriendsTextFriends.visibility = View.GONE
            adapter = FriendsAdapter(arrayList)
            binding.recyclerViewFriends.apply {
                visibility = View.VISIBLE
                adapter = this@FriendsFragment.adapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            adapter.notifyDataSetChanged()
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
                        "pendingTO" -> {
                            dateFriendItemView.visibility = View.VISIBLE
                            declineButtonFriendItemView.visibility = View.GONE
                            dateFriendItemView.text =
                                "Request Sent on: ${sdf.format(friend[bindingAdapterPosition].dateAdded)}"
                            addRemoveCancelButtonFriendItemView.apply {
                                background =
                                    resources.getDrawable(R.drawable.baseline_person_add_disabled_24)
                                setOnClickListener {
                                    cancelFriendRequest()
                                }
                            }
                        }
                        "none" -> {
                            dateFriendItemView.visibility = View.GONE
                            declineButtonFriendItemView.visibility = View.GONE
                            addRemoveCancelButtonFriendItemView.apply {
                                background =
                                    resources.getDrawable(R.drawable.baseline_person_add_24)
                                setOnClickListener {
                                    sendFriendRequest()
                                }
                            }
                        }
                        "accepted" -> {
                            dateFriendItemView.visibility = View.VISIBLE
                            declineButtonFriendItemView.visibility = View.GONE
                            dateFriendItemView.text =
                                "Friends since: ${sdf.format(friend[bindingAdapterPosition].dateAdded)}"
                            addRemoveCancelButtonFriendItemView.apply {
                                background =
                                    resources.getDrawable(R.drawable.baseline_person_remove_24)
                                setOnClickListener {
                                    removeFriend()
                                }
                            }
                            binding.root.setOnClickListener {
                                view?.let {
                                    it.findNavController().navigate(R.id.action_friendsFragment_to_userProfileFragment, bundleOf("userID" to friend[bindingAdapterPosition].uid))
                                }
                            }
                        }
                    }
                }
            }

            private fun declineFriendRequest() {
                friendsModel.declineFriendRequest(friend[bindingAdapterPosition], object : FirestoreGetCompleteAny{
                    override fun onGetComplete(result: Any) {
                        Toast.makeText(requireContext(), "Friend Request Declined", Toast.LENGTH_SHORT).show()
                        friend[bindingAdapterPosition].status = "none"
                        notifyItemChanged(bindingAdapterPosition)
                    }

                    override fun onGetFailure(string: String) {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }

                })
            }

            private fun acceptFriendRequest() {
                friendsModel.acceptFriendRequest(friend[bindingAdapterPosition], object : FirestoreGetCompleteAny{
                    override fun onGetComplete(result: Any) {
                        Toast.makeText(requireContext(), "Friend Request Accepted", Toast.LENGTH_SHORT).show()
                        friend[bindingAdapterPosition].status = "accepted"
                        notifyItemChanged(bindingAdapterPosition)
                    }

                    override fun onGetFailure(string: String) {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }

                })
            }

            private fun cancelFriendRequest(){
                Log.d("FriendsFragment", "cancelFriendRequest: ${friend[bindingAdapterPosition]}" )
                friendsModel.cancelFriendRequest(friend[bindingAdapterPosition], object : FirestoreGetCompleteAny{
                    override fun onGetComplete(result: Any) {
                        Toast.makeText(requireContext(), "Friend Request Cancelled", Toast.LENGTH_SHORT).show()
                        friend[bindingAdapterPosition].status = "none"
                        notifyItemChanged(bindingAdapterPosition)
                    }

                    override fun onGetFailure(string: String) {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }

                })
            }

            private fun removeFriend(){
                friendsModel.removeFriend(friend[bindingAdapterPosition], object : FirestoreGetCompleteAny{
                    override fun onGetComplete(result: Any) {
                        Toast.makeText(requireContext(), "Friend Removed", Toast.LENGTH_SHORT).show()
                        friend[bindingAdapterPosition].status = "none"
                        notifyItemChanged(bindingAdapterPosition)
                    }

                    override fun onGetFailure(string: String) {
                        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
                    }

                })
            }

            private fun sendFriendRequest(){
                friendsModel.sendFriendRequest(friend[bindingAdapterPosition], object : FirestoreGetCompleteAny{
                    override fun onGetComplete(result: Any) {
                        if (result == "alreadyFriends") {
                            Toast.makeText(requireContext(), "Already Friends", Toast.LENGTH_SHORT)
                                .show()
                            friend[bindingAdapterPosition].status = "accepted"
                            notifyItemChanged(bindingAdapterPosition)
                            return
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Friend Request Sent",
                                Toast.LENGTH_SHORT
                            ).show()
                            friend[bindingAdapterPosition].status = "pendingTO"
                            notifyItemChanged(bindingAdapterPosition)
                        }
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