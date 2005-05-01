/*
 * SmileTree.java
 *
 * Created on 6 ������� 2005 �., 19:38
 */

package Messages;

import java.io.*;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import ui.*;
/**
 *
 * @author Eugene Stahov
 */
public final class MessageParser {
    
    public int Smile=-1;   // ��� �������� � ����
    private String chs;     // ������� ���������
    private Vector child;
    
    /** Creates a new instance of SmileTree */
    public MessageParser() {
        child=new Vector ();
        chs=new String();
    }
    /** Loading Smile table */
    public MessageParser(String resource, Vector smileTable) {
        this();
        // opening file;
        try { // generic errors
            
            // ���� ������ ������, �� �� ����� ������
            int strnumber=0;
            // ��������
            int level=0; 
            boolean strhaschars=false;
            boolean endline=false;
            
            MessageParser p=this,   // ���� ������� ����� ������ �� ������
                    p1;
            
            InputStream in=this.getClass().getResourceAsStream(resource);
            DataInputStream f=new DataInputStream(in);
            
            StringBuffer s=new StringBuffer(10);
            boolean firstSmile=true;
            
            try { // eof
                char c;
                while (true) {
                    c=(char)f.readUnsignedByte();
                    
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
                            
                            p=this;// � ������ ������
                            break;
                        default:
                            if (firstSmile) s.append(c);
                            strhaschars=true;
                            p1=p.findChild(c);
                            if (p1==null) {
                                p1=new MessageParser();
                                p.addChild(c,p1);
                            }
                            p=p1;
                    }
                    if (endline) {
                        endline=strhaschars=false;
                        strnumber++;
                        firstSmile=true;
                    }
                }
            } catch (EOFException e) { f.close(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addChild(char c, MessageParser child){
        this.child.addElement(child);
        chs=chs+c;
    }
    public MessageParser findChild(char c){
        int index=chs.indexOf(c);
        return (index==-1)?null:(MessageParser)child.elementAt(index);
    }

    public Vector parseMsg(
            String txt, 
            ImageList il,       //!< ���� null, �� ������ ������������
            int width, 
            boolean singleLine, //!< ������� ������ ���� ������ �� ���������
            NotifyAddLine notify
            )
    {
        Vector v=new Vector();
        ComplexString l=new ComplexString(il);
        Font f=l.getFont();
        
        StringBuffer s=new StringBuffer();
        int i=0;
        
        if (singleLine) width-=f.charWidth('>');
        
        int w=0;
        while (i<txt.length()) {
            MessageParser p1,p=this;
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
                    if (notify!=null) notify.notifyRepaint(v);
                    l=new ComplexString(il);     // ����� ������
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
                if (w+cw>width || c==0x0d || c==0x0a) {
                    l.addElement(s.toString());    // ��������� ��������� � l
                    s.setLength(0); w=0;

                    if (singleLine) {
                        // ������� ����� ������
                        l.addRAlign();
                        l.addElement(">");
                        return l;
                    }
                    
                    v.addElement(l);    // ������� l � v
                    if (notify!=null) notify.notifyRepaint(v);
                    l=new ComplexString(il);     // ����� ������
                }
                if (c>0x1f) {  s.append(c); w+=cw; }
            }
            i++;
        }
        if (s.length()>0) l.addElement(s.toString());

        if (singleLine) return l;   // ������� ����� ������
        
        if (!l.isEmpty()) v.addElement(l);  // ��������� ������

        if (notify!=null) {
            notify.notifyRepaint(v);
            notify.notifyFinalized();
        }
        return v;

    }

    public interface NotifyAddLine {
        void notifyRepaint(Vector v);
        void notifyFinalized();
    }
}
