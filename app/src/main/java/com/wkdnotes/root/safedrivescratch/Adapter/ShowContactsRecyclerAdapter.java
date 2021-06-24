package com.wkdnotes.root.safedrivescratch.Adapter;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.wkdnotes.root.safedrivescratch.R;
import com.wkdnotes.root.safedrivescratch.Util.Contacts;

import java.util.ArrayList;
import java.util.List;

public class ShowContactsRecyclerAdapter extends RecyclerView.Adapter<ShowContactsRecyclerAdapter.ViewHolder> implements SectionIndexer
{
    public List<Contacts> contactsList;
    Context context;
    private ArrayList<Integer> mSectionPositions;
    public List<Contacts> selected_usersList=new ArrayList<>();

    @Override
    public Object[] getSections()
    {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);

        for(int i=0,size=contactsList.size();i<size;i++)
        {
            String section=String.valueOf(contactsList.get(i).getName().charAt(0)).toUpperCase();
            if(section.startsWith("+") || Character.isDigit(section.charAt(0)))
            {
                section="#";
                if(sections.contains(section))
                    mSectionPositions.add(i);
                else
                {
                    sections.add(section);
                    mSectionPositions.add(i);
                }
                continue;
            }
            if(!sections.contains(section))
            {
                sections.add(section);
                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int i) {
        return mSectionPositions.get(i);
    }

    @Override
    public int getSectionForPosition(int i) {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View v;
        public TextView t1,t2,color_text;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.v=itemView;
            t1=itemView.findViewById(R.id.names);
            t2=itemView.findViewById(R.id.numbers);
            color_text=itemView.findViewById(R.id.circle_text_ShowContacts);
            relativeLayout=(RelativeLayout) itemView.findViewById(R.id.Relative_ShowContacts);
        }
    }

    public ShowContactsRecyclerAdapter(List<Contacts> contactsList, List<Contacts> selected_userList , Context context)
    {
        this.context=context;
        this.contactsList=contactsList;
        this.selected_usersList=selected_userList;
    }

    @Override
    public ShowContactsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_show_contacts,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShowContactsRecyclerAdapter.ViewHolder holder, final int position) {
        final Contacts contacts=contactsList.get(position);
        holder.t1.setText(contacts.getName());
        holder.t2.setText(contacts.getNumber());
        String s=contacts.getName().substring(0,1).toUpperCase();
        if(s.matches("[0-9]") || s.matches("[+,-,=]"))
            s="#";
        holder.color_text.setText(s);

        GradientDrawable gradientDrawable=(GradientDrawable)holder.color_text.getBackground();
        int color=getColor(contacts.getName().substring(0,1).toUpperCase());
        gradientDrawable.setColor(color);

        if(selected_usersList.contains(contactsList.get(position)))
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_selected_state));
        else
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_normal_state));
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

    public void setFilter(ArrayList<Contacts> newList)
    {
        contactsList=new ArrayList<>();
        contactsList.addAll(newList);
        notifyDataSetChanged();
    }

}