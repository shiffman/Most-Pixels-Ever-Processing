/**
 * Default preferences for the server
 * @author Shiffman and Kairalla
 *
 */

package mpe.server;

public class MPEPrefs {

	public static int SCREENS = 2;
	public static int FRAMERATE = 30;
    
    public static boolean DEBUG = false;

	public static void setFramerate(int fr){
		if (fr > -1) FRAMERATE = fr;
        
	}
	public static void setScreens(int sc){
		if (sc > -1) SCREENS = sc;
	}
}
