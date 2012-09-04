package aB;

import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.Value;

import com.testingtech.ttcn.tci.codec.tabular.TabularDecoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;

/**
 * This class encodes TCI charstrings values decodes them. A string is always
 * encoded with a prepending length info (1 byte) containing the length of the
 * encoded string.
 */
public class StringWrapper {

	private CharstringValue fStringValue;

	/**
	 * Constructor
	 * 
	 * @param val the TCI charstring value
	 */
	public StringWrapper(Value val) {
		fStringValue = (CharstringValue) val;
	}

	/**
	 * Encodes the charstring provided in the constructor. A string is always
	 * encoded with a prepending length info (1 byte) containing the length of the
	 * encoded string.
	 * 
	 * @param enc
	 *          the tabular encoder object
	 * @throws TabularException
	 */
	public void encode(TabularEncoder enc) throws TabularException {
		// First write one byte of length
		writeOut(fStringValue.getString(), enc);
	}

	/**
	 * Encodes a given string.
	 * 
	 * @param s the string to encode
	 * @param enc the tabular encoder object
	 * @throws TabularException
	 */
	public static void encode(String s, TabularEncoder enc)
			throws TabularException {
		writeOut(s, enc);

	}

	/**
	 * Decodes a charstring from the provided tabular decoder
	 * 
	 * @param td	the tabular decoder object
	 * @return	the decoded string
	 * @throws TabularException
	 */
	public static String decode(TabularDecoder td) throws TabularException {
		int length = td.decodeByte();
		return td.decodeString(length);
	}

	/**
	 * Encodes a given string.
	 * 
	 * @param s the string to encode
	 * @param enc	the tabular encoder object
	 * @throws TabularException
	 */
	private static void writeOut(String s, TabularEncoder enc)
			throws TabularException {
		enc.encodeByte((byte) s.length());
		// Write out the rest
		enc.encodeOctetstring(s.getBytes());
	}

}
