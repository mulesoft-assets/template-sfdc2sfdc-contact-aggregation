/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class SFDCContactsMergeTest {
	private static final String QUERY_COMPANY_A = "contactsFromOrgA";
	private static final String QUERY_COMPANY_B = "contactsFromOrgB";

	@Mock
	private MuleContext muleContext;

	@Test
	public void testMerge() throws TransformerException {
		List<Map<String, String>> contactsA = createContactLists("A", 0, 1);
		List<Map<String, String>> contactsB = createContactLists("B", 1, 2);

		MuleMessage message = new DefaultMuleMessage(null, muleContext);
		message.setInvocationProperty(QUERY_COMPANY_A, contactsA.iterator());
		message.setInvocationProperty(QUERY_COMPANY_B, contactsB.iterator());

		SFDCContactMerge transformer = new SFDCContactMerge();
		List<Map<String, String>> mergedList = (List<Map<String, String>>) transformer.transform(message, "UTF-8");

		System.out.println(mergedList);
		Assert.assertEquals("The merged list obtained is not as expected", createExpectedList(), mergedList);

	}

	@Test
	public void testMergeWithNullEmailValues() throws TransformerException {
		List<Map<String, String>> contactsA = createContactLists("A", 0, 1);
		contactsA.get(0).put("Email", null);

		List<Map<String, String>> contactsB = createContactLists("B", 1, 2);

		MuleMessage message = new DefaultMuleMessage(null, muleContext);
		message.setInvocationProperty(QUERY_COMPANY_A, contactsA.iterator());
		message.setInvocationProperty(QUERY_COMPANY_B, contactsB.iterator());

		SFDCContactMerge transformer = new SFDCContactMerge();
		List<Map<String, String>> mergedList = (List<Map<String, String>>) transformer.transform(message, "UTF-8");

		System.out.println(mergedList);

		List<Map<String, String>> expectedList = createExpectedList();
		expectedList.get(0).put("Email", null);
		Assert.assertEquals("The merged list obtained is not as expected", expectedList, mergedList);

	}

	private List<Map<String, String>> createExpectedList() {
		Map<String, String> contact0 = new HashMap<String, String>();
		contact0.put("IDInA", "0");
		contact0.put("IDInB", "");
		contact0.put("Email", "some.email.0@fakemail.com");
		contact0.put("Name", "SomeName_0");

		Map<String, String> contact1 = new HashMap<String, String>();
		contact1.put("IDInA", "1");
		contact1.put("IDInB", "1");
		contact1.put("Email", "some.email.1@fakemail.com");
		contact1.put("Name", "SomeName_1");

		Map<String, String> contact2 = new HashMap<String, String>();
		contact2.put("IDInA", "");
		contact2.put("IDInB", "2");
		contact2.put("Email", "some.email.2@fakemail.com");
		contact2.put("Name", "SomeName_2");

		List<Map<String, String>> contactList = new ArrayList<Map<String, String>>();
		contactList.add(contact0);
		contactList.add(contact1);
		contactList.add(contact2);

		return contactList;

	}

	private List<Map<String, String>> createContactLists(String orgId, int start, int end) {
		List<Map<String, String>> contactList = new ArrayList<Map<String, String>>();
		for (int i = start; i <= end; i++) {
			contactList.add(createContact(orgId, i));
		}
		return contactList;
	}

	private Map<String, String> createContact(String orgId, int sequence) {
		Map<String, String> contact = new HashMap<String, String>();

		contact.put("Id", new Integer(sequence).toString());
		contact.put("Name", "SomeName_" + sequence);
		contact.put("Email", "some.email." + sequence + "@fakemail.com");

		return contact;
	}
}
