/*
 * Jid.java
 *
 * Created on 4 ���� 2005 �., 1:25
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Client;

/**
 *
 * @author Eugene Stahov
 */
public class Jid {
    
    private String fullJid;
    private int resourcePos;
    
    /** Creates a new instance of Jid */
    public Jid(String s) {
        setJid(s);
    }
    
    public void setJid(String s){
        fullJid=s;
        resourcePos=fullJid.indexOf('/');
        if (resourcePos<0) resourcePos=fullJid.length();
    }
    /** Compares two Jids */
    public boolean equals(Jid j, boolean compareResource) {
        if (j==null) return false;
        
        String cj=j.fullJid;
        // ���������� ������� jid,
        if (resourcePos!=j.resourcePos) return false;
        if (!fullJid.regionMatches(true,0,cj,0,resourcePos)) return false;
        if (!compareResource) return true;
        
        //��������� ������� �������� � �����
        int compareLen=fullJid.length();
        if (compareLen!=j.fullJid.length()) return false;

        // ��������� ������ ��������
        compareLen-=resourcePos;
        return fullJid.regionMatches(false,resourcePos,cj,resourcePos,compareLen);
        //int compareLen=(compareResource)?(j.getJidFull().length()):resourcePos;
        //return fullJid.regionMatches(true,0,j.fullJid,0,compareLen);
    }
    
    
    /** �������� jid �� "���������" */
    public boolean isTransport(){
        return fullJid.indexOf('@')==-1;
    }
    /** �������� ������� ������� */
    public boolean hasResource(){
        return fullJid.length()!=resourcePos;
    }
    
    /** ��������� ���������� */
    public String getTransport(){
        try {
            int beginIndex=fullJid.indexOf('@')+1;
            int endIndex=fullJid.indexOf('.',beginIndex);
            return fullJid.substring(beginIndex, endIndex);
        } catch (Exception e) {
            return "-";
        }
    }
    
    /** ��������� ������� �� ������ */
    public String getResource(){
        return fullJid.substring(resourcePos);
    }
    
    /** ��������� username */
    /*public String getUser(){
        return substr(this,(char)0,'@');
    }*/
    
    /** ��������� ����� ��� ������� */
    public String getBareJid(){
        return fullJid.substring(0,resourcePos);
    }
    
    /** ��������� jid/resource */
    public String getJid(){
        return fullJid;
    }
}
