package com.example.myfitzone.DataModels

data class Friend(
    var name: String,
    var username: String,
    var UID: String,
    var profilePic: String,
    var dateAdded: Long,
    var status: String
)


//Firestore-root
//--- users
//|
//--- requests (collection)
    //|
    //--- $userOneUid (document)
    //|     |
    //|     --- ownRequests (map)
    //|     |      |
    //|     |      --- $userTwoUid
    //|     |             |
    //|     |             --- displayName: "User Two"
    //|     |             |
    //|     |             --- photoUrl: "https://"
    //|     |             |
    //|     |             --- status: "requested"
    //|     |
    //|     --- friendRequests (map)
    //|     |      |
    //|     |      --- $userThreeUid
    //|     |             |
    //|     |             --- displayName: "User Three"
    //|     |             |
    //|     |             --- photoUrl: "https://"
    //|     |             |
    //|     |             --- status: "requested"
    //|     |
    //|     --- allFriends (map)
    //|            |
    //|            --- //All friends
    //|
    //--- $userTwoUid (document)
    //|     |
    //|     --- ownRequests (map)
    //|     |
    //|     --- friendRequests (map)
    //|            |
    //|            --- $userOneUid
    //|                   |
    //|                   --- displayName: "User One"
    //|                   |
    //|                   --- photoUrl: "https://"
    //|                   |
    //|                   --- status: "requested"
    //|
