package com.testingtech.examples.tutorial.procedure;

import java.util.Calendar;
import java.util.Enumeration;

import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.TciCDProvided;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriException;
import org.etsi.ttcn.tri.TriParameter;
import org.etsi.ttcn.tri.TriParameterList;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriPortIdList;
import org.etsi.ttcn.tri.TriSignatureId;
import org.etsi.ttcn.tri.TriStatus;
import org.etsi.ttcn.tri.TriTestCaseId;

import server.AddressBookInterfaceImpl;
import aB.Contact;
import aB.ContactWrapper;
import aB.EntryWrapper;
import aB.addressBookOperations;
import aB.addressBookPackage.notFound;
import aB.addressBookPackage.sizeLimitReached;
import aB.addressBookPackage.userExists;

import com.testingtech.ttcn.logging.RTLoggingConstants;
import com.testingtech.ttcn.tri.TestAdapter;
import com.testingtech.ttcn.tri.TriExceptionImpl;
import com.testingtech.ttcn.tri.TriParameterImpl;
import com.testingtech.ttcn.tri.TriStatusImpl;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;
import de.tu_berlin.cs.uebb.muttcn.runtime.ValueImpl;
import de.tu_berlin.cs.uebb.ttcn.tri.TriParameterListImpl;

/**
 * This is the System Adapter for a procedure based example.
 */
public class AddressBookTA extends TestAdapter {

	// This is the SUT !
	private addressBookOperations fABS = null;
	private TciCDProvided fPCodec = null;

	public AddressBookTA() {
		fABS = new AddressBookInterfaceImpl();
		return;
	}

	public TestAdapter setRB(RB rb) {
		// We need this, as we need "context free" access to the RB.
		MyExportedRB.setRB(rb);
		return super.setRB(rb);
	}

	public TciCDProvided getCodec(String encodingName) {
		// This example makes only use of the ParameterCodec

		encodingName = "com.testingtech.ttcn.tci.codec.ParameterCodec";
		TciCDProvided p = super.getCodec(encodingName);
		if (p != null)
			return p;

		fPCodec = parameterCodec;
		codecs.put(encodingName, fPCodec);

		return fPCodec;
	}

	public TriStatus triCall(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriParameterList parameterList) {

		if (signatureId.getSignatureName().endsWith("addEntry")) {
			return addEntry(componentId, tsiPortId, sutAddress, signatureId,
					parameterList);
		}
		if (signatureId.getSignatureName().endsWith("clear")) {
			return clear(componentId, tsiPortId, sutAddress, signatureId,
					parameterList);
		}

		if (signatureId.getSignatureName().endsWith("getEntry")) {
			return getEntry(componentId, tsiPortId, sutAddress, signatureId,
					parameterList);
		}

		throw new RuntimeException("TA could not handle call to "
				+ signatureId.getSignatureName());
	}

	public TriStatus triExecuteTestcase(TriTestCaseId testcase,
			TriPortIdList tsiList) {
		// check wether there is a port sp available
		Enumeration enumeration = tsiList.getPortIds();
		while (enumeration.hasMoreElements()) {
			TriPortId port = (TriPortId) enumeration.nextElement();
			if (port.getPortName().equals("sp")) {
				RB.getTciTLProvided().tliRT("", System.nanoTime(), "AddressBookTA", 0, null, RTLoggingConstants.RT_LOG_INFO, "found a port named sp");
				return super.triExecuteTestcase(testcase, tsiList);
			}
		}
		return new TriStatusImpl("Did not found at least one port named \"sp\"");
	}

	private TriStatus addEntry(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriParameterList parameterList) {
		TriParameter param = parameterList.get(0);
		Value entry = getValueForParam(param);
		EntryWrapper ew = new EntryWrapper(entry);
		try {
			fABS.addEntry(ew.getEntry());
		} catch (userExists e) {
			Type type = RB.getTciCDRequired().getTypeForName("aB.userExists");
			RecordValue rv = (RecordValue) type.newInstance();
			CharstringValue cv = (CharstringValue) RB.getTciCDRequired()
					.getCharstring().newInstance();
			cv.setString(e.firstName);
			rv.setField("firstName", cv);

			TriException ex = new TriExceptionImpl(fPCodec.encode(rv)
					.getEncodedMessage());

			RB.getTriCommunicationTE().triEnqueueException(tsiPortId, sutAddress,
					componentId, signatureId, ex);
			return new TriStatusImpl();
		} catch (sizeLimitReached e) {
			Type type = RB.getTciCDRequired().getTypeForName("aB.sizeLimitReached");
			RecordValue rv = (RecordValue) type.newInstance();
			TriException ex = new TriExceptionImpl(fPCodec.encode(rv)
					.getEncodedMessage());
			RB.getTriCommunicationTE().triEnqueueException(tsiPortId, sutAddress,
					componentId, signatureId, ex);
			return new TriStatusImpl();
		}

		// This operation is void !
		RB.getTriCommunicationTE().triEnqueueReply(tsiPortId, sutAddress,
				componentId, signatureId, parameterList, null);
		return new TriStatusImpl();
	}

	private TriStatus getEntry(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriParameterList parameterList) {
		TriParameter param = parameterList.get(0);
		CharstringValue surName = (CharstringValue) getValueForParam(param);

		Contact contact = null;
		try {
			contact = fABS.getEntry(surName.getString());
		} catch (notFound e) {
			Type type = RB.getTciCDRequired().getTypeForName("aB.notFound");
			RecordValue rv = (RecordValue) type.newInstance();
			((ValueImpl) rv).setPresent();
			TriException ex = new TriExceptionImpl(fPCodec.encode(rv)
					.getEncodedMessage());
			RB.getTriCommunicationTE().triEnqueueException(tsiPortId, sutAddress,
					componentId, signatureId, ex);
			return new TriStatusImpl();
		}

		ContactWrapper cw = new ContactWrapper(contact);
		Value returnValue = cw.getContactValue();
		TriParameterImpl pi = new TriParameterImpl(fPCodec.encode(returnValue)
				.getEncodedMessage());

		TriParameterList tl = new TriParameterListImpl(RB);
		TriParameterImpl ti = new TriParameterImpl(fPCodec.encode(surName)
				.getEncodedMessage());
		tl.add(ti);

		// This operation returns a Contact
		RB.getTriCommunicationTE().triEnqueueReply(tsiPortId, sutAddress,
				componentId, signatureId, tl, pi);
		return new TriStatusImpl();
	}

	private TriStatus clear(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriParameterList parameterList) {
		fABS.clear();

		// This operation is void !
		RB.getTriCommunicationTE().triEnqueueReply(tsiPortId, sutAddress,
				componentId, signatureId, parameterList, null);
		return new TriStatusImpl();
	}

	public static class MyExportedRB {

		public static RB fRB = null;

		public static void setRB(RB rb) {
			fRB = rb;
		}

	}

}
