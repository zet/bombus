/*
 * MessageList.java
 *
 * Created on 11.12.2005, 3:02
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

package Messages;

import Client.Config;
import Client.Msg;
//import Messages.MessageView;
import images.SmilesIcons;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import locale.SR;
import ui.ComplexString;
import ui.FontCache;
import ui.VirtualElement;
import ui.VirtualList;

/**
 *
 * @author EvgS
 */
public abstract class MessageList 
    extends VirtualList
    implements CommandListener
{
    
    protected Vector messages;
    
    protected Command cmdBack = new Command(SR.MS_BACK, Command.BACK, 99);
    protected Command cmdUrl = new Command(SR.MS_GOTO_URL, Command.SCREEN, 80);

    protected Command cmdSmiles = new Command(SR.MS_SMILES_TOGGLE, Command.SCREEN, 50);
    
    /** Creates a new instance of MessageList */
  
    public MessageList() {
        super();
	messages=new Vector();
        smiles=Config.getInstance().smiles;
        //sd.config.updateTime();
    
	enableListWrapping(false);
	
        cursor=0;//activate
        
        addCommand(cmdSmiles);
        addCommand(cmdBack);
        addCommand(cmdUrl);
        stringHeight=FontCache.getMsgFont().getHeight();
    }

    public MessageList(Display display) {
        this();
        attachDisplay(display);
    }
    
    
    public abstract int getItemCount(); // из protected сделали public

    protected VirtualElement getItemRef(int index) {
	if (messages.size()<getItemCount()) messages.setSize(getItemCount());
	MessageItem mi=(MessageItem) messages.elementAt(index);
	if (mi==null) {
	    mi=new MessageItem(getMessage(index), this, smiles);
            mi.setEven( (index & 1) == 0);
	    messages.setElementAt(mi, index);
	}
	return mi;
    }
    
    public abstract Msg getMessage(int index);
	//public Element getItemRef(int Index){ return (Element) contact.msgs.elementAt(Index); }

    // overriden, т.к. элементы списка - строки.
    /*public Object getFocusedObject(){
        try {
            return getMessage(cursor);
        } catch (Exception e) {}
        return null;
    }*/
    
    public void markRead(int msgIndex) {}
    
    protected boolean smiles;

    public void commandAction(Command c, Displayable d) {
        if (c==cmdBack) destroyView();
        if (c==cmdUrl) {
            try {
                Vector urls=((MessageItem) getFocusedObject()).getUrlList();
                new MessageUrl(display, urls); //throws NullPointerException if no urls
            } catch (Exception e) {/* no urls found */}
        }
        if (c==cmdSmiles) {
            try {
                ((MessageItem)getFocusedObject()).toggleSmiles();
            } catch (Exception e){}
        }
    }

    protected void keyPressed(int keyCode) { // overriding this method to avoid autorepeat
        super.keyPressed(keyCode);
        if (keyCode=='*') 
            try {
                ((MessageItem)getFocusedObject()).toggleSmiles();
            } catch (Exception e){}
    }

    public void keyGreen() { eventOk(); }
   
}
