package server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.etsi.ttcn.tri.TriAddress;

import sun.misc.HexDumpEncoder;

import aB.Contact;
import aB.ContactWrapper;
import aB.EntryWrapper;
import aB.StringWrapper;
import aB.aBMessagesTypes;
import aB.addressBookOperations;
import aB.addressBookPackage.notFound;
import aB.addressBookPackage.sizeLimitReached;
import aB.addressBookPackage.userExists;

import com.testingtech.ttcn.tci.codec.tabular.TabularDecoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;
import com.testingtech.ttcn.tri.TriAddressImpl;
import com.testingtech.util.BitArrayInputStream;
import com.testingtech.util.BitArrayOutputStream;
import com.testingtech.util.HexViewer;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;
import de.tu_berlin.cs.uebb.ttcn.runtime.HexString;

/**
 * This is the message based System Under Test. It represnts an address book
 * allowing to store contacts, get entries from it and to clear the whole
 * address book again. The communication can be performed through UDP. The
 * server is listening on port <code>RECEIVER_PORT_NUMBER</code> and sends the
 * response back to the address from where the request was received.
 */
public class MessageBasedSUT implements aBMessagesTypes {

	private addressBookOperations fABS = null;

	private DatagramSocket sendSocket;
	private DatagramSocket rcvSocket;
	public Object readlock = new Object();
	private Object threadlock = new Object();
	private boolean runThread = false;
	private static int RECEIVER_PORT_NUMBER = 8002;

	/**
	 * Opens a receiver socket and a sender socket
	 */
	public MessageBasedSUT() {
		fABS = new AddressBookInterfaceImpl();

		try {
			rcvSocket = new DatagramSocket(RECEIVER_PORT_NUMBER);
			sendSocket = new DatagramSocket();
		} catch (SocketException sex) {
			sex.printStackTrace();
			return;
		}

		return;
	}

	/**
	 * Main method for MessageBasedSUT.
	 * 
	 * @param args no arguments necessary
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		MessageBasedSUT mbs = new MessageBasedSUT();
		System.out.println("------------ Started MessageBasedSUT sucessfully ------------");
		System.out.println("------------------ listening on port: "+RECEIVER_PORT_NUMBER+" ------------------");
		try {
			mbs.startUdpReceiverThread();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes an incoming message
	 * 
	 * @param transactionId
	 *          the transaction ID
	 * @param message
	 *          the received message as byte array, following message types can be
	 *          processed
	 *          <ul>
	 *          <li> ADD_ENTRY
	 *          <li> GET_ENTRY
	 *          <li> CLEAR
	 *          </ul>
	 * @throws TabularException
	 */
	public void processMessage(int transactionId, byte[] message)
			throws TabularException {

		switch (message[0]) {
		case ADD_ENTRY:
			addEntry(transactionId, message);
			return;
		case GET_ENTRY:
			getEntry(transactionId, message);
			return;
		case CLEAR:
			clear(message);
			return;

		default:
			println("Recveived unknown message type: "+(byte)message[0]);
		}
	}

	/**
	 * An <code>ADD_ENTRY</code> was received and has to be inserted to the database.
	 * @param transactionId
	 * @param message	the <code>ADD_ENTRY</code> message 
	 * @throws TabularException
	 */
	private void addEntry(int transactionId, byte[] message)
			throws TabularException {
		byte[] theEncEntryParam = new byte[message.length - 1];
		System.arraycopy(message, 1, theEncEntryParam, 0, message.length - 1);

		BitArrayInputStream bais = new BitArrayInputStream(theEncEntryParam);
		TabularDecoder td = new TabularDecoder(bais);

		try {
			fABS.addEntry(EntryWrapper.decode(td));
		} catch (userExists e) {
			// catched the userExists exception
			BitArrayOutputStream baos = new BitArrayOutputStream();
			TabularEncoder enc = new TabularEncoder(baos);
			enc.encodeByte(EX_USER_EXIST);
			StringWrapper.encode(e.firstName, enc);
			sendAnswer(transactionId, enc.toByteArray());
		} catch (sizeLimitReached e) {
			BitArrayOutputStream baos = new BitArrayOutputStream();
			TabularEncoder enc = new TabularEncoder(baos);
			enc.encodeByte(EX_SIZE_LIMIT_REACHED);
			enc.encodeInteger(5, 16);
			sendAnswer(transactionId, enc.toByteArray());
		} catch (TabularException tex) {
			System.out.println("Catched a TabularCodecException ");
			tex.printStackTrace();
		}

		// This operation is void !
	}

