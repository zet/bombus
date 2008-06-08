/*
  Copyright (c) 2000,2001 Al Sutton (al@alsutton.com)
  All rights reserved.
  Redistribution and use in source and binary forms, with or without modification, are permitted
  provided that the following conditions are met:
 
  1. Redistributions of source code must retain the above copyright notice, this list of conditions
  and the following disclaimer.
 
  2. Redistributions in binary form must reproduce the above copyright notice, this list of
  conditions and the following disclaimer in the documentation and/or other materials provided with
  the distribution.
 
  Neither the name of Al Sutton nor the names of its contributors may be used to endorse or promote
  products derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.alsutton.jabber;
import Client.Account;
import Client.Config;
import Client.StaticData;
import io.Utf8IOStream;
import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import com.alsutton.jabber.datablocks.*;
//import com.alsutton.xmlparser.*;
import xml.*;
import locale.SR;
import util.StringLoader;
import xmpp.XmppError;



/**
 * The stream to a jabber server.
 */

public class JabberStream implements XMLEventListener, Runnable {
    
    private Utf8IOStream iostream;
    
    /**
     * The dispatcher thread.
     */
    
    private JabberDataBlockDispatcher dispatcher;
    
    //private Vector sendQueue;
    
    private boolean rosterNotify;
    
    private boolean xmppV1;
    
    private String server; // for ping

    public boolean pingSent;
    
    public boolean loggedIn;
    
    public void enableRosterNotify(boolean en){ rosterNotify=en; }
    
    /**
     * Constructor. Connects to the server and sends the jabber welcome message.
     *
     */
    
    public JabberStream( String server, String hostAddr, boolean xmppV1, String proxy)
    throws IOException {
        this.server=server;
        this.xmppV1=xmppV1;
        
        boolean waiting=Config.getInstance().istreamWaiting;
        
        StreamConnection connection;
        if (proxy==null) {
            connection = (StreamConnection) Connector.open(hostAddr);
        } else {
//#if HTTPCONNECT
//#             connection = io.HttpProxyConnection.open(hostAddr, proxy);
//#elif HTTPPOLL  
//#             connection = new io.HttpPollingConnection(hostAddr, proxy);
//#else            
            throw new IllegalArgumentException ("no proxy supported");
//#endif            
        }

        iostream=new Utf8IOStream(connection);
        //iostream.setStreamWaiting(waiting);

        
        dispatcher = new JabberDataBlockDispatcher(this);
     
        new Thread( this ). start();
        
        //initiateStream();
    }

    public void initiateStream() throws IOException {
        
        //sendQueue=new Vector();
        
        StringBuffer header=new StringBuffer("<stream:stream to='" )
            .append( server )
            .append( "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams'" );
        
        if (xmppV1) header.append(" version='1.0'");
        
        if (SR.MS_XMLLANG!=null) {
            header.append(" xml:lang='").append(SR.MS_XMLLANG).append("'");
        }
        header.append( '>' );
        send(header.toString());
    }

    
    public void startKeepAliveTask(){
        Account account=StaticData.getInstance().account;
        if (account.keepAliveType==0) return;
        keepAlive=new TimerTaskKeepAlive(account.keepAlivePeriod, account.keepAliveType);
    }
    
    /**
     * The threads run method. Handles the parsing of incomming data in its
     * own thread.
     */
    public void run() {
        try {
            XMLParser parser = new XMLParser( this );
            
            byte cbuf[]=new byte[512];
            
            while (true) {
                int length=iostream.read(cbuf);
                
                if (length==0) {
                    try { Thread.sleep(100); } catch (Exception e) {};
                    continue;
                }

                parser.parse(cbuf, length);
            }
            
            //dispatcher.broadcastTerminatedConnection( null );
        } catch( Exception e ) {
            System.out.println("Exception in parser:");
            e.printStackTrace();
            dispatcher.broadcastTerminatedConnection(e);
        };
    }
    
    /**
     * Method to close the connection to the server and tell the listener
     * that the connection has been terminated.
     */
    
    public void close() {
        if (keepAlive!=null) keepAlive.destroyTask();
        
        dispatcher.setJabberListener( null );
        try {
            //TODO: see FS#528
            try {  Thread.sleep(500); } catch (Exception e) {};
            send( "</stream:stream>" );
            int time=10;
            while (dispatcher.isActive()) {
                try {  Thread.sleep(500); } catch (Exception e) {};
                if ((--time)<0) break;
            }
            //connection.close();
        } catch( IOException e ) {
            // Ignore an IO Exceptions because they mean that the stream is
            // unavailable, which is irrelevant.
        } finally {
            dispatcher.halt();
	    iostream.close();
        }
    }
    
    /**
     * Method of sending data to the server.
     *
     * @param data The data to send.
     */
    
 /* public void send( byte[] data ) throws IOException
  {
    outStream.write( data );
    outStream.flush();
  }
  */
    
    /**
     * Method of sending data to the server.
     *
     * @param The data to send to the server.
     */
    public void sendKeepAlive(int type) throws IOException {
        switch (type){
            case 3:
                if (pingSent) {
                    dispatcher.broadcastTerminatedConnection(new Exception("Ping Timeout"));
                } else {
                    //System.out.println("Ping myself");
                    ping();
                }
                break;
            case 2:
                send("<iq/>");
                break;
            case 1:
                send(" ");
        }
    }
    
    public void send( String data ) throws IOException {
	iostream.send(new StringBuffer(data));
        //System.out.println(data);
    }
    
