/*
 * Jid.java
 *
 * Created on 4 ���� 2005 �., 1:25
 */

package Client;

/**
 *
 * @author Eugene Stahov
 */
public class Jid {
    
    private String fullJid;
    private int resourcePos;
    
    private static String substr(Jid j, char begin, char end){
        int beginIndex=j.fullJid.indexOf(begin)+1;
        int endIndex=j.fullJid.indexOf(end,beginIndex);
        return j.fullJid.substring(beginIndex, endIndex);
    }
    
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
        int compareLen=(compareResource)?(j.getJidFull().length()):resourcePos;
        // ���� ��������� � ��������, �� ������� ���������
        //if (compareResource) return fullJid.equals(j.fullJid);
        // ����� ����������
        return fullJid.regionMatches(true,0,j.fullJid,0,compareLen);
    }
    
    
    /** �������� jid �� "���������" */
    public boolean isTransport(){
        return fullJid.indexOf('@')==-1;
    }
    
    /** ��������� ���������� */
    public String getTransport(){
        return substr(this,'@','.');
    }
    
    /** ��������� ������� */
    public String getResource(){
        return fullJid.substring(resourcePos);
    }
    
    /** ��������� username */
    public String getUser(){
        return substr(this,(char)0,'@');
    }
    
    /** ��������� ����� ��� ������� */
    public String getJid(){
        return fullJid.substring(0,resourcePos);
    }
    
    /** ��������� ����� ��� ������� */
    public String getJidFull(){
        return fullJid;
    }
}
