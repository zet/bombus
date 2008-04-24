/*
 * Account.java
 *
 * Created on 19.03.2005, 21:52
 *
 * Copyright (c) 2005-2007, Eugene Stahov (evgs), http://bombus-im.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * You can also redistribute and/or modify this program under the
 * terms of the Psi License, specified in the accompanied COPYING
 * file, as published by the Psi Project; either dated January 1st,
 * 2005, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package Client;
import Info.Version;
import com.alsutton.jabber.datablocks.Presence;
import images.RosterIcons;
import io.NvStorage;
import java.util.*;
import java.io.*;
import javax.microedition.midlet.MIDlet;
import midlet.Bombus;
import ui.Colors;
import ui.IconTextElement;
import ui.ImageList;
import javax.microedition.rms.*;
import javax.microedition.lcdui.*;
import Client.Roster;
import com.alsutton.jabber.*;

/**
 *
 * @author Eugene Stahov
 */
public class Account extends IconTextElement{
    
    public final static String storage="accnt_db";
            
    private String userName;
    private String password;
    private String server;
    private String hostAddr;
    private int port=5222;
    public boolean active;
    private boolean useSSL;
    private boolean compression;
    private boolean plainAuth;
    private boolean mucOnly;
    
    private String nick="";
    private String resource=Version.NAME;
    
    private boolean enableProxy;
    private String proxyHostAddr;
    private int proxyPort;

    public int keepAlivePeriod=200;
    public int keepAliveType=1;
    
    //private String jid;
        
    /** Creates a new instance of Account */
    public Account() {
        super(RosterIcons.getInstance());
    }
    
    public static Account loadAccount(boolean launch){
	StaticData sd=StaticData.getInstance();
	Account a=sd.account=Account.createFromStorage(Config.getInstance().accountIndex);
	if (a!=null) {
            sd.roster.logoff();
	    sd.roster.resetRoster();
            if (launch) sd.roster.sendPresence(Presence.PRESENCE_ONLINE);
        }
        return a;
    }

    public static Account createFromJad(){
        Account a=new Account();
        MIDlet m=Bombus.getInstance();
        try {
            a.userName=m.getAppProperty("def_user");
            a.password=m.getAppProperty("def_pass");
            a.server=m.getAppProperty("def_server");
            a.hostAddr=m.getAppProperty("def_ip");
        } catch (Exception e) { }
            if (a.server==null) return null;
        return a;
    }
    
    public static Account createFromDataInputStream(DataInputStream inputStream){
        
        int version=0;
        Account a=new Account();
        try {
            version    = inputStream.readByte();
            a.userName = inputStream.readUTF();
            a.password = inputStream.readUTF();
            a.server   = inputStream.readUTF();
            a.hostAddr = inputStream.readUTF();
            a.port     = inputStream.readInt();

            a.nick     = inputStream.readUTF();
            a.resource = inputStream.readUTF();
	    
            // version используется для корректной работы midp1 - аккаунты
            // хранятся в файле без разделения на записи
            if (version>=2) a.useSSL=inputStream.readBoolean();
            if (version>=3) a.plainAuth=inputStream.readBoolean();
            
	    if (version>=4) a.mucOnly=inputStream.readBoolean();
            
            if (version>=5) {
                a.setEnableProxy(inputStream.readBoolean());
                a.setProxyHostAddr(inputStream.readUTF());
                a.setProxyPort(inputStream.readInt());
            }
            
            if (version>=6) 
                a.compression=inputStream.readBoolean();

            if (version>=7) {
                a.keepAliveType=inputStream.readInt();
                a.keepAlivePeriod=inputStream.readInt();
            }
            
        } catch (IOException e) { e.printStackTrace(); }
            
        return (a.userName==null)?null:a;
    }

    public String toString(){
        StringBuffer s=new StringBuffer();
        if (nick.length()!=0)
            s.append(nick);
        else {
            s.append(userName);
            s.append('@');
            s.append(server);
        }
        s.append('/');
        s.append(resource);
        return s.toString();
        //jid=userName+'@'+server+'/'+resource;
    }
    public String getJid(){
        return userName+'@'+server+'/'+resource;
    }

    public String getTipString() { return getJid(); }
    public String getBareJid(){
        return userName+'@'+server;
    }
    
