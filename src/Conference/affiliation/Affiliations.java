/*
 * AffiliationList.java
 *
 * Created on 30 ������� 2005 �., 12:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package Conference.affiliation;

import Client.*;
import com.alsutton.jabber.JabberBlockListener;
import com.alsutton.jabber.JabberDataBlock;
import com.alsutton.jabber.JabberStream;
import com.alsutton.jabber.datablocks.Iq;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.*;
import ui.ImageList;
import ui.VirtualElement;
import ui.VirtualList;

/**
 *
 * @author EvgS
 */
public class Affiliations 
        extends VirtualList 
        implements CommandListener,
        JabberBlockListener
{

    private Vector items;
    private String id="admin";
    private String namespace="http://jabber.org/protocol/muc#admin";
    private String room;

    private JabberStream stream=StaticData.getInstance().roster.theStream;
    
    private Command cmdCancel=new Command ("Back", Command.BACK, 99);
    private Command cmdModify=new Command ("Modify", Command.SCREEN, 1);
    private Command cmdNew=new Command ("New Jid", Command.SCREEN, 2);
 
    
    protected VirtualElement getItemRef(int index) { return (VirtualElement) items.elementAt(index); }
    protected int getItemCount() { return items.size(); }
    
    
    
    /** Creates a new instance of AffiliationList */
    public Affiliations(Display display, String room, int affiliationIndex) {
        super (display);
        this.room=room;
        this.id=AffiliationItem.getAffiliationName(affiliationIndex);
        setTitleImages(StaticData.getInstance().rosterIcons);
        createTitleItem(2, null, id);
        items=new Vector();
        
        addCommand(cmdCancel);
        addCommand(cmdModify);
        addCommand(cmdNew);
        
        setCommandListener(this);
        getList();
    }
    
    public void getList() {
        JabberDataBlock item=new JabberDataBlock("item", null, null);
        item.setAttribute("affiliation", id);
        listRq(false, item, id);
    }
    
    public void commandAction(Command c, Displayable d){
        if (c==cmdNew) new AffiliationModify(display, room, null, "none");
        if (c==cmdModify) eventOk();
        if (c!=cmdCancel) return;
        stream.cancelBlockListener(this);
        destroyView();
    }
    
    public void eventOk(){
        try {
            AffiliationItem item=(AffiliationItem)atCursor;
            new AffiliationModify(display, room, item.jid, 
                    AffiliationItem.getAffiliationName(item.affiliation));
        } catch (Exception e) { }
    }
    
    private void processIcon(boolean processing){
        getTitleItem().setElementAt((processing)?(Object)new Integer(ImageList.ICON_PROGRESS_INDEX):(Object)null, 0);
        redraw();
    }
    
    public int blockArrived(JabberDataBlock data) {
        try {
            ///System.out.println(data.toString());
            
            if (data.getAttribute("id").equals(id)) {
                JabberDataBlock query=data.findNamespace(namespace);
                Vector items=new Vector();
                try {
                    for (Enumeration e=query.getChildBlocks().elements(); e.hasMoreElements(); ){
                        items.addElement(new AffiliationItem((JabberDataBlock)e.nextElement()));
                    }
                } catch (Exception e) { /* no any items */}
                //StaticData.getInstance().roster.bookmarks=
                this.items=items;
                
                if (display!=null) redraw();
                
                processIcon(false);
                return JabberBlockListener.NO_MORE_BLOCKS;
            }
        } catch (Exception e) { }
        return JabberBlockListener.BLOCK_REJECTED;
    }
    
    public void listRq(boolean set, JabberDataBlock child, String id) {
        
        JabberDataBlock request=new Iq();
        request.setTypeAttribute((set)?"set":"get");
        request.setAttribute("to", room);
        request.setAttribute("id", id);
        JabberDataBlock query=request.addChild("query", null);
        query.setNameSpace(namespace);
        query.addChild(child);
        
        processIcon(true);
        //System.out.println(request.toString());
        stream.addBlockListener(this);
        stream.send(request);
    }
}
