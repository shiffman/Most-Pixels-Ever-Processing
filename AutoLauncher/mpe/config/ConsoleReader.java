package mpe.config;

//ConsoleReader.java
//Cay Horstmann, 2/14/99
//Computing Concepts with Java 2 Essentials, 2nd Edition

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/** 
 A class to read strings and numbers from an input stream.
 This class is suitable for beginning Java programmers.
 It constructs the necessary buffered reader, 
 handles I/O exceptions, and converts strings to numbers.
 @author Cay Horstmann
 */

public class ConsoleReader
{  /**
Constructs a console reader from an input stream
such as System.in
@param inStream an input stream 
 */
	public ConsoleReader(InputStream inStream)
	{  reader = new BufferedReader
		(new InputStreamReader(inStream)); 
	}

	/**
     Reads a line of input and converts it into an integer.
     The input line must contain nothing but an integer.
     Not even added white space is allowed.
     @return the integer that the user typed
	 */
	public int readInt() 
	{  String inputString = readLine();
	int n = Integer.parseInt(inputString);
	return n;
	}

	/**
     Reads a line of input and converts it into a floating-
     point number. The input line must contain nothing but 
     a nunber. Not even added white space is allowed.
     @return the number that the user typed
	 */
	public double readDouble() 
	{  String inputString = readLine();
	double x = Double.parseDouble(inputString);
	return x;
	}

	/**
     Reads a line of input. In the (unlikely) event
     of an IOException, the program terminates. 
     @return the line of input that the user typed, null
     at the end of input
	 */
	public String readLine() 
	{  String inputLine = "";

	try
	{  inputLine = reader.readLine();
	}
	catch(IOException e)
	{  System.out.println(e);
	System.exit(1);
	}

	return inputLine;
	}

	private BufferedReader reader; 
}
