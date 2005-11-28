/*
 * Groups.java
 *
 * Created on 8 ��� 2005 �., 0:36
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Client;

import java.util.*;
import ui.ImageList;

/**
 *
 * @author Evg_S
 */
public class Groups{
    
    Vector groups;
    
    public final static int TRANSP_INDEX=0;
    public final static String TRANSP_GROUP="Transports";
    public final static int SELF_INDEX=1;
    public final static String SELF_GROUP="Self-Contact";
    public final static int SRC_RESULT_INDEX=2;
    public final static String SRC_RESULT_GROUP="Search results";
    public final static int NIL_INDEX=3;
    public final static String NIL_GROUP="Not-In-List";
    public final static int IGNORE_INDEX=4;
    public final static String IGNORE_GROUP="Ignore-List";
    public final static int COMMON_INDEX=5;
    public final static String COMMON_GROUP="General";
    
    public Groups(){
        groups=new Vector();
        addGroup(Groups.TRANSP_GROUP, null);
        addGroup(Groups.SELF_GROUP, null);
        addGroup(Groups.SRC_RESULT_GROUP, null);
        addGroup(Groups.NIL_GROUP, null);
        addGroup(Groups.IGNORE_GROUP, null);
        addGroup(Groups.COMMON_GROUP, null);
    }
    
    public void resetCounters(){
        for (Enumeration e=groups.elements();e.hasMoreElements();){
            Group grp=(Group)e.nextElement();
            grp.tncontacts=grp.tonlines=0;
            grp.Contacts=new Vector();
        }
    }
    
    public void addToVector(Vector d, int index){
        Group gr=getGroup(index);
        if (gr.Contacts.size()>0){
            d.addElement(gr);
            if (!gr.collapsed) for (Enumeration e=gr.Contacts.elements();e.hasMoreElements();){
                d.addElement(e.nextElement());
            }
        }
        gr.onlines=gr.tonlines;
        gr.ncontacts=gr.tncontacts;
        gr.Contacts=null;
    }

    public Group getGroup(int Index) {
        return (Group)groups.elementAt(Index);
    }
    
    public Group getGroup(String Name) {
        for (Enumeration e=groups.elements();e.hasMoreElements();){
            Group grp=(Group)e.nextElement();
            if (Name.equals(grp.name)) return grp;
        }
        return null;
    }
    public Group addGroup(String name, String label) {
        Group grp=new Group(groups.size(),name, label);
        groups.addElement(grp);
        return grp;
    }

    public Vector getRosterGroupNames(){
        Vector s=new Vector();
        for (int i=Groups.COMMON_INDEX; i<groups.size(); i++) {
	    Group grp=(Group) groups.elementAt(i);
	    if (grp.imageExpandedIndex!=ImageList.ICON_GCJOIN_INDEX)
            s.addElement(grp.name);
        }
        s.addElement(Groups.IGNORE_GROUP);
        return s;
    }
    public int getCount() {return groups.size();}
    
}
