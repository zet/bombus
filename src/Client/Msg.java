/*
 * msg.java
 *
 * Created on 6 ������ 2005 �., 19:20
 */

package Client;
import java.util.*;
import ui.Time;
import Client.MessageList;
import javax.microedition.lcdui.Image;

/**
 *
 * @author Eugene Stahov
 */
public class Msg //implements MessageList.Element
{
    // without signaling
    public final static int MESSAGE_TYPE_OUT=1;
    public final static int MESSAGE_TYPE_PRESENCE=2;
    public final static int MESSAGE_TYPE_HISTORY=3;
    // with signaling
    public final static int MESSAGE_TYPE_IN=10;
    public final static int MESSAGE_TYPE_AUTH=11;
    
    /** Creates a new instance of msg */
    public Msg(int messageType, String from, String subj, String body) {
        this.messageType=messageType;
        this.from=from;
        this.body=body;
        this.subject=subj;
        this.dateGmt=Time.localTime();
        if (messageType==MESSAGE_TYPE_IN) unread=true;
        if (messageType==MESSAGE_TYPE_AUTH) unread=true;
    }
    
    public void onSelect(){}
    public String getMsgHeader(){
        return getTime()+from; 
    }
    public String getTime(){
        return '['+Time.timeString(dateGmt)+"] "; 
    }
    public String getDayTime(){
        return '['+Time.dayString(dateGmt)+Time.timeString(dateGmt)+"] "; 
    }
    //private TimeZone tz(){ return StaticData.getInstance().config.tz;}
    
    public int getColor() {
        switch (messageType) {
            case MESSAGE_TYPE_IN: return 0x0000B0;
            case MESSAGE_TYPE_OUT: return 0xB00000;
            case MESSAGE_TYPE_PRESENCE: return 0x006000;
            case MESSAGE_TYPE_AUTH: return 0x400040;
            case MESSAGE_TYPE_HISTORY: return 0x535353;
        }
        return 0;
    }
    //public int getColor2(){ return 0; }
    public String toString(){
        return (messageType==MESSAGE_TYPE_PRESENCE)?getTime()+body:body; 
    }
    
    public boolean isPresence() { return messageType==MESSAGE_TYPE_PRESENCE; }
    

    /** 0=in, 1=out, 2=presence */
    public int messageType=0;
    
    /** ����������� ��������� */
    public String from;
    
    /** ���� ��������� */
    public String subject;

    /** ���� ��������� */
    public String body;

    /** ���� ��������� */
    public long dateGmt;
    
    public boolean unread = false;
}
