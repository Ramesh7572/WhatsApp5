package com.example.whatsapp;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager myviewPager;
    private TabLayout mytabLayout;
    private TabsAcceessorAdapter myTabAccessorAdaptor;
    private FirebaseUser currentUser;
    private DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        //This will set default actionbar to mToolbar
        setSupportActionBar(mToolbar);
        //This will set the title of ActionBar
        getSupportActionBar().setTitle("WhatsApp");
        //We will now put TabAccessorAdaptor in ViewPager to return the title and fragments for different tab index.
        myviewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabAccessorAdaptor = new TabsAcceessorAdapter(getSupportFragmentManager());
        myviewPager.setAdapter(myTabAccessorAdaptor);

        //We wil pass myviewPager to mytabLayout in order to connect both of them
        mytabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mytabLayout.setupWithViewPager(myviewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            VerifyUserExistence();
        }
    }

    private void VerifyUserExistence() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists())
                {
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_LONG);
                }
                else
                {
                    sendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_option)
        {
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        else if(item.getItemId() == R.id.main_settings_option)
        {
            sendUserToSettingActivity();
        }
        else if(item.getItemId() == R.id.main_create_group_option)
        {
            requestNewGroup();
        }
        else if(item.getItemId() == R.id.main_finds_friends_option)
        {
            sendUserToFindFriendActivity();
        }
        return true;
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g.- Family");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if(!TextUtils.isEmpty(groupName)){
                    createNewGroup(groupName);
                }
            }


        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void createNewGroup(String groupName) {
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this,"Group Created Successfully",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void sendUserToSettingActivity() {
        Intent settingIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingIntent);

    }
    private void sendUserToFindFriendActivity() {
        Intent findFriend = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriend);
    }
}
