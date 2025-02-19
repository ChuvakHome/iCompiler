package ru.itmo.compiler.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

public class TextReader {
	public static final int BUFFER_SIZE = 128;
	
	private int pos = 0;
	
	private char[] buffer = new char[BUFFER_SIZE];
	private int len = 0;
	
	boolean ended;
	
	private BufferedReader reader;
	
	public TextReader(InputStream in) {
		reader = new BufferedReader(new InputStreamReader(in));
	}
	
	public TextReader(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}
	
	public TextReader(String s) {
		reader = new BufferedReader(new StringReader(s));
	}
	
	public char lookupChar() {
		if (ended)
			return 0;
		
		if (pos < len)
			return buffer[pos];
		
		try {
			char[] temp = new char[1];
			int count = reader.read(temp);
			
			if (count > 0) {
				pos = pos >= buffer.length ? 0 : pos;
				
				buffer[pos] = temp[0];
				len = pos + 1 + reader.read(buffer, pos + 1, buffer.length - pos - 1);
			}
			else {
				ended = true;
				
				return 0;
			}
			
			return buffer[pos];
		} catch (IOException e) {
			e.printStackTrace();
			
			ended = true;
		}
			
		return 0;
	}
	
	public void toNextChar() {
		if (!ended && pos < len)
			pos++;
	}
	
	public char nextChar() {
		char ch = lookupChar();
		toNextChar();
		
		return ch;
	}
	
	public void flush() {
		if (pos < len)
			System.arraycopy(buffer, pos, buffer, 0, len - pos);

		len = Math.max(len - pos, 0);
		pos = 0;
	}
	
	public int backtrack() {
		return backtrack(1);
	}
		
	public int backtrack(int n) {
		int newpos = Math.max(pos - n, 0);
		int count = pos - newpos;
		pos = newpos;
		
		ended = pos >= len;
		
		return count;
	}
	
	public boolean isEndReached() {
		return ended;
	}
}
