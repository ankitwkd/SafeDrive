package com.wkdnotes.root.safedrivescratch.Adapter;





import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wkdnotes.root.safedrivescratch.R;
import com.wkdnotes.root.safedrivescratch.Util.Contacts;

import java.util.List;

import co.dift.ui.SwipeToAction;



public class PriorityContactsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Contacts> contactsList;
    private Context context;
    public PriorityContactsRecyclerAdapter(Context context , List<Contacts> contacts) {
        this.context=context;
        this.contactsList = contacts;
    }

    public class Priority extends SwipeToAction.ViewHolder<Contacts>
    {
        public TextView name,number,circle_text;

        public Priority(View v) {
            super(v);
            name=v.findViewById(R.id.priority_name);
            number=v.findViewById(R.id.priority_number);
            circle_text=v.findViewById(R.id.circle_text);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item_prority, parent, false);

        return new Priority(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Contacts contact=contactsList.get(position);
        Priority priority= (Priority)holder;

        priority.name.setText(contact.getName());
        priority.number.setText(contact.getNumber());
        String s=contact.getName().substring(0,1).toUpperCase();
        if(s.matches("[0-9]") || s.matches("[+,-,=]"))
            s="#";
        priority.circle_text.setText(s);
        GradientDrawable gradientDrawable=(GradientDrawable)priority.circle_text.getBackground();
        int color=getColor(contact.getName().substring(0,1).toUpperCase());
        gradientDrawable.setColor(color);

        priority.data=contact;
    }
    public int getColor(String s)
    {
        int colorResourceId;
        switch (s)
        {
            case "A":colorResourceId=R.color.A;
                break;
            case "B":colorResourceId=R.color.B;
                break;
            case "C":colorResourceId=R.color.C;
                break;
            case "D":colorResourceId=R.color.D;
                break;
            case "E":colorResourceId=R.color.E;
                break;
            case "F":colorResourceId=R.color.F;
                break;
            case "G":colorResourceId=R.color.G;
                break;
            case "H":colorResourceId=R.color.H;
                break;
            case "I":colorResourceId=R.color.I;
                break;
            case "J":colorResourceId=R.color.J;
                break;
            case "K":colorResourceId=R.color.K;
                break;
            case "L":colorResourceId=R.color.L;
                break;
            case "M":colorResourceId=R.color.M;
                break;
            case "N":colorResourceId=R.color.N;
                break;
            case "O":colorResourceId=R.color.O;
                break;
            case "P":colorResourceId=R.color.P;
                break;
            case "Q":colorResourceId=R.color.Q;
                break;
            case "R":colorResourceId=R.color.R;
                break;
            case "S":colorResourceId=R.color.S;
                break;
            case "T":colorResourceId=R.color.T;
                break;
            case "U":colorResourceId=R.color.U;
                break;
            case "V":colorResourceId=R.color.V;
                break;
            case "W":colorResourceId=R.color.W;
                break;
            case "X":colorResourceId=R.color.X;
                break;
            case "Y":colorResourceId=R.color.Y;
                break;
            case "Z":colorResourceId=R.color.Z;
                break;
            default:colorResourceId=R.color.U;
                break;
        }
        return ContextCompat.getColor(context,colorResourceId);
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

}
