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
 *  DATE:        April, 2003
 *
 *  REVISION INFO:
 *    $Revision: 1.2 $ $Date: 2010/01/07 15:37:23 $
 *    $Source: /usr/local/cvs_root/Consulting/Tutorial/AddressBookSUT/javasrc/server/AddressBookInterfaceImpl.java,v $
 *
 * ----------------------------------------------------------------------------
 */
package server;

import java.util.Date;
import java.util.HashMap;

import aB.Entry;
import aB.addressBookOperations;
import aB.addressBookPOA;

/**
 * This class implements the AddressBook interface.
 * 
 * @author gedi
 * @version $Revision: 1.2 $ $Date: 2010/01/07 15:37:23 $
 */
public class AddressBookInterfaceImpl extends addressBookPOA implements
		addressBookOperations {

	private HashMap db = new HashMap();
	private final int sizeLimit = 5;
	private int size = 0;

	private String startedAt = "";

	public AddressBookInterfaceImpl() {
		startedAt = new Date().toLocaleString();

		printStatistics();
	}

	public void addEntry(aB.Entry p_entry)
			throws aB.addressBookPackage.userExists,
			aB.addressBookPackage.sizeLimitReached {

		// printStatistics();

		System.out.println("+ add entry");
		System.out.println("	surname   = [" + p_entry.surName + "]");
		System.out.println("	firstname = [" + p_entry.firstName + "]");
		System.out.println("	gender    = [" + p_entry.gender.value() + "]");

		if (p_entry.contact.discriminator().value() == 0) {
			System.out.println("	phone     = [" + p_entry.contact.number().toString()
					+ "]");
		} else {
			System.out.println("	email     = [" + p_entry.contact.email() + "]");
		}

		String key = p_entry.surName;

		if (db.containsKey(key))
			throw new aB.addressBookPackage.userExists("this user already exists ",
					p_entry.firstName);

		if (size == sizeLimit)
			throw new aB.addressBookPackage.sizeLimitReached("maximal limit is "
					+ sizeLimit);

		size++;
		db.put(key, p_entry);


	}

	public aB.Contact getEntry(String surName)
			throws aB.addressBookPackage.notFound {
		// printStatistics();

		System.out.println("+ getEntry");
		System.out.println("	surname   = [" + surName + "]");

		if (!db.containsKey(surName))
			throw new aB.addressBookPackage.notFound("this user does not exist");

		Entry entry = (Entry) db.get(surName);
		return entry.contact;
	}

	public void clear() {
		size = 0;
		db = new HashMap();

		printStatistics();

		System.out.println("+ clear all");
	}

	void printStatistics() {
		// System.out.print ("\033c");
		//
		// System.out.println("usage = " + size);
		// System.out.println("maximal limit = " + sizeLimit);
		// System.out.println("running since = " + startedAt);
		// System.out.println("\n\n\n");
	}
}
