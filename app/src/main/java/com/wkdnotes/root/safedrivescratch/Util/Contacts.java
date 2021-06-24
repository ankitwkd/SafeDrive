package com.wkdnotes.root.safedrivescratch.Util;



public class Contacts implements Comparable<Contacts>{
    private String name,number;
    public Contacts(String name, String number) {
        this.name = name;
        this.number=number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


    //Sort
    @Override
    public int compareTo(Contacts contacts) {
        if(contacts.name.compareToIgnoreCase(name)==0)
            return 0;
        else if(contacts.name.compareToIgnoreCase(name)<1)
            return 1;
        else
            return -1;
    }
}
