/*
 * vCard.java
 *
 * Created on 24 Сентябрь 2005 г., 1:24
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package vcard;
import Client.StaticData;
import com.alsutton.jabber.JabberDataBlock;
import com.alsutton.jabber.datablocks.Iq;
import java.util.*;
import java.io.*;
import javax.microedition.lcdui.Image;
import util.StringLoader;
import util.strconv;

/**
 *
 * @author EvgS
 */
public class VCard {

    public final static int NICK_INDEX=1;
    
    public static Vector vCardFields;
    public static Vector vCardFields2;
    public static Vector vCardLabels;
    
    private Vector vCardData;
    private String jid;
    private String id;
    
    byte photo[];
    
    private boolean empty=true;
    
    /** Creates a new instance of vCard */
    public VCard() {
        if (vCardFields==null) fieldsLoader();
    }
    
    public VCard(JabberDataBlock data) {
        this();
        jid=data.getAttribute("from");
        id=data.getAttribute("id");
        int itemsCount=getCount();
        vCardData=new Vector(itemsCount);
        vCardData.setSize(itemsCount);
        
        if (data==null) return; 
        if (data.getTypeAttribute().equals("error")) return;
        JabberDataBlock vcard=data.findNamespace("vcard-temp");
        if (vcard==null) return; //"No vCard available" 
        
        for (int i=0; i<itemsCount; i++){
            try {
                String f1=(String)VCard.vCardFields.elementAt(i);
                String f2=(String)VCard.vCardFields2.elementAt(i);
                
                JabberDataBlock d2=
                        (f2==null) ? vcard : vcard.getChildBlock(f2);
                
                String field=d2.getChildBlockText(f1);
                
                if (field.length()>0) setVCardData(i, field);
            } catch (Exception e) {/**/}
        }
        
       try {
           JabberDataBlock photoXML=vcard.getChildBlock("PHOTO").getChildBlock("BINVAL");
           photo=(byte[])photoXML.getChildBlocks().lastElement();
       } catch (Exception e) {};
    }

    public JabberDataBlock constructVCard(){
        JabberDataBlock vcardIq=new Iq(null, Iq.TYPE_SET, "vcard-set");
        JabberDataBlock vcardTemp=vcardIq.addChild("vCard", null);
        vcardTemp.setNameSpace("vcard-temp");
        
        int itemsCount=getCount();
        
        for (int i=0; i<itemsCount; i++){
            String field=getVCardData(i);
            if (field==null) continue;
            
            String f1=(String)VCard.vCardFields.elementAt(i);
            String f2=(String)VCard.vCardFields2.elementAt(i);
            
            JabberDataBlock subLevel=vcardTemp;
            if (f2!=null) {
                subLevel=vcardTemp.getChildBlock(f2);
                if (subLevel==null) subLevel=vcardTemp.addChild(f2, null);
            }
            subLevel.addChild(f1, field);
            
        }
        if (photo!=null) {
            vcardTemp.addChild("PHOTO", null).addChild("BINVAL", strconv.toBase64(photo, -1));
        }
        //System.out.println(vcard.toString());
        return vcardIq;
    }
    
    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) {
        this.photo=photo;
    }
    
    public String getNickName() { return getVCardData(NICK_INDEX);}
    
    public static JabberDataBlock getQueryVCard(String to, String id ) 
    {
        JabberDataBlock req=new Iq(to, Iq.TYPE_GET, id);
        req.addChild("vCard", null).setNameSpace( "vcard-temp" );

        /*
        System.out.print("to=");
        System.out.print(to);
        System.out.print(" id=");
        System.out.println(id);
         */
        return req;
    }
    
    public static void request(String jid, String id) {
        StaticData.getInstance().roster.setQuerySign(true); 
        StaticData.getInstance().roster.theStream.send(getQueryVCard(jid, "getvc"+id));
    }
    
    private void fieldsLoader(){
	Vector table[]=new StringLoader().stringLoader("/vcard.txt", 3);

	vCardFields=table[1];
        vCardFields2=table[0];
        vCardLabels=table[2];
        
    }
    public String getVCardData(int index) {
        return (String) vCardData.elementAt(index);
    }

    public void setVCardData(int index, String data) {
        vCardData.setElementAt(data, index);
    }
    
    public int getCount(){ return vCardFields.size(); }

    public String getJid() { return jid; }

    public String getId() {
        return id;
    }

    public boolean isEmpty() {
        return empty;
    }


}
