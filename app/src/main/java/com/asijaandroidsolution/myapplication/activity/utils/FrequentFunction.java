package com.asijaandroidsolution.myapplication.activity.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FrequentFunction {

    public static String getCityName(Context context, double latitude, double longitude){
        Geocoder geocoder=new Geocoder(context, Locale.getDefault());
        List<Address> addressList= null;
        try {
            addressList=geocoder.getFromLocation(latitude,longitude,1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addressList.isEmpty())
            return "";
        else
            return addressList.get(0).getLocality();
    }
}
