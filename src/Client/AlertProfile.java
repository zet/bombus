/*
 * Profile.java
 *
 * Created on 28 ���� 2005 �., 0:05
 */

package Client;

import ui.*;
import javax.microedition.lcdui.*;

/**
 *
 * @author Eugene Stahov
 */
public class AlertProfile extends IconTextList implements CommandListener, IconTextList.Element{
    public final static int AUTO=0;
    public final static int ALL=1;
    public final static int NONE=2;
    public final static int VIBRA=3;
    public final static int SOUND=4;
    
    private final static String[] alertNames=
    { "Auto", "All signals", "Vibra", "Sound", "No signals"};
    private final static int[] alertCodes=
    { AUTO, ALL, VIBRA, SOUND, NONE };
    
    /** Creates a new instance of Profile */
    
    private Command cmdOk=new Command("Select",Command.OK,1);
    private Command cmdCancel=new Command("Back",Command.BACK,99);
    /** Creates a new instance of SelectStatus */
    public AlertProfile(Display d) {
        super(StaticData.getInstance().rosterIcons);
        createTitle(1, "Alert Profile",null);
        
        addCommand(cmdOk);
        addCommand(cmdCancel);
        setCommandListener(this);
        
        int p=0;
        switch (StaticData.getInstance().config.profile){
            case AUTO:  p=0; break;
            case ALL:   p=1; break;
            case VIBRA: p=2; break;
            case SOUND: p=3; break;
            case NONE:  p=4; break;
        }
        moveCursorTo(p);
        attachDisplay(d);
    }
    
    int index;
    public Element getItemRef(int Index){ index=Index; return this;}
    public void onSelect(){}
    public int getColor(){ return 0; }
    public int getImageIndex(){return alertCodes[index]+ImageList.ICON_PROFILE_INDEX;}
    public String toString(){ return alertNames[index];}
    
    public void commandAction(Command c, Displayable d){
        if (c==cmdOk) eventOk(); 
        if (c==cmdCancel) destroyView();
    }
    
    public void eventOk(){
        StaticData.getInstance().config.profile=alertCodes[cursor];
        destroyView();
    }
    
    public int getItemCount(){   return alertCodes.length; }
    

    /** */
    public static void playNotify(Display display, int event) {
        Config cf=StaticData.getInstance().config;
        String message=cf.messagesnd;
        int profile=cf.profile;
        if (profile==AUTO) profile=ALL;
        
        EventProfile ep=null;
        
        boolean blFlashEn=!cf.ghostMotor;   // motorola e398 backlight bug
        
        switch (profile) {
            case ALL:   ep=new EventProfile(message,500,blFlashEn); break;
            case NONE:  ep=new EventProfile(null,   0,  false    ); break;
            case VIBRA: ep=new EventProfile(null,   500,blFlashEn); break;
            case SOUND: ep=new EventProfile(message,0,  blFlashEn); break;
        }
        if (ep!=null) new EventNotify (display, ep).startNotify();
    }
}
