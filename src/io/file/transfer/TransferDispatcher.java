/*
 * TransferDispatcher.java
 *
 * Created on 28 Октябрь 2006 г., 19:44
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package io.file.transfer;

import Client.StaticData;
import com.alsutton.jabber.JabberBlockListener;
import com.alsutton.jabber.JabberDataBlock;
import com.alsutton.jabber.datablocks.Iq;
import com.alsutton.jabber.datablocks.Message;
import java.util.Enumeration;
import java.util.Vector;
import util.strconv;

/**
 *
 * @author Evg_S
 */
public class TransferDispatcher implements JabberBlockListener{

    /** Singleton */
    private static TransferDispatcher instance;
    
    public static TransferDispatcher getInstance() {
        if (instance==null) instance=new TransferDispatcher();
        return instance;
    }
   
    
    private Vector taskList;
    public Vector getTaskList() { return taskList;  }
    
    /** Creates a new instance of TransferDispatcher */
    private TransferDispatcher() {
        taskList=new Vector();
    }

    public int blockArrived(JabberDataBlock data) {
        if (data instanceof Iq) {
            String id=data.getAttribute("id");
            
            JabberDataBlock si=data.getChildBlock("si");
            if (si!=null) {
                // stream initiating
                String sid=si.getAttribute("id");
                
                JabberDataBlock file=si.getChildBlock("file");
                JabberDataBlock feature=si.getChildBlock("feature");
                
                String type=data.getTypeAttribute();
                if (type.equals("set")) {
                    // sender initiates file sending process
                    TransferTask task=new TransferTask(
                            data.getAttribute("from"),
                            id,   sid,
                            file.getAttribute("name"),
                            file.getChildBlockText("desc"),
                            Integer.parseInt(file.getAttribute("size")),
                            null);
                    
                    synchronized (taskList){ taskList.addElement(task); }
                    
                    eventNotify();
                    StaticData.getInstance().roster.playNotify(0);
                    return BLOCK_PROCESSED;
                }
                if (type.equals("result")) {
                    // our file were accepted
                    TransferTask task=getTransferBySid(id);
                    task.initIBB();
                    
                    eventNotify();
                    return BLOCK_PROCESSED;
                }
            }
            JabberDataBlock open=data.getChildBlock("open");
            if (open!=null) {
                String sid=open.getAttribute("sid");
                TransferTask task=getTransferBySid(sid);
                
                JabberDataBlock accept=new Iq(task.jid, Iq.TYPE_RESULT, id);
                send(accept, true);
                eventNotify();
                return BLOCK_PROCESSED;
            }
            JabberDataBlock close=data.getChildBlock("close");
            if (close!=null) {
                String sid=close.getAttribute("sid");
                TransferTask task=getTransferBySid(sid);
                
                JabberDataBlock done=new Iq(task.jid, Iq.TYPE_RESULT, id);
                send(done, true);
                task.closeFile();
                eventNotify();
                return BLOCK_PROCESSED;
            }
            if (data.getTypeAttribute().equals("result")) {
                TransferTask task=getTransferBySid(id);
                if (task!=null) {
                    task.startTransfer();
                }
            }
            if (data.getTypeAttribute().equals("error")) {
                TransferTask task=getTransferBySid(id);
                if (task!=null) {
                    task.cancel();
                }
            }
        }
        if (data instanceof Message) {
            JabberDataBlock bdata=data.getChildBlock("data");
            if (bdata==null) return BLOCK_REJECTED;
            if (!bdata.isJabberNameSpace("http://jabber.org/protocol/ibb")) return BLOCK_REJECTED;
            String sid=bdata.getAttribute("sid");
            TransferTask task=getTransferBySid(sid);
            
            byte b[]=strconv.fromBase64(bdata.getText());
            System.out.println("data chunk received");
            repaintNotify();
            task.writeFile(b);
            
        }
        return BLOCK_REJECTED;
    }
    
    // send shortcut
    void send(JabberDataBlock data, boolean async) {
        //StaticData.getInstance().roster.theStream.send(data);
        try {
            StringBuffer sb=new StringBuffer();
            data.constructXML(sb);
            StaticData.getInstance().roster.theStream.sendBuf( sb );
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TransferTask getTransferBySid(String sid) {
        synchronized (taskList) {
            for (Enumeration e=taskList.elements(); e.hasMoreElements(); ){
                TransferTask task=(TransferTask)e.nextElement();
                if (task.sid.equals(sid)) return task;
            }
        }
        return null;
    }

    void eventNotify() {
        int event=-1;
        synchronized (taskList) {
            for (Enumeration e=taskList.elements(); e.hasMoreElements(); ) {
                TransferTask t=(TransferTask) e.nextElement();
                if (t.showEvent) event=t.getImageIndex();
            }
        }
        Integer icon=(event<0)? null:new Integer(event);
        StaticData.getInstance().roster.setEventIcon(icon);
    }

    void repaintNotify() {
        StaticData.getInstance().roster.redraw();
    }

    void sendFile(TransferTask task) {
        synchronized (taskList){ taskList.addElement(task); }
        task.sendInit();
    }
}
