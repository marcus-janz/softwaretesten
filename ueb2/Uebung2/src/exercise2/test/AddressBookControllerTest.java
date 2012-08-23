/*
 * Example code used in exercises for lecture "Grundlagen des Software-Testens"
 * Created and given by Ina Schieferdecker and Edzard Hoefig
 * Freie Universitaet Berlin, SS 2012
 */
package exercise2.test;

import org.junit.Before;

import exercise2.addressbook.controller.AddressBookController;
import exercise2.addressbook.controller.AddressBookControllerImpl;


/**
 * Uebung 2 - Komponenten und Integrationstest
 * Komponententests für den Controller
 * 
 * Bitte Nummer der Gruppe eintragen:
 * 0
 * 
 * Bitte Gruppenmitglieder eintragen:
 * @author Edzard Hoefig
 * @author ...
 */
public class AddressBookControllerTest {
	
	/*
	 *  Aufgabe 3
	 *  Führen Sie im Rahmen eines Komponententests der Klasse exercise2.addressbook.controller.AddressBookControllerImpl einen Test der Methode add(...) durch.
	 *  Schreiben Sie für die model und view Komponenten Mock-Up Klassen und verwenden Sie dies im Komponententest der AddressBookController Komponente.
	 *  Testen Sie die add() Methode auf Herz und Nieren - es sind durchaus Fehler zu finden.
	 */
	
	// Model component for the test
	AddressBookModelMockUp model;
	
	// View component for the test
	AddressBookViewMockUp view;
	
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
	
}
