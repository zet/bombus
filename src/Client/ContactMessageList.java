/*
 * ContactMessageList.java
 *
 * Created on 19.02.2005, 23:54
 *
 * Copyright (c) 2005-2007, Eugene Stahov (evgs), http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * You can also redistribute and/or modify this program under the
 * terms of the Psi License, specified in the accompanied COPYING
 * file, as published by the Psi Project; either dated January 1st,
 * 2005, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package Client;
import Conference.MucContact;
import Messages.MessageList;
import Messages.MessageParser;
import archive.MessageArchive;
import images.RosterIcons;
import images.SmilesIcons;
import locale.SR;
import vcard.VCard;
import ui.*;
import java.util.*;
import javax.microedition.lcdui.*;
/**
 *
 * @author Eugene Stahov
 */
public class ContactMessageList extends MessageList
        implements YesNoAlert.YesNoListener
{
    
    Contact contact;
    Command cmdSubscribe=new Command(SR.MS_SUBSCRIBE, Command.SCREEN, 1);
    Command cmdUnsubscribed=new Command(SR.MS_DECLINE, Command.SCREEN, 2);
    Command cmdMessage=new Command(SR.MS_NEW_MESSAGE,Command.SCREEN,3);
    Command cmdResume=new Command(SR.MS_RESUME,Command.SCREEN,1);
    Command cmdQuote=new Command(SR.MS_QUOTE,Command.SCREEN,5);
    Command cmdReply=new Command(SR.MS_REPLY,Command.SCREEN,4);
    Command cmdArch=new Command(SR.MS_ADD_ARCHIVE,Command.SCREEN,6);
    Command cmdPurge=new Command(SR.MS_CLEAR_LIST, Command.SCREEN, 10);
    Command cmdContact=new Command(SR.MS_CONTACT,Command.SCREEN,11);
    Command cmdActive=new Command(SR.MS_ACTIVE_CONTACTS,Command.SCREEN,11);
    
    StaticData sd;
    
    /** Creates a new instance of MessageList */
    public ContactMessageList(Contact contact, Display display) {
        super(display);
        this.contact=contact;
        sd=StaticData.getInstance();
        
        Title title=new Title(contact);
        setTitleItem(title);
        
        title.addRAlign();
        title.addElement(null);
        title.addElement(null);
        //setTitleLine(title);

        cursor=0;//activate
        
        addCommand(cmdMessage);
        addCommand(cmdPurge);
        addCommand(cmdContact);
	addCommand(cmdActive);
        //if (getItemCount()>0) {
        addCommand(cmdQuote);
        addCommand(cmdArch);
	//}
        if (contact instanceof MucContact && contact.origin==Contact.ORIGIN_GROUPCHAT) {
            addCommand(cmdReply);
        }
        setCommandListener(this);
        moveCursorTo(contact.firstUnread(), true);
        //setRotator();
    }
    
    public void showNotify(){
        super.showNotify();
        if (cmdResume==null) return;
        if (contact.msgSuspended==null) removeCommand(cmdResume);
        else addCommand(cmdResume);
        
        if (cmdSubscribe==null) return;
        try {
            Msg msg=(Msg) contact.msgs.elementAt(cursor); 
            if (msg.messageType==Msg.MESSAGE_TYPE_AUTH) {
                addCommand(cmdSubscribe);
                addCommand(cmdUnsubscribed);
            }
            else {
                removeCommand(cmdSubscribe);
                removeCommand(cmdUnsubscribed);
            }
        } catch (Exception e) {}
        
    }
    
    protected void beginPaint(){
        getTitleItem().setElementAt(sd.roster.getEventIcon(), 2);
        getTitleItem().setElementAt((contact.vcard==null)?null:RosterIcons.iconHasVcard, 3);
        //getTitleItem().setElementAt(contact.incomingComposing, 3);
    }
    
    public void markRead(int msgIndex) {
	if (msgIndex>=getItemCount()) return;
	//Msg msg=getMessage(msgIndex);
        //if (msg.unread) contact.resetNewMsgCnt();
        //msg.unread=false;
        if (msgIndex<contact.lastUnread) return;
        //if (contact.needsCount())
            sd.roster.countNewMsgs();
    }
    
    
    public int getItemCount(){ return contact.msgs.size(); }
    //public Element getItemRef(int Index){ return (Element) contact.msgs.elementAt(Index); }

    public Msg getMessage(int index) { 
	Msg msg=(Msg) contact.msgs.elementAt(index); 
	if (msg.unread) contact.resetNewMsgCnt();
	msg.unread=false;
	return msg;
    }
    
    public void focusedItem(int index){ 
        markRead(index); 
        /*try {
            Msg msg=(Msg) contact.msgs.elementAt(index); 
            if (msg.messageType==Msg.MESSAGE_TYPE_AUTH) addCommand(cmdSubscribe);
            else removeCommand(cmdSubscribe);
        } catch (Exception e) {}*/
    }
        
    public void commandAction(Command c, Displayable d){
        super.commandAction(c,d);

        /** login-insensitive commands */
        if (c==cmdArch) {
            try {
                MessageArchive.store(getMessage(cursor));
            } catch (Exception e) {/*no messages*/}
        }
        if (c==cmdPurge) {
            clearMessageList();
        }
        
        /** login-critical section */
        if (!sd.roster.isLoggedIn()) return;

        if (c==cmdMessage) { 
            contact.msgSuspended=null; 
            keyGreen(); 
        }
        if (c==cmdResume) { keyGreen(); }
        if (c==cmdQuote) {
            try {
                String msg=new StringBuffer()
                    .append((char)0xbb) // »
                    .append(getMessage(cursor).toString())
                    .append("\n")
                    .toString();
                new MessageEdit(display,contact,msg);
            } catch (Exception e) {/*no messages*/}
        }
        if (c==cmdReply) {
            try {
                if (getMessage(cursor).messageType < Msg.MESSAGE_TYPE_HISTORY) return;
                if (getMessage(cursor).messageType == Msg.MESSAGE_TYPE_SUBJ) return;
                
                Msg msg=getMessage(cursor);
                /*String body=msg.toString();
                int nickLen=body.indexOf(">");
                if (nickLen<0) nickLen=body.indexOf(" ");
                if (nickLen<0) return;*/
                
                new MessageEdit(display,contact,msg.from+": ");
            } catch (Exception e) {/*no messages*/}
        }
        if (c==cmdContact) {
            new RosterItemActions(display, contact, -1);
        }
	
	if (c==cmdActive) {
	    new ActiveContacts(display, contact);
	}
        
        if (c==cmdSubscribe) {
            sd.roster.doSubscribe(contact);
        }
        
        if (c==cmdUnsubscribed) {
            sd.roster.sendPresence(contact.getBareJid(), "unsubscribed", null);
        }
    }


    private void clearMessageList() {
        //TODO: fix scrollbar size
        moveCursorHome();
        contact.purge();
        messages=new Vector();
        System.gc();
        redraw();
    }
    
    public void keyGreen(){
        if (!sd.roster.isLoggedIn()) return;
        (new MessageEdit(display,contact,contact.msgSuspended)).setParentView(this);
        contact.msgSuspended=null;
    }
    
    public void keyRepeated(int keyCode) {
	if (keyCode==KEY_NUM3) new ActiveContacts(display, contact);
	else super.keyRepeated(keyCode);
    }

    public void userKeyPressed(int keyCode) {
        super.userKeyPressed(keyCode);
        if (keyCode==keyClear) {
            if (messages.isEmpty()) return;
            new YesNoAlert(display, SR.MS_CLEAR_LIST, SR.MS_SURE_CLEAR, this);
        }
    }

    public void ActionConfirmed() { clearMessageList(); }
}