	/**
	 * An <code>GET_ENTRY</code> was received and has to be processed.
	 * @param transactionId
	 * @param message	the <code>GET_ENTRY</code> message 
	 * @throws TabularException
	 */	
	private void getEntry(int transactionId, byte[] message)
			throws TabularException {
		byte[] theEncSurNameParam = new byte[message.length - 1];
		System.arraycopy(message, 1, theEncSurNameParam, 0, message.length - 1);

		BitArrayInputStream bais = new BitArrayInputStream(theEncSurNameParam);
		TabularDecoder td = new TabularDecoder(bais);
		String surName;
		Contact contact = null;

		try {
			surName = StringWrapper.decode(td);
		} catch (TabularException tex) {
			System.out.println("Catched a TabularCodecException ");
			tex.printStackTrace();
			return;
		}
		try {
			contact = fABS.getEntry(surName);
		} catch (notFound e) {
			BitArrayOutputStream baos = new BitArrayOutputStream();
			TabularEncoder enc = new TabularEncoder(baos);
			enc.encodeByte(EX_NOT_FOUND);
			StringWrapper.encode(surName, enc);
			sendAnswer(transactionId, enc.toByteArray());
			return;
		}

		BitArrayOutputStream baos = new BitArrayOutputStream();
		TabularEncoder enc = new TabularEncoder(baos);
		enc.encodeByte(GET_ENTRY_REPLY);
		ContactWrapper.encode(contact, enc);
		sendAnswer(transactionId, enc.toByteArray());
		return;
	}

	/**
	 * An <code>CLEAR</code> was received and has to be processed.
	 * 
	 * @param message the <code>CLEAR</code> message
	 */
	private void clear(byte[] message) {
		fABS.clear();

		// as this operation is void, just do nothing
	}

	/**
	 * Sends an answer based on the incoming request
	 * 
	 * @param transactionId
	 * @param answer the answer as byte array
	 * @throws TabularException
	 */
	public void sendAnswer(int transactionId, byte[] answer)
			throws TabularException {

		TabularEncoder enc = new TabularEncoder(new BitArrayOutputStream());
		enc.encodeInteger(transactionId, 16);
		enc.encodeOctetstring(answer);

		byte[] mesg = enc.toByteArray();

		try {
			InetAddress addr = InetAddress.getByName("127.0.0.1");
			DatagramPacket packet = new DatagramPacket(mesg, mesg.length, addr, 8001);

			println("\nSending ( " + mesg.length + " bytes) to " + addr 
					+ " with transactionID " + transactionId 
					+ "\n-------------------------------------------------------------------\n"
					+ new HexViewer(new ByteArrayInputStream(mesg)).toString());
			
			sendSocket.send(packet);
		} catch (IOException ioex) {
			System.out.println("Catched IOException while sending");
			ioex.printStackTrace();
			return;
		}
		return;
	}

	/**
	 * Dumps the given message to System.out
	 * 
	 * @param theString the message to dump
	 */
	public void println(String theString) {
		System.out.println(theString + "\n");
	}

	public static class MyExportedRB {

		public static RB fRB = null;

		public static void setRB(RB rb) {
			fRB = rb;
		}

	}

	private void startUdpReceiverThread() throws InterruptedException {

		runThread = true;
		Thread theThread = new Thread() {
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
							int transactionId = td.decodeInteger(16);
							trimmedMsg = td.decodeOctetstring(trimmedMsg.length - 2);

							String rcvFrom = packet.getAddress().getHostAddress();
							String rcvPort = Integer.toString(RECEIVER_PORT_NUMBER);
							String rcvAddrPort = rcvFrom.concat("-").concat(rcvPort);

							byte[] rcvAddrPortByte = rcvAddrPort.getBytes();
							TriAddress triEnqAddr = new TriAddressImpl(rcvAddrPortByte);
				
							println("\nReceived ( " + trimmedMsg.length + " bytes) from " + rcvFrom 
									+ " with transactionID " + transactionId
									+ "\n-------------------------------------------------------------------\n"
									+ new HexViewer(new ByteArrayInputStream(trimmedMsg)).toString());

							if (runThread) {
								processMessage(transactionId, trimmedMsg);
							}

						} catch (InterruptedIOException iioex) {
						} catch (TabularException tex) {
							println("Catched a TabularException");
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
		};
		theThread.start();
		theThread.join();

	}
}
