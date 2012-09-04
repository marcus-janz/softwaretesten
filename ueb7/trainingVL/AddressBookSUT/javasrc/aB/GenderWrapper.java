package aB;

import org.etsi.ttcn.tci.EnumeratedValue;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.Value;

import com.testingtech.examples.tutorial.message.AddressBookTA;
import com.testingtech.ttcn.tci.codec.tabular.TabularDecoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;

/**
 * A helper class to encode and decode a Gender. A Gender is encoded as 0x00 for
 * e_male and as 0x01 in case of e_female.
 * 
 */
public class GenderWrapper {

	private Gender fGender = null;
	private EnumeratedValue fGenderValue = null;

	/**
	 * Constructor
	 * 
	 * @param genderValue the TCI Gender value
	 */
	public GenderWrapper(Value genderValue) {
		setGenderValue(genderValue);
	}

	/**
	 * Constructor
	 * 
	 * @param gender the Gender object
	 */
	public GenderWrapper(Gender gender) {
		setGender(gender);
	}

	/**
	 * To set the TCI Gender value.
	 * 
	 * @param genderValue the TCI Gender value
	 */
	public void setGenderValue(Value genderValue) {
		if (!genderValue.getType().getName().equals("Gender"))
			throw new RuntimeException("Trying to wrap non Gender value. Value was "
					+ genderValue);

		EnumeratedValue ev = (EnumeratedValue) genderValue;

		Gender g = null;
		String enumeration = ev.getEnum();
		if (enumeration.equals("e_male"))
			g = Gender.e_male;
		else
			g = Gender.e_female;

		fGender = g;
		fGenderValue = ev;

	}

	/**
	 * To set the Gender object.
	 * 
	 * @param gender the Gender object
	 */
	public void setGender(Gender gender) {
		RB rb = AddressBookTA.MyExportedRB.fRB;
		Type type = rb.getTciCDRequired().getTypeForName("Gender");
		EnumeratedValue ev = (EnumeratedValue) type.newInstance();
		switch (gender.value()) {
		case 0:
			ev.setEnum("e_male");
			break;
		case 1:
			ev.setEnum("e_female");
			break;
		}

		fGender = gender;
		fGenderValue = ev;
	}

	/**
	 * returns the set Gender object
	 * 
	 * @return	the Gender object
	 */
	public Gender getGender() {
		return fGender;
	}

	/**
	 * returns the set TCI Gender value
	 * 
	 * @return	the TCI Gender value
	 */
	public EnumeratedValue getGenderValue() {
		return fGenderValue;
	}

	/**
	 * Encodes the set Gender. A Gender is encoded as 0x00 for e_male and as 0x01
	 * in case of e_female.
	 * 
	 * @param enc
	 *          the tabular encoder object
	 */
	public void encode(TabularEncoder enc) {
		try {
			switch (fGender.value()) {
			case 0:

				enc.encodeByte((byte) 0x00);
				break;
			case 1:
				enc.encodeByte((byte) 0x01);
				break;
			}

		} catch (TabularException e) {
			throw new RuntimeException("TabularException in GenderWrapper " + e);
		}
	}

	/**
	 * Decodes a Gender from the given tabular decoder object.
	 * 
	 * @param td	the tabular decoder object
	 * @return	the decoded gender object
	 * @throws TabularException
	 */
	public static Gender decode(TabularDecoder td) throws TabularException {
		byte theGender = td.decodeByte();
		return new Gender(theGender);
	}

}
