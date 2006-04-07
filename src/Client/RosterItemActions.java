/*
 * RosterItemActions.java
 *
 * Created on 11 ������� 2005 �., 19:05
 *
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Client;

import Conference.ConferenceGroup;
import Conference.QueryConfigForm;
import Conference.affiliation.Affiliations;
import ServiceDiscovery.ServiceDiscovery;
import com.alsutton.jabber.datablocks.IqVersionReply;
import com.alsutton.jabber.datablocks.Presence;
import java.util.Hashtable;
import javax.microedition.lcdui.Display;
import locale.SR;
import ui.IconTextElement;
import ui.Menu;
import ui.MenuItem;
import ui.YesNoAlert;
import vcard.VCard;
import vcard.vCardForm;

/**
 *
 * @author EvgS
 */
public class RosterItemActions extends Menu{
    
    Object item;
    
    /** Creates a new instance of RosterItemActions */
    public RosterItemActions(Display display, Object item) {
	super(item.toString());
	this.item=item;
	
        if (item==null) return;
        boolean isContact=( item instanceof Contact );

	if (isContact) {
	    Contact contact=(Contact)item;
	    if (contact.getGroupIndex()==Groups.TRANSP_INDEX) {
		addItem(SR.MS_LOGON,5);
		addItem(SR.MS_LOGOFF,6);
		addItem(SR.MS_RESOLVE_NICKNAMES, 7);
	    }
	    //if (contact.group==Groups.SELF_INDEX) addItem("Commands",30);
	    
	    addItem(SR.MS_VCARD,1);
	    addItem(SR.MS_CLIENT_INFO,0);
	    addItem(SR.MS_COMMANDS,30);
	    
	    if (contact.getGroupIndex()!=Groups.SELF_INDEX && contact.getGroupIndex()!=Groups.SRC_RESULT_INDEX && contact.origin<Contact.ORIGIN_GROUPCHAT) {
		if (contact.getGroupIndex()!=Groups.TRANSP_INDEX)
		    addItem(SR.MS_EDIT,2);
		addItem(SR.MS_SUBSCRIPTION,3);
		addItem(SR.MS_DELETE,4);
	    }
	    if (contact.realJid!=null) {
		addItem(SR.MS_KICK,8);
		addItem(SR.MS_BAN,9);
                addItem(SR.MS_GRANT_VOICE,31);
                addItem(SR.MS_REVOKE_VOICE,32);
               //m.addItem(new MenuItem("Set Affiliation",15));
	    }
	} else {
	    Group group=(Group)item;
	    if (group.index==Groups.SRC_RESULT_INDEX)
		addItem(SR.MS_DISCARD,21);
	    if (group instanceof ConferenceGroup) {
		Contact self=((ConferenceGroup)group).getSelfContact();
		if (self.status==Presence.PRESENCE_OFFLINE)
		    addItem(SR.MS_REENTER,23);
		else {
		    addItem(SR.MS_LEAVE_ROOM,22);
		    if (self.transport>0) { // �������� ���
			addItem(SR.MS_CONFIG_ROOM,10);
			addItem(SR.MS_OWNERS,11);
			addItem(SR.MS_ADMINS,12);
			addItem(SR.MS_MEMBERS,13);
			addItem(SR.MS_BANNED,14);
		    }
		}
	    }
	    //m.addItem(new MenuItem("Cleanup offlines"))
	}
	if (getItemCount()>0) attachDisplay(display);
	
    }
    
    public void eventOk(){
	final Roster roster=StaticData.getInstance().roster;
        boolean isContact=( item instanceof Contact );
	Contact c = null;
	Group g = null;
	if (isContact) c=(Contact)item; else g=(Group) item;
	
	MenuItem me=(MenuItem) getFocusedObject();
	if (me==null) {
	    destroyView(); return;
	}
	int index=me.index;
	String to=null;
	if (isContact) to=(index<3)? c.getJid() : c.getBareJid();
	destroyView();
	switch (index) {
	    case 0: // info
		roster.setQuerySign(true);
		roster.theStream.send(new IqVersionReply(to));
		break;
	    case 1: // vCard
		if (c.vcard!=null) {
		    new vCardForm(display, c.vcard, c.getGroupIndex()==Groups.SELF_INDEX);
		    return;
		}
		VCard.request(c.getJid());
		break;
		
	    case 2:
		(new ContactEdit(display, c )).parentView=roster;
		return; //break;
		
	    case 3: //subscription
		new SubscriptionEdit(display, c);
		return; //break;
	    case 4:
		new YesNoAlert(display, roster, SR.MS_DELETE_ASK, c.getNickJid()){
		    public void yes() {
			roster.deleteContact((Contact)item);
		    };
		};
		return;
		//new DeleteContact(display,c);
		//break;
	    case 6: // logoff
	    {
		//querysign=true; displayStatus();
		Presence presence = new Presence(
		Presence.PRESENCE_OFFLINE, -1, "");
		presence.setTo(c.getJid());
		roster.theStream.send( presence );
		break;
	    }
	    case 5: // logon
	    {
		//querysign=true; displayStatus();
		Presence presence = new Presence(roster.myStatus, 0, "");
		presence.setTo(c.getJid());
		roster.theStream.send( presence );
		break;
	    }
	    case 7: // Nick resolver
	    {
		roster.resolveNicknames(c.transport);
		break;
	    }
	    case 8: // kick
	    {
		Hashtable attrs=new Hashtable();
		attrs.put("role", "none");
		attrs.put("nick", c.jid.getResource().substring(1));
		roster.setMucMod(c, attrs);
		break;
	    }
	    case 9: // ban
	    {
		Hashtable attrs=new Hashtable();
		attrs.put("affiliation", "outcast");
		attrs.put("jid", c.realJid);
		roster.setMucMod(c, attrs);
		break;
	    }
	    case 10: // room config
	    {
		String roomJid=((ConferenceGroup)g).getConference().getJid();
		new QueryConfigForm(display, roomJid);
		break;
	    }
	    case 11: // owners
	    case 12: // admins
	    case 13: // members
	    case 14: // outcasts
	    {
		String roomJid=((ConferenceGroup)g).getConference().getJid();
		new Affiliations(display, roomJid, index-10);
		return;
	    }
		    /*case 15: // affiliation
		    {
			String roomJid=conferenceRoomContact(g.index).getJid();
			new AffiliationModify(display, roomJid, c.realJid, affiliation)(display, roomJid, index-10);
		    }
		     */
	    case 21:
	    {
		roster.cleanupSearch();
		break;
	    }
	    case 22:
	    {
		roster.leaveRoom( 0, g);
		break;
	    }
	    case 23:
	    {
		roster.reEnterRoom( g );
		break;
	    }
	    case 30:
	    {
		new ServiceDiscovery(display, c.getJid(), "http://jabber.org/protocol/commands");
		return;
	    }
            case 31:
            {
                Hashtable attrs=new Hashtable();
                attrs.put("role", "participant");
                attrs.put("nick", c.jid.getResource().substring(1));
                roster.setMucMod(c, attrs);
                break;
            }
            case 32:
            {
                Hashtable attrs=new Hashtable();
                attrs.put("role", "visitor");
                attrs.put("nick", c.jid.getResource().substring(1));
                roster.setMucMod(c, attrs);
                break;
            }
	}
	destroyView();
    }
}
