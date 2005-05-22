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
    public final static int NIL_INDEX=2;
    public final static String NIL_GROUP="Not-In-List";
    public final static int IGNORE_INDEX=3;
    public final static String IGNORE_GROUP="Ignore-List";
    public final static int COMMON_INDEX=4;
    public final static String COMMON_GROUP="General";
    
    
    /**
     * The resource to log in as
     */
    
    public String RESOURCE = "Bombus";
    private Jid myJid;
    
    /**
     * The stream representing the connection to ther server
     */
    private JabberStream theStream ;
    
    
    /**
     * Creates a new instance of Roster
     * Sets up the stream to the server and adds this class as a listener
     */
    //public Roster(VList L) {
    int messageCount;
    Object messageIcon;
    
    //EventNotify msgNotify;
    
    boolean reconnect=false;
    boolean querysign=false;
    
    private Command cmdStatus=new Command("Status >",Command.SCREEN,1);
    private Command cmdContact=new Command("Contact >",Command.SCREEN,2);
    private Command cmdAdd=new Command("Add Contact",Command.SCREEN,3);
    //private Command cmdGroup=new Command("Group menu",Command.SCREEN,3);
    private Command cmdAlert=new Command("Alert Profile >",Command.SCREEN,8);
    //private Command cmdShowOfflines=new Command("Show Offlines",Command.SCREEN,9);
    //private Command cmdHideOfflines=new Command("Hide Offlines",Command.SCREEN,9);
    private Command cmdReconnect=new Command("Reconnect",Command.SCREEN,10);
    private Command cmdLogoff=new Command("Logoff",Command.SCREEN,11);
    private Command cmdAccount=new Command("Account >",Command.SCREEN,12);
    //private Command cmdSetFullScreen=new Command("Fullscreen",Command.SCREEN,20);
    private Command cmdOptions=new Command("Options",Command.SCREEN,20);
    private Command cmdQuit=new Command("Quit",Command.SCREEN,99);
    
    private Config cf;
    private StaticData sd=StaticData.getInstance();
    
    public Roster(Display display /*, boolean selAccount*/) {
        super();
        setTitleImages(StaticData.getInstance().rosterIcons);
        
        this.display=display;
        
        cf=sd.config;
        
        //msgNotify=new EventNotify(display, Profile.getProfile(0) );
        
        setTitleImages(sd.rosterIcons);
        
        createTitle(4, null, null).setElementAt("title",3);
        getTitleLine().addRAlign();
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
        addCommand(cmdReconnect);
        addCommand(cmdLogoff);
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
        
    }
    
    void addOptionCommands(){
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
        getTitleLine().setElementAt(pgs, 3);
        redraw();
    }
    // establishing connection process
    public void run(){
        if (!reconnect) {
            hContacts=new Vector();
            vGroups=new Groups();
            vContacts=new Vector(); // just for displaying
            myJid=new Jid(sd.account.toString()+"/"+RESOURCE);
            updateContact(null, myJid.getJid(), SELF_GROUP, "self", false);
            
            System.gc();
        };
        
        logoff();
        
        try {
            Account a=sd.account;
            setProgress("Connect to "+a.getServerN(), 30);
            theStream= a.openJabberStream();
            setProgress("Login", 40);
            theStream.setJabberListener( this );
        } catch( Exception e ) {
            setProgress("Failed",0);
            querysign=reconnect=false;
            displayStatus();
            redraw();
            e.printStackTrace();
            //l.setTitleImgL(0);//offline
        }
        //l.setCallback(this);
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
        /*#M55,M55_Release#*///<editor-fold>
//--                int pattern=cf.m55_led_pattern;//StaticData.getInstance().config.m55_led_pattern;
//--                if (pattern>0) EventNotify.leds(pattern-1, m>0);
        /*$M55,M55_Release$*///</editor-fold>
        displayStatus();
        return (m>0);
    }
    
    public void reEnumRoster(){
        
        int locCursor=cursor;
        Object focused=getSelectedObject();
        
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
                if (online) grp.tonlines++;
                // hide offlines whithout new messages
                if (offlines || online || c.getNewMsgsCount()>0 || c.group==Roster.NIL_INDEX)
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
        if (cf.showTransports) vGroups.addToVector(tContacts,0);
        
        vContacts=tContacts;
        //resetStrCache();
        if (cursor<0) cursor=0;
        
        // ������ ������ �� ������� �������
        // TODO: ����������������!
        if (locCursor==cursor) moveCursorTo(focused);
        else {
            if (cursor>=vContacts.size()) moveCursorEnd();
        }
        focusedItem(cursor);
        redraw();
    }
    
    public void moveCursorTo(Object focused){
        if (focused!=null) {
            int c=vContacts.indexOf(focused);
            if (c>=0) moveCursorTo(c);
        }
    }
    
    
    public int myStatus=Presence.PRESENCE_OFFLINE;
    
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
    
    public final Contact PresenceContact(final String jid, int Status) {
        
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
            //c.origin=2;
            c.group=NIL_INDEX;
            hContacts.addElement(c);
        } else {
            // ����� jid � ����� ��������
            if (c.origin==0) {
                c.origin=1;
                c.status=Status;
                c.jid=J;
                //System.out.println("add resource");
            } else {
                c=c.clone(J, Status);
                hContacts.addElement(c);
                //System.out.println("cloned");
            }
        }
        sort();
        reEnumRoster();
        return c;
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
                    if (c.status<Presence.PRESENCE_UNKNOWN)
                        c.status=Presence.PRESENCE_OFFLINE; // keep error & unknown
                }
            }
        }
        Vector v=sd.statusList;//StaticData.getInstance().statusList;
        ExtendedStatus es=null;
        for (Enumeration e=v.elements(); e.hasMoreElements(); ){
            es=(ExtendedStatus)e.nextElement();
            if (status==es.getImageIndex()) break;
        }
        Presence presence = new Presence(myStatus, es.getPriority(), es.getMessage());
        theStream.send( presence );
        
        PresenceContact(myJid.getJidFull(), myStatus);
        reEnumRoster();
    }
    
    public void sendPresence(String to, String type) {
        theStream.send(new Presence(to, type));
    }
    /**
     * Method to send a message to the specified recipient
     */
    
    public void sendMessage(final String to, final String body) {
        Message simpleMessage = new Message( to, body );
        theStream.send( simpleMessage );
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
                        setProgress("Login failed",0);
                        querysign=reconnect=false;
                        displayStatus();
                        redraw();
                    }
                }
                if ( type.equals( "result" ) ) {
                    String id=(String) data.getAttribute("id");
                    if (id.equals("auth-s") ) {
                        // ������������. ������, ���� ��� ���������, �� ������ ����� ������
                        if (reconnect) {
                            querysign=reconnect=false;
                            sendPresence(Presence.PRESENCE_ONLINE);
                            return;
                        }
                        
                        // ����� ����� ������ ������
                        JabberDataBlock qr=new IqQueryRoster();
                        setProgress("Roster request ", 70);
                        theStream.send( qr );
                    }
                    if (id.equals("getros")) {
                        // � ��� � ������ ������� :)
                        SplashScreen.getInstance().setProgress(85);
                        
                        processRoster(data);
                        reEnumRoster();
                        
                        setProgress("Connected",100);
                        // ������ ����� �����������
                        querysign=reconnect=false;
                        sendPresence(Presence.PRESENCE_ONLINE);
                        display.setCurrent(this);
                        SplashScreen.getInstance().img=null;    // ����������� ������
                        
                    }
                    if (id.equals("getvc")) {
                        JabberDataBlock vc=data.getChildBlock("vcard");
                        if (vc!=null) {
                            querysign=false;
                            String from=data.getAttribute("from");
                            String body=IqGetVCard.dispatchVCard(vc);
                            
                            Msg m=new Msg(Msg.MESSAGE_TYPE_IN, from, "vCard "+from, body);
                            messageStore(m, -1);
                            redraw();
                            
                        }
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
                if (body.length()==0) return;
                
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
                redraw();
                
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
        Contact c=PresenceContact(message.from,status);
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
        
        setFocusTo(c);
        AlertProfile.playNotify(display, 0);
        return c;
    }
    
    
    /**
     * Method to begin talking to the server (i.e. send a login message)
     */
    
    public void beginConversation(String SessionId) {
        //try {
        Account a=sd.account;//StaticData.getInstance().account;
        Login login = new Login( a.getUserName(), a.getServerN(), a.getPassword(), SessionId, RESOURCE );
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
        if( e != null )
            e.printStackTrace();
        try {
            sendPresence(Presence.PRESENCE_OFFLINE);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        redraw();
    }
    
    //private VList l;
    //private IconTextList l;
    
    public void eventOk(){
        super.eventOk();
        Object e=getSelectedObject();
        if (e instanceof Contact) {
            new MessageList((Contact)e,display);
        }
        reEnumRoster();
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
        try {
            if (theStream!=null) {
                try {
                    sendPresence(Presence.PRESENCE_OFFLINE);
                    theStream.close();
                } catch (Exception e) { e.printStackTrace(); }
            }
            theStream=null;
            System.gc();
        } catch (Exception e) { e.printStackTrace(); }
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
        if (c==cmdReconnect) {
            querysign=reconnect=true;
            
            displayStatus();
            redraw();
            
            new Thread(this).start();
            return;
        }
        if (c==cmdLogoff) {
            logoff();
            return;
        }
        if (c==cmdAccount){
            new AccountSelect(display);
            return;
        }
        if (c==cmdStatus) {
            new StatusSelect(display);
        }
        if (c==cmdAlert) {
            new AlertProfile(display);
        }
        if (c==cmdOptions){
            new ConfigForm(display);
        }
        //        if (c==cmdHideOfflines || c==cmdShowOfflines) {
        //            //Config cf=StaticData.getInstance().config;
        //            cf.showOfflineContacts=!cf.showOfflineContacts;
        //            addOptionCommands();
        //            reEnumRoster();
        //            moveCursorTo(cursor);
        //        }
        if (c==cmdContact) {
            contactMenu((Contact) getSelectedObject());
        }
        if (c==cmdAdd) {
            //new MIDPTextBox(display,"Add to roster", null, new AddContact());
            Object o=getSelectedObject();
            Contact cn=null;
            if (o instanceof Contact) {
                cn=(Contact)o;
                if (cn.group!=Roster.NIL_INDEX) cn=null;
            }
            new ContactEdit(display, cn);
        }
        /*#DefaultConfiguration,Release#*///<editor-fold>
        //        if (c==cmdSetFullScreen) {
        //            //Config cf=StaticData.getInstance().config;
        //            cf.fullscreen=!cf.fullscreen;
        //            setFullScreenMode(cf.fullscreen);
        //        }
        /*$DefaultConfiguration,Release$*///</editor-fold>
    }
    protected void showNotify() {
        countNewMsgs();
    }
    
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
        
        if (keyCode==cf.keyLock) new KeyBlock(display, getTitleLine(), cf.keyLock); 

        if (keyCode==cf.keyVibra) {
            cf.profile=(cf.profile==AlertProfile.VIBRA)? 
                cf.def_profile : AlertProfile.VIBRA;
            displayStatus();
            redraw();
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
                        theStream.send(new IqGetVCard(to));
                        break;
                        
                    case 2:
                        new ContactEdit(display, c );
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
                }
                destroyView();
            }
        };
        m.addItem(new MenuItem("Client Info",0));
        m.addItem(new MenuItem("vCard",1));
        if (c.group!=SELF_INDEX) {
            m.addItem(new MenuItem("Edit",2));
            m.addItem(new MenuItem("Subscription",3));
            m.addItem(new MenuItem("Delete",4));
        }
        m.attachDisplay(display);
    }
    
    
    public void storeContact(String jid, String name, String group, boolean newContact){
        
        theStream.send(new IqQueryRoster(jid, name, group, null));
        if (newContact) theStream.send(new Presence(jid,"subscribe"));
    }
    /*private class AddContact implements MIDPTextBox.TextBoxNotify{
        public void OkNotify(String jid){
     
            //try {
                theStream.send(new IqQueryRoster(jid,null,null,null));
                theStream.send(new Presence(jid,"subscribe"));
            //} catch (Exception e) {e.printStackTrace();}
        }
    }*/
    
    
}



/////////////////////////////////////////////////////////////////////////////

/*class DeleteContact extends YesNoAlert{
    String delJid;
    DeleteContact(Display display, Contact c ){
        super(display, "Delete contact?", c.jid.getJid());
        delJid=c.jid.getJid();
    }
 
    public void yes() {
    }
 
}*/

