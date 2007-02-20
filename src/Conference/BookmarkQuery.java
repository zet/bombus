/*
 * BookmarkQurery.java
 *
 * Created on 6.11.2006, 22:24
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
import com.alsutton.jabber.JabberBlockListener;
import com.alsutton.jabber.JabberDataBlock;
import com.alsutton.jabber.datablocks.Iq;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author Evg_S
 */
public class BookmarkQuery implements JabberBlockListener{

    public final static boolean SAVE=true;
    public final static boolean LOAD=false;
    
    /** Creates a new instance of BookmarkQurery */
    public BookmarkQuery(boolean saveBookmarks) {
        
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
        //System.out.println("Bookmarks query sent");
    }
    
    
    public int blockArrived(JabberDataBlock data) {
        try {
            if (!(data instanceof Iq)) return JabberBlockListener.BLOCK_REJECTED;
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
                
                //System.out.println("Bookmark query result success");
                return JabberBlockListener.NO_MORE_BLOCKS;
            }
        } catch (Exception e) {}
        return JabberBlockListener.BLOCK_REJECTED;
    }
}
