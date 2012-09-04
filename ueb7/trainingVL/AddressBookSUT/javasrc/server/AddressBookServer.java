/*
 * ----------------------------------------------------------------------------
 *  (C) Copyright Testing Technologies, 2002-2003.  All Rights Reserved.
 *
 *  All copies of this program, whether in whole or in part, and whether
 *  modified or not, must display this and all other embedded copyright
 *  and ownership notices in full.
 *
 *  See the file COPYRIGHT and LICENSE for details of redistribution
 *  and use.
 *
 *  You should have received a copy of the COPYRIGHT and LICENSE file
 *  along with this file; if not, write to the Testing Technologies,
 *  Oranienburger Str. 65, 10117 Berlin, Germany.
 *
 * ----------------------------------------------------------------------------
 *
 *  AUTHOR:      George Din
 *  DATE:        August, 2003
 *
 *  REVISION INFO:
 *    $Revision: 1.1 $ $Date: 2009/12/08 16:09:10 $
 *    $Source: /usr/local/cvs_root/Consulting/Tutorial/AddressBookSUT/javasrc/server/AddressBookServer.java,v $
 *
 * ----------------------------------------------------------------------------
 */
package server;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * @author gedi
 */
public class AddressBookServer {
	private static NamingContext namingContext;
	private static ORB orb = null;
	private static org.omg.CORBA.Object nameService = null;
	private static POA rootpoa = null;

	/**
	 * The main method of the server. It initialize the connection with Corba and
	 * starts the server.
	 * 
	 * @param args
	 *          some initializatio parameters.
	 */
	public static void main(String[] args) {
		init(args);

		bindAddressBookInterface();

		startOrbThread();
	}

	public static void startOrbThread() {
		orb.run();
	}

	/**
	 * Initializes the ORB, NameService, POA and NamingContext
	 * 
	 * @param orbargs
	 *          The hostname and the port where the NamingService was started
	 * @return the status of the initialization
	 */
	public static void init(String[] orbargs) {
		try {
			// init the orb
			orb = org.omg.CORBA.ORB.init(orbargs, null);

			// get reference to rootpoa & activate the POAManager
			rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// init the NamingService
			nameService = orb.resolve_initial_references("NameService");

			// init the NamingContext
			namingContext = NamingContextHelper.narrow(nameService);
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	/**
	 * Create an object of type Interface1 and bind it to the NamingServer.
	 */
	public static void bindAddressBookInterface() {
		try {
			AddressBookInterfaceImpl aBinterface = new AddressBookInterfaceImpl();

			// bind the Object Reference in Naming
			org.omg.CORBA.Object o = rootpoa.servant_to_reference(aBinterface);

			// create a NameComponent
			NameComponent[] aBComponent = new NameComponent[1];
			aBComponent[0] = new NameComponent("addressBookServer", "");

			// bind the interface1 to the component
			namingContext.rebind(aBComponent, o);

			System.out.println("server> AddressBook is now available");
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	/**
	 * Unbind the Interface1 object.
	 */
	public static void unbindAddressBookInterface() {
		try {
			AddressBookInterfaceImpl addressBook = new AddressBookInterfaceImpl();

			// create a NameComponent
			NameComponent[] aBComponent = new NameComponent[1];
			aBComponent[0] = new NameComponent("addressBookServer", "");

			// unbind...
			namingContext.unbind(aBComponent);

			System.out.println("server> addressBook was removed");
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

}
