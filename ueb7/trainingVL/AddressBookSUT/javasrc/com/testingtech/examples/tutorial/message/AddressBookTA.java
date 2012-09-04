package com.testingtech.examples.tutorial.message;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;

import org.etsi.ttcn.tci.TciCDProvided;
import org.etsi.ttcn.tci.TciCDRequired;
import org.etsi.ttcn.tci.TciTMProvided;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriPortIdList;
import org.etsi.ttcn.tri.TriStatus;
import org.etsi.ttcn.tri.TriTestCaseId;

import server.AddressBookInterfaceImpl;
import aB.addressBookOperations;

import com.testingtech.ttcn.logging.RTLoggingConstants;
import com.testingtech.ttcn.logging.TciTLProvided;
import com.testingtech.ttcn.logging.TciTLProvidedV321TT;
import com.testingtech.ttcn.tci.codec.tabular.TabularDecoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;
import com.testingtech.ttcn.tri.TestAdapter;
import com.testingtech.ttcn.tri.TriAddressImpl;
import com.testingtech.ttcn.tri.TriMessageImpl;
import com.testingtech.ttcn.tri.TriStatusImpl;
import com.testingtech.util.BitArrayInputStream;
import com.testingtech.util.BitArrayOutputStream;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;

/**
 * This is the System Adapter for a message based example.
 */
public class AddressBookTA extends TestAdapter {

	// This is the SUT !
	private addressBookOperations fABS = null;

	// Dedicated to UDP communication
	private DatagramSocket sendSocket;
	private DatagramSocket rcvSocket;
	private static int RECEIVER_PORT_NUMBER = 8001;
	public Object readlock = new Object();
	private Object threadlock = new Object();
	private boolean runThread = false;

	// Of general use
	private TciTMProvided management;
	private TciTLProvidedV321TT logging;
	private TciCDRequired typeServer;

	/**
	 * This map contains key/value pairs in both direction
	 */
	private HashMap componentMap = null;

	/**
	 * The constuctor is always needed, if you would like to do initialization
	 * here.
	 * 
	 */
	public AddressBookTA() {
		super();
		fABS = new AddressBookInterfaceImpl();
		componentMap = new HashMap();
		return;
	}

	/**
	 * Will be called by the TE, in order inform about the current Runtime
	 * Behaviour, RB
	 * 
	 * @see com.testingtech.ttcn.tri.TestAdapter#setRB(de.tu_berlin.cs.uebb.muttcn.runtime.RB)
	 */
	public TestAdapter setRB(RB rb) {
		// We need this, as we need "context free" access to the RB.
		MyExportedRB.setRB(rb);

		// A handle to the type server, aka TciCDRequired interface.
		typeServer = rb.getTciCDRequired();
	  // A handle to the test management, aka TciTMProvided interface.
		management = rb.getTciTMProvided();
	  // A handle to the logging, aka TciTLProvided interface.
		logging = rb.getTciTLProvidedV321TT();

		return super.setRB(rb);
	}

	/**
	 * Will be called by the TE to ask for codecs. You have to return here a codec
	 * for a given name.
	 * 
	 * If the encoding of the message was empty or unspecified <code>encodingName</name>
	 * will be <code>null</code> or <code>""</code>
	 * 
	 * @see com.testingtech.ttcn.tci.TciEncoding#getCodec(java.lang.String)
	 */
	public TciCDProvided getCodec(String encodingName) {
		// This example makes only use of the ParameterCodec

		// This is encoding name is arbitrary chosen name. It must not bee the class
		// name
		encodingName = "com.testingtech.examples.tutorial.message.AddressBookSimpleCodec";

		// Call the super implementation to see, whether there is a coder known
		// Attention: YOU ALWAYS HAVE TO CALL THIS OPERATION
		TciCDProvided p = super.getCodec(encodingName);

		// AND CHECK ITS RETURN VALUE.
		if (p != null)
			return p;

		// No, codec was not yet created.
		// You always need a constructor with RB.
		TciCDProvided codec = new AddressBookSimpleCodec(RB);

		// store the codec for later usage.
		codecs.put(encodingName, codec);

		return codec;
	}

