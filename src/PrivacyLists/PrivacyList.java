/*
 * PrivacyList.java
 *
 * Created on 26 Август 2005 г., 23:08
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package PrivacyLists;
import com.alsutton.jabber.datablocks.Iq;
import images.RosterIcons;
import java.util.*;
import com.alsutton.jabber.*;
import ui.*;
import Client.*;
import com.alsutton.jabber.*;

/**
 *
 * @author EvgS
 */
public class PrivacyList extends IconTextElement{
    
    String name;
    boolean isActive;
    boolean isDefault;
    
    Vector rules=new Vector(); 
    
    /** Creates a new instance of PrivacyList */
    public PrivacyList(String name) {
        super(RosterIcons.getInstance());
        this.name=name;
    }
    
    public int getImageIndex() {return (isActive)?
        RosterIcons.ICON_PRIVACY_ACTIVE:
        RosterIcons.ICON_PRIVACY_PASSIVE; }
    public int getColor() {return Colors.LIST_INK; }
    
    public String toString() {
        StringBuffer result=new StringBuffer((name==null)? "<none>": name);
        result.append(' ');
        if (isDefault) result.append("(default)");
        return result.toString();
    }
    
    
    public void generateList(){
        int index=0;
        
        JabberDataBlock list = listBlock();
        for (Enumeration e=rules.elements(); e.hasMoreElements(); ) {
            
            PrivacyItem item=(PrivacyItem)e.nextElement();
            item.order=index++;
                        
            list.addChild( item.constructBlock() );
        }
        PrivacyList.privacyListRq(true, list, "storelst");
    }

    private JabberDataBlock listBlock() {
        JabberDataBlock list=new JabberDataBlock("list", null, null);
        list.setAttribute("name", name);
        return list;
    }
    
    public void deleteList(){
        JabberDataBlock list=listBlock();
        PrivacyList.privacyListRq(true, list, "storelst");
    }
  
    public void activate (String atr) {
        JabberDataBlock a=new JabberDataBlock(atr, null, null);
        a.setAttribute("name", name);
        privacyListRq(true, a, "plset");
    }
    
    public void addRule(PrivacyItem rule) {
        int index=0;
        while (index<rules.size()) {
            if ( rule.order <= ((PrivacyItem)rules.elementAt(index)).order ) break;
            index++;
        }
        rules.insertElementAt(rule, index);
    }
    
    public final static void privacyListRq(boolean set, JabberDataBlock child, String id){
        JabberDataBlock pl=new Iq(null, (set)? Iq.TYPE_SET: Iq.TYPE_GET, id);
        JabberDataBlock qry=pl.addChild("query", null);
        qry.setNameSpace("jabber:iq:privacy");
        if (child!=null) qry.addChild(child);
        
        //System.out.println(pl);
        StaticData.getInstance().roster.theStream.send(pl);
    }
}
