package com.wkdnotes.root.safedrivescratch.ContactPreview;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wkdnotes.root.safedrivescratch.Adapter.PriorityContactsRecyclerAdapter;
import com.wkdnotes.root.safedrivescratch.Background.BackgroundTask;
import com.wkdnotes.root.safedrivescratch.Database.ContactContract;
import com.wkdnotes.root.safedrivescratch.Database.ContactsDbHelper;
import com.wkdnotes.root.safedrivescratch.R;
import com.wkdnotes.root.safedrivescratch.SharedPref.SharedPreferenceContacts;
import com.wkdnotes.root.safedrivescratch.Util.Contacts;
import com.wkdnotes.root.safedrivescratch.Util.Flag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.dift.ui.SwipeToAction;


public class ShowPriorityContacts extends AppCompatActivity{

    private SharedPreferenceContacts sharedPref;
    private int f=1;
    private ProgressDialog progress;
    private ContactsDbHelper contactsDbHelper;
    private Cursor cursor;
    private SQLiteDatabase sqLiteDatabase;
    private static int flag;
    private PriorityContactsRecyclerAdapter priorityContactsRecyclerAdapter;
    private RecyclerView recyclerView;
    private List<Contacts> contacts;
    private SwipeToAction swipeToAction;
    private Handler handler=null;
    private boolean isUndoClicked=false;
    private ImageView empty_bucket;
    private static final String TAG=ShowPriorityContacts.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.priority_recycler);
        // TODO REFRESH CONTACTS LIST
        recyclerView= (RecyclerView)findViewById(R.id.recycler_priority);
        empty_bucket=findViewById(R.id.iv_empty_contacts);
        initializeData();
        initializeUi();
        handler=new Handler();//used in line 95

        //fab button onclick for show contacts if shared pref is true that means contacts is loaded else load the contacts
        final FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(sharedPref.checkLoad()==false)
                    loadContacts();
                else
                {
                    startActivity(new Intent(ShowPriorityContacts.this,ShowAllContacts.class));
                }

            }
        });

        // perform action on recycler swipe
        swipeToAction=new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<Contacts>() {

            @Override
            public boolean swipeLeft(final Contacts itemData) {
                final int position=removeContact(itemData);
                displaySnackbar(itemData.getName() + " removed", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isUndoClicked=true;
                        addContact(position,itemData);
                    }
                });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isUndoClicked==false)
                        {
                            String num=itemData.getNumber();
                            sqLiteDatabase=contactsDbHelper.getWritableDatabase();
                            sqLiteDatabase.execSQL(ContactContract.ContactEntry.SQL_CREATE_PRIORITY_CONTACTS);
                            long row=sqLiteDatabase.delete(ContactContract.ContactEntry.Table_Priority_Contacts,"number= ?",new String[]{num});

                        }
                    }
                },3500);
                return true;
            }

            @Override
            public boolean swipeRight(final Contacts itemData) {
                final int position=removeContact(itemData);

                displaySnackbar(itemData.getName() + " removed", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isUndoClicked=true;
                        addContact(position,itemData);
                    }
                });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isUndoClicked==false)
                        {
                            String num=itemData.getNumber();
                            sqLiteDatabase=contactsDbHelper.getWritableDatabase();
                            sqLiteDatabase.execSQL(ContactContract.ContactEntry.SQL_CREATE_PRIORITY_CONTACTS);
                            long row=sqLiteDatabase.delete(ContactContract.ContactEntry.Table_Priority_Contacts,"number= ?",new String[]{num});

                        }
                    }
                },3500);

                return true;
            }

            @Override
            public void onClick(Contacts itemData) {
                displaySnackbar(itemData.getName() + " clicked", null, null);
            }

            @Override
            public void onLongClick(Contacts itemData) {
                displaySnackbar(itemData.getName() + " long clicked", null, null);
            }
        });

        sharedPref = new SharedPreferenceContacts(this);
        progress = new ProgressDialog(this);
        flag = 0;
    }

    //remove the contacts from list when swipes and return the position of the deleted contact
    public int removeContact(Contacts c)
    {
        int position=contacts.indexOf(c);
        contacts.remove(c);
        priorityContactsRecyclerAdapter.notifyDataSetChanged();
        if(priorityContactsRecyclerAdapter.getItemCount()==0)
            empty_bucket.setVisibility(View.VISIBLE);
        else
            empty_bucket.setVisibility(View.GONE);
        return position;
    }

    //on press undo in the snackbar add contacts in the recycler view and notify the adapter
    public void addContact(int position,Contacts c)
    {
        contacts.add(position,c);
        if(priorityContactsRecyclerAdapter.getItemCount()==0)
            empty_bucket.setVisibility(View.VISIBLE);
        else
            empty_bucket.setVisibility(View.GONE);
        priorityContactsRecyclerAdapter.notifyDataSetChanged();
    }

    //fetching the list and sort it according to the name
    public void initializeData()
    {
        contacts=new ArrayList<>();
        contacts=getAllData();
        if(contacts.size()==0)
        {
            empty_bucket.setVisibility(View.VISIBLE);
        }
        else
        {
            empty_bucket.setVisibility(View.GONE);
        }
        Collections.sort(contacts);
    }

    //initialize the recycler view
    public void initializeUi()
    {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        priorityContactsRecyclerAdapter =new PriorityContactsRecyclerAdapter(this,contacts);
        recyclerView.setAdapter(priorityContactsRecyclerAdapter);
    }

    //get the contacts from the database and add in the list
    public List<Contacts> getAllData()
    {
        contactsDbHelper=new ContactsDbHelper(this);
        sqLiteDatabase=contactsDbHelper.getReadableDatabase();
        sqLiteDatabase.execSQL(ContactContract.ContactEntry.SQL_CREATE_PRIORITY_CONTACTS);
        cursor= sqLiteDatabase.query(ContactContract.ContactEntry.Table_Priority_Contacts,null,null,null,null,null,null);

        List<Contacts> contactsList=new ArrayList<>();
        while(cursor.moveToNext())
        {
            String name=cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NAME));
            String number=cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NUMBER));

            Contacts contacts=new Contacts(name,number);
            contactsList.add(contacts);
        }
        return contactsList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(flag==1) {  //when activity resumes adapter has to be refreshed
            initializeData();
            initializeUi();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        flag=1; //check whether the activity has been paused or not
    }
    //Load contacts first time
    public void loadContacts()
    {
        String method="load";
        progress.setMessage("Reading Contacts...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);
        BackgroundTask task=new BackgroundTask(getApplicationContext(),method);
        task.execute();
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                while (f==1)
                {
                    int x= Flag.getFlag();
                    if(x==0)
                    {
                        sharedPref.onSuccessLoad(1);
                        progress.cancel();
                        startActivity(new Intent(ShowPriorityContacts.this, ShowAllContacts.class));
                        f = 0;
                    }
                }
            }
        });t.start();
    }

    //Snack Bar when contacts swipe
    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {

        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), text,3000)
                .setAction(actionName, action);

        View v = snack.getView();
        v.setBackgroundColor(getResources().getColor(R.color.P));
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.Q));
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(getResources().getColor(R.color.Z));

        snack.show();
    }
}