	/** 
	 * Will be called in case <code>send</code> is called within the TTCN-3 code.
	 *   
	 * @see com.testingtech.ttcn.tri.TestAdapter#triSend(org.etsi.ttcn.tri.TriComponentId, org.etsi.ttcn.tri.TriPortId, org.etsi.ttcn.tri.TriAddress, org.etsi.ttcn.tri.TriMessage)
	 */
	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress address, TriMessage message) {
		byte[] cmd = message.getEncodedMessage();

		TabularEncoder enc = new TabularEncoder(new BitArrayOutputStream());
		try {
			enc.encodeInteger(((Integer) componentMap.get(componentId)).intValue(),
					16);
			enc.encodeOctetstring(cmd);
		} catch (TabularException e) {
			return new TriStatusImpl(e.getMessage());
		}
		byte[] mesg = enc.toByteArray();

		try {
			InetAddress addr = InetAddress.getByName("127.0.0.1");
			DatagramPacket packet = new DatagramPacket(mesg, mesg.length, addr, 8002);

			println("\nSending (to: " + addr + ")\n-------\n");
			sendSocket.send(packet);
		} catch (IOException ioex) {
			return new TriStatusImpl(ioex.getMessage());
		}
		return new TriStatusImpl();
	}

	/**
	 * Will be called in case <code>map</code> is called within the TTCN-3 code.
	 *   
	 * @see com.testingtech.ttcn.tri.TestAdapter#triMap(org.etsi.ttcn.tri.TriPortId, org.etsi.ttcn.tri.TriPortId)
	 */
	public TriStatus triMap(TriPortId compPortId, TriPortId tsiPortId) {
		// YOU ALWAYS HAVE TO DO THIS !
		TriStatus mapStatus = CsaDef.triMap(compPortId, tsiPortId);

		int rndComponentId = (int) (Math.random() * 1000);

		componentMap.put(new Integer(rndComponentId), compPortId.getComponent());
		componentMap.put(compPortId.getComponent(), new Integer(rndComponentId));

		return new TriStatusImpl(); // OK
	}

	/**
	 * Called whenever a test case is started.
	 * 
	 * @see com.testingtech.ttcn.tri.TestAdapter#triExecuteTestcase(org.etsi.ttcn.tri.TriTestCaseId, org.etsi.ttcn.tri.TriPortIdList)
	 */
	public TriStatus triExecuteTestcase(TriTestCaseId testcase,
			TriPortIdList tsiList) {
		// check wether there is a port sp available
		Enumeration enumeration = tsiList.getPortIds();
		while (enumeration.hasMoreElements()) {
			TriPortId port = (TriPortId) enumeration.nextElement();
			if (port.getPortName().equals("sp")) {
				try {
					rcvSocket = new DatagramSocket(RECEIVER_PORT_NUMBER);
					sendSocket = new DatagramSocket();
				} catch (SocketException sex) {
					return new TriStatusImpl("Unable to open socket for TSI Port: "
							+ port.getPortName());
				}

				startUdpReceiverThread(port);
				return super.triExecuteTestcase(testcase, tsiList);
			}
		}
		return new TriStatusImpl("Did not found at least one port named \"sp\"");
	}

	/**
	 * Logs the given string as tliRT event and log level INFO.
	 * 
	 * @see com.testingtech.ttcn.tri.TestAdapter#println(java.lang.String)
	 */
	public void println(String theString) {
		logging.tliRT("", System.nanoTime(), "AddressBookTA", 0, null, RTLoggingConstants.RT_LOG_INFO, theString);
	}

	/**
	 * This class is used in oder to get access to the defined RB. It can be used as following:
	 * <code>RB rb = AddressBookTA.MyExportedRB.fRB;</code>
	 */
	public static class MyExportedRB {

		public static RB fRB = null;

		public static void setRB(RB rb) {
			fRB = rb;
		}

	}

	/**
	 * Enqueues a received message to the TE.
	 * 
	 * @param sender the TSI port ID
	 * @param address the SUT address (mostly not necessary)
	 * @param receiver the receiver component ID
	 * @param message the encoded message
	 */
	private synchronized void enqueueMsg(TriPortId sender, TriAddress address,
			TriComponentId receiver, TriMessage message) {
		Cte.triEnqueueMsg(sender, address, receiver, message);
	}

	/**
	 * Starts a UDP receiver thread for asynchronous communication listening on
	 * <code>RECEIVER_PORT_NUMBER</code> . In case a message is received it is
	 * enqueued to the provided <code>tsiPort</code>. Receiving events can happen
	 * at any time.
	 * 
	 * @param tsiPortId
	 *          the TSI port where the message will be enqueued
	 */
	private void startUdpReceiverThread(TriPortId tsiPortId) {
		final TriPortId myTsiPortId = tsiPortId;

		runThread = true;
		new Thread() {
			public void run() {
				try {
					rcvSocket.setSoTimeout(100);

					while (runThread) {
						byte[] msg = new byte[1024];
						DatagramPacket packet = new DatagramPacket(msg, msg.length);

						try {
							rcvSocket.receive(packet);

							byte[] trimmedMsg = new byte[packet.getLength()];
							System.arraycopy(msg, 0, trimmedMsg, 0, packet.getLength());

							TabularDecoder td = new TabularDecoder(new BitArrayInputStream(
									trimmedMsg));
							int componentIdentifier = td.decodeInteger(16);
							trimmedMsg = td.decodeOctetstring(trimmedMsg.length - 2);

							String rcvFrom = packet.getAddress().getHostAddress();
							String rcvPort = "8001";
							String rcvAddrPort = rcvFrom.concat("-").concat(rcvPort);

							byte[] rcvAddrPortByte = rcvAddrPort.getBytes();
							TriAddress triEnqAddr = new TriAddressImpl(rcvAddrPortByte);

							println("\nReceived ( " + trimmedMsg.length + " bytes) from "
									+ rcvFrom + " for component " + componentIdentifier
									+ "\n----------------\n");

							// It is a asynchronous message
							TriMessage rcvMessage = new TriMessageImpl(trimmedMsg);

							if (runThread) {
								TriComponentId myCompId = (TriComponentId) componentMap
										.get(new Integer(componentIdentifier));
								if (myCompId != null) {
									enqueueMsg(myTsiPortId, triEnqAddr, myCompId, rcvMessage);
								} else {
									println("Could not ennque received message as did not found receiver component");
								}

							}
						} catch (InterruptedIOException iioex) {
						} catch (TabularException tex) {
							tex.printStackTrace();
						} catch (IOException ioex) {
							println("IOException " + ioex + " - closing the receiver socket");

							if (rcvSocket != null) {
								rcvSocket.close();
								rcvSocket = null;
							}

							return;
						}
					}

					if (rcvSocket != null) {
						rcvSocket.close();
						rcvSocket = null;
					}
				} catch (SocketException se) {
					println("SocketException " + se);
				}
			}
		}.start();
	}

	/**
	 * Describe <code>triSAReset</code> method here.
	 * 
	 * @return a <code>TriStatus</code> value
	 */
	public TriStatus triSAReset() {
		cancel();

		return super.triSAReset();
	}
	
	

	/**
	 * Closes the sockets and cancels the receiver thread.
	 */
	public void cancel() {
		println("Shutting down connections");

		if (!runThread) {
			return;
		}

		runThread = false;

		if (sendSocket != null) {
			sendSocket.close();
			sendSocket = null;
		}
	}

}
