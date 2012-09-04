package aB;

import org.etsi.ttcn.tci.IntegerValue;
import org.etsi.ttcn.tci.RecordOfValue;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.Value;

import com.testingtech.examples.tutorial.message.AddressBookTA;
import com.testingtech.ttcn.tci.codec.tabular.TabularDecoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;

/**
 * Helper class to encode and decode a phone number. A phone number is encoded
 * always with an prepending length info (1 byte). Every digit is encoded also
 * as a 4 byte signed integer.
 */
public class PhoneNumberWrapper {

	private int[] fPhoneNumber = null;
	private RecordOfValue fPhoneNumberValue = null;

	/**
	 * Constructor for setting a TCI phone number value.
	 * @param phoneNumberValue the TCI phone number
	 */
	public PhoneNumberWrapper(Value phoneNumberValue) {
		setPhoneNumberValue(phoneNumberValue);
	}

	/**
	 * Constructor for seting an  int array phone number
	 * 
	 * @param phoneNumber the phone number as int array
	 */
	public PhoneNumberWrapper(int[] phoneNumber) {
		setPhoneNumber(phoneNumber);
	}

	/**
	 * To set a TCI phone number value.
	 * 
	 * @param phoneNumberValue the TCI phone number
	 */
	public void setPhoneNumberValue(Value phoneNumberValue) {
		if (!phoneNumberValue.getType().getName().equals("PhoneNumber"))
			throw new RuntimeException("Trying to wrap non PhoneNumber. Value was "
					+ phoneNumberValue);

		RecordOfValue rof = (RecordOfValue) phoneNumberValue;
		int length = rof.getLength();

		int[] thePhoneNumber = new int[length];
		for (int i = length; i-- > 0;) {
			thePhoneNumber[i] = ((IntegerValue) rof.getField(i)).getInt();
		}

		fPhoneNumber = thePhoneNumber;
		fPhoneNumberValue = rof;

	}

	/**
	 * To set a TCI phone number value.
	 * 
	 * @param phoneNumber the TCI phone number
	 */
	public void setPhoneNumber(int[] phoneNumber) {
		RB rb = AddressBookTA.MyExportedRB.fRB;
		Type type = rb.getTciCDRequired().getTypeForName("aB.PhoneNumber");
		RecordOfValue rov = (RecordOfValue) type.newInstance();

		rov.setLength(phoneNumber.length);
		for (int i = phoneNumber.length; i-- > 0;) {
			IntegerValue iv = (IntegerValue) rb.getTciCDRequired().getInteger()
					.newInstance();
			iv.setInt(phoneNumber[i]);
			rov.setField(i, iv);
		}

		fPhoneNumber = phoneNumber;
		fPhoneNumberValue = rov;

	}

	/**
	 * Returns the set phone number.
	 * 
	 * @return the phone number as int array
	 */
	public int[] getPhoneNumber() {
		return fPhoneNumber;
	}

	/**
	 * Returns the set phone number TCI value. 
	 * 
	 * @return the RecordOf TCI phone number
	 */
	public RecordOfValue getPhoneNumberValue() {
		return fPhoneNumberValue;
	}

	/**
	 * Encodes a given phone number.
	 * @param number the phone number to encode
	 * @param enc the tabular encoder object
	 * @throws TabularException
	 */
	public static void encode(int[] number, TabularEncoder enc)
			throws TabularException {

		int length = number.length;
		// first write the number of elements as a byte
		enc.encodeByte((byte) length);

		//the phone number encoding is explicitely wrong, it is used to find a problem in the SUT for the tutorials 
//		for (int i = 0; i < number.length; i++) {
//			enc.encodeInteger(number[i], 8);
//		}
		for (int i = number.length; i > 0; i--) {
			enc.encodeInteger(number[i-1], 8);
		}
	}

	/**
	 * Encodes the set TCI phone number value.
	 * 
	 * @param enc	the tabular encoder object
	 * @throws TabularException
	 */
	public void encode(TabularEncoder enc) throws TabularException {
		RecordOfValue rof = (RecordOfValue) fPhoneNumberValue;

		int length = rof.getLength();
		// first write the number of elements as a byte
		enc.encodeByte((byte) length);
		// then write one digit after another
		for (int i = 0; i < length; i++) {
			enc.encodeInteger(((IntegerValue) rof.getField(i)).getInt(), 8);
		}
	}

	/**
	 * Decodes a phone number from the provided tabular decoder object.
	 * 
	 * @param td the tabular decoder object
	 * @return	the decoded phone number as int array
	 * @throws TabularException
	 */
	public static int[] decode(TabularDecoder td) throws TabularException {

		int length = td.decodeInteger(8);
		int[] number = new int[length];
		for (int i = 0; i < length; i++) {
			number[i] = td.decodeInteger(8);
		}

		return number;
	}
}
