package com.wkdnotes.root.safedrivescratch.ContactPreview;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.wkdnotes.root.safedrivescratch.Adapter.RecyclerItemClickListener;
import com.wkdnotes.root.safedrivescratch.Adapter.ShowContactsRecyclerAdapter;
import com.wkdnotes.root.safedrivescratch.Background.BackgroundTask;
import com.wkdnotes.root.safedrivescratch.Database.ContactContract;
import com.wkdnotes.root.safedrivescratch.Database.ContactsDbHelper;
import com.wkdnotes.root.safedrivescratch.R;
import com.wkdnotes.root.safedrivescratch.Util.Check;
import com.wkdnotes.root.safedrivescratch.Util.Contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;

public class ShowAllContacts extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private IndexFastScrollRecyclerView indexFastScrollRecyclerView;
    private ProgressDialog progressDialog;
    private ShowContactsRecyclerAdapter mAdapter;
    private List<Contacts> contactsList;
    private boolean isMultiSelect=false;
    private static int f=1;
    private List<Contacts> multiselect_list=new ArrayList<>();
    private Cursor cursor;
    private ActionMode mActionMode;
    private Menu context_menu;
    private static final String TAG=ShowAllContacts.class.getSimpleName();
    private ContactsDbHelper contactsDbHelper;
    private SQLiteDatabase sqLiteDatabase;
    private PullRefreshLayout pullRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_all_contact);

        contactsList=new ArrayList<>();
        indexFastScrollRecyclerView= (IndexFastScrollRecyclerView) findViewById(R.id.recycler);

        // pull refresh layout
        pullRefreshLayout= (PullRefreshLayout) findViewById(R.id.pull_refresh);
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContactList();
            }
        });

        progressDialog = new ProgressDialog(this);

        initializeData();

        //touch callbacks
        indexFastScrollRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(),indexFastScrollRecyclerView,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        if(isMultiSelect)
                            multi_select(position);
                        else
                        {
                            TextView name=view.findViewById(R.id.names);
                            TextView number=view.findViewById(R.id.numbers);
                            String n1=name.getText().toString();
                            String n2=number.getText().toString();
                            if(getPriorityData(n2)==true)
                            {
                                String text=n1+" already aded";
                                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), text,3000);
                                View v = snack.getView();
                                v.setBackgroundColor(getResources().getColor(R.color.P));
                                ((TextView) v.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.Q));
                                ((TextView) v.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(getResources().getColor(R.color.Z));

                                snack.show();
                            }
                            else{

                                insertDataIntoPriorityTable(n1,n2);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        if (!isMultiSelect) {
                            multiselect_list = new ArrayList<>();
                            isMultiSelect = true;

                            if (mActionMode == null) {
                                mActionMode = startActionMode(mActionModeCallback);
                            }
                        }

                        multi_select(position);
                    }
                })
        );
    }
    public void initializeData()
    {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                contactsList=getAllData();
                Collections.sort(contactsList);
                initializeIndexUI();
            }
        });
    }
    public void initializeIndexUI()
    {
        indexFastScrollRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter=new ShowContactsRecyclerAdapter(contactsList,multiselect_list,getApplicationContext());
        indexFastScrollRecyclerView.setAdapter(mAdapter);
        //style attributes
        indexFastScrollRecyclerView.setIndexTextSize(12);
        indexFastScrollRecyclerView.setIndexBarCornerRadius(10);
        indexFastScrollRecyclerView.setIndexBarColor("#000000" );
        indexFastScrollRecyclerView.setIndexBarTransparentValue((float) 0.3);
        indexFastScrollRecyclerView.setIndexbarMargin(0);
        indexFastScrollRecyclerView.setIndexbarWidth(40);
        indexFastScrollRecyclerView.setPreviewPadding(0);
        indexFastScrollRecyclerView.setIndexBarTextColor("#ffffff");
        indexFastScrollRecyclerView.setIndexBarVisibility(true);
        indexFastScrollRecyclerView.setIndexbarHighLateTextColor("#33334c");
        indexFastScrollRecyclerView.setIndexBarHighLateTextVisibility(true);
    }
    //********Search Bar Show Contacts
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_show_contacts,menu);
        MenuItem menuItem=menu.findItem(R.id.search_contact);
        SearchView searchView=(SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }
    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {

        s=s.toLowerCase();
        ArrayList<Contacts> newList=new ArrayList<>();
        for(Contacts contacts: contactsList)
        {
            String name=contacts.getName().toLowerCase();
            if(name.contains(s))
                newList.add(contacts);
        }
        if(contactsList.size()==newList.size())
            indexFastScrollRecyclerView.setIndexBarVisibility(true);
        else
            indexFastScrollRecyclerView.setIndexBarVisibility(false);
        mAdapter.setFilter(newList);

        return true;
    }
    //Search Bar Ends Here**********************
    //**************MultiSelect Insertion Callbacks Start Here
    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(contactsList.get(position)))
                multiselect_list.remove(contactsList.get(position));
            else
                multiselect_list.add(contactsList.get(position));

            if (multiselect_list.size() > 0){
                mActionMode.setTitle("" + multiselect_list.size());
            }
            else
                mActionMode.finish();

            refreshAdapter();

        }
    }
    public void refreshAdapter()
    {
        mAdapter.selected_usersList=multiselect_list;
        mAdapter.contactsList=contactsList;
        mAdapter.notifyDataSetChanged();
    }
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_bar, menu);
            context_menu = menu;
            return true;
        }

      @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.right:
                    if(multiselect_list.size()>0)
                    {
                        for(int i=0;i<multiselect_list.size();i++)
                        {
                            String name=multiselect_list.get(i).getName();
                            String number=multiselect_list.get(i).getNumber();
                            insertDataIntoPriorityTable(name,number);
                        }
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                        Toast.makeText(getApplicationContext(),"Successful inserted",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return true;
                default:
                    return false;
            }
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiselect_list = new ArrayList<>();
            refreshAdapter();
        }
    };
    public boolean getPriorityData(String num)
    {
        List<Contacts> temp=new ArrayList<Contacts>();
        ContactsDbHelper contactsDbHelper=new ContactsDbHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase=contactsDbHelper.getReadableDatabase();
        sqLiteDatabase.execSQL(ContactContract.ContactEntry.SQL_CREATE_PRIORITY_CONTACTS);
        String query="SELECT * FROM "+ ContactContract.ContactEntry.Table_Priority_Contacts+" where "+ ContactContract.ContactEntry.COLUMN_NUMBER+" = '"+ num+"';";
        Cursor c=sqLiteDatabase.rawQuery(query,null);
        if(c.getCount()>0)
            return true;
        return false;
    }
    public void insertDataIntoPriorityTable(String name,String number)
    {
        //insert data into database
        contactsDbHelper=new ContactsDbHelper(this);
        SQLiteDatabase sqLiteDatabase=contactsDbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(ContactContract.ContactEntry.SQL_CREATE_PRIORITY_CONTACTS);
        ContentValues contentValues=new ContentValues();
        contentValues.put(ContactContract.ContactEntry.COLUMN_NAME,name);
        contentValues.put(ContactContract.ContactEntry.COLUMN_NUMBER,number);
        sqLiteDatabase.insert(ContactContract.ContactEntry.Table_Priority_Contacts,null,contentValues);
        sqLiteDatabase.close();
    }

    //Fetch the data from the database and display
    public List<Contacts>getAllData()
    {
        contactsDbHelper=new ContactsDbHelper(this);
        sqLiteDatabase=contactsDbHelper.getReadableDatabase();
        sqLiteDatabase.execSQL(ContactContract.ContactEntry.SQL_CREATE_CONTACTS_TABLE);
        cursor= sqLiteDatabase.query(ContactContract.ContactEntry.TABLE_NAME,null,null,null,null,null,null);

        List<Contacts> contacts=new ArrayList<>();

        while(cursor.moveToNext())
        {
            String name=cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NAME));
            String number=cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NUMBER));

            Contacts contacts1=new Contacts(name,number);
            contacts.add(contacts1);
        }
        return contacts;
    }
    public void refreshContactList() {
        String method = "refresh";
        Check.setRefresh(false);
        BackgroundTask task = new BackgroundTask(getApplicationContext(), method);
        task.execute();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    boolean x = Check.isRefresh();
                    if (x)
                    {
                        break;
                    }
                }
            }
        });t.start();

        Toast.makeText(getApplicationContext(),"Database Updated",Toast.LENGTH_SHORT).show();
        pullRefreshLayout.setRefreshing(false);

    }
}


