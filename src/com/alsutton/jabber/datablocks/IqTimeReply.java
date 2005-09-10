/*
 * IqTimeReply.java
 *
 * Created on 10 �������� 2005 �., 23:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.alsutton.jabber.datablocks;

import com.alsutton.jabber.JabberDataBlock;

/**
 *
 * @author EvgS
 */
public class IqTimeReply extends JabberDataBlock{
    
    /** Creates a new instance of IqTimeReply */
    public IqTimeReply(JabberDataBlock request) {
        super();
        setTypeAttribute("result");
        setAttribute("id",request.getAttribute("id"));
        setAttribute("to",request.getAttribute("from"));
        JabberDataBlock query=new JabberDataBlock("query",this,null);
        addChild(query);
        query.setNameSpace("jabber:iq:time");
        query.addChild("utc",ui.Time.utcLocalTime());
        query.addChild("display", ui.Time.dispLocalTime());
        System.out.println(this);
    }
    public String getTagName() {
        return "iq";
    }
}
