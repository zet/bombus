/*
 * ScrollBar.java
 *
 * Created on 19 ������ 2005 �., 21:26
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package ui.controls;

import javax.microedition.lcdui.Graphics;
import ui.VirtualList;

/**
 *
 * @author EvgS
 */
public class ScrollBar {
    
    private static final int COLOR_SCROLL_PTR     =0x0033ff;
    private static final int COLOR_SCROLL_BGND    =0x888888;
    public static final int COLOR_BGND           =0xFFFFFF;
    private static final int WIDTH_SCROLL_1      =4;
    private static final int WIDTH_SCROLL_2      =8;
    
    private int size;
    private int windowSize;
    private int position;
    
    private int scrollerX;
    
    private int drawHeight;
    
    private int point_y;    // �����, �� ������� "��������" ���������
    
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

    public void pointerPressed(int x, int y, VirtualList v) {
	if (x<scrollerX) return; // not in area
	if (y<scrollerPos) { v.keyLeft(); v.repaint(); return; } // page up
	if (y>scrollerPos+scrollerSize) { v.keyRight(); v.repaint(); return; } // page down
	point_y=y-scrollerPos;
    }
    public void pointerDragged(int x, int y, VirtualList v) {
	if (point_y<0) return;
	int new_top=y-point_y;
	int new_pos=(new_top*size)/drawHeight;
	System.err.println(new_top+" "+new_pos+" "+y+" "+point_y+" ");
	if ((position-new_pos)==0) return;
	if (new_pos<0) new_pos=0;
	if (new_pos+windowSize>size) new_pos=size-windowSize;
	v.win_top=new_pos; v.repaint();
    }
    public void pointerReleased(int x, int y, VirtualList v) { 	point_y=-1; }
    
    public void draw(Graphics g) {
	drawHeight=g.getClipHeight();
	int drawWidth=g.getClipWidth();
	
	scrollerX=drawWidth-scrollWidth;

	g.translate(scrollerX, 0);

        g.setColor(COLOR_SCROLL_BGND);
	g.fillRect(1, 1, scrollWidth-2, drawHeight-2);
	
        g.setColor(COLOR_BGND);
        g.drawRect(0,0,scrollWidth-1,drawHeight-1);
            
	drawHeight-=minimumHeight;
        
	scrollerSize=(drawHeight*windowSize)/size+minimumHeight;
	
	scrollerPos=(drawHeight*position)/size;
        g.setColor(COLOR_SCROLL_PTR);
        g.drawRect(0, scrollerPos, scrollWidth-1, scrollerSize);
	
	//scrollerPos+=g.getTranslateY();
    }
}
