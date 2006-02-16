/*
 * FontCache.java
 *
 * Created on 5 ������� 2006 �., 3:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui;

import javax.microedition.lcdui.Font;

/**
 *
 * @author Evg_S
 */
public class FontCache {

    private static Font normal;
    private static Font bold;
    private static Font msgFont;
    private static Font balloonFont;
    
    public static int rosterFontSize=Font.SIZE_MEDIUM;
    public static int msgFontSize=Font.SIZE_MEDIUM;
    public static int balloonFontSize=Font.SIZE_SMALL;

    public static Font getRosterNormalFont() {
        if (normal==null) {
            normal=Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, rosterFontSize);
        }
        return normal;
    }
    
    public static Font getRosterBoldFont() {
        if (bold==null) {
            bold=Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, rosterFontSize);
        }
        return bold;
    }

    public static Font getMsgFont() {
        if (msgFont==null) {
            msgFont=Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, msgFontSize);
        }
        return msgFont;
    }

    public static Font getBalloonFont() {
        if (balloonFont==null) {
            balloonFont=Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, balloonFontSize);
        }
        return balloonFont;
    }

    public static void resetCache() {
        normal=bold=msgFont=balloonFont=null;
    }
    
}
