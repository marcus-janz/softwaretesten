/*
 * Example code used in exercises for lecture "Grundlagen des Software-Testens"
 * Created and given by Ina Schieferdecker and Edzard Hoefig
 * Freie Universitaet Berlin, SS 2012
 */
package exercise2.test;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import exercise2.addressbook.controller.AddressBookController;
import exercise2.addressbook.controller.AddressBookControllerImpl;
import exercise2.addressbook.controller.ParameterException;
import exercise2.addressbook.model.AddressBookModel;
import exercise2.addressbook.model.SizeLimitReachedException;
import exercise2.addressbook.view.AddressBookView;


/**
 * Uebung 2 - Komponenten und Integrationstest
 * Komponententests für den Controller
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
public class AddressBookControllerTest {
	
	/*
	 *  Aufgabe 3
	 *  Führen Sie im Rahmen eines Komponententests der Klasse exercise2.addressbook.controller.AddressBookControllerImpl einen Test der Methode add(...) durch.
	 *  Schreiben Sie für die model und view Komponenten Mock-Up Klassen und verwenden Sie dies im Komponententest der AddressBookController Komponente.
	 *  Testen Sie die add() Methode auf Herz und Nieren - es sind durchaus Fehler zu finden.
	 */
	
	// Model component for the test
	AddressBookModel model;
	
	// View component for the test
	AddressBookView view;
	
	// Controller component for the test
	AddressBookController controller;
	
	/**
	 * Set up test system
	 */
	@Before
	public void setUp() {
		// Instantiate and wire components
		this.model = new AddressBookModelMockUp();
		this.view = new AddressBookViewMockUp();
		this.controller = new AddressBookControllerImpl(model, view);
	}
	
	// TODO: Hier die Testfälle für den Komponententest hinschreiben
	
	@Test
	public void testAddSizeLimit(){
		try {
			controller.add("SizeLimit", "lastName", "M", "12345", null);
			fail("Size Limit schould be reached.");
		} catch (ParameterException e) {
			fail("Inputs are correct, so it must not throw ParameterException.");
		} catch (SizeLimitReachedException e) {
			//expected, OK
		}
	}
	
	@Test
	public void testAddTelephone1(){
		try {
			controller.add("firstName", "lastName", "M", "df", "test");
			fail("Controller should throw a Parameter exception.");	
		} catch (ParameterException e) {
			// expected, Only one sort of contact information can be set at one time
		} catch (SizeLimitReachedException e) {
			fail("Controller should throw a ParameterException.");
		}
	}
		
	@Test
	public void testAddTelephone2(){
		try {
			controller.add("firstName", "lastName", "M", "abcde", null);
			fail("Controller should throw a ParameterException.");
		} catch (ParameterException e) {
			//expected, OK
		} catch (SizeLimitReachedException e) {
			fail("Controller should throw a ParameterException.");
		}	
	}
	
	@Test
	public void testAddGender(){
		try {
			controller.add("firstName", "lastName", "X", "12345", null);
			fail("Controller should throw a Parameter exception.");
		} catch (ParameterException e) {
			// expected, only M or F is a valid gender
		} catch (SizeLimitReachedException e) {
			fail("Controller should throw a ParameterException.");			
		}
	}
	
	@Test
	public void testAddFirstName(){
		try {
			controller.add(null, "lastName", "M", "12345", null);
			fail("Controller should throw a Parameter exception.");
		} catch (ParameterException e) {
			// expected, only M or F is a valid gender
		} catch (SizeLimitReachedException e) {
			fail("Controller should throw a ParameterException.");			
		}
	}
	
	@Test
	public void testAddLastName(){
		try {
			controller.add("firstName", null, "M", "12345", null);
			fail("Controller should throw a Parameter exception. ");
		} catch (ParameterException e) {
			// expected, only M or F is a valid gender
		} catch (SizeLimitReachedException e) {
			fail("Controller should throw a ParameterException.");			
		}
	}
	
	@Test
	public void testAddNames(){
		try {
			controller.add(null, null, "M", "12345", null);
			fail("Controller should throw a Parameter exception. ");
		} catch (ParameterException e) {
			// expected, only M or F is a valid gender
		} catch (SizeLimitReachedException e) {
			fail("Controller should throw a ParameterException.");			
		}
	}
}
