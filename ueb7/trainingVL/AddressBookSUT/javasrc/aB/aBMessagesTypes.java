package aB;

/**
 * Class defining the message types for all kind of protocol data units (PDUs)
 * used for the communication between the test system and the system under test.
 * 
 */
public interface aBMessagesTypes {
	public static final byte ADD_ENTRY = 0x01;
	public static final byte GET_ENTRY = 0x02;
	public static final byte CLEAR = 0x03;
	public static final byte GET_ENTRY_REPLY = 0x02;

	public static final byte EX_USER_EXIST = 0x20;
	public static final byte EX_NOT_FOUND = 0x21;
	public static final byte EX_SIZE_LIMIT_REACHED = 0x22;
}
