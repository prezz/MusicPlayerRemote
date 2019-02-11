package net.prezz.mpr.mpd.connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.prezz.mpr.mpd.MpdSettings;
import android.util.Log;

public class MpdConnection {

	public static final String OK = "OK";
	public static final String ACK = "ACK";
	public static final String LIST_OK = "list_OK";
	public static final String LIST_OK_BEGIN = "command_list_ok_begin\n";
	public static final String LIST_OK_END = "command_list_end\n";
	
	private MpdSettings settings;
	private int readTimeout;
	private Socket socket;
	private BufferedWriter writer;
	private BufferedInputStream inputStream;
	private String version;

	
	public MpdConnection(MpdSettings settings) {
		this(settings, 30000);
	}
	
	public MpdConnection(MpdSettings settings, int readTimeout) {
		this.settings = settings;
		this.readTimeout = readTimeout;
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}
	
	public void connect() throws IOException {
		if (!isConnected()) {
			try {
				String host = settings.getMpdHost();
				int port = Integer.parseInt(settings.getMpdPort());
				socket = new Socket();
				socket.connect(new InetSocketAddress(host, port), 10000);
				socket.setSoTimeout(readTimeout);
				
				writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                inputStream = new BufferedInputStream(socket.getInputStream());
				
				String response = readLine();
				String[] split = response.split(" ");
				
				if (split.length != 3) {
					throw new IOException("Invalid MPD server response");
				}
				
				if (!"OK".equals(split[0])) {
					throw new IOException("Invalid MPD server response");
				}
				
				if (!"MPD".equals(split[1])) {
					throw new IOException("Invalid MPD server response");
				}
				
				version = split[2];
				
				String password = settings.getMpdPassword();
				if (password != null && !password.isEmpty()) {
					writeCommand(String.format("password %s\n", password));
					response = readLine();
					if (!"OK".equals(response)) {
						throw new IOException("Invalid MPD password");
					}
				}
			} catch (IOException ex) {
				Log.e(MpdConnection.class.getName(), "failed to establish connection to MPD server", ex);
				disconnect();
				throw ex;
			}
		}
	}
	
	public void disconnect() {
		if (isConnected()) {
			try {
				writer.close();
			} catch (IOException ex) {
				Log.e(MpdConnection.class.getName(), "error closing write", ex);
			}
			
			try {
                inputStream.close();
			} catch (IOException ex) {
				Log.e(MpdConnection.class.getName(), "error closing reader", ex);
			}

			try {
				socket.close();
			} catch (IOException ex) {
				Log.e(MpdConnection.class.getName(), "error closing connection", ex);
			}
		}

		writer = null;
        inputStream = null;
		socket = null;
	}
	
	public boolean isMinimumVersion(int major, int minor, int point) {
		try {
			if (version == null) {
				return false;
			}
			
			String[] split = version.split("\\.");
			int serverMajor = Integer.parseInt(split[0]);
			if (serverMajor < major) {
				return false;
			}
			
			int serverMinor = Integer.parseInt(split[1]);
			if ((serverMajor == major) && (serverMinor < minor)) {
				return false;
			}
			
			int serverPoint = Integer.parseInt(split[2]);
			if ((serverMajor == major) && (serverMinor == minor) && (serverPoint < point)) {
				return false;
			}
		} catch (Exception ex) {
			Log.e(MpdConnection.class.getName(), "unable to parse Mpd version number");
			return false;
		}
		
		return true;
	}
	
	public String[] writeResponseCommand(String command) throws IOException {
		return writeResponseCommand(command, null);
	}
	
	public String[] writeResponseCommand(String command, Filter filter) throws IOException {
		if (command != null && !command.isEmpty() && isConnected()) {
			writer.write(command);
			writer.flush();
			return readResponse(OK, ACK, filter);
		}
		
		return new String[0];
	}
	
	public String[][] writeResponseCommandList(String[] commands) throws IOException {
		return writeResponseCommandList(commands, null);
	}
	
	public String[][] writeResponseCommandList(String[] commands, Filter filter) throws IOException {
		writeCommandList(commands);
		
		String[][] result = new String[commands.length][];
		for (int i = 0; i < commands.length; i++) {
			result[i] = readResponse(LIST_OK, ACK, filter);
		}
		
		//empty buffer
		readResponse(OK, ACK, RejectAllFilter.INSTANCE);
		
		return result;
	}

	public void writeCommand(String command) throws IOException {
		if (command != null && !command.isEmpty() && isConnected()) {
			writer.write(command);
			writer.flush();
		}
	}

	public void writeCommandList(String[] commands) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(LIST_OK_BEGIN);
		for (String c : commands) {
			stringBuilder.append(c);
		}
		stringBuilder.append(LIST_OK_END);
		
		writeCommand(stringBuilder.toString());
	}
	
	public String[] readResponse(Filter filter) throws IOException {
		return readResponse(OK, ACK, filter);
	}

	public String readLine() throws IOException {
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

	    int next;
	    while ((next = inputStream.read()) != -1) {
	        if (next == '\n') {
	            break;
            }
            bytes.write(next);
        }

	    return (bytes.size() == 0 && next == -1) ? null : new String(bytes.toByteArray(), "UTF-8");
    }

    public int readBinary(byte[] buffer, int offset, int length) throws IOException {
	    int total = 0;

		while (length > 0) {
            int read = inputStream.read(buffer, offset, length);
            if (read == -1) {
                break;
            }

            total += read;
			offset += read;
			length -= read;
		}

		return total;
    }

	private String[] readResponse(String successTerminator, String errorTerminator, Filter filter) throws IOException {
		if (isConnected()) {
			List<String> buffer = new LinkedList<String>();
			String line = null;
			while ((line = readLine()) != null) {
				if (line.startsWith(successTerminator)) {
					break;
				}
				if (line.startsWith(errorTerminator)) {
					throw new IOException("Error reading MPD response: " + line);
				}
				if (filter == null || filter.accepts(line)) {
					buffer.add(line);
				}
			}
			return buffer.toArray(new String[buffer.size()]);
		}
		
		return null;
	}
}
