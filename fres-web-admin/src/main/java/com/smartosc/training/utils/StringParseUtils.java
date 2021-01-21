package com.smartosc.training.utils;

public class StringParseUtils {
    private StringParseUtils() {
        throw new IllegalStateException("Utility class");
    }
    public static int [] parse(String[] line)
    {
        int[] intarray=new int[line.length];
        if (line.length > 1){
            int i=0;
            for(String str:line){
                intarray[i]=Integer.parseInt(str);
                i++;
            }
        }
        return intarray;
    }
}
