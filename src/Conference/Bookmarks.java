/*
 * Bookmarks.java
 *
 * Created on 18 Сентябрь 2005 пїЅ., 0:03
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Conference;
import Client.*;
import images.RosterIcons;
import javax.microedition.lcdui.*;
import locale.SR;
import ui.*;
import java.util.*;
import com.alsutton.jabber.*;
import com.alsutton.jabber.datablocks.Iq;

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
    
    private BookmarkItem toAdd;
    
    private Command cmdCancel=new Command (SR.MS_CANCEL, Command.BACK, 99);
    private Command cmdJoin=new Command (SR.MS_JOIN, Command.SCREEN, 10);
    private Command cmdRfsh=new Command (SR.MS_REFRESH, Command.SCREEN, 11);
    private Command cmdDel=new Command (SR.MS_DELETE, Command.SCREEN, 12);
    
    Roster roster=StaticData.getInstance().roster;

    JabberStream stream=roster.theStream;
    /** Creates a new instance of Bookmarks */
    public Bookmarks(Display display, BookmarkItem toAdd) {
        super (display);
        setTitleItem(new Title(2, null, SR.MS_BOOKMARKS));
        
        this.toAdd=toAdd;
        
        bookmarks=roster.bookmarks;
        if ( bookmarks==null ) loadBookmarks(); 
        else if (toAdd!=null) addBookmark();
        
        addCommand(cmdCancel);
        addCommand(cmdJoin);
        addCommand(cmdRfsh);
        addCommand(cmdDel);
        setCommandListener(this);
    }
    
    private void processIcon(boolean processing){
        getTitleItem().setElementAt((processing)?(Object)new Integer(RosterIcons.ICON_PROGRESS_INDEX):(Object)null, 0);
        redraw();
    }
    
    protected int getItemCount() { return (bookmarks==null)?0: bookmarks.size(); }
    protected VirtualElement getItemRef(int index) { return (VirtualElement) bookmarks.elementAt(index); }
    
    public void loadBookmarks() {
        stream.addBlockListener(this);
        JabberDataBlock rq=new JabberDataBlock("storage", null, null);
        rq.setNameSpace("storage:bookmarks");
        bookmarksRq(false, rq, "getbookmarks");
    }
    
    // пока здесь, но вообще-то это storageRq
    public void bookmarksRq(boolean set, JabberDataBlock child, String id) {
        JabberDataBlock request=new Iq(null, /*(set)?"set":"get"*/ set, id);
        //request.setAttribute("to", StaticData.getInstance().account.getBareJid());
        JabberDataBlock query=request.addChild("query", null);
        query.setNameSpace("jabber:iq:private");
        query.addChild(child);
        
        processIcon(true);
        //System.out.println(request.toString());
        stream.send(request);
    }
    
    public int blockArrived(JabberDataBlock data) {
        try {
            ///System.out.println(data.toString());
            
            if (data.getAttribute("id").equals("getbookmarks")) {
                JabberDataBlock storage=data.findNamespace("jabber:iq:private").
                        findNamespace("storage:bookmarks");
                Vector bookmarks=new Vector();
                try {
                    for (Enumeration e=storage.getChildBlocks().elements(); e.hasMoreElements(); ){
                        bookmarks.addElement(new BookmarkItem((JabberDataBlock)e.nextElement()));
                    }
                } catch (Exception e) { /* no any bookmarks */}
                //StaticData.getInstance().roster.bookmarks=
                this.bookmarks=bookmarks;
                
                addBookmark();
                
                if (display!=null) redraw();
                roster.bookmarks=this.bookmarks=bookmarks;
                
                processIcon(false);
                return JabberBlockListener.NO_MORE_BLOCKS;
            }
        } catch (Exception e) { }
        return JabberBlockListener.BLOCK_REJECTED;
    }

    private void addBookmark() {
        if (toAdd!=null) {
            this.bookmarks.addElement(toAdd);
            saveBookmarks();
        }
    }
    
    public void eventOk(){
        BookmarkItem join=(BookmarkItem)getFocusedObject();
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
        if (c==cmdDel) deleteBookmark();
    }
    
    private void deleteBookmark(){
        BookmarkItem del=(BookmarkItem)getFocusedObject();
        if (del==null) return;
        if (del.isUrl) return;
        bookmarks.removeElement(del);
        saveBookmarks();
        roster.bookmarks=this.bookmarks=bookmarks;
        redraw();
    }
    
    private void saveBookmarks() {
        JabberDataBlock rq=new JabberDataBlock("storage", null, null);
        rq.setNameSpace("storage:bookmarks");
        for (Enumeration e=bookmarks.elements(); e.hasMoreElements(); ) {
            rq.addChild( ((BookmarkItem)e.nextElement()).constructBlock() );
        }
        bookmarksRq(true, rq, "getbookmarks");
    }

    private void exitBookmarks(){
        stream.cancelBlockListener(this);
        destroyView();
        //display.setCurrent(StaticData.getInstance().roster);
    }
}
