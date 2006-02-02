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
public final class MessageParser implements Runnable{
    
    private final static int URL=-2;
    private final static int NOSMILE=-1;
    private Vector smileTable;
    
    private Leaf root;

    // Singleton
    private static MessageParser instance=null;
    
    private int width; // window width
    private ImageList il;
    
    private class ParseTask {
        Msg msg; // source data
        MessageParserNotify callback; // callback interfaces
        Vector result;  // resulting data
    }
    private Vector tasks=new Vector();
    
    private Thread thread;
    
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
        public int Smile=NOSMILE;   // ��� �������� � ����
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
    
    private void addSmile(String smile, int index) {
	Leaf p=root;   // ���� ������� ����� ������ �� ������
	Leaf p1;
	
	int len=smile.length();
	for (int i=0; i<len; i++) {
	    char c=smile.charAt(i);
	    p1=p.findChild(c);
	    if (p1==null) {
		p1=new Leaf();
		p.addChild((char)c,p1);
	    }
	    p=p1;
	}
	p.Smile=index;
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
                            // ����� ������ ��������
                            
			    String smile=s.toString();
                            if (firstSmile) smileTable.addElement(smile);
			    
			    addSmile(smile,strnumber);
			    
                            s.setLength(0);
                            //s=new StringBuffer(6);
                            firstSmile=false;
                            
                            break;
                        default:
                            s.append((char)c);
                            strhaschars=true;
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
	addSmile("http://",URL);
    }

    public Vector parseMsg(
            Msg msg, 
            ImageList il,       //!< ���� null, �� ������ ������������
            int width, 
            MessageParserNotify notify
            )
    {
        ParseTask task=new ParseTask();
        task.msg=msg;
        task.callback=notify;
        task.result=new Vector();
        this.il=il;
        this.width=width;
        
        synchronized (tasks) {
            tasks.addElement(task);
            if (thread==null) {
                thread=new Thread(this);
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.start();
            }
        }
        return task.result;
    }
    
    public void run() {
        while(true) {
            
            ParseTask task=null;
            synchronized (tasks) {
                if (tasks.size()==0) {
                    thread=null;
                    return;
                }
                task=(ParseTask) tasks.lastElement();
                tasks.removeElement(task);
            }
            
            Vector v=task.result;
            
            StringBuffer url = null;
            
            //boolean noWrapSpace=false;
            boolean inUrl=false;
            
            int state=0;
            if (task.msg.subject==null) state=1;
            while (state<2) {
                int w=0;
                StringBuffer s=new StringBuffer();
                ComplexString l=new ComplexString(il);
                Font f=l.getFont();
                
                String txt=(state==0)? task.msg.subject: task.msg.toString();
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
                        
                        if (inUrl) {
                            switch (c) {
                                case ' ':
                                case 0x09:
                                case 0x0d:
                                case 0x0a:
                                case 0xa0:
                                case ')':
                                    inUrl=false;
                                    task.callback.notifyUrl(url.toString());
                                    url=null;
                                    if (s.length()>0) {
                                        l.addUnderline();
                                        l.addElement(s.toString());
                                    }
                                    s.setLength(0);
                            }
                            break; // �� �����
                        }
                        
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
                    if (smileIndex==URL) {
                        if (s.length()>0) l.addElement(s.toString());
                        s.setLength(0);
                        inUrl=true;
                        url=new StringBuffer();
                        //l.addUnderline();
                    }
                    if (smileIndex>=0) {
                        // ���� �������
                        // ������� ������
                        if (s.length()>0) {
                            if (inUrl) l.addUnderline();
                            l.addElement(s.toString());
                        }
                        // �������
                        s.setLength(0);
                        // ������� �������
                        int iw=il.getWidth();
                        if (w+iw>width) {
                            v.addElement(l);    // ������� l � v
                            task.callback.notifyRepaint(v, task.msg, false);
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
                        
                        if (inUrl) url.append(c);
                        
                        int cw=f.charWidth(c);
                        if (c!=0x20)
                            if (w+cw>width || c==0x0d || c==0x0a || c==0xa0) {
                            if (inUrl) l.addUnderline();
                            l.addElement(s.toString());    // ��������� ��������� � l
                            s.setLength(0); w=0;
                            
                            if (c==0xa0) l.setColor(0x904090);
                            
                            v.addElement(l);    // ������� l � v
                            task.callback.notifyRepaint(v, task.msg, false);
                            l=new ComplexString(il);     // ����� ������
                            l.setColor(color);
                            }
                        if (c>0x1f) {  s.append(c); w+=cw; } else if (c==0x09) {  s.append((char)0x20); w+=cw; }
                    }
                    i++;
                    }
                if (s.length()>0) {
                    if (inUrl) {
                        l.addUnderline();
                        task.callback.notifyUrl(url.toString());
                    }
                    l.addElement(s.toString());
                }
                
                if (!l.isEmpty()) v.addElement(l);  // ��������� ������
                
                task.callback.notifyRepaint(v, task.msg, true);
                state++;
            }
        }
    }

    public interface MessageParserNotify {
        void notifyRepaint(Vector v, Msg parsedMsg, boolean finalized);
	void notifyUrl(String url);
    }
}
