/*
 * BookmarkQurery.java
 *
 * Created on 6 Ноябрь 2006 г., 22:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Conference;

import Client.StaticData;
import com.alsutton.jabber.JabberBlockListener;
import com.alsutton.jabber.JabberDataBlock;
import com.alsutton.jabber.datablocks.Iq;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author Evg_S
 */
public class BookmarkQurery implements JabberBlockListener{

    public final static boolean SAVE=true;
    public final static boolean LOAD=false;
    
    /** Creates a new instance of BookmarkQurery */
    public BookmarkQurery(boolean saveBookmarks) {
        
        JabberDataBlock request=new Iq(null, (saveBookmarks)?Iq.TYPE_SET: Iq.TYPE_GET, "getbookmarks");
        JabberDataBlock query=request.addChild("query", null);
        query.setNameSpace("jabber:iq:private");

        JabberDataBlock storage=query.addChild("storage", null);
        storage.setNameSpace("storage:bookmarks");
        if (saveBookmarks) 
            for (Enumeration e=StaticData.getInstance().roster.bookmarks.elements(); e.hasMoreElements(); ) {
            storage.addChild( ((BookmarkItem)e.nextElement()).constructBlock() );
        }
        
        StaticData.getInstance().roster.theStream.send(request);
    }
    
    
    public int blockArrived(JabberDataBlock data) {
        try {
            
            if (data.getAttribute("id").equals("getbookmarks")) {
                JabberDataBlock storage=data.findNamespace("jabber:iq:private").
                        findNamespace("storage:bookmarks");
                Vector bookmarks=new Vector();
                try {
                    for (Enumeration e=storage.getChildBlocks().elements(); e.hasMoreElements(); ){
                        bookmarks.addElement(new BookmarkItem((JabberDataBlock)e.nextElement()));
                    }
                } catch (Exception e) { /* no any bookmarks */}

                StaticData.getInstance().roster.bookmarks=bookmarks;
                StaticData.getInstance().roster.redraw();
                
                return JabberBlockListener.NO_MORE_BLOCKS;
            }
        } catch (Exception e) { }
        return JabberBlockListener.BLOCK_REJECTED;
    }
}
