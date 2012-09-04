package aB;

import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.EnumeratedValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.UnionValue;
import org.etsi.ttcn.tci.Value;

import com.testingtech.examples.tutorial.message.AddressBookTA;
import com.testingtech.ttcn.tci.codec.tabular.TabularDecoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;

/**
 * A helper class for encoding and decoding an Entry. An entry consists of the
 * fields:
 * <ul>
 * <li>firstName
 * <li>surName
 * <li>gender
 * <li>contact
 * </ul>
 */
public class EntryWrapper {
	private Entry fEntry = null;
	private Value fEntryValue = null;

	/**
	 * Default constructor
	 */
	public EntryWrapper() {
	}

	/**
	 * Constructor with TCI Entry value
	 * @param entryValue the TCI Entry value
	 */
	public EntryWrapper(Value entryValue) {
		setEntryValue(entryValue);
	}

	/**
	 * Constructor with Entry object
	 * @param entry	the entry object
	 */
	public EntryWrapper(Entry entry) {
		setEntry(entry);
	}

	/**
	 * Returns the set entry object.
	 * @return	the set entry object
	 */
	public Entry getEntry() {
		return fEntry;
	}

	/**
	 * To set the entry object.
	 * @param theEntry	the entry object
	 */
	public void setEntry(Entry theEntry) {
		RB rb = AddressBookTA.MyExportedRB.fRB;
		Type type = rb.getTciCDRequired().getTypeForName("Entry");
		RecordValue rv = (RecordValue) type.newInstance();

		CharstringValue firstNameV = (CharstringValue) rb.getTciCDRequired()
				.getCharstring().newInstance();
		firstNameV.setString(theEntry.firstName);
		CharstringValue lastNameV = (CharstringValue) rb.getTciCDRequired()
				.getCharstring().newInstance();
		lastNameV.setString(theEntry.surName);
		ContactWrapper cw = new ContactWrapper(theEntry.contact);
		UnionValue contactV = (UnionValue) cw.getContactValue();

		GenderWrapper gw = new GenderWrapper(theEntry.gender);
		EnumeratedValue genderV = (EnumeratedValue) gw.getGenderValue();

		rv.setField("firstName", firstNameV);
		rv.setField("surName", lastNameV);
		rv.setField("gender", genderV);
		rv.setField("contact", contactV);

		fEntryValue = rv;
		fEntry = theEntry;

	}

	/**
	 * Returns the set TCI Entry value
	 * 
	 * @return	the set TCI Entry value
	 */
	public Value getEntryValue() {
		return fEntryValue;
	}

	/**
	 * To set the TCI entry value.
	 * 
	 * @param entryValue the TCI entry value
	 */
	public void setEntryValue(Value entryValue) {

		if (!entryValue.getType().getName().equals("Entry"))
			throw new RuntimeException(
					"Trying to wrap a non Entry value to EntryWrapper. Value was "
							+ entryValue);
		RecordValue rv = (RecordValue) entryValue;
		String firstName = ((CharstringValue) rv.getField("firstName")).getString();
		String surName = ((CharstringValue) rv.getField("surName")).getString();
		ContactWrapper cw = new ContactWrapper(rv.getField("contact"));
		Contact contact = cw.getContact();

		GenderWrapper gw = new GenderWrapper(rv.getField("gender"));
		Gender gender = gw.getGender();

		fEntry = new Entry(surName, firstName, gender, contact);
		fEntryValue = entryValue;

	}

	/**
	 * Encodes the set Entry value.
	 * 
	 * @param enc	the tabular encoder
	 * @throws TabularException
	 */
	public void encode(TabularEncoder enc) throws TabularException {
		RecordValue rv = (RecordValue) fEntryValue;

		StringWrapper sw1 = new StringWrapper(rv.getField("firstName"));
		sw1.encode(enc);

		StringWrapper sw2 = new StringWrapper(rv.getField("surName"));
		sw2.encode(enc);

		GenderWrapper gw = new GenderWrapper(rv.getField("gender"));
		gw.encode(enc);

		ContactWrapper cw = new ContactWrapper(rv.getField("contact"));
		cw.encode(enc);

	}

	
	/**
	 * Decodes an Entry from a given tabular decoder object
	 * @param td	the tabular decoder object
	 * @return	the decoded Entry
	 * @throws TabularException
	 */
	public static Entry decode(TabularDecoder td) throws TabularException {
		String fN = StringWrapper.decode(td);
		String sN = StringWrapper.decode(td);
		Gender gender = GenderWrapper.decode(td);
		Contact contact = ContactWrapper.decode(td);

		return new Entry(sN, fN, gender, contact);
	}
}
