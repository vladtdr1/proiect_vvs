package main.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.java.ServerThread;
import main.java.exceptions.FileAccessPermissionException;
import main.java.exceptions.FilePermissionException;
import main.java.exceptions.MyFileNotFoundException;

public class ServerThreadTest {
	
	private static ServerThread serverThread;
	private static final int testPort = 12345;
	private static final String testFilePath = "testFiles/";
	private static final String testText = "HELLOWORLD";
	
	private static Socket readableSocket;
	private ByteArrayOutputStream socketOutput;
	
	@BeforeAll
	public static void setup()
	{
		try {
			serverThread = new ServerThread(testPort, testFilePath);
		} catch(Exception e){
			e.printStackTrace();
		}
		readableSocket = mock(Socket.class);
	}
	
	@BeforeEach
	public void socketSetup() throws IOException {
		//so we can read data that is sent to socket. We have no test cases that need to fake send data to socket yet.
		
		socketOutput = new ByteArrayOutputStream();
        when(readableSocket.getOutputStream()).thenReturn(socketOutput);
	}
	
	@Ignore
	@Test
	public void testRunMethod() {
		//must fake data transfers between socket and server ( will need to mock socketInput too )
	}
	
	@Ignore
	@Test
	public void testRunMethod_SocketClose() {
		//test the quick and painful death of the server thread on socket closing.
	}
	
	
	@Test
	public void testSendContent(){
		String contentToSend = testText + testText;
		try {
			serverThread.sendContent(readableSocket, contentToSend, 'h');
			assertTrue(socketOutput.toString().contains(contentToSend));
			//System.out.println(socketOutput.toString());
		}catch(Exception e)
		{
			e.printStackTrace();
			fail(e);
		}
	}
	
	@Test
	public void testFindFileInDirectory(){
		Path hiddenFilePath = Paths.get(testFilePath + "hide/here/findthis.html");
		try {
			Path p = hiddenFilePath.getParent();
			if(p!=null)
				Files.createDirectories(p);
			if(!Files.exists(hiddenFilePath))
				Files.createFile(hiddenFilePath);
		
			Path p2 = hiddenFilePath.getFileName();
			if(p2!=null) {
				File file = serverThread.findFileInDirectory(p2.toString());
				assertTrue(file.exists());
			}
		
		} catch (IOException e1) {
			fail("Could not create file.");
			e1.printStackTrace();
		} catch (FilePermissionException | MyFileNotFoundException e) {
			fail("File not found => exception.");
			e.printStackTrace();
		} finally {
			try {
				Files.deleteIfExists(hiddenFilePath);
			} catch (IOException e) {
				System.out.println("could not delete file.");
				e.printStackTrace();
			}
		}
		
	}
	
	@Test
	public void testFindFileInDirectory_DoesNotExist(){
		Path hiddenFilePath = Paths.get(testFilePath + "hide/here/findthis.html");
		
		try {
			Files.deleteIfExists(hiddenFilePath);
			
			assertThrows(MyFileNotFoundException.class, ()->{
				serverThread.findFileInDirectory(hiddenFilePath.getFileName().toString());
			});
		
		} catch (IOException e1) {
			fail("Could not create file.");
			e1.printStackTrace();
		} 
	}
	
	@Test
	public void loadContentFromPath(){
		Path hiddenFilePath = Paths.get(testFilePath + "hide/here/findthis.html");
		
		try {
			Path p = hiddenFilePath.getParent();
			if(p!=null)
				Files.createDirectories(p);
			if(!Files.exists(hiddenFilePath))
				Files.createFile(hiddenFilePath);
			
			FileWriter myWriter = new FileWriter(hiddenFilePath.toFile());
		      myWriter.write(testText);
		      myWriter.close();
			hiddenFilePath.toFile();
			
			assertEquals(testText,serverThread.loadContentFromPath(hiddenFilePath.toFile()));
		
		} catch (IOException e1) {
			fail("Could not create/write to file.");
			e1.printStackTrace();
		} catch (FileAccessPermissionException e) {
			fail("Could not access file.");
			e.printStackTrace();
		} 
	}
	
	@Test
	public void loadContentFromPath_DoesNotExist(){
		Path hiddenFilePath = Paths.get(testFilePath + "hide/here/findthis.html");
		
		try {
			Files.deleteIfExists(hiddenFilePath);
			
			assertThrows(FileAccessPermissionException.class, ()->{
				serverThread.loadContentFromPath(hiddenFilePath.toFile());
			});
		
		} catch (IOException e1) {
			fail("Could not delete file.");
			e1.printStackTrace();
		} 
	}
	
	@Ignore
	@Test
	public void testHandleFirstLine(){
		//success scenario of treating a request
	}
	
	@Test
	public void testHandleFirstLine_UnknownFormatException(){
		try {
			serverThread.handleFirstLine(readableSocket, "this is not the right format");
			assertEquals("HTTP/1.1 200 OK\r\n"
					+ "Content-Length: 11\r\n"
					+ "\r\n"
					+ "BAD_REQUEST\r\n"
					+ "\r\n"
					+ "",readableSocket.getOutputStream().toString());
		} catch (IOException e) {
			fail("Could not read data from socket.");
			e.printStackTrace();
		}
	}
	
	@Ignore
	@Test
	public void testHandleFirstLine_MyFileNotFoundException(){
		try {
			serverThread.handleFirstLine(readableSocket, "GET " + testFilePath + "fasdfsdfasdfasdasdfasdf   ");
			assertEquals("HTTP/1.1 200 OK\r\n"
					+ "Content-Length: 9\r\n"
					+ "\r\n"
					+ "NOT_FOUND\r\n"
					+ "\r\n"
					+ "",readableSocket.getOutputStream().toString());
		} catch (IOException e) {
			fail("Could not read data from socket.");
			e.printStackTrace();
		}
	}
	
	@Ignore
	@Test
	public void testHandleFirstLine_FileAccessPermissionException(){
		//WILL NEED TO CREATE A FILE WITH NO READ PERMISSIONS FOR THIS. IDK HOW YET.
	}
	
	
}
