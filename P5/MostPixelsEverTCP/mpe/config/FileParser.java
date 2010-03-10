/**
 * The MPE Client
 * The FileParser class is used to read values
 * from the INI files for both the server and clients
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 */

package mpe.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParser {
private String fileContent = null;
private boolean fileExists = false;

/**
 * Constructs FileParser and loads in the init file from a String.  
 * @param filePathS
 */
	public FileParser(String filePathS){
		File filePath = new File(filePathS);
		FileParser(filePath);
	}
	/**
	 * Constructs FileParser and loads in the init file from a File.
	 * @param filePath
	 */
	public FileParser(File filePath){
		FileParser(filePath);
	}
	private void FileParser(File file){
		//check for ini file
		if (file.exists()) {
			try {
				fileContent = getIniFile(file);
				fileExists = true;
			} catch (IOException e) {
				print("I could not load the file " + fileContent
						+ ".  Is this the right path?");
				e.printStackTrace();
			}
			//print(fileText);
		} else {
			print("Can't find ini file.  Using defaults");
		}
	}
	
	/**
	 * returns true if ini file exists.
	 */
	public boolean fileExists(){
		return fileExists;
	}
	/**
	 * Returns the integer associated with the init attribute.  Returns -1 if attribute doesn't exist.
	 * @param attribute
	 */
	public int getIntValue(String attribute){
		int value = -1;
		if (fileExists){
			value = parseInitInt(attribute, fileContent);
		}
		return value;
	}
	
	/**
	 * Returns the String associated with the init attribute.  Returns null if attribute doesn't exist.
	 * @param attribute
	 */
	public String getStringValue(String attribute){
		String value = null;
		if (fileExists){
			value = parseInitString(attribute, fileContent);
		}
		return value;
	}
	/**
	 * Returns an Integer array associated with the init attribute.  Returns an array with a length of 2
	 * and values of -1 if attribute doesn't exist.
	 * @param attribute
	 */
	public int[] getIntValues(String attribute){
		int[] value = null;
		if (fileExists){
			value = parseInitInts(attribute, fileContent);
		} else {
			value = new int[]{-1, -1};
		}
		return value;
	}
	/**
	 * reads the ini file, strips out the comments and returns the user defined
	 * variables.
	 * @param iniFile
	 * @return
	 * @throws IOException
	 */
	private String getIniFile(File iniFile) throws IOException {
		BufferedReader in = null;
		String inputtext = "";
		// Create an input stream and file channel
		// Using first arguemnt as file name to read in
		FileInputStream fis = new FileInputStream(iniFile);
		FileChannel fc = fis.getChannel();
		// Read the contents of a file into a ByteBuffer
		ByteBuffer bb;
		bb = ByteBuffer.allocate((int) fc.size());
		fc.read(bb);
		fc.close();
		// Convert ByteBuffer to one long String
		inputtext = new String(bb.array());
		//remove comments
		String tagregex = "#.*\n";
		Pattern p2 = Pattern.compile(tagregex);
		Matcher m2 = p2.matcher(inputtext);
		inputtext = m2.replaceAll("");
		return inputtext;
	}
	
	private void print(String s){
		System.out.println("File Parser: "+s);
	}
	
	/***
	 * HELPER FUNCTIONS
	 */

	/**
	 * Parses the init file for the attribute and returns the value
	 * @param attribute
	 * @param iniFile
	 * @return value
	 */
	private String parseInitString(String attribute, String iniFile) {
		String parsedInfo = null;
		int value = -1;
		String tagregex = attribute + "=(.*?);";
		Pattern p2 = Pattern.compile(tagregex);
		Matcher m2 = p2.matcher(iniFile);
		if (m2.find()) {
			parsedInfo = m2.group(1);
		}
		return parsedInfo;
	}

	/**
	 * Parses the init file for the atribute and returns the value
	 * @param attribute
	 * @param iniFile
	 * @return value
	 */
	private int parseInitInt(String attribute, String iniFile) {
		String parsedInfo = "-1";
		int value = -1;
		String tagregex = attribute + "=(.*?);";
		Pattern p2 = Pattern.compile(tagregex);
		Matcher m2 = p2.matcher(iniFile);
		if (m2.find()) {
			parsedInfo = m2.group(1);
		}
		try {
			value = Integer.parseInt(parsedInfo);
		} catch (Exception e) {
			print("Can't parse attribute " + attribute);
		}
		return value;
	}

	/**
	 * Parses the init file for the atribute and returns the multiple int values
	 * @param attribute
	 * @param iniFile
	 * @return value
	 */
	private int[] parseInitInts(String attribute, String iniFile) {
		String parsedInfo = "-1,-1";
		int[] values = null;
		String tagregex = attribute + "=(.*?);";
		Pattern p2 = Pattern.compile(tagregex);
		Matcher m2 = p2.matcher(iniFile);
		if (m2.find()) {
			parsedInfo = m2.group(1);
		}
		String[] splitInfo = parsedInfo.split(",");
		values = new int[splitInfo.length];
		for (int i = 0; i < values.length; i++) {
			Pattern p = Pattern.compile("\\s");
			Matcher m = p.matcher(splitInfo[i]);
			splitInfo[i] = m.replaceAll("");
			try {
				values[i] = Integer.parseInt(splitInfo[i]);
			} catch (Exception e) {
				values[i] = -1;
			}
		} //values loop
		return values;
	}

}
