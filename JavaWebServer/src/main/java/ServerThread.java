package main.java;

import static main.java.ContentBuilder.buildContent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.ContentBuilder.CONTENTTYPE;
import main.java.exceptions.FileAccessPermissionException;
import main.java.exceptions.FilePermissionException;
import main.java.exceptions.MyFileNotFoundException;
import main.java.exceptions.UnknownFormatException;

public class ServerThread extends Thread {
	private final static Logger LOGGER = LoggerFactory.getLogger(ServerThread.class);
	private int port;
	private ServerSocket serverSocket;
	
	public static String basePath = "contentRoot/";
	public static String maintenancePath = "maintenance/"; 
	public static boolean inMaintenance;
	private final String[] defaultPaths = {"index.html", "index.htm"}; //add more if needed

	public ServerThread(int port) throws IOException {
		this.port = port;
		this.serverSocket = new ServerSocket(this.port);
	}
	
	public ServerThread(int port, String basePath) throws IOException {
		this.port = port;
		this.basePath = basePath;
		this.serverSocket = new ServerSocket(this.port);
	}

	public void logInfo(String message, Object... arguments)
	{
		if(LOGGER.isInfoEnabled()) LOGGER.info(message, new Object[] {arguments});
	}
	
	public void logDebug(String message, Object... arguments)
	{			
		if(LOGGER.isDebugEnabled()) LOGGER.debug(message, new Object[] {arguments});
	}

	public void logError(String message, Object... arguments)
	{
		if(LOGGER.isErrorEnabled()) LOGGER.error(message, new Object[] {arguments});
	}
	
	@Override
	public void run() {
		logInfo("Server started");
		try {
			while (serverSocket.isBound() && !serverSocket.isClosed()) {
					
				logInfo(" Waiting for connection on port {}",  port);
				Socket socket = serverSocket.accept();
				logInfo(" Connection accepted to address {}", socket.getInetAddress());
				
				PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				String buffer = inputReader.readLine();
				
				handleFirstLine(socket, buffer); //first line of request contains the path
				
				while ((buffer = inputReader.readLine()) != null) {
					if (buffer.trim().equals("")) break;
					logDebug(" request from user: {}", buffer);
				}
				
				while (socket.getKeepAlive()) {
					
				}
				
				outputWriter.close();
				inputReader.close();
			}

		} catch (IOException e) {
			logError("An unexpected error occured {}", e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					logError("Legends will be told of the surviving socket. {}", e);
				}
			}
		}
	}

	public void handleFirstLine(Socket socket, String readLine) {
		if (readLine != null) {
			logDebug(" First line of request from user: {}", readLine);
			try
			{
				String path = readRelativePathFromRequest(readLine); //UnknownFormat
				
				File contentFile = findFileInDirectory(path); //FilePermission, MyFileNotFound
				
				String content = loadContentFromPath(contentFile); //FileAccessPermission
				
				if(contentFile.getPath().endsWith(".html") || contentFile.getPath().endsWith(".htm"))
					sendContent(socket, buildContent(CONTENTTYPE.OK,content),'h'); //h=html
				else
					if(contentFile.getPath().endsWith(".jpg"))
						sendContent(socket, buildContent(CONTENTTYPE.OK,content),'j'); //j=jpg
				
				logDebug("Webpage loaded successfully");
				
			} catch(UnknownFormatException e) {
				logError("Request with unknown format received from user {}",  e);
				sendContent(socket, buildContent(CONTENTTYPE.BAD_REQUEST), 'f'); //f=fail
			} catch(FilePermissionException e) {
				logError("Request with unknown format received from user {}",  e);
				sendContent(socket, buildContent(CONTENTTYPE.UNAUTHORISED), 'f');
			} catch(MyFileNotFoundException e) {
				logError("Request with unknown format received from user {}",  e);
				sendContent(socket, buildContent(CONTENTTYPE.NOT_FOUND), 'f');
			} catch(FileAccessPermissionException e) {
				logError("Request with unknown format received from user {}",  e);
				sendContent(socket, buildContent(CONTENTTYPE.UNAUTHORISED), 'f');
			}
		}
	}

	 private String readRelativePathFromRequest(String firstLine) throws UnknownFormatException {
		//format: "<method> <path> ..."
		try {
			if (firstLine.startsWith("GET")) {
				return firstLine.substring(firstLine.indexOf(' ') + 1, firstLine.indexOf(' ') + 1
						+ firstLine.substring((firstLine.indexOf(' ')+1)).indexOf(' '));
			} else {
				throw new UnknownFormatException();
			}
		} catch(Exception e) {
			logError("Unknown request format.");
			throw new UnknownFormatException();
		}
	}

	public void sendContent(Socket socket, String content, char type) { 
		//ALL respnse types are  HTTP/1.1 200 OK
		//TODO Fix that ^^^^
		try {
			OutputStream outputStream = socket.getOutputStream();

			final String CRLF = "\r\n"; // string used in http protocol to terminate a line
			String response;
			if( type=='h' || type=='f' )
				response = "HTTP/1.1 200 OK" + CRLF + // Status Line : HTTTP_VERSION RESPONSE_CODE RESPONSE_MESSAGE
						"Content-Length: " + content.getBytes().length + CRLF + // HEADER
						CRLF + content + CRLF + CRLF;
			else
			{
				//THIS DOES NOT WORK BECAUSE READLINE IS NOT QUITE OK IN A BINARY FILE (CR and LF are removed, others are changed)
				response = "HTTP/1.1 200 OK" + CRLF + // Status Line : HTTTP_VERSION RESPONSE_CODE RESPONSE_MESSAGE
				"Content-Type: image/png" + CRLF +
				"Content-Length: " + content.getBytes().length + CRLF + // HEADER
				CRLF + content + CRLF + CRLF;
			}	
			outputStream.write(response.getBytes());

			logInfo("Connection Processing Finished.");
		} catch (IOException e) {
			logError("Problem with communication", e);
		}
	}

	public File findFileInDirectory(String filename) throws FilePermissionException, MyFileNotFoundException {

		if(	filename.trim().equals("/") || filename.trim().equals("") || inMaintenance )
		{
			for(String defaultPath : defaultPaths)
				if(Files.exists(Paths.get((inMaintenance ? maintenancePath : basePath)+defaultPath))) {
					logDebug("Found default file: {}", defaultPath);
					return Paths.get(((inMaintenance ? maintenancePath : basePath)+defaultPath)).toFile();
				}
			logError("No default file found.");
			throw new MyFileNotFoundException();
		}
		else
		{
			String filenameDecoded = filename.replace("%20", " ").replace("/", "\\");
			List<Path> filePaths;
			try {
				filePaths = Files.walk(Paths.get(basePath)).filter(Files::isRegularFile)
						.filter(path -> path.toString().endsWith(filenameDecoded)).collect(Collectors.toList());
			} catch (IOException e) {
				logError("Problem searching through files.", e);
				throw new FilePermissionException();
			}
			if (filePaths.size() == 0) {
				logError("Could not find file.");
				throw new MyFileNotFoundException();
			} else {
				return filePaths.get(0).toFile();
			}
		}
	}

	public String loadContentFromPath(File file) throws FileAccessPermissionException{
		
		StringBuilder fileContent = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				fileContent.append(str);
			}
			in.close();
		} catch (IOException e) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String str;
				while ((str = in.readLine()) != null) {
					fileContent.append(str);
				}
				in.close();
			} catch (IOException ex) {
				logError("Failed to read from file", ex);
				throw new FileAccessPermissionException();
			} 
		}
		return fileContent.toString();

	}
	
	public void closeSocket() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
