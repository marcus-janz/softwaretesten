package aB;

import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.Value;

import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;

/**
 * A helper class to encode a GetEntry value. The encoded message starts with
 * the byte 0x02 and follows with the encoded surName.
 */
public class GetEntryWrapper implements aBMessagesTypes {

	private RecordValue fVal;

	/**
	 * Contructor
	 * 
	 * @param val the GetEntry value
	 */
	public GetEntryWrapper(Value val) {
		fVal = (RecordValue) val;
	}

	/**
	 * Encodes the set GetEntry value. The encoded message starts with the byte
	 * 0x02 and follows with the encoded surName.
	 * 
	 * @param enc
	 *          the tabular encoder object
	 * @throws TabularException
	 */
	public void encode(TabularEncoder enc) throws TabularException {
		Value val = fVal.getField("surName");
		StringWrapper sw = new StringWrapper(val);
		enc.encodeByte(GET_ENTRY); // The message type
		sw.encode(enc);
	}

}
