package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private String messageReceiverID,messageReceiverName,messageReceiverImage,messageSenderID;
    private TextView userName,userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private ImageButton SendMessageButton;
    private EditText MessageInput;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private final List<Messages>messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private RecyclerView userMessagesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverID=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage=getIntent().getExtras().get("visit_image").toString();

       InitializeController();

       userName.setText(messageReceiverName);
       Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

       SendMessageButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v)
           {
               SendMessage();
           }
       });
    }

    private void InitializeController()
    {

        ChatToolBar=(Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =layoutInflater.inflate(R.layout.costom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView)findViewById(R.id.costom_profile_image);
        userName=(TextView)findViewById(R.id.costom_profile_name);
        userLastSeen=(TextView)findViewById(R.id.costom_user_last_seen);

        SendMessageButton=(ImageButton)findViewById(R.id.send_message_btn);
        MessageInput=(EditText)findViewById(R.id.input_message);

        messagesAdapter=new MessagesAdapter(messagesList);
        userMessagesList=(RecyclerView)findViewById(R.id.message_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        RootRef.child("Message").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages=dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage()
    {
        String MessageText=MessageInput.getText().toString();
        if(TextUtils.isEmpty(MessageText))
        {
            Toast.makeText(this, "First write your message..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef="Message/"+messageSenderID+"/"+messageReceiverID;
            String messageReceiverRef="Message/"+messageReceiverID+"/"+messageSenderID;

            DatabaseReference userMessageKeyRef=RootRef.child("Message")
                    .child(messageSenderID).child(messageReceiverID).push();
            String messagePushID=userMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",MessageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef +"/"+messagePushID,messageTextBody);
            messageBodyDetails.put(messageReceiverRef +"/"+messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfuly...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    MessageInput.setText("");

                }
            });

        }
    }

}
