/*
 * SmileTree.java
 *
 * Created on 6 ������� 2005 �., 19:38
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Messages;

import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.Font;

import ui.*;
import Client.Msg;
/**
 *
 * @author Eugene Stahov
 */
public final class MessageParser {
    
    private Vector smileTable;
    
    private Leaf root;

    // Singleton
    private static MessageParser instance=null;
    
    public static MessageParser getInstance() {
        if (instance==null) instance=new MessageParser("/images/smiles.txt");
        return instance;
    }
    /**
     * Smile table loader
     * @param resource - path to smiles-description text file
     * @param smileTable - (result) Vector of smile's string-representations
     */
    
    public Vector getSmileTable() { return smileTable; }
    
    private class Leaf {
        public int Smile=-1;   // ��� �������� � ����
        public String smileChars;     // ������� ���������
        public Vector child;

        public Leaf() {
            child=new Vector();
            smileChars=new String();
        }
        
        public Leaf findChild(char c){
            int index=smileChars.indexOf(c);
            return (index==-1)?null:(Leaf)child.elementAt(index);
        }

        private void addChild(char c, Leaf child){
            this.child.addElement(child);
            smileChars=smileChars+c;
        }
    }
    
    private MessageParser(String resource) {
        smileTable=new Vector();
        root=new Leaf();
        // opening file;
        try { // generic errors
            
            // ���� ������ ������, �� �� ����� ������
            int strnumber=0;
            // ��������
            // int level=0; 
            boolean strhaschars=false;
            boolean endline=false;
            
            Leaf p=root,   // ���� ������� ����� ������ �� ������
                    p1;
            
            InputStream in=this.getClass().getResourceAsStream(resource);
            //DataInputStream f=new DataInputStream(in);
            
            StringBuffer s=new StringBuffer(10);
            boolean firstSmile=true;
            
            //try { // eof
                int c;
                while (true) {
                    c=in.read();
                    //System.out.println(c);
                    if (c<0) break;
                    switch (c) {
                        case 0x0d:
                        case 0x0a:
                            if (strhaschars) endline=true; else break;
                        case 0x09:
                        //case 0x20:
                            // ����� ������ �������� - ������ ��� �����
                            p.Smile=strnumber;
                            
                            if (firstSmile) smileTable.addElement(s.toString());
                            s.setLength(0);
                            //s=new StringBuffer(6);
                            firstSmile=false;
                            
                            p=root;// � ������ ������
                            break;
                        default:
                            if (firstSmile) s.append((char)c);
                            strhaschars=true;
                            p1=p.findChild((char)c);
                            if (p1==null) {
                                p1=new Leaf();
                                p.addChild((char)c,p1);
                            }
                            p=p1;
                    }
                    if (endline) {
                        endline=strhaschars=false;
                        strnumber++;
                        firstSmile=true;
                    }
                }
            //} catch (Exception e) { /* ������������ ���� ������� */ }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Vector parseMsg(
            Msg msg, 
            ImageList il,       //!< ���� null, �� ������ ������������
            int width, 
            boolean singleLine, //!< ������� ������ ���� ������ �� ���������
            NotifyAddLine notify
            )
    {
        Vector v=new Vector();
        
        //boolean noWrapSpace=false;
        
        int state=0;
        if (msg.subject==null) state=1;
        while (state<2) {
            int w=0;
            StringBuffer s=new StringBuffer();
            ComplexString l=new ComplexString(il);
            Font f=l.getFont();
        
        
            if (singleLine) width-=f.charWidth('>');
            
            String txt=(state==0)? msg.subject: msg.toString();
            int color=(state==0)? 0xa00000:0x000000;
            l.setColor(color);
            
            int i=0;
            if (txt!=null)
            while (i<txt.length()) {
                Leaf p1,p=root;
                int smileIndex=-1;
                int smileStart=i;
                int smileEnd=i;
                while (i<txt.length()) {
                    char c=txt.charAt(i);

                    if (il==null) break;

                    p1=p.findChild(c);
                    if (p1==null) break;    //���� ������ c �� ����� � �����
                    p=p1;
                    if (p.Smile!=-1) {
                        // ����� �����
                        smileIndex=p.Smile;
                        smileEnd=i;
                    }
                    i++; // ���������� ����� ������
                }
                if (smileIndex!=-1) {
                    // ���� �������
                    // ������� ������
                    if (s.length()>0) l.addElement(s.toString());
                    // �������
                    s.setLength(0);
                    // ������� �������
                    int iw=il.getWidth();
                    if (w+iw>width) {
                        if (singleLine) {
                            // ������� ����� ������
                            l.addRAlign();
                            l.addElement(">");
                            return l;
                        }
                        v.addElement(l);    // ������� l � v
                        if (notify!=null) notify.notifyRepaint(v, msg);
                        l=new ComplexString(il);     // ����� ������
                        l.setColor(color);
                        w=0;
                    }
                    l.addImage(smileIndex); w+=iw;
                    // ���������� ���������
                    i=smileEnd;
                } else {
                    // ������ � ������-����������
                    i=smileStart;
                    char c=txt.charAt(i);
                    int cw=f.charWidth(c);
                    if (c!=0x20)
                    if (w+cw>width || c==0x0d || c==0x0a || c==0xa0) {
                        l.addElement(s.toString());    // ��������� ��������� � l
                        s.setLength(0); w=0;

                        if (c==0xa0) l.setColor(0x904090);

                        if (singleLine) {
                            // ������� ����� ������
                            l.addRAlign();
                            l.addElement(">");
                            return l;
                        }

                        v.addElement(l);    // ������� l � v
                        if (notify!=null) notify.notifyRepaint(v, msg);
                        l=new ComplexString(il);     // ����� ������
                        l.setColor(color);
                    }
                    if (c>0x1f) {  s.append(c); w+=cw; } 
                    else if (c==0x09) {  s.append((char)0x20); w+=cw; }
                }
                i++;
            }
            if (s.length()>0) l.addElement(s.toString());

            if (singleLine) {
                if (state==0){
                    l.addRAlign();
                    l.addElement(">");
                }
                return l;   // ������� ����� ������
            }

            if (!l.isEmpty()) v.addElement(l);  // ��������� ������

            if (notify!=null) {
                notify.notifyRepaint(v, msg);
                notify.notifyFinalized();
            }
            state++;
        }
        return v;

    }

    public interface NotifyAddLine {
        void notifyRepaint(Vector v, Msg parsedMsg);
        void notifyFinalized();
    }
}
