package com.example.nightscoutlite;

class DateSorter implements java.util.Comparator<NightscoutData> {

    @Override
    public int compare(NightscoutData o1, NightscoutData o2) {
        return o1.getDate().compareTo(o2.getDate());
    }
}
