package main.java.gui;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import main.java.ServerThread;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class GuiApplicationWindow {

	private JFrame frmJavaWebServer;
	private JTextField serverPathField;
	private JTextField maintenancePathField;
	private JTextField portField;
	
	private boolean serverRunning;
	private ServerThread serverThread;
	public static final int defaultPort = 10008;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiApplicationWindow window = new GuiApplicationWindow();
					window.frmJavaWebServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GuiApplicationWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmJavaWebServer = new JFrame();
		frmJavaWebServer.setTitle("Java web server");
		frmJavaWebServer.setBounds(100, 100, 357, 187);
		frmJavaWebServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmJavaWebServer.getContentPane().setLayout(null);
		
		JButton startStopButton = new JButton("Start server");
		
		startStopButton.setBounds(0, 0, 170, 49);
		frmJavaWebServer.getContentPane().add(startStopButton);
		
		JCheckBox maintenanceCheckBox = new JCheckBox("Maintenance");
		maintenanceCheckBox.setEnabled(false);
		maintenanceCheckBox.setBounds(170, 0, 170, 49);
		maintenanceCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		frmJavaWebServer.getContentPane().add(maintenanceCheckBox);
		
		serverPathField = new JTextField();
		serverPathField.setBounds(0, 49, 170, 49);
		serverPathField.setHorizontalAlignment(SwingConstants.CENTER);
		serverPathField.setToolTipText("Server path");
		frmJavaWebServer.getContentPane().add(serverPathField);
		serverPathField.setColumns(10);
		
		maintenancePathField = new JTextField();
		maintenancePathField.setBounds(170, 49, 170, 49);
		maintenancePathField.setHorizontalAlignment(SwingConstants.CENTER);
		maintenancePathField.setToolTipText("Maintenance path");
		maintenancePathField.setColumns(10);
		frmJavaWebServer.getContentPane().add(maintenancePathField);
		
		JLabel statusLabel = new JLabel("Status: Idle");
		statusLabel.setBounds(0, 98, 170, 49);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frmJavaWebServer.getContentPane().add(statusLabel);
		
		portField = new JTextField();
		portField.setBounds(258, 112, 50, 20);
		frmJavaWebServer.getContentPane().add(portField);
		portField.setColumns(10);
		
		JLabel lblNewLabel_1_1 = new JLabel("port");
		lblNewLabel_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel_1_1.setBounds(198, 115, 50, 14);
		frmJavaWebServer.getContentPane().add(lblNewLabel_1_1);
		
		if (serverPathField.getText().length() == 0) {  
		    serverPathField.setText("contentRoot/");  
		    serverPathField.setForeground(new Color(150, 150, 150));  
		}  

		serverPathField.addFocusListener(new FocusListener() {  

			@Override
			public void focusGained(FocusEvent e) { 
				if (serverPathField.getText().equals("contentRoot/")) {
			        serverPathField.setText("");  
			        serverPathField.setForeground(new Color(50, 50, 50));  
				}
		    }  

			@Override
			public void focusLost(FocusEvent e) {
				if (serverPathField.getText().length() == 0) {  
		            serverPathField.setText("contentRoot/");  
		            serverPathField.setForeground(new Color(150, 150, 150));  
		        }
				ServerThread.basePath=serverPathField.getText();
			}
		});
		
		if (maintenancePathField.getText().length() == 0) {  
			maintenancePathField.setText("maintenance/");  
			maintenancePathField.setForeground(new Color(150, 150, 150));  
		}  

		maintenancePathField.addFocusListener(new FocusListener() {  

			@Override
			public void focusGained(FocusEvent e) { 
				if (maintenancePathField.getText().equals("maintenance/")) {
					maintenancePathField.setText("");  
					maintenancePathField.setForeground(new Color(50, 50, 50));  
				}
		    }  

			@Override
			public void focusLost(FocusEvent e) {
				if (maintenancePathField.getText().length() == 0) {  
					maintenancePathField.setText("maintenance/");  
		            maintenancePathField.setForeground(new Color(150, 150, 150));  
		        }
				ServerThread.maintenancePath=maintenancePathField.getText();
			}
		});
		
		
		if (portField.getText().length() == 0) {  
			portField.setText("10008");  
			portField.setForeground(new Color(150, 150, 150));  
		}  

		portField.addFocusListener(new FocusListener() {  

			@Override
			public void focusGained(FocusEvent e) {  

				if (portField.getText().equals("10008")) {
					portField.setText("");  
					portField.setForeground(new Color(50, 50, 50));  
				}
		    }  

			@Override
			public void focusLost(FocusEvent e) {
				if (portField.getText().length() == 0) {  
					portField.setText("10008");  
					portField.setForeground(new Color(150, 150, 150));  
		        }
				
			}
		});
		    
		    
		
		
		
		
		startStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!serverRunning) {
					try {
						int port;
						if(!portField.getText().isBlank())
							port = Integer.valueOf(portField.getText());
						else
							port = defaultPort;
						serverThread = new ServerThread(port);
						
						serverThread.start();
						serverRunning=true;
						statusLabel.setText("Server is running");
						portField.setEditable(false);
						maintenanceCheckBox.setEnabled(true);
						startStopButton.setText("Stop server");
					} catch (NumberFormatException | IOException e1) {
						e1.printStackTrace();
						statusLabel.setText("ERROR");
					}
				}
				else {
					try {
						serverThread.closeSocket();
						serverRunning=false;
						portField.setEditable(true);
						maintenanceCheckBox.setEnabled(false);
						maintenanceCheckBox.setSelected(false);
						startStopButton.setText("Start server");
					} catch (NumberFormatException e1) {
						e1.printStackTrace();
						statusLabel.setText("ERROR");
					}
				}
					
			}
		});
		maintenanceCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerThread.inMaintenance = !ServerThread.inMaintenance;
			}
		});
		
		
		
	}
}
