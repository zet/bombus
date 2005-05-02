/*
 * MessageView.java
 *
 * Created on 20 ������� 2005 �., 17:42
 */

package Client;
import ui.*;
import javax.microedition.lcdui.*;
import java.util.*;

/**
 *
 * @author Eugene Stahov
 */
public class MessageView 
        extends ComplexStringList 
        implements 
            CommandListener, 
            Messages.MessageParser.NotifyAddLine,
            Runnable
{

    int titlecolor; // ������� �� ���� ���������
    boolean smiles;
    Thread t=null;
    
    Command CmdBack=new Command("Back",Command.BACK,99);
/*#DefaultConfiguration,Release#*///<editor-fold>
    Command CmdTSM=new Command("Smiles", "Toggle Smiles", Command.SCREEN,1);
/*$DefaultConfiguration,Release$*///</editor-fold>
/*#!DefaultConfiguration,Release#*///<editor-fold>
//--    Command CmdTSM=new Command("Toggle Smiles", Command.SCREEN,1);
/*$!DefaultConfiguration,Release$*///</editor-fold>

    public int getTitleBGndRGB() {return 0x338888;} 
    public int getTitleRGB() {return titlecolor;} 
    
    int repaintCounter=5;
    
    public void notifyRepaint(Vector v){ 
        AttachList(v);
        if ((--repaintCounter)>=0) return;
        repaintCounter=5;
        redraw(); 
    }
    
    public void notifyFinalized(){ redraw(); }
    
    Msg msg;
    StaticData sd;
    
    public void run() {
        sd.parser.parseMsg(
                msg.body,
                (smiles)?sd.smilesIcons:null, 
                getWidth()-6,
                false, this);
    }

    /** Creates a new instance of MessageView */
    public MessageView(Display display, Msg msg) {
        super(display);

        sd=StaticData.getInstance();
        
        titlecolor=msg.getColor1();
        ComplexString title=new ComplexString(null);
        title.addElement(msg.getMsgHeader());
        setTitleLine(title);
        
        smiles=sd.config.smiles;
        
        this.msg=msg;
        (t=new Thread(this)).start();

        addCommand(CmdBack);
        addCommand(CmdTSM);
        setCommandListener(this);

    }
    public void eventOk(){
        destroyView();
    }
    
    public void commandAction(Command c, Displayable d){
        if (c==CmdBack) {
            destroyView();
            return;
        }
        if (c==CmdTSM) toggleSmiles();
    }
    
    public void userKeyPressed(int KeyCode){
        if (KeyCode==KEY_STAR) toggleSmiles();
    }
    
    private void toggleSmiles(){
        smiles=!smiles;
        while (t.isAlive());
        (t=new Thread(this)).start();
    }
}
