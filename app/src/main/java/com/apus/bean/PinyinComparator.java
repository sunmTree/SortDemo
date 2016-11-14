package com.apus.bean;

import java.util.Comparator;

/**
 * Created by sunmeng on 2016/11/11.
 */

public class PinyinComparator implements Comparator<CitySortModel>{
    @Override
    public int compare(CitySortModel citySortModel, CitySortModel t1) {
        if (t1.getSortLetters().equals("#"))
            return -1;
        else if (citySortModel.getSortLetters().equals("#"))
            return 1;
        else
            return citySortModel.getSortLetters().compareTo(t1.getSortLetters());
    }
}


