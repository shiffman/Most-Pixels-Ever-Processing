/**
 * Default preferences for the server
 * @author Shiffman and Kairalla
 *
 */

package mpe.server;

public class MPEPrefs {

	public static int NUMREQUIREDCLIENTS = 1;
	public static int FRAMERATE = 30;
    
    public static boolean VERBOSE = false;
    
    public static boolean WAITFORALL = false;

	public static void setFramerate(int fr){
		if (fr > -1) FRAMERATE = fr;
        
	}
	public static void setRequiredClients(int sc){
		if (sc > -1) NUMREQUIREDCLIENTS = sc;
	}
}
