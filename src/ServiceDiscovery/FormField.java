/*
 * FormField.java
 *
 * Created on 5 ���� 2005 �., 20:30
 */

package ServiceDiscovery;
import javax.microedition.lcdui.*;
import com.alsutton.jabber.*;
/**
 *
 * @author Evg_S
 */
public class FormField {
    
    public String label;
    public String type;
    public String name;
    public Item formItem;
    boolean hidden;
    public boolean instructions;
    /** Creates a new instance of FormField */
    public FormField(JabberDataBlock field) {
        name=field.getTagName();
        label=name;
        String body=field.getText();
        if (name.equals("field")) {
            // x:data
            type=field.getAttribute("type");
            if (type.equals("fixed")) name="title";
            else name=field.getAttribute("var");
            label=field.getAttribute("label");
            body=field.getChildBlockText("value");
        }
        
        if ( instructions=name.equals("instructions") )
            formItem=new StringItem("Instructions", body);
        else if ( name.equals("title") )
            formItem=new StringItem(null, body);
        else
            formItem=new TextField(label, body, 64, 0);
        
        if ( name.equals("key") ) hidden=true; 
    }
    JabberDataBlock constructJabberDataBlock(){
        JabberDataBlock j=null;
        if (formItem instanceof TextField) {
            if (name.equals("registered")) return null;
            String value=((TextField)formItem).getString();
            if (type==null) {
                j=new JabberDataBlock(null, name, value);
            } else {
                // x:data
                j=new JabberDataBlock("field", null, null);
                j.setAttribute("var", name);
                j.setAttribute("type", type);
                j.addChild("value", value);
            }
        }
        return j;
    }
}
