package com.heads.thinking.headhelper.mvvm

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.heads.thinking.headhelper.models.Message
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.util.FirestoreUtil

class ChatViewModel: ViewModel() {

    private var messagesListener: ListenerRegistration? = null

    var membersArray: MutableLiveData<HashMap<String, User>> = MutableLiveData<HashMap<String, User>>()
    var messages: MutableLiveData<ArrayList<Message>> = MutableLiveData<ArrayList<Message>>()

    fun getMessagesArray():MutableLiveData<ArrayList<Message>> {
        //TODO updateListener
        return messages
    }

    fun getMembers(): MutableLiveData<HashMap<String, User>> {
        FirestoreUtil.getMemb { isSuccessful, members ->
            if(isSuccessful)
                membersArray.postValue(members)
        }
        return membersArray
    }
}