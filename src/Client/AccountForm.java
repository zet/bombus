/*
 * AccountForm.java
 *
 * Created on 20 ������� 2005 �., 21:20
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package Client;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;
import locale.SR;
import ui.ConstMIDP;
import ui.controls.NumberField;

class AccountForm implements CommandListener, ItemStateListener {
    
    private final AccountSelect accountSelect;
    
    private Display display;
    private Displayable parentView;
    
    private Form f;
    private TextField userbox;
    private TextField passbox;
    private TextField servbox;
    private TextField ipbox;
    private NumberField portbox;
    private TextField resourcebox;
    private TextField nickbox;
    private ChoiceGroup register;
    
    Command cmdOk = new Command(SR.MS_OK /*"OK"*/, Command.OK, 1);
    Command cmdCancel = new Command(SR.MS_BACK /*"Back"*/, Command.BACK, 99);
    
    Account account;
    
    boolean newaccount;
    
    public AccountForm(AccountSelect accountSelect, Display display, Account account) {
	this.accountSelect = accountSelect;
	this.display=display;
	parentView=display.getCurrent();
	
	newaccount= account==null;
	if (newaccount) account=new Account();
	this.account=account;
	
	String title = (newaccount)?
	    SR.MS_NEW_ACCOUNT /*"New Account"*/:
	    (account.toString());
	f = new Form(title);
	userbox = new TextField(SR.MS_USERNAME, account.getUserName(), 32, TextField.URL); f.append(userbox);
	passbox = new TextField(SR.MS_PASSWORD, account.getPassword(), 32, TextField.URL | TextField.PASSWORD);	f.append(passbox);		passStars();
	servbox = new TextField(SR.MS_SERVER,   account.getServer(),   32, TextField.URL); f.append(servbox);
	ipbox = new TextField(SR.MS_HOST_IP, account.getHostAddr(), 32, TextField.URL);	f.append(ipbox);
	portbox = new NumberField(SR.MS_PORT, account.getPort(), 0, 65535); f.append(portbox);
	register = new ChoiceGroup(null, Choice.MULTIPLE);
	register.append(SR.MS_SSL,null);
	register.append(SR.MS_PLAIN_PWD,null);
	register.append(SR.MS_CONFERENCES_ONLY,null);
	register.append(SR.MS_REGISTER_ACCOUNT,null);
	boolean b[] = {account.getUseSSL(), account.getPlainAuth(), account.isMucOnly(), false};
	
	register.setSelectedFlags(b);
	f.append(register);
	resourcebox = new TextField(SR.MS_RESOURCE, account.getResource(), 32, TextField.ANY); f.append(resourcebox);
	nickbox = new TextField(SR.MS_ACCOUNT_NAME, account.getNickName(), 32, TextField.ANY); f.append(nickbox);
	
	f.addCommand(cmdOk);
	f.addCommand(cmdCancel);
	
	f.setCommandListener(this);
	f.setItemStateListener(this);
	
	display.setCurrent(f);
    }
    
    private void passStars() {
	if (passbox.size()==0)
	    passbox.setConstraints(TextField.URL | ConstMIDP.TEXTFIELD_SENSITIVE);
    }
    
    public void itemStateChanged(Item item) {
	if (item==userbox) {
	    String user = userbox.getString();
	    int at = user.indexOf('@');
	    if (at==-1) return;
	    //userbox.setString(user.substring(0,at));
	    servbox.setString(user.substring(at+1));
	}
	if (item==passbox) passStars();
    }
    
    public void commandAction(Command c, Displayable d) {
	if (c==cmdCancel) {
	    destroyView();
	    return;
	}
	if (c==cmdOk) {
	    boolean b[] = new boolean[4];
	    register.getSelectedFlags(b);
	    String user = userbox.getString();
	    int at = user.indexOf('@');
	    if (at!=-1) user=user.substring(0, at);
	    account.setUserName(user.trim());
	    account.setPassword(passbox.getString());
	    account.setServer(servbox.getString().trim());
	    account.setHostAddr(ipbox.getString());
	    account.setResource(resourcebox.getString());
	    account.setNickName(nickbox.getString());
	    account.setUseSSL(b[0]);
	    account.setPlainAuth(b[1]);
	    account.setMucOnly(b[2]);
	    //account.updateJidCache();
	    
	    account.setPort(portbox.getValue());
	    
	    if (newaccount) accountSelect.accountList.addElement(account);
	    accountSelect.rmsUpdate();
	    accountSelect.commandState();
	    
	    if (b[3])
		new AccountRegister(account, display, parentView); 
	    else destroyView();
	}
    }
    
    public void destroyView()	{
	if (display!=null)   display.setCurrent(parentView);
    }
}
