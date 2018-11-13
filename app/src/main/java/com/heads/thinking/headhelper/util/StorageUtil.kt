package com.heads.thinking.headhelper.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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

    fun uploadNewsImage(imageBytes: ByteArray, imagePath: String,
                        onComplete: (onSuccess: Boolean, message: String) -> Unit) {
        FirestoreUtil.getCurrentUser {
            storageInstance.reference.child(imagePath).putBytes(imageBytes)
                    .addOnCompleteListener{
                onComplete(it.isSuccessful, it.exception?.message ?: "")
            }
        }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
}