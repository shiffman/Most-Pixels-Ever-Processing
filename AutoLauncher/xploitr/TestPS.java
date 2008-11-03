package xploitr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPS {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {


			/*Process p = Runtime.getRuntime().exec("ps -A | grep FullScreenDemo.app");
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String str = null;
		while ((str = in.readLine()) != null)  {
			if (str.indexOf("bigscreens") > 0) {
				System.out.println(str);
				break;
			}
		}*/
		String str = " 3199  ??";
		Pattern p = Pattern.compile("(\\d++)\\s??");
		Matcher m = p.matcher(str);
		m.find();
		String id = m.group(1);//str.substring(0,str.indexOf("  "));
		System.out.println(id.trim());
		//Runtime rt = Runtime.getRuntime();
		//rt.exec("kill " + id);
	}

}
