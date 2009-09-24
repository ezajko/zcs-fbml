package ru.korusconsulting.test;

public class KeyGenerator {
    private static long lastGenTime=-1;
    private static int nodeId=0;
    
    public synchronized static String getKey(){
        long ct=System.currentTimeMillis();
        if(ct==lastGenTime){
            nodeId++;
        }
        else{
            nodeId=0;
            lastGenTime=ct;
        }
        return String.valueOf(lastGenTime)+nodeId;
    } 
}
