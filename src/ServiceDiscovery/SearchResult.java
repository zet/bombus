/*
 * SearchResult.java
 *
 * Created on 10 ���� 2005 �., 21:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package ServiceDiscovery;
import java.util.*;
import javax.microedition.lcdui.*;
import ui.*;
import com.alsutton.jabber.*;
import com.alsutton.jabber.datablocks.*;
import Client.*;

/**
 *
 * @author EvgS
 */
public class SearchResult
        extends VirtualList
        implements CommandListener {
    
    StaticData sd=StaticData.getInstance();
    private Command cmdBack=new Command("Back", Command.BACK, 98);
    private Command cmdAdd=new Command("Add", Command.SCREEN, 1);
    
    private Vector items;
    private Vector vcards;
    boolean xData;
    
    /** Creates a new instance of SearchResult */
    public SearchResult(Display display, JabberDataBlock result) {
        super(display);
        
        String service=result.getAttribute("from");
        
        setTitleImages(sd.rosterIcons);
        
        createTitle(2, null, service);
        
        addCommand(cmdBack);
        setCommandListener(this);
        
        items=new Vector();
        vcards=new Vector();
        
        JabberDataBlock query=result.getChildBlock("query");
        if (query==null) return;
        
        JabberDataBlock x=query.getChildBlock("x");
        if (x!=null) { query=x; xData=true; }
        
        for (Enumeration e=query.getChildBlocks().elements(); e.hasMoreElements(); ){
            JabberDataBlock child=(JabberDataBlock) e.nextElement();
            if (child.getTagName().equals("item")) {
                String jid=null;
                Form vcard=new Form(null);
                if (!xData) { jid=child.getAttribute("jid"); }
                // ���� item
                for (Enumeration f=child.getChildBlocks().elements(); f.hasMoreElements(); ){
                    JabberDataBlock field=(JabberDataBlock) f.nextElement();
                    String name;
                    String value;
                    if (xData) {
                        name=field.getAttribute("var");
                        value=field.getTextForChildBlock("value");
                    } else {
                        name=field.getTagName();
                        value=field.getText();
                    }
                    if (name.equals("jid")) jid=value;
                    if (value!=null) if (value.length()>0)
                        //vcard.append(new StringItem(name,value+"\n"));
                        vcard.append(new TextField(name,value, 60, 0));
                }
                Contact serv=new Contact(null,jid,0,null);
                serv.group=Roster.NIL_INDEX;
                items.addElement(serv);
                vcard.setTitle(jid);
                vcards.addElement(vcard);
            }
        }
        
        addCommand(cmdAdd);
    }
    
    public int getItemCount(){ return items.size();}
    public VirtualElement getItemRef(int index) { return (VirtualElement) items.elementAt(index);}

    public void commandAction(Command c, Displayable d){
        if (c==cmdAdd){
            destroyView();
            new ContactEdit(display, (Contact)getSelectedObject());
            return;
        }
        
        if (c==cmdBack){ 
            if (d!=this) display.setCurrent(this); else destroyView(); 
        }
    }
    
    public void eventOk(){
        Form f=(Form)vcards.elementAt(cursor);
        
        display.setCurrent(f);
        f.setCommandListener(this);
        f.addCommand(cmdBack);
        f.addCommand(cmdAdd);
    }
}
