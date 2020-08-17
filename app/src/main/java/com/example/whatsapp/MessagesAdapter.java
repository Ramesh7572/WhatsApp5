package com.example.whatsapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
   private List<Messages>userMessagesList;
   private FirebaseAuth mAuth;
   private DatabaseReference usersRef;
   public MessagesAdapter(List<Messages>userMessagesList)
   {
       this.userMessagesList=userMessagesList;
   }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessagesText,receiverMessagesText;
        public CircleImageView receiverProofileImage;
        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessagesText=(TextView)itemView.findViewById(R.id.sender_message_text);
            receiverMessagesText=(TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverProofileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);
        mAuth=FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }





    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int position)
    {
        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);

        String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();

        usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage=dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProofileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        if(fromMessageType.equals("text"))
        {
            messageViewHolder.receiverMessagesText.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverProofileImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessagesText.setVisibility(View.INVISIBLE);

            if(fromUserID.equals(messageSenderId))
            {
                messageViewHolder.senderMessagesText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessagesText.setBackgroundResource(R.drawable.sender_messages_layout);
                //messageViewHolder.senderMessagesText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessagesText.setText(messages.getMessage());
            }
            else
            {
                messageViewHolder.receiverProofileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessagesText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessagesText.setBackgroundResource(R.drawable.receiver_message_layout);
                //messageViewHolder.receiverMessagesText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessagesText.setText(messages.getMessage());
            }
        }

    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }
}
