/*
 * Bookmarks.java
 *
 * Created on 18 �������� 2005 �., 0:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package Conference;
import Client.*;
import javax.microedition.lcdui.*;
import ui.*;
import java.util.*;
import com.alsutton.jabber.*;

/**
 *
 * @author EvgS
 */
public class Bookmarks 
        extends VirtualList 
        implements CommandListener,
        JabberBlockListener
{
    
    private Vector bookmarks;
    
    private Command cmdCancel=new Command ("Back", Command.BACK, 99);
    private Command cmdJoin=new Command ("Join", Command.SCREEN, 10);
    private Command cmdRfsh=new Command ("Refresh", Command.SCREEN, 11);
    private Command cmdDel=new Command ("Delete", Command.SCREEN, 12);

    JabberStream stream=StaticData.getInstance().roster.theStream;
    /** Creates a new instance of Bookmarks */
    public Bookmarks(Display display) {
        super (display);
        createTitleItem(1, "Privacy Lists", null);
        
        bookmarks=new Vector();
        
        loadBookmarks();
        addCommand(cmdCancel);
        addCommand(cmdJoin);
        addCommand(cmdRfsh);
        addCommand(cmdDel);
        setCommandListener(this);
    }
    
    public int getItemCount() { return bookmarks.size(); }
    public VirtualElement getItemRef(int index) { return (VirtualElement) bookmarks.elementAt(index); }
    
    public void loadBookmarks() {
        stream.addBlockListener(this);
        JabberDataBlock rq=new JabberDataBlock("storage", null, null);
        rq.setNameSpace("storage:bookmarks");
        bookmarksRq(false, rq, "getbookmarks");
    }
    
    // ���� �����, �� ������-�� ��� storageRq
    public void bookmarksRq(boolean set, JabberDataBlock child, String id) {
        JabberDataBlock request=new JabberDataBlock("iq", null, null);
        request.setTypeAttribute((set)?"set":"get");
        //request.setAttribute("to", StaticData.getInstance().account.getBareJid());
        request.setAttribute("id", id);
        JabberDataBlock query=request.addChild("query", null);
        query.setNameSpace("jabber:iq:private");
        query.addChild(child);
        System.out.println(request.toString());
        stream.send(request);
    }
    
    public int blockArrived(JabberDataBlock data) {
        try {
            ///System.out.println(data.toString());
            
            if (data.getAttribute("id").equals("getbookmarks")) {
                JabberDataBlock storage=data.findNamespace("jabber:iq:private").
                        findNamespace("storage:bookmarks");
                Vector bookmarks=new Vector();
                for (Enumeration e=storage.getChildBlocks().elements(); e.hasMoreElements(); ){
                    bookmarks.addElement(new BookmarkItem((JabberDataBlock)e.nextElement()));
                }
                //StaticData.getInstance().roster.bookmarks=
                this.bookmarks=bookmarks;
                if (display!=null) redraw();
                return JabberBlockListener.NO_MORE_BLOCKS;
            }
        } catch (Exception e) { }
        return JabberBlockListener.BLOCK_REJECTED;
    }
    
    public void eventOk(){
        BookmarkItem join=(BookmarkItem)atCursor;
        if (join==null) return;
        if (join.isUrl) return;
        ConferenceForm.join(join.toString(), join.password);
        stream.cancelBlockListener(this);
        display.setCurrent(StaticData.getInstance().roster);
    }
    
    public void commandAction(Command c, Displayable d){
        if (c==cmdCancel) exitBookmarks();
        if (c==cmdJoin) eventOk();
        if (c==cmdRfsh) loadBookmarks();
    }

    private void exitBookmarks(){
        stream.cancelBlockListener(this);
        destroyView();
        //display.setCurrent(StaticData.getInstance().roster);
    }
}
