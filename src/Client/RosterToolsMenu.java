/*
 * RosterToolsMenu.java
 *
 * Created on 11 ������� 2005 �., 20:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Client;

import PrivacyLists.PrivacySelect;
import ServiceDiscovery.ServiceDiscovery;
import javax.microedition.lcdui.Display;
import ui.Menu;
import ui.MenuItem;

/**
 *
 * @author EvgS
 */
public class RosterToolsMenu 
    extends Menu
{
    
    /** Creates a new instance of RosterToolsMenu */
    public RosterToolsMenu(Display display) {
	super("Jabber Tools");
	addItem("Service Discovery", 0);
	addItem("Privacy Lists", 1);
	/*if (m.getItemCount()>0)*/
	attachDisplay(display);
    }
    public void eventOk(){
	destroyView();
	MenuItem me=(MenuItem) getFocusedObject();
	if (me==null)  return;
	int index=me.index;
	switch (index) {
	    case 0: // Service Discovery
		new ServiceDiscovery(display, null, null);
		break;
	    case 1: // Privacy Lists
		new PrivacySelect(display);
		break;
	}
    }
}