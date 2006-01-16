/*
 * ConferenceGroup.java
 *
 * Created on 29 ������ 2005 �., 23:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Conference;

import Client.Contact;
import Client.Group;
import images.RosterIcons;
import java.util.*;
import ui.ImageList;

/**
 *
 * @author EvgS
 */
public class ConferenceGroup extends Group{
    
    /** Creates a new instance of ConferenceGroup */
    public ConferenceGroup(String name, String label) {
	super(name);
	this.label=label;
	imageExpandedIndex=RosterIcons.ICON_GCJOIN_INDEX;
    }

    String label;
    
    private Contact selfContact;
    private Contact conference;
    public String toString(){ return title(label); }

    public Contact getSelfContact() { return selfContact; }
    public void setSelfContact(Contact selfContact) { this.selfContact=selfContact; }
    public Contact getConference() { return conference; }
    public void setConference(Contact conference) { this.conference=conference; }
    // �� ������� ������ � ����� ����������
    public int getOnlines(){ return (onlines>0)? onlines-1:0; }
    public int getNContacts(){ return (nContacts>0)? nContacts-1:0; }

}
