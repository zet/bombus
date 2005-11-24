/*
 * QueryConfigForm.java
 *
 * Created on 11 ������� 2005 �., 0:35
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Conference;

import Client.StaticData;
import ServiceDiscovery.DiscoForm;
import com.alsutton.jabber.JabberBlockListener;
import com.alsutton.jabber.JabberDataBlock;
import com.alsutton.jabber.datablocks.Iq;
import com.alsutton.jabber.JabberStream;
import javax.microedition.lcdui.Display;

/**
 *
 * @author EvgS
 */
public class QueryConfigForm implements JabberBlockListener{
    
    private final static String OWNER_XMLNS="http://jabber.org/protocol/muc#owner";
    private Display display;
    /** Creates a new instance of QueryConfigForm */
    public QueryConfigForm(Display display, String roomJid) {
        JabberDataBlock getform=new Iq();
        getform.setTypeAttribute("get");
        getform.setAttribute("to", roomJid);
        getform.addChild("query", null).setNameSpace(OWNER_XMLNS);
        JabberStream stream=StaticData.getInstance().roster.theStream;
        stream.addBlockListener(this);
        stream.send(getform);
        StaticData.getInstance().roster.setQuerySign(true);
        this.display=display;
    }
    
    public int blockArrived(JabberDataBlock data) {
        JabberDataBlock query=data.findNamespace(OWNER_XMLNS);
        if (query!=null) {
            StaticData.getInstance().roster.setQuerySign(false);
            if (data.getTypeAttribute().equals("result")) {
                new DiscoForm(display, data, StaticData.getInstance().roster.theStream, "muc_owner", "query");
            }
            return JabberBlockListener.NO_MORE_BLOCKS;
        }
        return JabberBlockListener.BLOCK_REJECTED;
    }
}
