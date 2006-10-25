/*
 * vCardForm.java
 *
 * Created on 3 Jrnz,hm 2005 г., 0:37
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package vcard;
import Client.StaticData;
//#if (FILE_IO)
import io.file.FileIO;
import io.file.browse.Browser;
import io.file.browse.BrowserListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//#endif

//#if (!MIDP1)
import images.camera.*;
//#endif

import java.util.*;
import javax.microedition.lcdui.*;
import locale.SR;

/**
 *
 * @author EvgS
 */
public class vCardForm 
        implements CommandListener, Runnable
//#if (FILE_IO)
        , BrowserListener
//#endif

//#if (!MIDP1)
        , CameraImageListener
//#endif
{
    
    private Display display;
    private Displayable parentView;
    
    protected Command cmdCancel=new Command(SR.MS_CANCEL, Command.BACK, 99);
    protected Command cmdPublish=new Command(SR.MS_PUBLISH, Command.OK /*Command.SCREEN*/, 1);
    protected Command cmdRefresh=new Command(SR.MS_REFRESH, Command.SCREEN, 2);
    protected Command cmdPhoto=new Command("Load Photo", Command.SCREEN,3);
    protected Command cmdDelPhoto=new Command("Clear Photo", Command.SCREEN,4);
    protected Command cmdCamera=new Command("Camera", Command.SCREEN,5);
    
    private Form f;
    private Vector items=new Vector();
    private VCard vcard;
    
    private byte[] photo;
    private int photoIndex;
    
    /** Creates a new instance of vCardForm */
    public vCardForm(Display display, VCard vcard, boolean editable) {
        this.display=display;
        parentView=display.getCurrent();
        
        this.vcard=vcard;
        
        f=new Form(SR.MS_VCARD);
        f.append(vcard.getJid());
        
        for (int index=0; index<vcard.getCount(); index++) {
            String data=vcard.getVCardData(index);
            String name=(String)VCard.vCardLabels.elementAt(index);
            Item item=null;
            if (editable) {
                item=new TextField(name, data, 200, TextField.ANY);
                items.addElement(item);
            } else if (data!=null) {
                item=new StringItem (name, data);
            }
            if (item!=null) {
                f.append(item);
//#if !(MIDP1)
                f.append(new Spacer(256, 3));
//#else
//--                f.append("\n");
//#endif
            }
        }
        
        
        photoIndex=f.append("[no photo available]");
        
        f.append("\n\n[end of vCard]");
        
        photo=vcard.getPhoto();
        setPhoto();
        
        f.addCommand(cmdCancel);
        f.addCommand(cmdRefresh);
        if (editable) {
            f.addCommand(cmdPublish);
//#if (FILE_IO)
            f.addCommand(cmdPhoto);
//#endif
//#if !(MIDP1)
            f.addCommand(cmdCamera);
//#endif
            f.addCommand(cmdDelPhoto);
        }
        f.setCommandListener(this);
        display.setCurrent(f);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c==cmdCancel) destroyView();
        if (c==cmdRefresh) {
            VCard.request(vcard.getJid());
            destroyView();
        }
        
//#if (FILE_IO)
        if (c==cmdPhoto) {
            new Browser(display, this, false);
        }
//#endif

//#if (!MIDP1)
        if (c==cmdCamera)
            new CameraImage(display, this);
//#endif

    if (c==cmdDelPhoto) {photo=null; setPhoto();}
        
        if (c!=cmdPublish) return;
        
        vcard.setPhoto(photo);
        
        for (int index=0; index<vcard.getCount(); index++) {
            String field=((TextField)items.elementAt(index)).getString();
            if (field.length()==0) field=null;
            vcard.setVCardData(index, field);
        }
        //System.out.println(vcard.constructVCard().toString());
        new Thread(this).start();
        destroyView();
    }
    
    private void destroyView() {
        display.setCurrent(parentView);
    }

    public void run() {
        StaticData.getInstance().roster.theStream.send(vcard.constructVCard());
        System.out.println("VCard sent");
    }

//#if (FILE_IO)
    public void BrowserFilePathNotify(String pathSelected) {
        try {
            FileIO f=FileIO.createConnection(pathSelected);
            InputStream is=f.openInputStream();
            byte[] b=new byte[(int)f.fileSize()];
            is.read(b);
            is.close();
            f.close();
            photo=b;
            setPhoto();
        } catch (Exception e) {e.printStackTrace();}
    }
//#endif

//#if (!MIDP1)
    public void cameraImageNotify(byte[] capturedPhoto) {
        photo=capturedPhoto;
        setPhoto();
    }
//#endif

    private void setPhoto() {
        if (photo==null) return;
        String size=String.valueOf(photo.length)+" bytes";
        Item photoItem;
//#if !(MIDP1)
        try {
            Image photoImg=Image.createImage(photo, 0, photo.length);
            photoItem=new ImageItem(size, photoImg, 0, null);
        } catch (Exception e) { photoItem=new StringItem(size, "[Unsupported format]"); }
        f.set(photoIndex, photoItem);
//#endif
    }

}
