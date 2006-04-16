/*
 * ScrollBar.java
 *
 * Created on 19 Ноябрь 2005 г., 21:26
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package ui.controls;

import javax.microedition.lcdui.Graphics;
import ui.Colors;
import ui.VirtualList;

/**
 *
 * @author EvgS
 */
public class ScrollBar {
    
    private static final int WIDTH_SCROLL_1      =4;
    private static final int WIDTH_SCROLL_2      =10;
    
    private int yTranslate;
    
    private int size;
    private int windowSize;
    private int position;
    
    private int scrollerX;
    
    private int drawHeight;
    
    private int point_y;    // точка, за которую "держится" указатель
    
    private int scrollerSize;
    private int scrollerPos;
    
    private boolean hasPointerEvents;
    
    private int minimumHeight=3;
    private int scrollWidth=WIDTH_SCROLL_1;
    
    /** Creates a new instance of ScrollBar */
    public ScrollBar() {
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPostion() {
        return position;
    }

    public void setPostion(int postion) {
        this.position = postion;
    }

    public void setHasPointerEvents(boolean hasPointerEvents) {
        this.hasPointerEvents = hasPointerEvents;
	scrollWidth=(hasPointerEvents)? WIDTH_SCROLL_2: WIDTH_SCROLL_1;
    }

    public int getScrollWidth() {
        return scrollWidth;
    }

    public boolean pointerPressed(int x, int y, VirtualList v) {
	if (size==0) return false;
	if (x<scrollerX) return false; // not in area
	y-=yTranslate;
	if (y<scrollerPos) { 
            // page up
            int pos=position-windowSize;
            if (pos<0) pos=0;
            v.win_top=pos;
            v.repaint(); 
            return true; 
        } 
	if (y>scrollerPos+scrollerSize) { 
            int pos=position+windowSize;
            int listEnd=size-windowSize;
            v.win_top=(pos<listEnd)?pos:listEnd;
            v.repaint(); 
            return true; 
        } // page down
	point_y=y-scrollerPos;
	return true;
    }
    public boolean pointerDragged(int x, int y, VirtualList v) {
	y-=yTranslate;
	if (point_y<0) return false;
	int new_top=y-point_y;
	int new_pos=(new_top*size)/drawHeight;
	if ((position-new_pos)==0) return true;
	if (new_pos<0) new_pos=0;
	if (new_pos+windowSize>size) new_pos=size-windowSize;
	v.win_top=new_pos; v.repaint();
	return true;
    }
    public void pointerReleased(int x, int y, VirtualList v) { point_y=-1; }
    
    public void draw(Graphics g) {
	
	yTranslate=g.getTranslateY();
	
	drawHeight=g.getClipHeight();
	int drawWidth=g.getClipWidth();
	
	scrollerX=drawWidth-scrollWidth;

	g.translate(scrollerX, 0);

        g.setColor(Colors.SCROLL_BGND);
	g.fillRect(1, 1, scrollWidth-2, drawHeight-2);
	
        g.setColor(Colors.LIST_BGND);
        g.drawRect(0,0,scrollWidth-1,drawHeight-1);
            
	drawHeight-=minimumHeight;
        
	scrollerSize=(drawHeight*windowSize)/size+minimumHeight;
	
	scrollerPos=(drawHeight*position)/size;
        g.setColor(Colors.SCROLL_BAR);
        g.fillRect(1, scrollerPos, scrollWidth-2, scrollerSize);
        
        g.setColor(Colors.SCROLL_BRD);
        g.drawRect(0, scrollerPos, scrollWidth-1, scrollerSize);
	
	//scrollerPos+=g.getTranslateY();
    }
}
