/*
 * Config.java
 *
 * Created on 19 ���� 2005 �., 18:37
 */

package Client;

import Info.Version;
import java.io.*;
import java.util.*;
import ui.Time;
//import javax.microedition.rms.*;

/**
 *
 * @author Eugene Stahov
 */
public class Config {
    
    public final int keepAlive=getProperty("keep_alive",200);
    public final int vibraLen=getProperty("vibra_len",500);
    
    public boolean ghostMotor;//=getProperty("moto_e398",false);
    public boolean blFlash=true;

    public boolean msgLog=getProperty("msg_log",false);
    
/*#DefaultConfiguration,Release#*///<editor-fold>
    public String messagesnd=getProperty("msg_snd","/sounds/message.amr");
    public char keyLock=getProperty("key_lock",'*');
    public char keyVibra=getProperty("key_vibra",'#');
/*$DefaultConfiguration,Release$*///</editor-fold>
/*#M55,M55_Release#*///<editor-fold>
//--    public boolean msgLogPresence=getProperty("msg_log_presence",false);
//--    public boolean msgLogConfPresence=getProperty("msg_log_conf_presence",false);
//--    public boolean msgLogConf=getProperty("msg_log_conf",false);
//--    public final String msgPath=getProperty("msg_log_path","");
//--    public String messagesnd=getProperty("msg_snd","/sounds/message.wav");
//--    public final String siemensCfgPath=getProperty("cfg_path","");
//--    public char keyLock=getProperty("key_lock",'#');
//--    public char keyVibra=getProperty("key_vibra",'*');
/*$M55,M55_Release$*///</editor-fold>

    public char keyHide=getProperty("key_hide",'9');
    public char keyOfflines=getProperty("key_offlines",'0');

/*#M55,M55_Release#*///<editor-fold>
//--    public int m55LedPattern=0;
/*$M55,M55_Release$*///</editor-fold>

    public String defGcRoom=getProperty("gc_room","bombus");

/*#USE_LOGGER#*///<editor-fold>
//--    public boolean logMsg=getProperty("syslog_msg",false);
//--    public boolean logEx=getProperty("syslog_exceptions",false);
//--    public boolean logStream=getProperty("syslog_stream",false);
/*$USE_LOGGER$*///</editor-fold>
    
    //public TimeZone tz=new RuGmt(0);
    public int gmtOffset;
    public int locOffset;
    public int accountIndex=-1;
    public boolean fullscreen=false;
    public int def_profile=0;
    public int profile=0;
    public boolean smiles=true;
    public boolean showOfflineContacts=true;
    public boolean showTransports=true;
    public boolean selfContact=false;
    public boolean notInList=true;
    public boolean ignore=false;
    public boolean eventComposing=false;
    
    public boolean allowMinimize=false;
    
    public void LoadFromStorage(){
        
        DataInputStream inputStream=NvStorage.ReadFileRecord("config", 0);
        if (inputStream!=null)
        try {
            accountIndex = inputStream.readInt();
            showOfflineContacts=inputStream.readBoolean();
            fullscreen=inputStream.readBoolean();
            def_profile = inputStream.readInt();
            smiles=inputStream.readBoolean();
            showTransports=inputStream.readBoolean();
            selfContact=inputStream.readBoolean();
            notInList=inputStream.readBoolean();
            ignore=inputStream.readBoolean();
            eventComposing=inputStream.readBoolean();
            
            gmtOffset=inputStream.readInt();
            locOffset=inputStream.readInt();
            
            inputStream.close();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        profile=def_profile;
        updateTime();
    }
    
    public void saveToStorage(){
        
        DataOutputStream outputStream=NvStorage.CreateDataOutputStream();

        try {
            outputStream.writeInt(accountIndex);
            outputStream.writeBoolean(showOfflineContacts);
            outputStream.writeBoolean(fullscreen);
            outputStream.writeInt(def_profile);
            outputStream.writeBoolean(smiles);
            outputStream.writeBoolean(showTransports);
            outputStream.writeBoolean(selfContact);
            outputStream.writeBoolean(notInList);
            outputStream.writeBoolean(ignore);
            outputStream.writeBoolean(eventComposing);

            outputStream.writeInt(gmtOffset);
            outputStream.writeInt(locOffset);
        } catch (IOException e) { 
            e.printStackTrace(); 
/*#USE_LOGGER#*///<editor-fold>
//--            NvStorage.log(e, "Config:112");
/*$USE_LOGGER$*///</editor-fold>
        }

        NvStorage.writeFileRecord(outputStream, "config", 0, true);
    }


    public void updateTime(){
        Time.setOffset(gmtOffset, locOffset);
    }
    
    /** Creates a new instance of Config */
    public Config() {
        int gmtloc=TimeZone.getDefault().getRawOffset()/3600000;
        locOffset=getProperty( "time_loc_offset", 0);
        gmtOffset=getProperty("time_gmt_offset", gmtloc);

        String platform=Version.platform();
        
        if (platform.startsWith("SonyE")) {
            allowMinimize=true;
        }
        if (platform.startsWith("Nokia")) {
            blFlash=false;
        }
        if (platform.startsWith("Moto")) {
            ghostMotor=true;
            blFlash=false;
        }
        //System.out.println(locOffset);
/*#M55,M55_Release#*///<editor-fold>
//--        if (platform.startsWith("M55")) 
//--        m55LedPattern=getProperty("led_pattern",5);
/*$M55,M55_Release$*///</editor-fold>
    }
    
    public final String getProperty(final String key, final String defvalue) {
        try {
            String s=StaticData.getInstance().midlet.getAppProperty(key);
            return (s==null)?defvalue:s;
        } catch (Exception e) {
            return defvalue;
        }
    }

    public final int getProperty(final String key, final int defvalue) {
        try {
            String s=StaticData.getInstance().midlet.getAppProperty(key);
            return (s==null)?defvalue:Integer.parseInt(s);
        } catch (Exception e) {
            return defvalue;
        }
    }
    
    public final char getProperty(final String key, final char defvalue) {
        try {
            String s=StaticData.getInstance().midlet.getAppProperty(key);
            return (s==null)?defvalue:s.charAt(0);
        } catch (Exception e) {
            return defvalue;
        }
    }
    
    public final boolean getProperty(final String key, final boolean defvalue) {
        try {
            String s=StaticData.getInstance().midlet.getAppProperty(key);
            if (s==null) return defvalue;
            if (s.equals("true")) return true;
            if (s.equals("yes")) return true;
            if (s.equals("1")) return true;
            return false;
        } catch (Exception e) {
            return defvalue;
        }
    }

}