    public static Account createFromStorage(int index) {
        Account a=null;
        DataInputStream is=NvStorage.ReadFileRecord(storage, 0);
        if (is==null) return null;
        try {
            do {
                if (is.available()==0) {a=null; break;}
                a=createFromDataInputStream(is);
                //a.updateJidCache();
                index--;
            } while (index>-1);
            is.close();
        } catch (Exception e) { e.printStackTrace(); }
        return a;
    }
    
    public void saveToDataOutputStream(DataOutputStream outputStream){
        
        if (hostAddr==null) hostAddr="";
        if (proxyHostAddr==null) proxyHostAddr="";
        
        try {
            outputStream.writeByte(7);
            outputStream.writeUTF(userName);
            outputStream.writeUTF(password);
            outputStream.writeUTF(server);
            outputStream.writeUTF(hostAddr);
            outputStream.writeInt(port);
            
            outputStream.writeUTF(nick);
            outputStream.writeUTF(resource);

            outputStream.writeBoolean(useSSL);
            outputStream.writeBoolean(plainAuth);
	    
	    outputStream.writeBoolean(mucOnly);
            
            outputStream.writeBoolean(enableProxy);
            outputStream.writeUTF(proxyHostAddr);
            outputStream.writeInt(proxyPort);
            
            outputStream.writeBoolean(compression);
            
            outputStream.writeInt(keepAliveType);
            outputStream.writeInt(keepAlivePeriod);
	    
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    //public void onSelect(){ }
    //public String toString(){ return jid; }
    public int getColor(){ return Colors.LIST_INK; }
    
    protected int getImageIndex() {return active?0:5;}
    public void onSelect(){};

    public String getUserName() { return userName;  }
    public void setUserName(String userName) { this.userName = userName;  }

    public String getPassword() {  return password;  }
    public void setPassword(String password) { this.password = password;  }

    public String getServer() { return server; }
    public String getHostAddr() { return hostAddr; }
    
    public void setServer(String server) { this.server = server; }

    public void setHostAddr(String hostAddr) { this.hostAddr = hostAddr; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public boolean getUseSSL() { return useSSL; }
    public void setUseSSL(boolean ssl) { this.useSSL = ssl; }

    public boolean getPlainAuth() { return plainAuth; }
    public void setPlainAuth(boolean plain) { this.plainAuth = plain; }
    
    public String getResource() { return resource;  }
    public void setResource(String resource) { this.resource = resource;  }

    public String getNickName() { return (nick.length()==0)?getUserName():nick;  }
    public String getNick() { return (nick.length()==0)? null:nick;  }

    public void setNick(String nick) { this.nick = nick;  }

    boolean isMucOnly() { return mucOnly; }
    public void setMucOnly(boolean mucOnly) {  this.mucOnly = mucOnly; }

    public JabberStream openJabberStream() throws java.io.IOException{

        String proxy=null;
        
        String host=this.server;
        int port=this.port;
        
        if (hostAddr!=null) if (hostAddr.length()>0)
            host=hostAddr;
          
        else {
            io.DnsSrvResolver dns=new io.DnsSrvResolver();
            if (dns.getSrv(server)) {
                host=dns.getHost();
                port=dns.getPort();
            } 
        }

	StringBuffer url=new StringBuffer();

        url.append(host).append(':').append(port);

        if (!isEnableProxy()) {
	    url.insert(0, (useSSL)?"ssl://":"socket://");
        } else {
//#if HTTPPOLL
//#             proxy=getProxyHostAddr();
//#elif HTTPCONNECT
//#             proxy="socket://" + getProxyHostAddr() + ':' + getProxyPort();
//#endif            
        }
        return new JabberStream(  getServer(), url.toString(), true, proxy);    
    }

    public boolean isEnableProxy() {
        return enableProxy;
    }

    public void setEnableProxy(boolean enableProxy) {
        this.enableProxy = enableProxy;
    }

    public String getProxyHostAddr() {
        return proxyHostAddr;
    }

    public void setProxyHostAddr(String proxyHostAddr) {
        this.proxyHostAddr = proxyHostAddr;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean useCompression() { return compression; }

    public void setUseCompression(boolean value) { this.compression = value;  }

    public boolean useGoogleToken() {
        if (useSSL) return false;
        /*if (hostAddr==null) return false;
        if (hostAddr.indexOf("google")<0) return false; */
        return (server.startsWith("gmail.com"));
    }
}