    public void sendBuf( StringBuffer data ) throws IOException {
	iostream.send(data);
        //System.out.println(data);
    }
    
    /**
     * Method of sending a Jabber datablock to the server.
     *
     * @param block The data block to send to the server.
     */
    
    public void send( JabberDataBlock block )  { new SendJabberDataBlock(block); }
    
    /**
     * Set the listener to this stream.
     */
    
    public void addBlockListener(JabberBlockListener listener) { 
        dispatcher.addBlockListener(listener);
    }
    public void cancelBlockListener(JabberBlockListener listener) { 
        dispatcher.cancelBlockListener(listener);
    }
    
    public void cancelBlockListenerByClass(Class removeClass) {
        dispatcher.cancelBlockListenerByClass(removeClass);
    }
    
    public void setJabberListener( JabberListener listener ) {
        dispatcher.setJabberListener( listener );
    }
    
    /**
     * The current class being constructed.
     */
    
    private JabberDataBlock currentBlock;
    
    /**
     * Method called when an XML tag is started in the stream comming from the
     * server.
     *
     * @param name Tag name.
     * @param attributes The tags attributes.
     */
    
    public boolean tagStart( String name, Vector attributes ) {
        if (currentBlock!=null){
            
            currentBlock = new JabberDataBlock( name, currentBlock, attributes );
            // TODO: remove stub
            // M55 STUB
//#if !(MIDP1)
            // photo reading
            if ( name.equals("BINVAL") ){
                return true;
            }
//#endif
            
            if (rosterNotify) if (name.equals("item")) dispatcher.rosterNotify();
            
        } else if ( name.equals( "stream:stream" ) ) {
            JabberDataBlock stream=new JabberDataBlock( name, null, attributes );
            String SessionId=stream.getAttribute("id");
            dispatcher.broadcastBeginConversation(SessionId);
        } else if ( name.equals( "message" ) )
            currentBlock = new Message( currentBlock, attributes );
        else if ( name.equals("iq") )
            currentBlock = new Iq( currentBlock, attributes );
        else if ( name.equals("presence") )
            currentBlock = new Presence( currentBlock, attributes );
        else if ( name.equals("xml")) return false;
        else currentBlock = new JabberDataBlock(name, null, attributes);
        return false;
    }
    
    /**
     * Method called when some plain text is encountered in the XML stream
     * comming from the server.
     *
     * @param text The plain text in question
     */
    
    public void plainTextEncountered( String text ) {
        if( currentBlock != null ) {
            currentBlock.setText( text );
        }
    }
    
    public void binValueEncountered( byte binVaule[] ) {
        if( currentBlock != null ) {
            //currentBlock.addText( text );
            currentBlock.addChild(binVaule);
        }
    }
    
    /**
     * The method called when a tag is ended in the stream comming from the
     * server.
     *
     * @param name The name of the tag that has just ended.
     */
    
    public void tagEnd( String name ) throws XMLException {
        if( currentBlock == null ) {
            if ( name.equals( "stream:stream" ) ) {
                dispatcher.halt();
                iostream.close();
                throw new XMLException("Normal stream shutdown");
            }
            return;
        }
        
        if (currentBlock.childBlocks!=null) currentBlock.childBlocks.trimToSize();

        JabberDataBlock parent = currentBlock.getParent();
        if( parent == null ) {

            if (currentBlock.getTagName().equals("stream:error")) {
                XmppError xe=XmppError.decodeStreamError(currentBlock);

                dispatcher.halt();
                iostream.close();
                throw new XMLException("Stream error: "+xe.toString());
                
            }
            
            dispatcher.broadcastJabberDataBlock( currentBlock );
            //System.out.println(currentBlock.toString());
        } else
            parent.addChild( currentBlock );
        currentBlock = parent;
    }

    private void ping() {
        JabberDataBlock ping=new Iq(StaticData.getInstance().account.getServer(), Iq.TYPE_GET, "ping");
        //ping.addChildNs("query", "jabber:iq:version");
        ping.addChildNs("ping", "urn:xmpp:ping");
        pingSent=true;
        send(ping);
    }

//#if ZLIB
    public void setZlibCompression() {
        iostream.setStreamCompression();
    }

    public String getStreamStats() {
        return iostream.getStreamStats();
    }
//#endif
    
    private class TimerTaskKeepAlive extends TimerTask{
        private Timer t;
        private int verifyCtr;
        private int period;
        private int type;
        public TimerTaskKeepAlive(int periodSeconds, int type){
            t=new Timer();
            this.type=type;
            this.period=periodSeconds;
            long periodRun=periodSeconds*1000; // milliseconds
            t.schedule(this, periodRun, periodRun);
        }
        public void run() {
            try {
                System.out.println("Keep-Alive");
                sendKeepAlive(type);
                
            } catch (Exception e) { 
                dispatcher.broadcastTerminatedConnection(e);
                e.printStackTrace(); 
            }
        }
	
        public void destroyTask(){
            if (t!=null){
                this.cancel();
                t.cancel();
                t=null;
            }
        }
    }
    
    private TimerTaskKeepAlive keepAlive;
    
    private class SendJabberDataBlock implements Runnable {
        private JabberDataBlock data;
        public SendJabberDataBlock(JabberDataBlock data) {
            this.data=data;
            new Thread(this).start();
        }
        public void run(){
            try {
                Thread.sleep(100);
                StringBuffer buf=new StringBuffer();
                data.constructXML(buf);
                sendBuf( buf );
            } catch (Exception e) {e.printStackTrace(); }
        }
    }
}
