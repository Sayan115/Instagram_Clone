package com.example.instagram;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.autocomplete.Hashtag;
import com.hendraanggrian.appcompat.socialview.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    private Uri imageUri;
    private String imageUrl;
    private ImageView close, imageAdded;
    private TextView post;
    SocialAutoCompleteTextView description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        close =findViewById(R.id.close);
        imageAdded=findViewById(R.id.imageAdded);
        post=findViewById(R.id.post);
        description=findViewById(R.id.description);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this,MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload();
            }
        });
        CropImage.activity().setAspectRatio(1,1).start(PostActivity.this);
    }

    private void upload() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        if(imageUri!=null){
            StorageReference filePath= FirebaseStorage.getInstance().getReference("Posts").child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            StorageTask uploadTask=filePath.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri = task.getResult();
                    imageUrl = downloadUri.toString();

                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                    String postId=ref.push().getKey();

                    HashMap<String, Object> map=new HashMap<>();
                    map.put("postId",postId);
                    map.put("imageUrl",imageUrl);
                    map.put("description",description.getText().toString());
                    map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    ref.child(postId).setValue(map);

                    DatabaseReference mHash=FirebaseDatabase.getInstance().getReference().child("HashTags");
                    List<String>hashTags=description.getHashtags();
                    if(!hashTags.isEmpty()){
                        for(String tag:hashTags){
                            map.clear();

                            map.put("tag",tag.toLowerCase());
                            map.put("postid",postId);
                            mHash.child(tag.toLowerCase()).child(postId).setValue(map);
                        }
                    }
                    pd.dismiss();
                    startActivity(new Intent(PostActivity.this, MainActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(this, "No image was selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri imageUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(imageUri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            imageUri=result.getUri();
            imageAdded.setImageURI(imageUri);
        }
        else{
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        ArrayAdapter<Hashtag>hashtagArrayAdapter=new HashtagArrayAdapter<>(getApplicationContext());
        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    hashtagArrayAdapter.add(new Hashtag(dataSnapshot.getKey(),(int)dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        description.setHashtagAdapter(hashtagArrayAdapter);
    }
}