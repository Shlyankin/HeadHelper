package com.heads.thinking.headhelper.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*


object StorageUtil {
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference
                .child("users").child(FirebaseAuth.getInstance().currentUser?.uid
                        ?: throw NullPointerException("UID is null."))

    fun uploadProfilePhoto(imageBytes: ByteArray,
                           onSuccess: (imagePath: String) -> Unit) {
        FirestoreUtil.getCurrentUser {
            if (it.profilePicturePath != null && it.profilePicturePath != "")
                storageInstance.reference.child(it.profilePicturePath).delete()
                        .addOnCompleteListener {
                            currentUserRef.child("profilePictures").delete().addOnCompleteListener {
                                val ref = currentUserRef.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
                                ref.putBytes(imageBytes)
                                        .addOnSuccessListener {
                                            onSuccess(ref.path)
                                        }
                            }
                        }
            else
                currentUserRef.child("profilePictures").delete().addOnCompleteListener {
                    val ref = currentUserRef.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
                    ref.putBytes(imageBytes)
                            .addOnSuccessListener {
                                onSuccess(ref.path)
                            }
                }
        }
    }


    fun uploadMessageImage(imageBytes: ByteArray,
                           onSuccess: (imagePath: String) -> Unit) {
        val ref = currentUserRef.child("messages/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
                .addOnSuccessListener {
                    onSuccess(ref.path)
                }
    }

    fun uploadNewsImage(imageBytes: ByteArray, setUrl: (url: String) -> Unit) : UploadTask {
        // или fromBytes, но тогда одинаковые изображения будут храниться одним файлом и при удалении одного удалится фото во всех новостях
        val urlNews : String = UUID.randomUUID().toString()
        setUrl(urlNews)
        return storageInstance.reference.child(urlNews).putBytes(imageBytes)
    }

    fun deleteNewsImage(uriPath: String, OnComplete: (isSuccessful: Boolean) -> Unit) {
            storageInstance.reference.child(uriPath).delete().addOnCompleteListener {
                OnComplete(it.isSuccessful)
            }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
}