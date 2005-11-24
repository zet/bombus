/*
 * ImageListC.java
 *
 * Created on 31 ������ 2005 �., 0:06
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

/**
 *
 * @author Eugene Stahov
 */

package ui;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

public class ImageList {
    
    public final static int ICON_INVISIBLE_INDEX=0x51;
    public final static int ICON_ERROR_INDEX=0x52;
    public final static int ICON_TRASHCAN_INDEX=0x53;
    public final static int ICON_PROGRESS_INDEX=0x54;
    
    public final static int ICON_MODERATOR_INDEX=0x60;
    
    public final static int ICON_PRIVACY_ACTIVE=0x66;
    public final static int ICON_PRIVACY_PASSIVE=0x67;
    
    public final static int ICON_GROUPCHAT_INDEX=0x70;
    public final static int ICON_GCJOIN_INDEX=0x71;
    public final static int ICON_SEARCH_INDEX=0x72;
    public final static int ICON_REGISTER_INDEX=0x73;

    public final static int ICON_MESSAGE_INDEX=0x80;
    public final static int ICON_AUTHRQ_INDEX=0x81;
    public final static int ICON_COMPOSING_INDEX=0x82;
    public final static int ICON_EXPANDED_INDEX=0x83;
    public final static int ICON_COLLAPSED_INDEX=0x84;
    public final static int ICON_MESSAGE_BUTTONS=0x85;
    public final static int ICON_PROFILE_INDEX=0x90;

    public final static int ICON_PRIVACY_ALLOW=0x95;
    public final static int ICON_PRIVACY_BLOCK=0x96;
    
    public final static int ICON_KEYBLOCK_INDEX=0x97;

    //public final static int ICON_ASK_INDEX=0x06;
    
    
    Image resImage;
    int height,width;
    //int count,total;
    /** Creates a new instance of ImageListC */
    public ImageList(String resource, int sizeX, int sizeY) {
        loadMulty(resource, sizeX, sizeY);
    }
    
    private void loadMulty
            (
            String resName, //!< Name of image in resouce
            int sizeX, int sizeY 
            ) 
    {
        try {
            resImage = Image.createImage(resName);
        } catch (Exception e) {
        }
        height = sizeY; //resImage.getHeight()/countY;
        width = sizeX;  //resImage.getWidth()/countX;
        //this.count=resImage.getWidth()/width;
        //total=count*(resImage.getHeight()/height);
    }
    public void drawImage(Graphics g, int index, int x, int y){
        int ho=g.getClipHeight();
        int wo=g.getClipWidth();
        int xo=g.getClipX();
        int yo=g.getClipY();
        
        int iy=y-height*(int)(index>>4);
        int ix=x-width*(index&0x0f);
        g.setClip(x,y, width,height);
        g.drawImage(resImage,ix,iy,Graphics.TOP|Graphics.LEFT);
        g.setClip(xo,yo, wo, ho);
    };
    
    public int getHeight() {return height;}
    public int getWidth() {return width;}
    //public int getCount() {return total;}
}
