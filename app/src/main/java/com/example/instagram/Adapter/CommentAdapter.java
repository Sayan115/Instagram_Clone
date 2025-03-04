package com.example.instagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.MainActivity;
import com.example.instagram.Model.Comment;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context mContext;
    private List<Comment> mComments;
    private FirebaseUser fUser;
    String postId;

    public CommentAdapter(Context mContext, List<Comment> mComments, String postId) {
        this.mContext = mContext;
        this.mComments = mComments;
        this.postId=postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.comment_item,parent,false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        fUser=FirebaseAuth.getInstance().getCurrentUser();
        Comment comment=mComments.get(position);
        holder.comment.setText(comment.getComment());

        FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                holder.username.setText(user.getUsername());
                if(user.getImgUrl().equals("default")){
                    holder.imageProfile.setImageResource(R.drawable.ic_person);
                }
                else
                    Picasso.get().load(user.getImgUrl()).into(holder.imageProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId",comment.getPublisher());
                mContext.startActivity(intent);

            }
        });
        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId",comment.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(comment.getPublisher().endsWith(fUser.getUid())){
                    AlertDialog alertDialog=new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Do you want to delete?");
                    alertDialog.setButton(alertDialog.BUTTON_NEUTRAL, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialog.setButton(alertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Comments").child(postId).child(comment.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(mContext, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                                                dialogInterface.dismiss();
                                            }
                                        }
                                    });
                        }
                    });
                    alertDialog.show();

                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imageProfile;
        public TextView username, comment;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile=itemView.findViewById(R.id.imageProfile);
            username=itemView.findViewById(R.id.username);
            comment=itemView.findViewById(R.id.comment);


        }
    }
}
