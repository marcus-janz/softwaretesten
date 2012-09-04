package aB;

import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.Value;

import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;


/**
 * A helper class to encode a AddEntry value. The encoded message starts with
 * the byte 0x01 and follows with the encoded Entry.
 */
public class AddEntryWrapper implements aBMessagesTypes {

	private RecordValue fVal;

	/**
	 * Constructor with TCI Entry value
	 * @param val	the TCI Entry value
	 */
	public AddEntryWrapper(Value val) {
		fVal = (RecordValue) val;
	}

	/**
	 * Encodes the TCI Entry value. The encoded message starts with the byte 0x01
	 * and follows with the encoded Entry.
	 * 
	 * @param enc	the tabular encoder
	 * @throws TabularException
	 */
	public void encode(TabularEncoder enc) throws TabularException {
		Value val = fVal.getField("entry");
		enc.encodeByte(ADD_ENTRY); // The message type
		EntryWrapper ew = new EntryWrapper(val);
		ew.encode(enc);
	}

}
