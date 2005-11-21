/*
 * VirtualList.java
 *
 * Created on 30 ������ 2005 �., 14:46
 */

package ui;
import javax.microedition.lcdui.*;
import java.util.*;
import Client.*;
import ui.controls.ScrollBar;

/**
 * ������������ ������ ����������� ���������.
 * ����� ��������� ���������� �������, ���������,
 * ����� ��������� ������������ �� ������ ���������.
 * @author Eugene Stahov
 */
public abstract class VirtualList         
        extends Canvas 
{
    
    /**
     * ������� "������ ������� �������"
     * � ������ VirtualList ���������� ������� �� ��������� ��������, ����������
     * �������������� (override) ������� ��� ���������� ����������� ��������
     * @param index ������ ����������� ��������
     */
    public void focusedItem(int index) {}


    /**
     * ����� ��������� ������������ ������
     * ��� ������� �����������, ������ ���� �������������� ��� ������������
     * @return ����� ��������� ������, �������� ���������
     */
    abstract protected int getItemCount();

    /**
     * ������� ������������ ������
     * ��� ������� �����������, ������ ���� �������������� ��� ������������
     * @param index ����� �������� ������. �� ����������� ��������, ������������ getItemCount()
     * @return ������ �� ������� � ������� index.
     */
    abstract protected VirtualElement getItemRef(int index);
    
    /**
     * ���� ���� ���������
     * @return RGB-���� ���� ���������
     */
    protected int getTitleBGndRGB() {return VL_TITLE_BGND;} 

    /**
     * ���� ������ ���������
     * @return RGB-���� ������ ���������
     */
    protected int getTitleRGB() {return VL_TITLE;} 
    
    /**
     * ������� "������� ������ ��"
     * � ������ VirtualList ���������� ������� onSelect ���������� ��������, ����������
     * �������������� (override) ������� ��� ���������� ����������� ��������
     */
    public void eventOk(){
        if (atCursor!=null) ((VirtualElement)atCursor).onSelect();
    }
    /**
     * ���������� �������������� ������. ���������� � ������, ���� ��� ������ 
     * �� ��� ��������� �������� key(keyCode)
     * ���������� �������������� (override) ������� ��� ���������� ����������� ��������     
     * @param keyCode ��� �������
     */
    public void userKeyPressed(int keyCode){}
    //////////////////////////////////
    //public static final int VL_CURSOR_SHADE   =0x000000;
    public static final int VL_CURSOR_BODY    =0x00FF00;
//#if !(MIDP1)
    public static final int VL_CURSOR_OUTLINE =0x008800;
//#else
//--    public static final int VL_CURSOR_OUTLINE =VL_CURSOR_BODY;
//#endif
    public static final int VL_SCROLL_PTR     =0x0033ff;
    public static final int VL_SCROLL_BGND    =0x888888;
    public static final int VL_BGND           =0xFFFFFF;
    public static final int VL_SZ_SCROLL      =5;
    public static final int VL_TITLE_BGND     =0x0033ff;
    public static final int VL_TITLE          =0x33ffff;
    
    public static final int SIEMENS_GREEN=-11;
    public static final int NOKIA_GREEN=-10;
    public static final int MOTOROLA_GREEN=-10;
    
    public static int greenKeyCode=SIEMENS_GREEN;
    public static boolean fullscreen=false;
    
    int width;
    int height;
    
    Image offscreen;
    
    protected int cursor;
    protected boolean atEnd;
    protected boolean blockChangeFocus;
    
    protected VirtualElement atCursor;
    
    protected int win_top;    // ������ �������
    //int full_items; // ��������� ���������� � ����
    protected int offset;     // �������� �������
    
    protected ComplexString title;
    protected ImageList titleil;
    
    private boolean wrapping = true;

    /**
     * ��������� ������������� ������ � ������ (������� ������� ����� ����� ������)
     * �� ��������� ���������� true
     * @param wrap ������ ���������� true, ��������� ������� ������� ����� ����� ������
     */
    public void enableListWrapping(boolean wrap) { this.wrapping=wrap; }
    
    /**
     * ������ ��������� ������ �� ���� ������� ComplexString
     * @param size ����� ����� ������������ ComplexString
     * @param first ������ ���� ComplexString
     * @param second ������ ���� ComplexString
     * @return ��������� ������ ComplexString, ������������� � �������� ���������
     */
    public ComplexString createTitleItem(int size, Object first, Object second) {
        ComplexString title=new ComplexString(titleil);
        title.setSize(size);
        if (first!=null) title.setElementAt(first,0);
        if (second!=null) title.setElementAt(second,1);
        setTitleItem(title);
        return title;
    }
    
    /**
     * ������ �� ��������� ������
     * @return ������ ���� ComplexString
     */
    public ComplexString getTitleItem() {return title;}
    public void setTitleItem(ComplexString title) { this.title=title; }
    
    /**
     * ������������� �������-������ ����������� � ���������
     * @param il ������-������ �����������
     */
    public void setTitleImages(ImageList il) { this.titleil=il; }
    
    /**
     * ���������� ������ �� ������ � ������. 
     * � ������ VirtualList ���������� VirtualElement, �� ������� ��������� ������,
     * ������, �������� �������������� ������� ��� ������������
     * @return ������ �� ������ � ������.
     */
    public Object getFocusedObject() { return atCursor; }    

    protected Display display;
    protected Displayable parentView;

    ScrollBar scrollbar;
    /** Creates a new instance of VirtualList */
    public VirtualList() {
        width=getWidth();
        height=getHeight();
        // rotator
        rotator=new TimerTaskRotate(0);
//#if !(MIDP1)
        //addCommand(cmdSetFullScreen);
        setFullScreenMode(fullscreen);
//#endif
	
	scrollbar=new ScrollBar();
	scrollbar.setHasPointerEvents(hasPointerEvents());
    }

    /** Creates a new instance of VirtualList */
    public VirtualList(Display display) {
        this();
        
        attachDisplay(display);
    }
    
    /**
     * ����������� ����������� ������������� �������, ������������� � ���������
     * ������� � ����������� � ������� ������������ ������ (this) 
     * @param display �������� ������� ���������� ���������� {@link }
     */
    public void attachDisplay (Display display) {
        if (this.display!=null) return;
        this.display=display;
        parentView=display.getCurrent();
        display.setCurrent(this);
        redraw();
    }


    /** ������ ���������� ��������� ��������� Canvas */
    public void redraw(){
        //repaint(0,0,width,height);
        Displayable d=display.getCurrent();
        //System.out.println(d.toString());
        if (d instanceof Canvas) {
            ((Canvas)d).repaint();
        }
    }

    /** ���������� ����� ������� VirtualList. �������������� ����������� ����� 
     * Canvas.hideNotify(). �������� �� ��������� - ������������ ��������� 
     * ������ offscreen, ������������� ��� ������ ��� �������������� ������� �����������
     */
    protected void hideNotify() {
	offscreen=null;
    }
    
    /** ���������� ����� ������� ��������� VirtualList. �������������� ����������� ����� 
     * Canvas.showNotify(). �������� �� ��������� - �������� ��������� 
     * ������ offscreen, ������������� ��� ������ ��� �������������� ������� �����������
     */
    protected void showNotify() {
	if (!isDoubleBuffered()) 
	    offscreen=Image.createImage(width, height);
    }
    
    /** ���������� ��� ��������� ������� ������������ �������. �������������� ����������� ����� 
     * Canvas.sizeChanged(int width, int heigth). ��������� ����� ������� ������� ���������.
     * ����� ������ ����� �������� ����� offscreen, ������������ ��� ������ ��� �������������� 
     * ������� �����������
     */
//#if !(MIDP1)
    protected void sizeChanged(int w, int h) {
        width=w;
        height=h;
	if (!isDoubleBuffered()) 
	    offscreen=Image.createImage(width, height);
    }
//#endif
    
    /**
     * ������ ��������� ������.
     * ������� ���������� ����� ���������� ������, 
     * ����� ������ ����������� � ��������� ������.
     *
     * � ������ VirtualList ������� �� ��������� ������� ��������, ����������
     * �������������� (override) ������� ��� ���������� ����������� ��������
     */
    protected void beginPaint(){};
    
    /**
     * ���������
     */
    public void paint(Graphics graphics) {
	Graphics g=(offscreen==null)? graphics: offscreen.getGraphics();
        // ��������� ����
        
        beginPaint();
        
        int list_top=0; // ������� ������� ������
        if (title!=null) {
            list_top=title.getVHeight();
            g.setClip(0,0, width, list_top);
            g.setColor(getTitleBGndRGB());
            g.fillRect(0,0, width, list_top);
            g.setColor(getTitleRGB());
            title.drawItem(g,0,false);
        }


        int yp=list_top;
        
        int count=getItemCount(); // ������ ������
        
        boolean scroll=(visibleItemsCnt(0,1)<count) ;

        if (count==0) {
            cursor=(cursor==-1)?-1:0; 
            win_top=0;
        }

        int itemMaxWidth=(scroll) ?(width-scrollbar.getScrollWidth()) : (width);
        // �������� ����
        // ���������
        int i=win_top;
        int fullyDrawedItems=0;
        
        VirtualElement atCursor=null;
        
        atEnd=false;
        try {
            // try ������ �������� �� ����� ������
            while (yp<height) {
                
                //if (atEnd=(i>=count)) break;    // ������ ����� ��������
                VirtualElement el=getItemRef(i);
                
                boolean sel=(i==cursor);
                
                int lh=el.getVHeight();
                
                setAbsOrg(g, 0, yp);
                
                g.setClip(0,0, itemMaxWidth, lh);
                g.setColor(el.getColorBGnd());
                g.fillRect(0,0, itemMaxWidth, lh);
                if (sel) {
                    drawCursor(g, itemMaxWidth, lh);
                    atCursor=el;
                }
                g.setColor(el.getColor());
                el.drawItem(g, (sel)?offset:0, sel);
                
                i++;
                if ((yp+=lh)<=height) fullyDrawedItems++;   // ����� ������� ��������� � ����
            }
        } catch (Exception e) { atEnd=true; }

        this.atCursor=atCursor;
        // ������� ������� ����
        int clrH=height-yp+1;
        if (clrH>0) {
            setAbsOrg(g, 0,yp);
            g.setClip(0, 0, itemMaxWidth, clrH);
            g.setColor(VL_BGND);
            //g.setColor(VL_CURSOR_OUTLINE);
            g.fillRect(0, 0, itemMaxWidth, clrH);
        }

        // ��������� ����������
        //g.setColor(VL_BGND);
        if (scroll) {
	    
            setAbsOrg(g, 0, list_top);
            g.setClip(0, 0, width, height-list_top);

	    scrollbar.setPostion(win_top);
	    scrollbar.setSize(count);
	    scrollbar.setWindowSize(fullyDrawedItems);
	    
	    scrollbar.draw(g);
        }

        setAbsOrg(g, 0, 0);

	if (offscreen!=null) graphics.drawImage(offscreen, 0,0, Graphics.TOP | Graphics.LEFT );
	//full_items=fe;
    }
    
    
    /**
     * ������� ��������� (0.0) � ���������� ���������� (x,y)
     * @param g ����������� �������� ���������
     * @param x ���������� x-���������� ������ ������ ��������� 
     * @param y ���������� y-���������� ������ ������ ���������
     */
    private void setAbsOrg(Graphics g, int x, int y){
        g.translate(x-g.getTranslateX(), y-g.getTranslateY());
    }
    
    int visibleItemsCnt(int from, int direction){
        int count=getItemCount();
        if (count==0) return 0;
        if (from>=count) return 0;
        int wsize=height;
        int itemcnt=0;
        if (title!=null) wsize-=title.getVHeight();
        try { //TODO: ������ �������, ����������������
            while (wsize>0) {
                //wsize-=getItemHeight(from);
                wsize-=getItemRef(from).getVHeight();    
                if (wsize>=0) itemcnt++;
                from+=direction;
                if (from<0) break; // ����� �����
                if (from>=count) break;
            }
        } catch (Exception e) {e.printStackTrace();}
        return itemcnt;
    }
    
    
    /**
     * ����������� ������� �� ��������.
     * @param offset ������������� ��� ������������� �������� �������
     */
    synchronized private void moveCursor(int offset, boolean enableRotation){
        int count=getItemCount();
 
        if (cursor>=0) {
            cursor+=offset;

            // �������� ������ ������
            if (cursor<0) cursor=0;
            if (cursor>=count) cursor=(count==0)?0:count-1;
            // ����� �� ���� ? ������� ����
            if (cursor<win_top) win_top+=offset; else {
                if (cursor>=win_top+visibleItemsCnt(win_top,1))  win_top+=offset;
                // ������ ������� ������, ���� �� ��� ������
                int vt=win_top+visibleItemsCnt(win_top,1);
                if (vt>0 && cursor>=vt) cursor=vt-1;
            }
            focusedItem(cursor);
            
        } else win_top+=offset; // ��� �������
        
        // �������� ��������� ����
        if (win_top<0) win_top=0;
        int up_bound=count-visibleItemsCnt(count-1,-1);
        if (win_top>up_bound) win_top=up_bound; 

        if (enableRotation) if (getItemCount()>0) setRotator();
        
    }

    /**
     * ����������� ������� � ������ ������
     */
    public void moveCursorHome(){
        blockChangeFocus=true;
        win_top=0;
        if (cursor>0) {
            cursor=0;
            focusedItem(0);
        }
        setRotator();
    }

    /**
     * ����������� ������� � ����� ������
     */
    public void moveCursorEnd(){
        blockChangeFocus=true;
        int count=getItemCount();
        win_top=count-visibleItemsCnt(count-1, -1);
        if (cursor>=0) {
            cursor=(count==0)?0:count-1;
            focusedItem(cursor);
        }
        setRotator();
    }

    /**
     * ����������� ������� � ��������������� �������
     * @param index ������� ������� � ������
     */
    public void moveCursorTo(int index, boolean force){
        int count=getItemCount();
        if (index>=count) index=count-1;    // ���� �� ��������� ���������, �� ����������� �� ����
        else if ((!force) && blockChangeFocus) return;
        
        //int ih=getItemHeight(0);
        //int h=height;
        //if (title!=null) { h-=title.getHeight(); }
        //if (ih==0) ih=10;
        //full_items=h/ih;
        moveCursor(index-cursor, force); 
    }

    /*public void moveCursorTo(Object focused){
        int count=getItemCount();
        for (int index=0;index<count;index++){
            if (focused==getItemRef(index)) {
                moveCursorTo(index);
                break;
            }
        }
    }
     */
    /** ��� ������������ ������ */
    protected int kHold;
    public void keyRepeated(int keyCode){ key(keyCode); }
    public void keyReleased(int keyCode) { kHold=0; }
    public void keyPressed(int keyCode) { kHold=0; key(keyCode);  }
    
    /**
     * ��������� ����� ������
     * @param keyCode ��� ������� ������
     */
    private void key(int keyCode) {
        switch (keyCode) {
            case KEY_NUM1:  { moveCursorHome();    break; }
            case KEY_NUM7:  { moveCursorEnd();     break; }
            default:
                switch (getGameAction(keyCode)){
                    case UP:    { keyUp(); break; }
                    case DOWN:  { keyDwn(); break; }
                    case LEFT:  { keyLeft(); break; }
                    case RIGHT: { keyRight(); break; }
                    case FIRE:  { eventOk(); break; }
                    default: 
                        if (keyCode==greenKeyCode) { keyGreen(); break; }
                        userKeyPressed(keyCode);
                }
        }

        
        repaint();
    }
    
    /**
     * ������� "������� ������ UP"
     * � ������ VirtualList ������� ���������� ������ �� ���� ������� �����.
     * �������� �������������� (override) ������� ��� ���������� ����������� ��������
     */
    protected void keyUp() {
	if (wrapping) if (cursor==0) { moveCursorEnd(); return; }
        blockChangeFocus=true; 
        moveCursor(-1, true);  
    }
    
    /**
     * ������� "������� ������ DOWN"
     * � ������ VirtualList ������� ���������� ������ �� ���� ������� �����.
     * �������� �������������� (override) ������� ��� ���������� ����������� ��������
     */
    
    protected void keyDwn() { 
	if (wrapping) if (cursor==getItemCount()-1) { moveCursorHome(); return; }
        blockChangeFocus=true; 
        moveCursor(+1, true); 
    }
    
    /**
     * ������� "������� ������ LEFT"
     * � ������ VirtualList ������� ���������� ������ �� ���� �������� �����.
     * �������� �������������� (override) ������� ��� ���������� ����������� ��������
     */
    protected void keyLeft() {
	//if (cursor==0) { moveCursorEnd(); return; }
        blockChangeFocus=true; 
        int mov_org=(cursor!=-1)? cursor : win_top;
        moveCursor(-visibleItemsCnt(mov_org,-1), true); 
    }

    /**
     * ������� "������� ������ RIGHT"
     * � ������ VirtualList ������� ���������� ������ �� ���� �������� ����.
     * �������� �������������� (override) ������� ��� ���������� ����������� ��������
     */
    protected void keyRight() { 
	//if (cursor==getItemCount()-1) { moveCursorHome(); return; }
        blockChangeFocus=true; 
        moveCursor(visibleItemsCnt(win_top,1), true); 
    }
    
    /**
     * ������� "������� ��˨��� ������"
     * � ������ VirtualList ������� ��������� ����� eventOk().
     * �������� �������������� (override) ������� ��� ���������� ����������� ��������
     */
    protected void keyGreen() { eventOk(); }
    
    /** ���������� ������� ��������� ������� ����� */
    private void setRotator(){
        rotator.destroyTask();
        if (getItemCount()<1) return;
        if (cursor>=0) {
            int itemWidth=getItemRef(cursor).getVWidth();
            if (itemWidth>=width-VL_SZ_SCROLL) itemWidth-=width/2; else itemWidth=0;
            rotator=new TimerTaskRotate( itemWidth );
        }
    }
    // cursor rotator
    
    private class TimerTaskRotate extends TimerTask{
        private Timer t;
        private int Max;
        private int hold;
        public TimerTaskRotate(int max){
            offset=0;
            //if (max<1) return;
            Max=max;
            t=new Timer();
            t.schedule(this, 2000, 300);
        }
        public void run() {
            // ��������� ������ ���
            blockChangeFocus=false;
            if (hold==0) {
                if (offset>=Max) hold=6;  
                else offset+=20;
            }
            else { 
            	offset=0;
            	cancel();
            }
            
            redraw();
            //System.out.println("Offset "+offset);
        }
        public void destroyTask(){
            offset=0;
            if (t!=null){
                this.cancel();
                t.cancel();
                t=null;
            }
        }
    }
    private TimerTaskRotate rotator;

    
    /**
     * ��������� �������������� �������
     * @param g ����������� �������� ���������
     * @param width ������ �������
     * @param height ������ �������
     */
    protected void drawCursor (Graphics g, int width, int height){
        //g.setColor(VL_CURSOR_SHADE);   g.drawRoundRect(x+2, y+2, width-1, height-1, 3,3);
        g.setColor(VL_CURSOR_BODY);    g.fillRect(1, 1, width-1, height-1);
        g.setColor(VL_CURSOR_OUTLINE); g.drawRect(0, 0, width-1, height-1);
        /*
        g.drawLine(1,0,width-2,0);
        g.drawLine(0,1,0,height-2);
        g.drawLine(0,width-1,0,height-2);
        g.drawLine(1,height-1,width-2,height-1);
         */
    }

    public void setParentView(Displayable parentView){
        this.parentView=parentView;
    }
    
    /**
     * ������������ �� ��������� ������� �������� ������������ ������, 
     * ������������� � ��������� ����������� Displayable
     */
    public void destroyView(){
        if (display!=null)   display.setCurrent(parentView);
    }

}
