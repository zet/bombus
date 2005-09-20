/*
 * InfoWindow.java
 *
 * Created on 6 �������� 2005 �., 22:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package Info;
import javax.microedition.lcdui.*;

/**
 *
 * @author EvgS
 */
public class InfoWindow implements CommandListener{

    private Display display;
    private Displayable parentView;
    
    private Form form;

    /** Creates a new instance of InfoWindow */
    public InfoWindow(Display display) {
        this.display=display;
        parentView=display.getCurrent();
        
        form=new Form("About");
        form.addCommand(new Command("Close", Command.BACK, 99));
        try {
            form.append(Image.createImage("/_icon.png"));
        } catch (Exception e) { }
        form.append("Bombus v"+Version.version+"\nMobile Jabber client\n");
        form.append(Version.getOs());
        form.append("\n");
        form.append (new StringItem(null, Version.url
/*#!MIDP1#*///<editor-fold>
                , Item.HYPERLINK
/*$!MIDP1$*///</editor-fold>
                ));
        
        StringBuffer mem=new StringBuffer("\n\nMemory:\n");
        mem.append("Free=");
        //mem.append(Runtime.getRuntime().freeMemory()>>10);
        //mem.append("\nFree=");
        System.gc();
        mem.append(Runtime.getRuntime().freeMemory()>>10);
        mem.append("\nTotal=");
        mem.append(Runtime.getRuntime().totalMemory()>>10);
        form.append(mem.toString());
     
        form.setCommandListener(this);
        display.setCurrent(form);
    }
    
    public void commandAction(Command c, Displayable d) {
        display.setCurrent(parentView);
    }
}
