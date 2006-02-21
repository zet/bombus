/*
 * VirtualElement.java
 *
 * Created on 29 ���� 2005 �., 0:13
 *
 * Copyright (c) 2005, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package ui;
import javax.microedition.lcdui.*;

/**
 * ��������� ������������ �������� ������.
 * @author Eugene Stahov
 */
public interface VirtualElement {
    
    /**
     * ������ ��������
     * @return ������ �������� � ��������
     */
    public int getVHeight();
    
    /**
     * ������ ����� ���������
     * ������������ ��� ��������������.
     * ���������� null, ���� ������� �� ������� �� ������
     * @return ������ � �������� ����� ��������
     */
    public int[] getLinesHeight();
    
    /**
     * ������ ��������
     * @return ������ �������� � ��������
     */
    public int getVWidth();
    
    /**
     * 
     * ���� ���������� ���� ��������
     * ��� ������������� ������������� ����� ������� drawItem
     * @return RGB-���� ���������� ���� ��������
     */
    public int getColorBGnd(); 
    
    /**
     * ���� ������ ��������
     * ��������������� ����� ������� drawItem
     * @return RGB-���� ������ ��������
     */
    public int getColor(); 
    
    /**
     * 
     * ��������� ��������. ����� ������� ��������������� 
     * ���������� �������� ��������� <i>translate(x,y)</i> � ������� ��������
     * � ��������� <i>setClip(0,0,width,height)</i>. 
     * 
     * ��� ������������� ������������� ����� ������� drawItem
     * @param g �������� ��������� ��������
     * @param ofs �������������� �������� ������������� ����� ��������
     * @param selected ������� ���������� �������� ��������
     */
    public void drawItem(Graphics g, int ofs, boolean selected);

    /**
     * ���������� ��� ������������ ����
     */
    public String getTipString();
    /**
     * Callback-�����, �������������� ��� ���������� OK ��� ����������� �������� ��������
     */
    public void onSelect();
}
