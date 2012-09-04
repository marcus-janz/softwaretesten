package aB;

import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.UnionValue;
import org.etsi.ttcn.tci.Value;

import com.testingtech.examples.tutorial.message.AddressBookTA;
import com.testingtech.ttcn.tci.codec.tabular.TabularDecoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularEncoder;
import com.testingtech.ttcn.tci.codec.tabular.TabularException;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;

/**
 * A helper class for encoding and decoding a Contact. A Contact can be either
 * an email or a phone number. In case of an email 0x00 is prepended, in case of
 * a phone number 0x01.
 * 
 */
public class ContactWrapper {

	private Contact fContact = null;
	private Value fContactValue = null;

	/**
	 * Constructor with TCI contact value
	 * @param contactValue	the TCI contact value
	 */
	public ContactWrapper(Value contactValue) {
		setContactValue(contactValue);
	}

	/**
	 * Constructor with Contact object
	 * @param contact	the contact object
	 */
	public ContactWrapper(Contact contact) {
		setContact(contact);
	}

	/**
	 * To set the TCI Contact value
	 * @param contactValue	the TCI contact value
	 */
	public void setContactValue(Value contactValue) {
		if (!contactValue.getType().getName().equals("Contact"))
			throw new RuntimeException(
					"Trying to wrap non Contact value. The value was " + contactValue);
		UnionValue uv = (UnionValue) contactValue;
		String contactVariant = uv.getPresentVariantName();
		Contact contact = new Contact();
		if (contactVariant.equals("email")) {
			String emailValue = ((CharstringValue) uv.getVariant(contactVariant))
					.getString();
			contact.email(emailValue);
		} else {
			PhoneNumberWrapper pnw = new PhoneNumberWrapper(uv
					.getVariant(contactVariant));
			contact.number(pnw.getPhoneNumber());
		}

		fContact = contact;
		fContactValue = uv;
	}

	/**
	 * To set the Contact object
	 * 
	 * @param contact	the Contact object
	 */
	public void setContact(Contact contact) {
		RB rb = AddressBookTA.MyExportedRB.fRB;
		Type type = rb.getTciCDRequired().getTypeForName("aB.Contact");
		UnionValue uv = (UnionValue) type.newInstance();

		switch (contact.discriminator().value()) {
		case ContactType._e_email:
			CharstringValue cv = (CharstringValue) rb.getTciCDRequired()
					.getCharstring().newInstance();
			cv.setString(contact.email());
			uv.setVariant("email", cv);
			break;
		case ContactType._e_phoneNumber:
			PhoneNumberWrapper pnw = new PhoneNumberWrapper(contact.number());
			// RecordOfValue rov = pnw.getPhoneNumberValue() ;
			uv.setVariant("number", pnw.getPhoneNumberValue());
			break;
		}

		fContact = contact;
		fContactValue = uv;
	}

	/**
	 * To get the TCI Contact value
	 * @return	the TCI Contact value
	 */
	public Value getContactValue() {
		return fContactValue;
	}

	/**
	 * To get the Contact object.
	 * @return	the Contact object
	 */
	public Contact getContact() {
		return fContact;
	}

	/**
	 * Encodes the set TCI contact value. In case of an email 0x00 is prepended,
	 * in case of a phone number 0x01.
	 * 
	 * @param enc	the tabular encoder object
	 * @throws TabularException
	 */
	public void encode(TabularEncoder enc) throws TabularException {
		UnionValue uv = (UnionValue) fContactValue;
		String contactVariant = uv.getPresentVariantName();
		if (contactVariant.equals("email")) {
			enc.encodeByte((byte) 0); // because it is email
			String emailValue = ((CharstringValue) uv.getVariant(contactVariant))
					.getString();
			// First the length of bytes
			enc.encodeByte((byte) emailValue.length());
			enc.encodeOctetstring(emailValue.getBytes());
		} else {
			enc.encodeByte((byte) 1); // because it is phone number
			PhoneNumberWrapper pnw = new PhoneNumberWrapper(uv
					.getVariant(contactVariant));
			pnw.encode(enc);
		}

	}

	/**
	 * To encode a given Contact object. In case of an email 0x00 is prepended,
	 * in case of a phone number 0x01.
	 * @param c	the Contact object
	 * @param enc	the tabular encoder
	 * @throws TabularException
	 */
	public static void encode(Contact c, TabularEncoder enc)
			throws TabularException {
		switch (c.discriminator().value()) {
		case ContactType._e_email:
			enc.encodeByte((byte) 0); // because it is email
			enc.encodeByte((byte) c.email().length());
			enc.encodeOctetstring(c.email().getBytes());
			break;
		case ContactType._e_phoneNumber:
			enc.encodeByte((byte) 1); // because it is phone number
			PhoneNumberWrapper.encode(c.number(), enc);
			break;
		}
	}

	/**
	 * Decodes a Contact from the given tabular decoder object.
	 * 
	 * @param td	the tabular decoder object
	 * @return	the decoded Contact object
	 * @throws TabularException
	 */
	public static Contact decode(TabularDecoder td) throws TabularException {
		byte selector = td.decodeByte();
		Contact contact = new Contact();
		switch (selector) {
		case 0:
			// it is an email
			// read the length
			int length = td.decodeInteger(8);
			String theEmailString = td.decodeString(length);
			contact.email(theEmailString);
			break;
		case 1:
			// is is a phone number
			int[] phoneNumber = PhoneNumberWrapper.decode(td);
			contact.number(phoneNumber);
			break;
		default:
			System.out.println("Could not decode a contact as selector was "
					+ selector);
			return null;
		}

		return contact;
	}
}
