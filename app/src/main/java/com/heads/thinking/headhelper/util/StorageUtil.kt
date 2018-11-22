package com.heads.thinking.headhelper.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*


object StorageUtil {
    //ссылка на корень хранилища
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    //ссылка на папку пользователя
    private val currentUserRef: StorageReference
        get() = storageInstance.reference
                .child("users").child(FirebaseAuth.getInstance().currentUser?.uid
                        ?: throw NullPointerException("UID is null."))

    //отправка фото профиля пользователя на сервер
    fun uploadProfilePhoto(imageBytes: ByteArray,
                           onSuccess: (imagePath: String) -> Unit) { //TODO maybe UploadTask?
        val user = FirestoreUtil.currentUser
        if (user?.profilePicturePath != null && user.profilePicturePath != "")
            storageInstance.reference.child(user.profilePicturePath).delete()
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

    // загрузка изображений чата
    fun uploadMessageImage(imageBytes: ByteArray,
                           onSuccess: (imagePath: String) -> Unit) {
        val ref = currentUserRef.child("messages/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
                .addOnSuccessListener {
                    onSuccess(ref.path)
                }
    }

    // отправка  изображения новости
    fun uploadNewsImage(imageBytes: ByteArray, setUrl: (url: String) -> Unit) : UploadTask? {

        val currUser = FirestoreUtil.currentUser
        if(currUser != null) {

            // или fromBytes, но тогда одинаковые изображения будут храниться одним файлом
            // и при удалении одного удалится фото во всех новостях
            val urlNews: String = currUser.groupId + "\\" +UUID.randomUUID().toString()
            setUrl(urlNews)
            return storageInstance.reference.child(urlNews).putBytes(imageBytes)
        }
        return null
    }

    fun deleteNewsImage(uriPath: String, OnComplete: (isSuccessful: Boolean) -> Unit) {
            storageInstance.reference.child(uriPath).delete().addOnCompleteListener {
                OnComplete(it.isSuccessful)
            }
    }

    fun uploadChatImage(imageBytes: ByteArray, setUrl: (url: String) -> Unit) : UploadTask? {
        val curUser = FirestoreUtil.currentUser
        if(curUser != null) {
            // nameUUIDFromBytes, т.к. картинки не удаляются,
            // то мы можем хранить их для всех сообщений как один файл
            val url: String = curUser.groupId + "\\" + UUID.nameUUIDFromBytes(imageBytes)
            setUrl(url)
            return storageInstance.reference.child(url).putBytes(imageBytes)
        }
        return null
    }

    // Получить полный путь к файлу по частичному
    fun pathToReference(path: String) = storageInstance.getReference(path)
}