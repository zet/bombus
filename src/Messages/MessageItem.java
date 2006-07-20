/*
 * MessageItem.java
 *
 * Created on 21 Январь 2006 г., 23:17
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Messages;

import Client.Msg;
import images.RosterIcons;
import images.SmilesIcons;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import ui.Colors;
import ui.ComplexString;
import ui.VirtualElement;
import ui.VirtualList;

/**
 *
 * @author Evg_S
 */
public class MessageItem implements 
	VirtualElement,
	MessageParser.MessageParserNotify
{
    
    Msg msg;
    Vector msgLines;
    private VirtualList view;
    private boolean even;
    private Vector urlList;
    
    /** Creates a new instance of MessageItem */
    public MessageItem(Msg msg, VirtualList view) {
	this.msg=msg;
	this.view=view;
    }

    public int getVHeight() { return msg.itemHeight; }
    
    public int getVWidth() { return 0; }
    
    public int getColorBGnd() {
        return (even)?
            Colors.LIST_BGND_EVEN:
            Colors.LIST_BGND;
    }
    
    public int getColor() { return msg.getColor(); }
    
    public void drawItem(Graphics g, int ofs, boolean selected) {
        /*if (selected)*/
        g.translate(1,0);
        if (msgLines==null) {
            MessageParser.getInstance().parseMsg(this, SmilesIcons.getInstance(), view.getListWidth());
            return;
        }
        int y=0;
        for (Enumeration e=msgLines.elements(); e.hasMoreElements(); ) {
            ComplexString line=(ComplexString) e.nextElement();
            int h=line.getVHeight();
            if (y>=0 && y<g.getClipHeight()) {
                if (msg.itemCollapsed) if (msgLines.size()>1) {
                    RosterIcons.getInstance().drawImage(g, RosterIcons.ICON_MSGCOLLAPSED_INDEX, 0,0);
                    g.translate(8,0); //FIXME: хардкод
                }
                line.drawItem(g, 0, selected);
            }
            g.translate(0, h);
            if (msg.itemCollapsed) break;
        }
    }
    
    public void onSelect() {
        msg.itemCollapsed=!msg.itemCollapsed;
        updateHeight();
    }
    
    byte repaintCounter;
    public void notifyRepaint(Vector v, Msg parsedMsg, boolean finalized) {
        msgLines=v;
        updateHeight();
        if (!finalized) if ((--repaintCounter)>=0) return;
        repaintCounter=5;
        view.redraw();
    }
    
    private void updateHeight() {
        int height=0;
        for (Enumeration e=msgLines.elements(); e.hasMoreElements(); ) {
            ComplexString line=(ComplexString) e.nextElement();
            height+=line.getVHeight();
            if (msg.itemCollapsed) break;
        }
        msg.itemHeight=height;
    }
    
    public void notifyUrl(String url) { 
        if (urlList==null) urlList=new Vector();
        urlList.addElement(url);
    }
    
    public Vector getUrlList() { return urlList; }

    public void setEven(boolean even) {
        this.even = even;
    }

    public String getTipString() {
        return msg.getTime();
    }
}
