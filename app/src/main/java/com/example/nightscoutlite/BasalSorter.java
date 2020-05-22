package com.example.nightscoutlite;

class BasalSorter implements java.util.Comparator<ProfileRawData> {

    @Override
    public int compare(ProfileRawData o1, ProfileRawData o2) {
        return o1.getTimeAsSeconds() - o2.getTimeAsSeconds();
    }
}
