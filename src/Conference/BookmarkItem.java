/*
 * BookmarkItem.java
 *
 * Created on 17.09.2005, 23:21
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

package Conference;
import Client.StaticData;
import com.alsutton.jabber.JabberDataBlock;
import com.alsutton.jabber.datablocks.Presence;
import images.RosterIcons;
import ui.*;

/**
 *
 * @author EvgS
 */
public class BookmarkItem extends IconTextElement{
    
    String name;
    String jid;
    String nick;
    String password;
    boolean autojoin;
    boolean isUrl;
    
    public int getImageIndex(){ 
        if (isUrl) return RosterIcons.ICON_PRIVACY_ACTIVE;
        return (autojoin)? RosterIcons.ICON_GCJOIN_INDEX : RosterIcons.ICON_GROUPCHAT_INDEX;
    }
    public String toString(){ return jid+'/'+nick; }
    public String getJid() { return jid; }

    public int getColor(){ return Colors.LIST_INK;}
    
    /** Creates a new instance of BookmarkItem */
    public BookmarkItem() {
        super(RosterIcons.getInstance());
    }
    
    public BookmarkItem(JabberDataBlock data) {
        this();
        isUrl=!data.getTagName().equals("conference");
        name=data.getAttribute("name");
        try {
            autojoin=data.getAttribute("autojoin").equals("true");
        } catch (Exception e) {}
        jid=data.getAttribute((isUrl)?"url":"jid");
        nick=data.getChildBlockText("nick");
        password=data.getChildBlockText("password");
    }
    
    public BookmarkItem(String jid, String nick, String password, boolean autoJoin){
        this();
        this.name=this.jid=jid;
        this.nick=nick;
        this.password=password;
        this.autojoin=autoJoin;
    }
    
    public JabberDataBlock constructBlock() {
        JabberDataBlock data=new JabberDataBlock((isUrl)?"url":"conference", null, null);
        data.setAttribute("name", name);
        data.setAttribute((isUrl)?"url":"jid", jid);
        if (autojoin) data.setAttribute("autojoin", "true");
        if (nick.length()>0) data.addChild("nick",nick);
        if (password.length()>0) data.addChild("password",password);
        
        return data;
    }
}
