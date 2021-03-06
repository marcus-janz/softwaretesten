/*
 * Example code used in exercises for lecture "Grundlagen des Software-Testens"
 * Created and given by Ina Schieferdecker and Edzard Hoefig
 * Freie Universitaet Berlin, SS 2012
 */
package exercise2.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import exercise2.addressbook.controller.AddressBookController;
import exercise2.addressbook.controller.AddressBookControllerImpl;
import exercise2.addressbook.controller.ParameterException;
import exercise2.addressbook.model.AddressBookModel;
import exercise2.addressbook.model.AddressBookModelImpl;
import exercise2.addressbook.model.Entry;
import exercise2.addressbook.model.SizeLimitReachedException;

/**
 * Uebung 2 - Komponenten und Integrationstest
 * Integration Test für Addressbook und Controller.
 * 
 * Bitte Nummer der Gruppe eintragen:
 * 10
 * 
 * Bitte Gruppenmitglieder eintragen:
 * @author René Perschon
 * @author Martin Schulze
 * @author Anselm Brachmann
 * @author Marcus Janz
 */
public class ControllerAddressBookIntegrationTest {

	// Location of the address book file
	private static final File addressBookFile = new File("contacts.xml");
		
	/*
	 *  Aufgabe 4
	 *  Programmieren Sie einen Integrationstest für AddressBookModel und AddressBookController.
	 *  Testen Sie ob die Methoden des exercise2.addressbook.controller.AddressBookController Interface zu den erwarteten Resultate im Addressbuch führen.
	 *  Testen Sie intensiv und schreiben Sie MINDESTENS einen Testfall pro Methode des interfaces. Es sind Fehler zu finden.  
	 */
	
	// Model component for the test
	AddressBookModel model;
	
	// View component for the test
	AddressBookViewMockUp view;
	
	// Controller component for the test
	AddressBookController controller;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Instantiate and wire components
		this.model = new AddressBookModelImpl(addressBookFile);
		this.view = new AddressBookViewMockUp();
		this.controller = new AddressBookControllerImpl(model, view);
	}

	// TODO: Hier die Testfälle für den Integrationstest hinschreiben

	@Test
	public void testAddFunctional(){
		try {
			controller.add("firstName", "lastName", "M", "12345", null);
			Entry e=model.getEntry("lastName", "firstName");
			assertTrue("Added Entry has not the same firstname.",e.getFirstName().equals("firstName"));
			assertTrue("Added Entry has not the same lastname.",e.getSurName().equals("lastName"));
			assertTrue("Added Entry has not the same gender.",e.isMale());
			assertTrue("Added Entry has not the same contactInfo.",e.getContactInformation().toString().equals("12345"));
		} catch (ParameterException e) {
			fail("Inputs are correct, so it must not throw ParameterException.");
		} catch (SizeLimitReachedException e) {
			fail("SizeLimit can't be reached at this Point.");
		}
	}
	
	@Test
	public void testAddSizeLimit(){
		try {
			controller.add("SizeLimit", "lastName", "M", "12345", null);
			controller.add("SizeLimit1", "lastName1", "M", "12345", null);
			controller.add("SizeLimit2", "lastName2", "M", "12345", null);
			controller.add("SizeLimit3", "lastName3", "M", "12345", null);
			controller.add("SizeLimit4", "lastName4", "M", "12345", null);
			controller.add("SizeLimit5", "lastName5", "M", "12345", null);
			controller.add("SizeLimit6", "lastName6", "M", "12345", null);
			controller.add("SizeLimit7", "lastName7", "M", "12345", null);
			controller.add("SizeLimit8", "lastName8", "M", "12345", null);
			controller.add("SizeLimit9", "lastName9", "M", "12345", null);
			controller.add("SizeLimit10", "lastName10", "M", "12345", null);
			fail("Size Limit schould be reached.");
		} catch (ParameterException e) {
			fail("Inputs are correct, so it must not throw ParameterException.");
		} catch (SizeLimitReachedException e) {
			//expected, OK
			assertTrue("Entry must not be added.",model.getEntries().size()==10);
		}
	}
	
	@Test
	public void testAddParameterException(){
		try {
			controller.add("firstName", "lastName", "M", "df", "test");
			fail("Controller should throw a Parameter exception.");	
		} catch (ParameterException e) {
			// expected, Only one sort of contact information can be set at one time
			assertTrue("Entry must not be added.",model.getEntries().size()==0);
		} catch (SizeLimitReachedException e) {
			fail("Controller should throw a ParameterException.");
		}
	}
	
	@Test
	public void testErase(){
		try {
			controller.add("firstName", "lastName", "M", "12345", null);
		} catch (ParameterException e) {
			fail("Inputs are correct, so it must not throw ParameterException.");
		} catch (SizeLimitReachedException e) {
			fail("SizeLimit can't be reached at this Point.");
		}
		controller.erase();
		assertTrue("Model should be empty.",model.getEntries().size()==0);
	}
	
	@Test
	public void testEraseOnEmpty(){
		controller.erase();
		assertTrue("Model should be empty.",model.getEntries().size()==0);
	}
	
	@Test
	public void testRemove(){
		try {
			controller.add("firstName", "lastName", "M", "12345", null);
			controller.add("firstName2", "lastName2", "M", "12345", null);
		} catch (ParameterException e) {
			fail("Inputs are correct, so it must not throw ParameterException.");
		} catch (SizeLimitReachedException e) {
			fail("SizeLimit can't be reached at this Point.");
		}
		controller.remove(1);
		assertNull("Model must not contain this entry.",model.getEntry("lastName2", "firstName2"));
		assertNotNull("Model should contain this entry.",model.getEntry("lastName", "firstName"));
	}
	
	@Test
	public void testRemoveOnNoneExisting(){
		try {
			controller.add("firstName", "lastName", "M", "12345", null);
			controller.add("firstName2", "lastName2", "M", "12345", null);
		} catch (ParameterException e) {
			fail("Inputs are correct, so it must not throw ParameterException.");
		} catch (SizeLimitReachedException e) {
			fail("SizeLimit can't be reached at this Point.");
		}
		controller.remove(3);
	}
}
