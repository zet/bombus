/*
 * Version.java
 *
 * Created on 23 ������ 2005 �., 22:44
 */

package Info;

/**
 *
 * @author Evg_S
 */
public class Version {
    public final static String version="0.2.3.$WCREV$";
    // this string will be patched by build.xml/post-preprocess
    
    public final static String url="http://bombus.jrudevels.org";
    
    private static final String os=
/*#!MIDP1#*///<editor-fold>
                "MIDP2";
/*$!MIDP1$*///</editor-fold>
/*#MIDP1#*///<editor-fold>
//--                "MIDP1(siemens)";
/*$MIDP1$*///</editor-fold>


    public static String platform() {
        String platform=System.getProperty("microedition.platform");
        return (platform==null)? "Motorola-generic":platform;
    }

    public static String getOs() {
        return Version.os + " Platform=" +Version.platform();
    }
}
