/*
 * Roster.java
 *
 * Created on 6 ������ 2005 �., 19:16
 */

//TODO: ��������� ��������� ���������� ��� theStream.send

package Client;

import com.alsutton.jabber.*;
import com.alsutton.jabber.datablocks.*;
import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
//import javax.microedition.media.*;
//import Client.Contact.*;
import ui.*;
import ServiceDiscovery.ServiceDiscovery;
import GroupChat.GroupChatForm;

//import Client.msg.*;

/**
 *
 * @author Eugene Stahov
 */
//public class Roster implements JabberListener, VList.Callback{
public class Roster
        extends VirtualList
        implements
        JabberListener,
        CommandListener,
        Runnable
        //ContactEdit.StoreContact
        //Thread
{
    
    public final static int TRANSP_INDEX=0;
    public final static String TRANSP_GROUP="Transports";
    public final static int SELF_INDEX=1;
    public final static String SELF_GROUP="Self-Contact";
    public final static int SRC_RESULT_INDEX=2;
    public final static String SRC_RESULT_GROUP="Search results";
    public final static int NIL_INDEX=3;
    public final static String NIL_GROUP="Not-In-List";
    public final static int IGNORE_INDEX=4;
    public final static String IGNORE_GROUP="Ignore-List";
    public final static int COMMON_INDEX=5;
    public final static String COMMON_GROUP="General";
    
    
    /**
     * The resource to log in as
     */
    
    //public String RESOURCE = "Bombus";
    private Jid myJid;
    
    /**
     * The stream representing the connection to ther server
     */
    private JabberStream theStream ;
    
    
    int messageCount;
    public Object messageIcon;
   
    boolean reconnect=false;
    boolean querysign=false;
    
    private Command cmdStatus=new Command("Status >",Command.SCREEN,1);
    private Command cmdContact=new Command("Contact >",Command.SCREEN,2);
    private Command cmdDiscard=new Command("Discard Search",Command.SCREEN,3);
    private Command cmdAdd=new Command("Add Contact",Command.SCREEN,4);
    //private Command cmdGroup=new Command("Group menu",Command.SCREEN,3);
    private Command cmdAlert=new Command("Alert Profile >",Command.SCREEN,8);
    private Command cmdServiceDiscovery=new Command("Service Discovery",Command.SCREEN,9);
    private Command cmdGroupChat=new Command("Groupchat",Command.SCREEN,10);
    //private Command cmdShowOfflines=new Command("Show Offlines",Command.SCREEN,9);
    //private Command cmdHideOfflines=new Command("Hide Offlines",Command.SCREEN,9);
    //private Command cmdReconnect=new Command("Reconnect",Command.SCREEN,10);
    //private Command cmdLogoff=new Command("Logoff",Command.SCREEN,11);
    private Command cmdAccount=new Command("Account >",Command.SCREEN,12);
    //private Command cmdSetFullScreen=new Command("Fullscreen",Command.SCREEN,20);
    private Command cmdOptions=new Command("Options",Command.SCREEN,20);
    private Command cmdMinimize=new Command("Minimize", Command.SCREEN, 90);
    private Command cmdQuit=new Command("Quit",Command.SCREEN,99);
    
    private Config cf;
    private StaticData sd=StaticData.getInstance();
    
    public ServiceDiscoveryListener discoveryListener;
    
    /**
     * Creates a new instance of Roster
     * Sets up the stream to the server and adds this class as a listener
     */
    public Roster(Display display /*, boolean selAccount*/) {
        super();
/*#USE_LOGGER#*///<editor-fold>
//--        NvStorage.log("---------- INIT -------------");
/*$USE_LOGGER$*///</editor-fold>
        setProgress(20);
        //setTitleImages(StaticData.getInstance().rosterIcons);
        setTitleImages(sd.rosterIcons);
        
        this.display=display;
        
        cf=sd.config;
        
        //msgNotify=new EventNotify(display, Profile.getProfile(0) );
        
        setTitleImages(sd.rosterIcons);
        
        createTitle(4, null, null).addRAlign();
        getTitleLine().addElement(null);
        getTitleLine().addElement(null);
        
        //displayStatus();
        
        //l.setTitleImgL(6); //connect
        hContacts=new Vector();
        vGroups=new Groups();
        
        vContacts=new Vector(); // just for displaying
        
        addCommand(cmdStatus);
        addCommand(cmdAlert);
        addCommand(cmdAdd);
        addCommand(cmdServiceDiscovery);
        addCommand(cmdGroupChat);
        //addCommand(cmdReconnect);
        //addCommand(cmdLogoff);
        addCommand(cmdAccount);
/*#DefaultConfiguration,Release#*///<editor-fold>
        //addCommand(cmdSetFullScreen);
        setFullScreenMode(cf.fullscreen);
/*$DefaultConfiguration,Release$*///</editor-fold>
        addCommand(cmdOptions);
        addCommand(cmdQuit);
        
        
        addOptionCommands();
        //moveCursorTo(0);
        setCommandListener(this);
        //resetStrCache();
        
        //if (visible) display.setCurrent(this);
        /*if (selAccount) {
            new AccountSelect(display);
        } else {
            // connect whithout account select
            Account.launchAccount();
        }*/
        SplashScreen.getInstance().setExit(display, this);
    }
    
    void addOptionCommands(){
        if (cf.allowMinimize) addCommand(cmdMinimize);
        //Config cf=StaticData.getInstance().config;
        //        if (cf.showOfflineContacts) {
        //            addCommand(cmdHideOfflines);
        //            removeCommand(cmdShowOfflines);
        //        } else {
        //            addCommand(cmdShowOfflines);
        //            removeCommand(cmdHideOfflines);
        //        }
    }
    public void setProgress(String pgs,int percent){
        SplashScreen.getInstance().setProgress(pgs, percent);
/*#USE_LOGGER#*///<editor-fold>
//--        NvStorage.log(pgs+"%"+percent);
/*$USE_LOGGER$*///</editor-fold>
        setRosterTitle(pgs);
        redraw();
    }
    public void setProgress(int percent){
        SplashScreen.getInstance().setProgress(percent);
/*#USE_LOGGER#*///<editor-fold>
//--        NvStorage.log("%"+percent);
/*$USE_LOGGER$*///</editor-fold>
        //redraw();
    }
    
    private void setRosterTitle(String s){
        getTitleLine().setElementAt(s, 3);
    }
    
    private int rscaler;
    private int rpercent;
    
    public void rosterItemNotify(){
        rscaler++;
        if (rscaler<4) return;
        rscaler=0;
        rpercent++;
        if (rpercent==100) rpercent=60;
        SplashScreen.getInstance().setProgress(rpercent);
    }
    
    // establishing connection process
    public void run(){
        querysign=true;
        displayStatus();
        setProgress(25);
        try {
            if (!reconnect) {
                synchronized (hContacts) {
                    hContacts=new Vector();
                    vGroups=new Groups();
                    vContacts=new Vector(); // just for displaying
                }
                myJid=new Jid(sd.account.getJidStr());
                updateContact(sd.account.getNickName(), myJid.getJid(), SELF_GROUP, "self", false);
                
                System.gc();
            };
        } catch (Exception e) {
            e.printStackTrace();
/*#USE_LOGGER#*///<editor-fold>
//--            NvStorage.log(e, "Roster:214");
/*$USE_LOGGER$*///</editor-fold>
        }
        setProgress(26);
        
        //logoff();
        try {
            Account a=sd.account;
            setProgress("Connect to "+a.getServerN(), 30);
            theStream= a.openJabberStream();
            setProgress("Login", 40);
            theStream.setJabberListener( this );
        } catch( Exception e ) {
            setProgress("Failed",0);
            querysign=reconnect=false;
            myStatus=Presence.PRESENCE_OFFLINE;
            e.printStackTrace();
/*#USE_LOGGER#*///<editor-fold>
//--            NvStorage.log(e, "Roster:232");
/*$USE_LOGGER$*///</editor-fold>
            errorLog( e.getMessage() );
            displayStatus();
            redraw();
            //l.setTitleImgL(0);//offline
        }
        //l.setCallback(this);
    }
    
    private void errorLog(String s){
            Msg m=new Msg(Msg.MESSAGE_TYPE_OUT, myJid.getJidFull(), "Error", s);
            messageStore(m, -1);
    }
    
    public VirtualElement getItemRef(int Index){
        return (VirtualElement) vContacts.elementAt(Index);
    }
    
    public int getItemCount(){
        return vContacts.size();
    };
    
    private void displayStatus(){
        int s=querysign?ImageList.ICON_RECONNECT_INDEX:myStatus;
        int profile=cf.profile;//StaticData.getInstance().config.profile;
        Object en=(profile>1)? new Integer(profile+ImageList.ICON_PROFILE_INDEX):null;
        title.setElementAt(new Integer(s), 2);
        title.setElementAt(en, 5);
        if (messageCount==0) {
            messageIcon=null;
            title.setElementAt(null,1);
        } else {
            messageIcon=new Integer(ImageList.ICON_MESSAGE_INDEX);
            title.setElementAt(' '+String.valueOf(messageCount)+' ',1);
        }
        title.setElementAt(messageIcon, 0);
    }
    
    boolean countNewMsgs() {
        int m=0;
        synchronized (hContacts) {
            for (Enumeration e=hContacts.elements();e.hasMoreElements();){
                Contact c=(Contact)e.nextElement();
                m+=c.getNewMsgsCount();
            }
        }
        messageCount=m;
/*#USE_LED_PATTERN#*///<editor-fold>
//--                int pattern=cf.m55LedPattern;
//--                if (pattern>0) EventNotify.leds(pattern-1, m>0);
/*$USE_LED_PATTERN$*///</editor-fold>
        displayStatus();
        return (m>0);
    }
    
    public void cleanupSearch(){
        int i=0;
        while (i<hContacts.size()) {
            if ( ((Contact)hContacts.elementAt(i)).group==SRC_RESULT_INDEX )
                hContacts.removeElementAt(i);
            else i++;
        }
        reEnumRoster();
    }
    
    public void reEnumRoster(){
        
        int locCursor=cursor;
        Object focused=getSelectedObject();
        
        int tonlines=0;
        Vector tContacts=new Vector(vContacts.size());
        boolean offlines=cf.showOfflineContacts;//StaticData.getInstance().config.showOfflineContacts;
        
        Enumeration e;
        int i;
        vGroups.resetCounters();
        
        synchronized (hContacts) {
            for (e=hContacts.elements();e.hasMoreElements();){
                Contact c=(Contact)e.nextElement();
                boolean online=c.status<5;
                // group counters
                Group grp=vGroups.getGroup(c.group);
                grp.tncontacts++;
                if (online) {
                    grp.tonlines++;
                    tonlines++;
                }
                int gindex=c.group;
                // hide offlines whithout new messages
                if (offlines 
                 || online 
                 || c.getNewMsgsCount()>0 
                 || gindex==Roster.NIL_INDEX 
                 || gindex==Roster.TRANSP_INDEX)
                    grp.Contacts.addElement(c);
                //grp.addContact(c);
            }
        }
        // self-contact group
        if (cf.selfContact || vGroups.getGroup(SELF_INDEX).tonlines>1)
            vGroups.addToVector(tContacts, SELF_INDEX);
        // adding groups
        for (i=COMMON_INDEX;i<vGroups.getCount();i++)
            vGroups.addToVector(tContacts,i);
        // hiddens
        if (cf.ignore) vGroups.addToVector(tContacts,IGNORE_INDEX);
        // not-in-list
        if (cf.notInList) vGroups.addToVector(tContacts,NIL_INDEX);
        // transports
        if (cf.showTransports) vGroups.addToVector(tContacts,TRANSP_INDEX);
        
        // search result
        if (vGroups.getGroup(SRC_RESULT_INDEX).tncontacts>0) 
            vGroups.addToVector(tContacts, SRC_RESULT_INDEX);
        
        vContacts=tContacts;

        int tnContacts=hContacts.size();
        setRosterTitle('('+String.valueOf(tonlines)+'/'+String.valueOf(tnContacts)+')');
        
        //resetStrCache();
        if (cursor<0) cursor=0;
        
        // ����� ������ �� ������� �������
        // TODO: ����������������!
        if (locCursor==cursor) moveCursorTo(focused);
        if (cursor>=vContacts.size()) moveCursorEnd(); // ����� ������ �� �������
        
        focusedItem(cursor);
        redraw();
    }
    
    public void moveCursorTo(Object focused){
        if (focused!=null) {
            int c=vContacts.indexOf(focused);
            if (c>=0) moveCursorTo(c);
        }
    }
    
    
    public int myStatus=Presence.PRESENCE_ONLINE;
    
    private Vector hContacts;
    private Vector vContacts;
    public Groups vGroups;
    
    public Vector getHContacts() {return hContacts;}
    
    public final void updateContact(final String Nick, final String Jid, final String grpName, String subscr, boolean ask) {
        // called only on roster read
        int status=Presence.PRESENCE_OFFLINE;
        if (subscr.equals("none")) status=Presence.PRESENCE_UNKNOWN;
        if (ask) status=Presence.PRESENCE_ASK;
        //if (subscr.equals("remove")) status=Presence.PRESENCE_TRASH;
        if (subscr.equals("remove")) status=-1;
        
        Jid J=new Jid(Jid);
        Contact c=getContact(J,false);
        if (c==null) {
            c=new Contact(Nick, Jid, Presence.PRESENCE_OFFLINE, null);
            hContacts.addElement(c);
        }
        for (Enumeration e=hContacts.elements();e.hasMoreElements();) {
            c=(Contact)e.nextElement();
            if (c.jid.equals(J,false)) {
                Group group= (c.jid.isTransport())? 
                    vGroups.getGroup(TRANSP_INDEX) :
                    vGroups.getGroup(grpName);
                if (group==null) {
                    group=vGroups.addGroup(grpName);
                }
                c.nick=Nick;
                c.group=group.index;
                c.subscr=subscr;
                c.offline_type=status;
                c.ask_subscribe=ask;
                //if (status==Presence.PRESENCE_TRASH) c.status=status;
                //if (status!=Presence.PRESENCE_OFFLINE) c.status=status;
            }
        }
        if (status<0) removeTrash();
    }
    
    private final void removeTrash(){
        int index=0;
        while (index<hContacts.size()) {
            Contact c=(Contact)hContacts.elementAt(index);
            if (c.offline_type<0) {
                hContacts.removeElementAt(index);
            } else index++;
        }
    }
    
    public final Contact presenceContact(final String jid, int Status) {
        
        // �������� ������� �� ������ ������
        Jid J=new Jid(jid);
        
        Contact c=getContact(J, true); //Status!=Presence.PRESENCE_ASK);
        if (c!=null) {
            // ��������� ������
            if (Status>=0) {
                //if (c.status<7 || c.status==Presence.PRESENCE_ASK) 
                c.status=Status;
                sort();
                reEnumRoster();//redraw();
                //System.out.println("updated");
            }
            return c;
        }
        // �������� ������� ��� ��������
        
        if (Status<0) Status=Presence.PRESENCE_OFFLINE;
        c=getContact(J, false);
        if (c==null) {
            // ��... ��� ����� �����
            // ����� ����� �������� �����
            //System.out.println("new");
            c=new Contact(null, jid, Status, "not-in-list");
            c.origin=Contact.ORIGIN_PRESENCE;
            c.group=NIL_INDEX;
            addContact(c);
        } else {
            // ����� jid � ����� ��������
            if (c.origin==Contact.ORIGIN_ROSTER) {
                c.origin=Contact.ORIGIN_CLONE;
                c.status=Status;
                c.jid=J;
                //System.out.println("add resource");
            } else {
                c=c.clone(J, Status);
                addContact(c);
                //System.out.println("cloned");
            }
        }
        sort();
        reEnumRoster();
        return c;
    }
    public void addContact(Contact c) {
        hContacts.addElement(c);
    }
    
    private void sort(){
        synchronized (hContacts) {
            int f, i;
            Contact temp, temp2;
            
            for (f = 1; f < hContacts.size(); f++) {
                temp=getContact(f);
                if ( temp.compare(getContact(f-1)) >=0 ) continue;
                i    = f-1;
                while (i>=0){
                    temp2=getContact(i);
                    if (temp2.compare(temp) <0) break;
                    hContacts.setElementAt(temp2,i+1);
                    i--;
                }
                hContacts.setElementAt(temp,i+1);
            }
        }
    }
    
    private final Contact getContact(int index) {
        return (Contact)(hContacts.elementAt(index));
    }
    
    public final Contact getContact(final String Jid, boolean compareResources) {
        return (getContact(new Jid(Jid), compareResources));
    }
    public final Contact getContact(final Jid j, final boolean compareResources) {
        synchronized (hContacts) {
            for (Enumeration e=hContacts.elements();e.hasMoreElements();){
                Contact c=(Contact)e.nextElement();
                if (c.jid.equals(j,compareResources)) return c;
            }
        }
        return null;
    }
    
    /**
     * Method to inform the server we are now online
     */
    
    public void sendPresence(int status) {
        querysign=false;
        myStatus=status;
        displayStatus();
        if (status==Presence.PRESENCE_OFFLINE) {
            synchronized(hContacts) {
                for (Enumeration e=hContacts.elements(); e.hasMoreElements();){
                    Contact c=(Contact)e.nextElement();
                    //if (c.status<Presence.PRESENCE_UNKNOWN)
                        c.status=Presence.PRESENCE_OFFLINE; // keep error & unknown
                }
            }
        }
        Vector v=sd.statusList;//StaticData.getInstance().statusList;
        ExtendedStatus es=null;
        
        // reconnect if disconnected        
        if (status!=Presence.PRESENCE_OFFLINE && theStream==null ) {
            reconnect=(hContacts.size()>1);
            redraw();
            
            new Thread(this).start();
            return;
        }
        
        // send presence
        for (Enumeration e=v.elements(); e.hasMoreElements(); ){
            es=(ExtendedStatus)e.nextElement();
            if (status==es.getImageIndex()) break;
        }
        Presence presence = new Presence(myStatus, es.getPriority(), es.getMessage());
        if (theStream!=null) {
            theStream.send( presence );

            // disconnect
            if (status==Presence.PRESENCE_OFFLINE) {
                try {
                    theStream.close();
                } catch (Exception e) { 
                    e.printStackTrace(); 
/*#USE_LOGGER#*///<editor-fold>
//--                    NvStorage.log(e, "Roster:543");
/*$USE_LOGGER$*///</editor-fold>
                }
                theStream=null;
                System.gc();
            }
        }
        Contact c=presenceContact(myJid.getJidFull(), myStatus);
        JabberDataBlock x=presence.getChildBlock("x");
        if (x!=null) if (x.isJabberNameSpace("http://jabber.org/protocol/muc"))
            c.origin=Contact.ORIGIN_GC_MEMBER;
        
        reEnumRoster();
    }
    
    public void sendPresence(String to, String type, JabberDataBlock child) {
        JabberDataBlock presence=new Presence(to, type);
        if (child!=null) presence.addChild(child);
        theStream.send(presence);
    }
    /**
     * Method to send a message to the specified recipient
     */
    
    public void sendMessage(Contact to, final String body, final String subject , int composingState) {
        boolean groupchat=to.transport==6 && !to.jid.hasResource();
        Message simpleMessage = new Message( 
                to.getJid(), 
                body, 
                subject, 
                groupchat 
        );
        if (groupchat && body==null) return;
        if (composingState>0) {
            JabberDataBlock event=new JabberDataBlock("x", null,null);
            event.setNameSpace("jabber:x:event");
            //event.addChild(new JabberDataBlock("id",null, null));
            if (composingState==1) {
                event.addChild(new JabberDataBlock("composing",null, null));
            }
            simpleMessage.addChild(event);
        }
        //System.out.println(simpleMessage.toString());
        theStream.send( simpleMessage );
    }
    
    private Vector vCardQueue;
    private void sendVCardReq(){
        querysign=false; 
        if (vCardQueue!=null) if (!vCardQueue.isEmpty()) {
            JabberDataBlock req=(JabberDataBlock) vCardQueue.lastElement();
            vCardQueue.removeElement(req);
            //System.out.println(k.nick);
            theStream.send(req);
            querysign=true;
        }
        displayStatus();
    }
    /**
     * Method to handle an incomming datablock.
     *
     * @param data The incomming data
     */
    public void blockArrived( JabberDataBlock data ) {
        try {
            
            if( data instanceof Iq ) {
                String type = (String) data.getTypeAttribute();
                if ( type.equals( "error" ) ) {
                    if (data.getAttribute("id").equals("auth-s")) {
                        // ������ �����������
                        myStatus=Presence.PRESENCE_OFFLINE;
                        setProgress("Login failed",0);
                        
                        JabberDataBlock err=data.getChildBlock("error");
                        errorLog(err.toString());
                        
                        querysign=reconnect=false;
                        displayStatus();
                        redraw();
                    }
                }
                String id=(String) data.getAttribute("id");
                if (discoveryListener!=null && id!=null) 
                if (id.startsWith("disco")) {
                    discoveryListener.blockArrived(data);
                } 
                
                if (id!=null) if (id.startsWith("nickvc")) {
                    JabberDataBlock vc=data.getChildBlock("vcard");
                    String from=data.getAttribute("from");
                    String nick=IqGetVCard.getNickName(vc);
                    Contact c=getContact(from, false);
                    String group=(c.group==COMMON_INDEX)?
                        null: vGroups.getGroup(c.group).name;
                    if (nick.length()!=0)  storeContact(from,nick,group, false);
                    //updateContact( nick, c.rosterJid, group, c.subscr, c.ask_subscribe);
                    sendVCardReq();
                }
                
                if ( type.equals( "result" ) ) {
                    if (id.equals("auth-s") ) {
                        // ������������. ������, ���� ��� ���������, �� ������ ����� ������
                        if (reconnect) {
                            querysign=reconnect=false;
                            sendPresence(myStatus);
                            return;
                        }
                        
                        // ����� ����� ������ ������
                        theStream.enableRosterNotify(true);
                        rpercent=60;
                        JabberDataBlock qr=new IqQueryRoster();
                        setProgress("Roster request ", 60);
                        theStream.send( qr );
                    }
                    if (id.equals("getros")) {
                        // � ��� � ������ ������� :)
                        //SplashScreen.getInstance().setProgress(95);
                        
                        theStream.enableRosterNotify(false);

                        processRoster(data);
                        
                        setProgress("Connected",100);
                        reEnumRoster();
                        // ������ ����� �����������
                        querysign=reconnect=false;
                        sendPresence(myStatus);
                        //sendPresence(Presence.PRESENCE_INVISIBLE);
                        
                        SplashScreen.getInstance().close(); // display.setCurrent(this);
                        
                    }
                    if (id.startsWith("getvc")) {
                        JabberDataBlock vc=data.getChildBlock("vcard");
                        
                        querysign=false;
                        String from=data.getAttribute("from");
                        String body=IqGetVCard.dispatchVCard(vc);

                        Msg m=new Msg(Msg.MESSAGE_TYPE_IN, from, "vCard "+from, body);
                        m.photo=IqGetVCard.getPhoto(vc);

                        messageStore(m, -1);
                        redraw();
                            
                    }
                    if (id.equals("getver")) {
                        JabberDataBlock vc=data.getChildBlock("query");
                        if (vc!=null) {
                            querysign=false;
                            String from=data.getAttribute("from");
                            String body=IqVersionReply.dispatchVersion(vc);
                            
                            Msg m=new Msg(Msg.MESSAGE_TYPE_IN, from, "Client info", body);
                            messageStore(m, -1);
                            redraw();
                            
                        }
                    }
                    
                } else if (type.equals("get")){
                    JabberDataBlock query=data.getChildBlock("query");
                    if (query!=null){
                        // ��������� �� ������ ������ �������
                        if (query.isJabberNameSpace("jabber:iq:version"))
                            //String xmlns=query.getAttribute("xmlns");
                            //if (xmlns!=null) if (xmlns.equals("jabber:iq:version"))
                            theStream.send(new IqVersionReply(data));
                    }
                } else if (type.equals("set")) {
                    processRoster(data);
                    reEnumRoster();
                }
            }
            
            // If we've received a message
            
            else if( data instanceof Message ) {
                querysign=false;
                Message message = (Message) data;
                
                String from=message.getFrom();
                String body=message.getBody().trim();
                String tStamp=message.getTimeStamp();
                
                Contact c=presenceContact(from, -1);
                if (message.getTypeAttribute().equals("groupchat")) {
                    // muc message
                    int rp=from.indexOf('/');
                    body=from.substring(rp+1)+"> "+body;
                    from=from.substring(0, rp);
                    c.origin=Contact.ORIGIN_GROUPCHAT;
                }
                
                boolean compose=false;
                JabberDataBlock x=message.getChildBlock("x");
                if (body.length()==0) body=null; 
                
                if (x!=null) {
                    compose=(x.getChildBlock("composing")!=null);
                    if (compose) c.accept_composing=true;
                    if (body!=null) compose=false;
                    c.setComposing(compose);
                }
                redraw();

                if (body==null) return;
                
                String subj=message.getSubject().trim();
                if (subj.length()==0) subj=null;
  
            
                Msg m=new Msg(Msg.MESSAGE_TYPE_IN, from, subj, body);
                if (tStamp!=null) 
                    m.date=Time.dateIso8601(tStamp);
                messageStore(m, -1);
                //Contact c=getContact(from);
                //c.msgs.addElement(m);
                //countNewMsgs();
                //setFocusTo(c);
                //redraw();
                
            }
            // �����������
            else if( data instanceof Presence ) {
                if (myStatus==Presence.PRESENCE_OFFLINE) return;
                Presence pr= (Presence) data;
                
                String from=pr.getFrom();
                pr.dispathch();
                int ti=pr.getTypeIndex();
                //PresenceContact(from, ti);
                Msg m=new Msg(
                        (ti==Presence.PRESENCE_AUTH)?
                            Msg.MESSAGE_TYPE_AUTH:Msg.MESSAGE_TYPE_PRESENCE,
                        from,
                        null,
                        pr.getPresenceTxt());
                messageStore(m, ti).priority=pr.getPriority();
            }
        } catch( Exception e ) {
            e.printStackTrace();
/*#USE_LOGGER#*///<editor-fold>
//--            NvStorage.log(e, "Roster:743");
/*$USE_LOGGER$*///</editor-fold>
        }
    }
    
    void processRoster(JabberDataBlock data){
        JabberDataBlock q=data.getChildBlock("query");
        if (!q.isJabberNameSpace("jabber:iq:roster")) return;
        int type=0;
        
        Vector cont=(q!=null)?q.getChildBlocks():null;
        
        if (cont!=null)
            for (Enumeration e=cont.elements(); e.hasMoreElements();){
                JabberDataBlock i=(JabberDataBlock)e.nextElement();
                if (i.getTagName().equals("item")) {
                    //String name=strconv.convAscii2Utf8(i.getAttribute("name"));
                    String name=i.getAttribute("name");
                    String jid=i.getAttribute("jid");
                    String subscr=i.getAttribute("subscription");
                    boolean ask= (i.getAttribute("ask")!=null);

                    // ����� ������
                    JabberDataBlock g=i.getChildBlock("group");
                    String group=(g==null)?COMMON_GROUP:g.getText();

                    // ��� ����� ���������, ����� ������ jabber:iq:roster,
                    // �� ������ ������� ��� ��� ����������
                    //String iqType=data.getTypeAttribute();
                    //if (iqType.equals("set")) type=1;

                    updateContact(name,jid,group, subscr, ask);
                }
            
            }
    }
    
    Contact messageStore(Msg message, int status){
        Contact c=presenceContact(message.from,status);
        /*getContact(message.from, true);
        if (c==null) {
            // contact not in list
            if (cf.notInList) {
                c=new Contact(null, message.from, Presence.PRESENCE_UNKNOWN);
                c.group=NIL_INDEX;
                hContacts.addElement(c);
                reEnumRoster();
            }
        }*/
        if (c==null) return c;  // not to store/signal not-in-list message
        c.addMessage(message);
        //message.from=c.getNickJid();
        switch (message.messageType) {
            case Msg.MESSAGE_TYPE_PRESENCE:
            case Msg.MESSAGE_TYPE_OUT: return c;
        }
        if (countNewMsgs()) reEnumRoster();
        
        if (c.group==IGNORE_INDEX) return c;    // no signalling/focus on ignore
        
        if (sd.isMinimized) {
            display.setCurrent(this);
            sd.isMinimized=false;
        }
        setFocusTo(c);
        AlertProfile.playNotify(display, 0);
        return c;
    }
    
    
    /**
     * Method to begin talking to the server (i.e. send a login message)
     */
    
    public void beginConversation(String SessionId) {
        //try {
        setProgress("Login in progress", 42);
        Account a=sd.account;//StaticData.getInstance().account;
        Login login = new Login( 
                a.getUserName(), 
                a.getServerN(), 
                a.getPassword(), 
                SessionId, 
                a.getResource() 
        );
        theStream.send( login );
        //} catch( Exception e ) {
        //l.setTitleImgL(0);
        //e.printStackTrace();
        //}
        //l.setTitleImgL(2);
        
    }
    
    /**
     * If the connection is terminated then print a message
     *
     * @e The exception that caused the connection to be terminated, Note that
     *  receiving a SocketException is normal when the client closes the stream.
     */
    public void connectionTerminated( Exception e ) {
        //l.setTitleImgL(0);
        //System.out.println( "Connection terminated" );
        if( e != null ) {
            errorLog(e.getMessage());
            e.printStackTrace();
/*#USE_LOGGER#*///<editor-fold>
//--            NvStorage.log(e, "Roster:846");
/*$USE_LOGGER$*///</editor-fold>
        }
        setProgress("Disconnected", 0);
        try {
            sendPresence(Presence.PRESENCE_OFFLINE);
        } catch (Exception e2) {
            e2.printStackTrace();
/*#USE_LOGGER#*///<editor-fold>
//--            NvStorage.log(e2, "Roster:855");
/*$USE_LOGGER$*///</editor-fold>
        }
        redraw();
    }
    
    //private VList l;
    //private IconTextList l;
    
    public void eventOk(){
        super.eventOk();
        createMsgList();
        reEnumRoster();
    }
    
    private Displayable createMsgList(){
        Object e=getSelectedObject();
        if (e instanceof Contact) {
            return new MessageList((Contact)e,display);
        }
        return null;
    }
    protected void keyGreen(){
        Displayable pview=createMsgList();
        if (pview!=null) {
            Contact c=(Contact)getSelectedObject();
            ( new MessageEdit(display, c, c.msgSuspended) ).setParentView(pview);
            c.msgSuspended=null;
        }
        //reEnumRoster();
    }
    
    public void setFocusTo(Contact c){
        Group g=vGroups.getGroup(c.group);
        if (g.collapsed) {
            g.collapsed=false;
            reEnumRoster();
        }
        moveCursorTo(c);
    }
    public void userKeyPressed(int KeyCode){
        if (KeyCode==KEY_NUM0) {
            if (messageCount==0) return;
            Object atcursor=getSelectedObject();
            Contact c=null;
            if (atcursor instanceof Contact) c=(Contact)atcursor;
            // � ���� ������ �� ������, �� ���� ���.
            else c=(Contact)hContacts.firstElement();
            
            Enumeration i=hContacts.elements();
            Contact p=null;
            while (i.hasMoreElements()){
                p=(Contact)i.nextElement();
                if (p==c) break;
            }
            if (c==null) c=p;   // ��������� ������� ����
            
            // ���� ���������
            boolean search=true;
            while (search) {
                if (!i.hasMoreElements()) i=hContacts.elements();
                p=(Contact)i.nextElement();
                if (p==c) break; // ������ ���� �������
                if (p.getNewMsgsCount()>0)
                    setFocusTo(p);
                
            }
        }
    }
    
    public void logoff(){
        if (theStream!=null)
        try {
/*#USE_LOGGER#*///<editor-fold>
//--            NvStorage.log("logoff");
/*$USE_LOGGER$*///</editor-fold>
             sendPresence(Presence.PRESENCE_OFFLINE);
        } catch (Exception e) { 
            e.printStackTrace(); 
/*#USE_LOGGER#*///<editor-fold>
//--            NvStorage.log(e, "Roster:932");
/*$USE_LOGGER$*///</editor-fold>
        }
    };
    
    public void commandAction(Command c, Displayable d){
        if (c==cmdQuit) {
            destroyView();
            logoff();
            //StaticData sd=StaticData.getInstance();
            cf.saveToStorage();
            sd.midlet.notifyDestroyed();
            return;
        }
        if (c==cmdMinimize) { 
            sd.isMinimized=true;
            display.setCurrent(null); 
        }
        
        if (c==cmdAccount){ new AccountSelect(display, false); }
        if (c==cmdServiceDiscovery) { new ServiceDiscovery(display, theStream); }
        if (c==cmdGroupChat) { new GroupChatForm(display); }
        if (c==cmdStatus) { new StatusSelect(display); }
        if (c==cmdAlert) { new AlertProfile(display); }
        if (c==cmdOptions){ new ConfigForm(display); }
        if (c==cmdContact) { contactMenu((Contact) getSelectedObject()); }
        if (c==cmdDiscard) { cleanupSearch(); }
        if (c==cmdAdd) {
            //new MIDPTextBox(display,"Add to roster", null, new AddContact());
            Object o=getSelectedObject();
            Contact cn=null;
            if (o instanceof Contact) {
                cn=(Contact)o;
                if (cn.group!=NIL_INDEX && cn.group!=SRC_RESULT_INDEX) cn=null;
            }
            new ContactEdit(display, cn);
        }
    }
    protected void showNotify() { countNewMsgs(); }
    
    // temporary here
    public final String getProperty(final String key, final String defvalue) {
        try {
            String s=sd.midlet.getAppProperty(key);//StaticData.getInstance().midlet.getAppProperty(key);
            return (s==null)?defvalue:s;
        } catch (Exception e) {
            return defvalue;
        }
    }
    
    //void resetStrCache(){
    //System.out.println("reset roster cache");
    //stringCache=new Vector(vContacts.capacity());
    //}
    
    public void keyRepeated(int keyCode) {
        super.keyRepeated(keyCode);
        if (kHold==keyCode) return;
        //kHold=keyCode;
        kHold=keyCode;
        
        if (keyCode==cf.keyLock) 
            new KeyBlock(display, getTitleLine(), cf.keyLock, cf.ghostMotor); 

        if (keyCode==cf.keyVibra) {
            cf.profile=(cf.profile==AlertProfile.VIBRA)? 
                cf.def_profile : AlertProfile.VIBRA;
            displayStatus();
            redraw();
        }
        
        if (keyCode==cf.keyOfflines) {
            cf.showOfflineContacts=!cf.showOfflineContacts;
            reEnumRoster();
        }

        if (keyCode==cf.keyHide && cf.allowMinimize) {
            sd.isMinimized=true;
            display.setCurrent(null); 
        }
    }
    
    public void focusedItem(int index) {
        if (vContacts==null) return;
        if (index>=vContacts.size()) return;
        Object atCursor=vContacts.elementAt(index);
        if (atCursor instanceof Contact) {
            addCommand(cmdContact);
            //removeCommand(cmdGroup);
        } else removeCommand(cmdContact);
        
        if (atCursor instanceof Group) {
            if (((Group) atCursor).index==SRC_RESULT_INDEX)  addCommand(cmdDiscard);
        } else removeCommand(cmdDiscard);
        
        /*if (atCursor instanceof Group) {
            //addCommand(cmdGroup);
            removeCommand(cmdContact);
        }*/
    }
    
    public void contactMenu(final Contact c) {
        Menu m=new Menu(c.toString()){
            public void eventOk(){
                int index=((MenuItem)getSelectedObject()).index;
                String to=(index<3)? c.getJid() : c.getJidNR();
                destroyView();
                switch (index) {
                    case 0: // info
                        querysign=true; displayStatus();
                        theStream.send(new IqVersionReply(to));
                        break;
                    case 1: // info
                        querysign=true; displayStatus();
                        theStream.send(new IqGetVCard(to, "getvc"));
                        break;
                        
                    case 2:
                        (new ContactEdit(display, c )).parentView=sd.roster;
                        return; //break;
                        
                    case 3: //subscription
                        new SubscriptionEdit(display, c);
                        return; //break;
                    case 4:
                        new YesNoAlert(display, parentView, "Delete contact?", c.getNickJid()){
                            public void yes() {
                                for (Enumeration e=hContacts.elements();e.hasMoreElements();) {
                                    Contact c2=(Contact)e. nextElement();
                                    if (c.jid.equals(c2. jid,false)) {
                                        c2.status=c2.offline_type=Presence.PRESENCE_TRASH;
                                    }
                                }
                                
                                if (c.group==NIL_INDEX) {
                                    hContacts.removeElement(c);
                                    reEnumRoster();
                                } else
                                    theStream.send(new IqQueryRoster(c.getJidNR(),null,null,"remove"));
                            };
                        };
                        return;
                        //new DeleteContact(display,c);
                        //break;
                    case 6: // logoff
                    {
                        //querysign=true; displayStatus();
                        Presence presence = new Presence(
                                Presence.PRESENCE_OFFLINE, -1, "");
                        presence.setTo(c.getJid());
                        theStream.send( presence );
                        break;
                    }
                    case 5: // logon
                    {
                        //querysign=true; displayStatus();
                        Presence presence = new Presence(
                                myStatus, 0, "");
                        presence.setTo(c.getJid());
                        theStream.send( presence );
                        break;
                    }
                    case 7: // Nick resolver
                    {
                        vCardQueue=new Vector();
                        for (Enumeration e=hContacts.elements(); e.hasMoreElements();){
                            Contact k=(Contact) e.nextElement();
                            if (k.jid.isTransport()) continue;
                            if (k.transport==c.transport && k.nick==null && k.group>=COMMON_INDEX) {
                                vCardQueue.addElement(new IqGetVCard(k.getJid(), "nickvc"+k.rosterJid));
                            }
                        }
                        querysign=true; displayStatus();
                        sendVCardReq();
                    }
                }
                destroyView();
            }
        };
        if (c.group==TRANSP_INDEX) {
            m.addItem(new MenuItem("Logon",5));
            m.addItem(new MenuItem("Logoff",6));
            m.addItem(new MenuItem("Resolve Nicknames", 7));
        }
        m.addItem(new MenuItem("vCard",1));
        m.addItem(new MenuItem("Client Info",0));
        if (c.group!=SELF_INDEX && c.group!=SRC_RESULT_INDEX) {
            if (c.group!=TRANSP_INDEX) 
                m.addItem(new MenuItem("Edit",2));
            m.addItem(new MenuItem("Subscription",3));
            m.addItem(new MenuItem("Delete",4));
        }
       m.attachDisplay(display);
    }
    
    /**
     * store cotnact on server
     */
    public void storeContact(String jid, String name, String group, boolean newContact){
        
        theStream.send(new IqQueryRoster(jid, name, group, null));
        if (newContact) theStream.send(new Presence(jid,"subscribe"));
    }
    
}
