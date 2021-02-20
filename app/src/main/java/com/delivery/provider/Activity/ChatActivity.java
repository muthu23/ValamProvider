package com.delivery.provider.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.delivery.provider.Adapter.ChatMessageAdapter;
import com.delivery.provider.Bean.Chat;
import com.delivery.provider.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class ChatActivity extends AppCompatActivity {

    private ChatMessageAdapter mAdapter;

    String chatPath = null;
    String userId = null;
    String providerId = null;
    ListView chatLv;
    EditText message;
    ImageView send,gallery,camera;
    LinearLayout chatControlsLayout;

    private DatabaseReference myRef;
    public static String sender = "provider";
    StorageReference storageRef;
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatLv = findViewById(R.id.chat_lv);
        message = findViewById(R.id.message);
        send = findViewById(R.id.send);
        camera = findViewById(R.id.camera);
        gallery = findViewById(R.id.gallery);

        chatControlsLayout = findViewById(R.id.chat_controls_layout);
        storageRef = FirebaseStorage.getInstance().getReference();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chatPath = extras.getString("request_id", null);
            userId = extras.getString("user_id", null);
            providerId = extras.getString("provider_id", null);
            initChatView(chatPath);
        }
        camera.setOnClickListener(view -> {
            cameraIntent();
        });
        gallery.setOnClickListener(view -> {
            galleryIntent();
        });


    }

    private void cameraIntent() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                EasyImage.openCameraForImage(ChatActivity.this, 0);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
            }
        } else {
            EasyImage.openCameraForImage(ChatActivity.this, 0);
        }
    }

    private void galleryIntent() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                EasyImage.openGallery(ChatActivity.this, 0);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
            }
        } else {
            EasyImage.openGallery(ChatActivity.this, 0);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                sendFile(imageFiles.get(0), "image");
                //sendpost("Sent a file");
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean permission1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean permission2 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permission1 && permission2) {
                        cameraIntent();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please give permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }



    private void initChatView(String chatPath) {
        if (chatPath == null) return;

        message.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String myText = message.getText().toString().trim();
                if (myText.length() > 0) sendMessage(myText);
                handled = true;
            }
            return handled;
        });

        send.setOnClickListener(v -> {
            String myText = message.getText().toString();
            if (myText.length() > 0) sendMessage(myText);
        });

        mAdapter = new ChatMessageAdapter(this, new ArrayList<>());
        chatLv.setAdapter(mAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(chatPath)/*.child("chat")*/;
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                Chat mChat = dataSnapshot.getValue(Chat.class);
                if (Objects.requireNonNull(mChat).getSender() != null && mChat.getRead() != null)
                    if (!mChat.getSender().equals(sender) && mChat.getRead() == 0) {
                        mChat.setRead(1);
                        dataSnapshot.getRef().setValue(mChat);
                    }
                mAdapter.add(mChat);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendFile(File file, final String type) {

        if (myRef == null) {
            return;
        }

        StorageReference chatStorageRef = storageRef.child(chatPath + "/" + file.getName());
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, getString(R.string.uploading), Toast.LENGTH_SHORT).show();
        UploadTask uploadTask = chatStorageRef.putStream(stream);
        uploadTask.addOnFailureListener(exception -> Toast.makeText(ChatActivity.this, exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show()).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
            while (!uri.isComplete()) ;
            Uri downloadUrl = uri.getResult();
            Chat chat = new Chat();
            chat.setSender(sender);
            chat.setTimestamp(new Date().getTime());
            chat.setType(type);
            chat.setUrl(String.valueOf(downloadUrl));
            chat.setDriverId(Integer.valueOf(providerId));
            chat.setUserId(Integer.valueOf(userId));
            chat.setRead(0);
            chat.setText("sent a file");
            myRef.push().setValue(chat);
        });

    }

    private void sendMessage(String messageStr) {
        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setTimestamp(new Date().getTime());
        chat.setType("text");
        chat.setText(messageStr);
        chat.setRead(0);
        chat.setUrl("");
        chat.setDriverId(Integer.valueOf(providerId));
        chat.setUserId(Integer.valueOf(userId));
        myRef.push().setValue(chat);
        message.setText("");
        /*Call<Object> call = RetrofitClient.getAPIClient().postChatItem("provider", userId, messageStr);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, retrofit2.Response<Object> response) {
                System.out.println("RRR o = " + response);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
            }
        });*/
    }
}