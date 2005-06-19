/*
 * MessageList.java
 *
 * Created on 19 ������� 2005 �., 23:54
 */

package Client;
import ui.*;
import java.util.*;
import javax.microedition.lcdui.*;
/**
 *
 * @author Eugene Stahov
 */
public class MessageList extends ComplexStringList
implements CommandListener{
    
    Contact contact;
    
    Command cmdBack=new Command("Back",Command.BACK,99);
    Command cmdMessage=new Command("New Message",Command.SCREEN,2);
    Command cmdResume=new Command("Resume Message",Command.SCREEN,1);
    Command cmdQuote=new Command("Quote",Command.SCREEN,3);
    
    ImageList il;
    boolean smiles;
    
    StaticData sd;
    
    /*public interface Element {
        int getColor1();
        String getMsgHeader();
        int getColor2();
        String toString();
        void onSelect();
    }
     */

    /** Creates a new instance of MessageList */
    public MessageList(Contact contact, Display display) {
        super(display);
        this.contact=contact;
        sd=StaticData.getInstance();
        il=sd.smilesIcons;
        smiles=sd.config.smiles;
        Time.setOffset(sd.config.gmtOffset);
        
        AttachList(new Vector());

        title=new ComplexString(sd.rosterIcons);
        
        title.addElement(contact.toString());
        title.addRAlign();
        title.addElement(null);
        title.addElement(null);
        //setTitleLine(title);

        cursor=0;//activate
        
        addCommand(cmdMessage);
        addCommand(cmdBack);
        if (getItemCount()>0)
            addCommand(cmdQuote);
        setCommandListener(this);
        moveCursorTo(contact.firstUnread());
    }
    
    public void showNotify(){
        super.showNotify();
        if (contact.msgSuspended==null) removeCommand(cmdResume);
        else addCommand(cmdResume);
    }
    
    public void beginPaint(){ 
        title.setElementAt(sd.roster.messageIcon,2);
        title.setElementAt(contact.incomingComposing, 3);
    }
    
    public int getItemCount(){ return contact.msgs.size(); }
    //public Element getItemRef(int Index){ return (Element) contact.msgs.elementAt(Index); }

    protected ComplexString cacheUpdate(int index) {
        ComplexString m;
        Msg msg=(Msg)contact.msgs.elementAt(index);
        
        if (msg.unread) contact.resetNewMsgCnt();
        msg.unread=false;

        m= (ComplexString)StaticData.getInstance().parser.
                parseMsg( msg, (smiles)?il:null, getWidth()-6, true, null);
        m.setColor(msg.getColor1());
        
        /*if (msg.messageType==Msg.MESSAGE_TYPE_AUTH) {
            m.addImage(ImageList.ICON_AUTHRQ_INDEX);
        }*/
        //m.insertElementAt(new Integer(msg.getColor1()|0x1000000), 0); //color
        int sz=lines.size(); if (index>=sz) lines.setSize(index+1);
        lines.setElementAt(m, index);
        return m;
    }

    public Object getSelectedObject(){
        try {
            return contact.msgs.elementAt(cursor);
        } catch (Exception e) {}
        return null;
    }
    public void eventOk(){
        Msg msg=(Msg)getSelectedObject();
        if (msg!=null) {
            //if (contact.msgs.size()==cursor+1) refreshMsgCnt();
            new MessageView(display, cursor, contact);
        }
    }
    
    public void focusedItem(int index){
        
        if (index<contact.lastUnread) return;

        refreshMsgCnt();
    }
    
    private void refreshMsgCnt(){
        if (contact.needsCount()){
            //System.out.println("refreshMsgCnt()");
            sd.roster.countNewMsgs();
        }
    }
    
    public void commandAction(Command c, Displayable d){
        if (c==cmdBack) {
            //contact.lastReaded=contact.msgs.size();
            //contact.resetNewMsgCnt();            
            destroyView();
            return;
        }
        if (c==cmdMessage) { 
            contact.msgSuspended=null; 
            keyGreen(); 
        }
        if (c==cmdResume) { keyGreen(); }
        if (c==cmdQuote) {
            new MessageEdit(display,contact,((Msg)getSelectedObject()).toString());
        }
    }
    protected void keyGreen(){
        new MessageEdit(display,contact,contact.msgSuspended);
        contact.msgSuspended=null;
    }
}
