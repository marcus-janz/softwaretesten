package com.testingtech.examples.tutorial.message;

import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.IntegerValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.TciCDRequired;
import org.etsi.ttcn.tci.TciTMProvided;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriMessage;

import aB.AddEntryWrapper;
import aB.Contact;
import aB.ContactWrapper;
import aB.GetEntryWrapper;
import aB.StringWrapper;
import aB.aBMessagesTypes;

import com.testingtech.ttcn.logging.TciTLProvided;
import com.testingtech.ttcn.logging.TciTLProvidedV321TT;
import com.testingtech.ttcn.tci.ExtendedTciTypeClass;
import com.testingtech.ttcn.tci.codec.base.AbstractBaseCodec;
import com.testingtech.ttcn.tci.codec.tabular.TabularDecoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;
import com.testingtech.ttcn.tri.TriMessageImpl;
import com.testingtech.util.BitArrayInputStream;
import com.testingtech.util.BitArrayOutputStream;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;
import de.tu_berlin.cs.uebb.muttcn.runtime.ValueImpl;

/**
 * This is the codec implementation for the Address Book tests.
 */
public class AddressBookSimpleCodec extends AbstractBaseCodec implements
		aBMessagesTypes {

	private TciCDRequired typeServer;
	private TciTMProvided management;
	private TciTLProvidedV321TT logging;

	public AddressBookSimpleCodec(RB rb) {
		super(rb);
		// A handle to the type server, aka TciCDRequired interface.
		typeServer = rb.getTciCDRequired();
	  // A handle to the test management, aka TciTMProvided interface.
		management = rb.getTciTMProvided();
	  // A handle to the logging, aka TciTLProvided interface.
		logging = RB.getTciTLProvidedV321TT();
	}

	/**
	 * The decode implementation. It checks the first byte in order to detect the
	 * kind of the message and decodes appropriate. In case it is an unknown
	 * message null is returned. In case an exception happens the expection will
	 * be logged and null will be returned.
	 * 
	 * @see com.testingtech.ttcn.tci.codec.base.AbstractBaseCodec#decode(org.etsi.ttcn.tri.TriMessage,
	 *      org.etsi.ttcn.tci.Type)
	 */
	public Value decode(TriMessage message, Type dh) {
    //first check if the decoding hypothesis is ANY. In this case no further decoding is necessary.
		if (dh.getTypeClass() == ExtendedTciTypeClass.ANY) {
			Value theValue = dh.newInstance();
			((ValueImpl) theValue).setPresent();
			return theValue;
		}

		byte[] msg = message.getEncodedMessage();
		TabularDecoder td = new TabularDecoder(new BitArrayInputStream(msg));
		try {
			//get the first byte
			byte msgType = td.decodeByte();
			//check the first byte to find out the kind of message
			switch (msgType) {
			case EX_USER_EXIST:
				// the message is a 'UserExists' message, now check against the given
				// decoding hypothesis. If they are not the same, the log an info that
				// the message cannot be decoded and return null.
				if (!dh.getName().equals("userExists")) {
					logDecode("AddressBookDecoder: msgType indicated EX_USER_EXIST. DecodingHyp was "
							+ dh.getName());
					return null;
				}
				//get the firstname
				String firstName = StringWrapper.decode(td);
				RecordValue rv = (RecordValue) dh.newInstance();
				CharstringValue cv = (CharstringValue) rv.getField("firstName")
						.getType().newInstance();
				cv.setString(firstName);
				rv.setField("firstName", cv);
				return rv;

			case EX_NOT_FOUND:
				// check against decoding hypothesis
				if (!dh.getName().equals("notFound")) {
					logDecode("AddressBookDecoder: msgType indicated EX_NOT_FOUND. DecodingHyp was "
							+ dh.getName());
					return null;
				}
				//get the surname
				String surName = StringWrapper.decode(td);
				RecordValue rv2 = (RecordValue) dh.newInstance();
				CharstringValue cv2 = (CharstringValue) rv2.getField("surName")
						.getType().newInstance();
				cv2.setString(surName);
				rv2.setField("surName", cv2);
				return rv2;

			case EX_SIZE_LIMIT_REACHED:
			  // check against decoding hypothesis
				if (!dh.getName().equals("sizeLimitReached")) {
					logDecode("AddressBookDecoder: msgType indicated EX_SIZE_LIMIT_REACHED. DecodingHyp was "
							+ dh.getName());

					return null;
				}
				//get the size
				int size = td.decodeInteger(16);
				RecordValue rv3 = (RecordValue) dh.newInstance();
				IntegerValue iv = (IntegerValue) rv3.getField("size").getType()
						.newInstance();
				iv.setInt(size);
				rv3.setField("size", iv);
				return rv3;
			case GET_ENTRY_REPLY:
				// check against decoding hypothesis
				if (!dh.getName().equals("getEntryReply")) {
					logDecode("AddressBookDecoder: msgType indicated GET_ENTRY_REPLY. DecodingHyp was "
							+ dh.getName());
					return null;
				}
				RecordValue rv4 = (RecordValue) dh.newInstance();
				Contact c = ContactWrapper.decode(td);
				ContactWrapper c2 = new ContactWrapper(c);
				rv4.setField("contact", c2.getContactValue());
				return rv4;
			default:
				return null;
			}
		} catch (TabularException tex) {
			logDecode("Catched a TabularException " + tex.getMessage());
		}
		return null;
	}

	/**
	 * Encodes the given TCI message. In case it is an unknown message type null will be returned.
	 * 
	 * @see com.testingtech.ttcn.tci.codec.base.AbstractBaseCodec#encode(org.etsi.ttcn.tci.Value)
	 */
	public TriMessage encode(Value value) {
		BitArrayOutputStream baos = new BitArrayOutputStream();
		TabularEncoder enc = new TabularEncoder(baos);

		// First we distinguish between the different type of messages that
		// could be send

		String typeName = value.getType().getName();
		if (typeName.equals("addEntry")) {
			AddEntryWrapper d = new AddEntryWrapper(value);
			try {
				d.encode(enc);
			} catch (TabularException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while encoding addEntryMessage " + e);
			}
			return new TriMessageImpl(enc.toByteArray());
		}

		if (typeName.equals("getEntry")) {
			GetEntryWrapper d = new GetEntryWrapper(value);
			try {
				d.encode(enc);
			} catch (TabularException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while encoding getEntryMessage " + e);
			}
			return new TriMessageImpl(enc.toByteArray());
		}

		if (typeName.startsWith("clear")) {
			try {
				enc.encodeByte(CLEAR);
			} catch (TabularException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while encoding addEntryMessage " + e);
			}
			return new TriMessageImpl(enc.toByteArray());
		}

		return null;
	}

}
