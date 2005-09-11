/*
 * PrivacyForm.java
 *
 * Created on 11 �������� 2005 �., 2:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package PrivacyLists;
import javax.microedition.lcdui.*;
import java.util.*;

/**
 *
 * @author EvgS
 */
public class PrivacyForm
        implements
        CommandListener,
        ItemStateListener {
    
    private final static int POPUP=
            /*#MIDP2#*///<editor-fold>
//--            ChoiceGroup.POPUP;
            /*$MIDP2$*///</editor-fold>
            /*#!MIDP2#*///<editor-fold>
            ChoiceGroup.EXCLUSIVE;
            /*$!MIDP2$*///</editor-fold>

    private Display display;
    private Displayable parentView;
    private PrivacyItem item;
    
    private Vector targetList;
    
    Form form=new Form("Privacy rule");
    ChoiceGroup choiceAction=new ChoiceGroup("Action", POPUP, PrivacyItem.actions, null);
    ChoiceGroup choiseType=new ChoiceGroup("Type", POPUP, PrivacyItem.types, null);
    ChoiceGroup choiseStanzas=new ChoiceGroup("Stanzas", ChoiceGroup.MULTIPLE, PrivacyItem.stanzas, null);
    TextField textValue;
    TextField textOrder;
    ChoiceGroup choiceSubscr=new ChoiceGroup("Subscription", POPUP, PrivacyItem.subscrs, null);
    
    Command cmdCancel=new Command("Cancel", Command.BACK, 99);
    Command cmdOk=new Command("OK", Command.OK, 1);
    /** Creates a new instance of PrivacyForm */
    public PrivacyForm(Display display, PrivacyItem item, Vector list) {
        this.display=display;
        parentView=display.getCurrent();
        this.item=item;
        targetList=list;
        
        textValue=new TextField(null, item.value, 64, TextField.URL);
        
        form.append(choiceAction);
        choiceAction.setSelectedIndex(item.action, true);
        
        form.append(choiseType);
        
        form.append(textValue);
        choiseType.setSelectedIndex(item.type, true);
        switchType();
        
        textOrder=new TextField("Order", String.valueOf(item.order), 64, TextField.NUMERIC);
        form.append(textOrder);
        
        form.append(choiseStanzas);
        choiseStanzas.setSelectedFlags(item.stanzasSet);
        
        form.setItemStateListener(this);
        form.setCommandListener(this);
        form.addCommand(cmdOk);
        form.addCommand(cmdCancel);
        display.setCurrent(form);
        
    }
    
    private void switchType() {
        int index=choiseType.getSelectedIndex();
        try {
            if (index==2) {
                form.set(2, choiceSubscr);
            } else {
                textValue.setLabel(PrivacyItem.types[index]);
                form.set(2, textValue);
            }
        } catch (Exception e) {/* ��� ����� �� ������ ���� */ }
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c==cmdCancel) { destroyView(); return; }
        if (c==cmdOk) {
            try {
                int type=choiseType.getSelectedIndex();
                String value=textValue.getString();
                if (type==2) value=PrivacyItem.subscrs[choiceSubscr.getSelectedIndex()];
                if (value.length()==0) return;
                int order=Integer.parseInt(textOrder.getString());
                
                item.action=choiceAction.getSelectedIndex();
                item.type=type;
                item.value=value;
                item.order=order;
                choiseStanzas.getSelectedFlags(item.stanzasSet);
                
                if (!targetList.contains(item)) targetList.addElement(item); 
                destroyView();
            } catch (Exception e) {e.printStackTrace();}
        }
    }
    
    public void itemStateChanged(Item item){
        if (item==choiseType) switchType();
    }
    
    public void destroyView(){
        if (display!=null)   display.setCurrent(parentView);
    }
}
