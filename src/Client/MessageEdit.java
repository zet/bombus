/*
 * MessageEdit.java
 *
 * Created on 20 ������� 2005 �., 21:20
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Client;
import Conference.AppendNick;
import archive.ArchiveList;
import javax.microedition.lcdui.*;
import ui.VirtualList;

/**
 *
 * @author Eugene Stahov
 */
public class MessageEdit 
        implements CommandListener, Runnable
{
    
    private Display display;
    private Displayable parentView;
    private TextBox t;
    private String body;
    private String subj;
    
    private Contact to;
    private Command cmdSuspend=new Command("Suspend",Command.BACK,90);
    private Command cmdCancel=new Command("Cancel",Command.SCREEN,99);
    private Command cmdSend=new Command("Send",Command.OK /*Command.SCREEN*/,1);
    private Command cmdSmile=new Command("Add Smile",Command.SCREEN,2);
    private Command cmdInsNick=new Command("Nicknames",Command.SCREEN,3);
    private Command cmdInsMe=new Command("/me",Command.SCREEN,4);
    private Command cmdSubj=new Command("Set Subject", Command.SCREEN, 5);
    private Command cmdPaste=new Command("Archive", Command.SCREEN, 10);
    
    private boolean composing=true;

    //private Command cmdSubject=new Command("Subject",Command.SCREEN,10);
    
    /** Creates a new instance of MessageEdit */
    public MessageEdit(Display display, Contact to, String body) {
        this.to=to;
        this.display=display;
        parentView=display.getCurrent();
        t=new TextBox(to.toString(),null,500,TextField.ANY);
        try {
            if (body!=null) t.setString(body);
        } catch (Exception e) {
            t.setString("<large text>");
        }
        t.addCommand(cmdSend);
        t.addCommand(cmdInsMe);
        t.addCommand(cmdSmile);
        if (to.origin>=Contact.ORIGIN_GROUPCHAT)
            t.addCommand(cmdInsNick);
	t.addCommand(cmdPaste);
        t.addCommand(cmdSuspend);
        t.addCommand(cmdCancel);
        t.setCommandListener(this);
        
        if (to.origin==Contact.ORIGIN_GROUPCHAT)
            t.addCommand(cmdSubj);
        
        //t.setInitialInputMode("MIDP_LOWERCASE_LATIN");
        new Thread(this).start() ; // composing
        
        display.setCurrent(t);
    }
    
    public void addText(String s) {
        //t.insert(s, t.getCaretPosition());
        if ( t.size()>0 )
        if ( !t.getString().endsWith(" ") ) append(" ");
        append(s);  // ������ ������� ���������� ������ � ����� ������
        append(" "); // ��������� ������
    }
    
    private void append(String s) { t.insert(s, t.size()); }
    
    public void setParentView(Displayable parentView){
        this.parentView=parentView;
    }
    
    public void commandAction(Command c, Displayable d){
        body=t.getString();
        if (body.length()==0) body=null;
        
        if (c==cmdCancel) { 
            composing=false; 
            body=null; 
            /*destroyView(); return;*/ 
        }
        if (c==cmdSuspend) { 
            composing=false; 
            to.msgSuspended=body; 
            body=null;
            /*destroyView(); return;*/ 
        }
        if (c==cmdInsMe) { t.insert("/me ", 0); return; }
        if (c==cmdSmile) { new SmilePicker(display, this); return; }
        if (c==cmdInsNick) { new AppendNick(display, to); return; }
        if (c==cmdSend && body==null) return;
        if (c==cmdSubj) {
            if (body==null) return;
            subj=body;
            body="/me has set the topic to: "+subj;
        }
	if (c==cmdPaste) { new ArchiveList(display, t); return; }
        // message/composing sending
        destroyView();
        new Thread(this).start();
        return; 
    }
    
    
    public void run(){
        Roster r=StaticData.getInstance().roster;
        int comp=0; // composing event off
        
        if (body!=null /*|| subj!=null*/ ) {
            String from=StaticData.getInstance().account.toString();
            Msg msg=new Msg(Msg.MESSAGE_TYPE_OUT,from,subj,body);
            // �� ��������� � ������� ���� ���������
            // �� ��� composing
            if (to.origin!=Contact.ORIGIN_GROUPCHAT) {
                to.addMessage(msg);
                comp=1; // composing event in message
            }
            
        } else if (to.accept_composing) comp=(composing)? 1:2;
        
        if (!Config.getInstance().eventComposing) comp=0;
        
        try {
            if (body!=null /*|| subj!=null*/ || comp>0)
            r.sendMessage(to, body, subj, comp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((VirtualList)parentView).redraw();
    }
    
    public void destroyView(){
        if (display!=null)   display.setCurrent(parentView);
    }

}
