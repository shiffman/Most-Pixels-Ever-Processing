/**
 * Default preferences for the server
 * @author Shiffman and Kairalla
 *
 */

package mpe.server;

public class mpePrefs {

	public static int SCREENS = 2;
	public static int FRAMERATE = 25;
	public static int M_WIDTH = 640;
	public static int M_HEIGHT = 240;
    
    public static boolean DEBUG = false;

	public static void setFramerate(int fr){
		if (fr > -1) FRAMERATE = fr;
        
	}
	public static void setScreens(int sc){
		if (sc > -1) SCREENS = sc;
	}
	public static void setMasterDimensions(int mw, int mh){
		if (mw > -1 && mh > -1) {
			M_WIDTH = mw;
			M_HEIGHT = mh;
		}
	}
}
