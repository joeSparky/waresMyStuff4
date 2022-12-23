package comTest.security.myLinkObject;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.db.SessionVars;
import com.security.Company;
import com.security.MyLinkObject;

import comTest.security.Recurse;
import comTest.utilities.Utilities;

public class OneToMany {
	SessionVars sVars = null;

	@Before
	public void setUp() throws Exception {
		sVars = new SessionVars(true);

		new Utilities().allNewTables(sVars);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOneToMany() {
		Parent firstParent = null;
		Parent secondParent = null;
		Child child = null;
		Company company = null;
		try {
			company = Utilities.getACompany();
			firstParent = new Parent(sVars);
			firstParent.add(company);
			secondParent = new Parent(sVars);
			secondParent.add(company);
			child = new Child(sVars);
			child.add(company);
			firstParent.addChild(child);
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		try {
			if (!new MyLinkObject(firstParent, child, sVars).linkExists(firstParent, child, sVars))
//			if (!MyLinkObject.linkExists(firstParent, child, sVars))
				fail("link not set up");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		try {
			secondParent.addChild(child);
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		try {
			if (child.listAllParentsOfType(new Parent(sVars)).size() != 2)
				fail("expected size of 2, got " + child.listAllParentsOfType(new Parent(sVars)).size());
			if (!child.listAllParentsOfType(new Parent(sVars)).containsMyObject(firstParent))
				fail("did not find firstParent");
			if (!child.listAllParentsOfType(new Parent(sVars)).containsMyObject(secondParent))
				fail("did not find secondParentParent");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	/**
	 * ask a parent to become a child of its child
	 */

	@Test
	public void illegalAddTest() {
		Recurse first = null;
		Recurse second = null;
		Company company = null;
		try {
			company = Utilities.getACompany();
			first = new Recurse(sVars);
			first.add(company);
			second = new Recurse(sVars);
			second.add(company);
			first.addChild(second);


		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		try {
			second.addChild(first);
			fail("a parent allowed to be a child of its child");
		} catch (Exception e) {
			
		}
		
	}
	
	/**
	 * ask a parent to become a child of itself
	 */

	@Test
	public void illegalParentIsChildTest() {
		Recurse first = null;
		Company company = null;
		try {
			company = Utilities.getACompany();
			first = new Recurse(sVars);
			first.add(company);
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		try {
			first.addChild(first);
			fail("a parent allowed to be a child of itself");
		} catch (Exception e) {
			
		}
		
	}

}
