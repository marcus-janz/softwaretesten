/*
 * Example code used in exercises for lecture "Grundlagen des Software-Testens"
 * Created and given by Ina Schieferdecker and Edzard Hoefig
 * Freie Universitaet Berlin, SS 2012
 */
package exercise2.addressbook.model;


/**
 * Combines access to address book and persistent storage in one interface
 * @author Edzard Hoefig
 */
public interface AddressBookModel extends AddressBookAccess, PersistentStorageAccess {
	// Just a combination of two interfaces
}
