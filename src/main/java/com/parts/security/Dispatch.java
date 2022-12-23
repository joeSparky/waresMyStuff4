package com.parts.security;

import java.sql.SQLException;
import java.util.HashSet;

import com.db.MyConnection;
import com.db.SessionVars;
import com.errorLogging.Internals;
import com.forms.DispatchRunStuff;
import com.forms.EndOfInputException;
import com.forms.FormsArray;
import com.forms.MainPartsForm;
import com.forms.SmartForm;
import com.security.ExceptionCoding;
import com.security.Permission;
import com.security.User;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/dispatch")
public class Dispatch extends SmartForm {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8430354314875902725L;

	public Dispatch() throws Exception {
		super(null);
	}

	class MyVars extends com.forms.MyVars {

		Exception dbChangeException = null;
		boolean survived = true;

		protected MyVars(SessionVars sVars) throws Exception {
			super(sVars, Dispatch.class.getCanonicalName());
			if (get() == null) {
				survived = false;
				put();
			}

		}
	}

	private static final String MYNAME = Dispatch.class.getCanonicalName();
	private String PARTS = MYNAME + "a";
	private String LOGOUT = MYNAME + "b";
	private String RUNDBDIAGS = MYNAME + "c";
	/**
	 * should match WebServlet string
	 */
	private static String MYRETURN = "dispatch";

	// public String DISPATCHID = Utils.getNextString();

	/**
	 * create a form to select the class to run
	 * 
	 * @param sVars
	 * @return
	 * @throws Exception
	 */
	@Override
	public FormsArray getForm(SessionVars sVars) throws Exception {
//		fdfdsVars.setApToRun(GOTO, this);
		FormsArray ret = new FormsArray();
		MyVars myVars = (MyVars) new MyVars(sVars).get();
		if (myVars.dbChangeException != null) {
			ret.errorToUser(myVars.dbChangeException);
			myVars.dbChangeException = null;
		}
		User user = new User(sVars);
		user.find(sVars.getUserNumber());
		ret.rawText(user.firstName + " " + user.lastName + "<br>");
		ret.rawText("Database:" + sVars.xml.readXML(MyConnection.XMLDBNAME) + "<br>");
//			ret.rawText("Error log:" + Internals.getLogFilePathandName() + "<br>");
//		ret.rawText(myVars.currentNode.buttonName + " menu:<br>");
		ret.submitButton("Parts", PARTS);
		ret.submitButton("Log Out", LOGOUT);
		ret.submitButton("Database Diagnostics", RUNDBDIAGS);

//		for (DispatchRunStuff thisNode : myVars.currentNode.children) {
//			Permissions p = new Permissions();
//			p.add(Permission.ADMINISTRATOR);
//			if (thisNode.hasRunnables(p)) {
//				ret.submitButton(thisNode.buttonName, NODE_GOTO + " " + thisNode.thisId);
//				// ret.radioButton(NODE_GOTO, "" + thisNode.thisId,
//				// thisNode.buttonName, true, false);
//				// ret.newLine();
//			}
//		}
//		if (!myVars.currentNode.equals(mainBranch)) {
//			ret.submitButton("Go up one level", GOUP);
//			// ret.newLine();
//		}
		ret.setReturnTo(MYRETURN);
		// ret.hiddenField(FormsArray.HIDDEN, this.toString());
		return ret;

	}

	/**
	 * just dispatched a new class. use the just dispatched class for getForm
	 */
//	private SmartForm justDispatched = null;

	@Override
	public FormsArray extractParams(SessionVars sVars) throws Exception {
		FormsArray ret = new FormsArray();
		MyVars myVars = (MyVars) new MyVars(sVars).get();

		// move to login
		try {
			DBChanges.doDBChanges(sVars);
		} catch (SQLException e) {
			myVars.dbChangeException = e;
		} catch (Exception e) {
			myVars.dbChangeException = e;
		}

		// run dispatch again by default
//		ret.nextFormToRun = this;
		if (sVars.getParameterKeys().contains(PARTS)) {
			MainPartsForm mpf = new MainPartsForm(sVars);
			ret.addAll(mpf.getForm(sVars));
			throw new EndOfInputException(ret);
		}

		if (sVars.getParameterKeys().contains(LOGOUT)) {
			sVars.logout();
			Login login = new Login();
			ret.addAll(login.getForm(sVars));
			throw new EndOfInputException(ret);
		}

		if (sVars.getParameterKeys().contains(RUNDBDIAGS)) {
			DbDiagnostics login = new DbDiagnostics();
			ret.addAll(login.getForm(sVars));
			throw new EndOfInputException(ret);
		}

//			int foundID = Integer.parseInt(sVars.getParameterValue(NODE_GOTO));
//			for (DispatchRunStuff thisRun : myVars.currentNode.children) {
//				if (thisRun.thisId == foundID) {
//					myVars.currentNode = thisRun;
//					sVars.buttonName = thisRun.buttonName;
//					if (thisRun.runName.isEmpty())
//						return ret;
//					else
//						try {
//							ret.nextFormToRun = new RunApplication().dispatchThis(thisRun.runName, sVars);
//						} catch (Exception e) {
//							ret.errorToUser(e);
//						}
//					return ret;
//				}
//			}
//			ret.errorToUser(new Exception("couldn't find " + foundID));
////			Internals.dumpStringContinue("couldn't find " + foundID);
//			sVars.backToOrigin("index.html");
//			return ret;
//		}
//		if (sVars.getParameterKeys().contains(GOUP)) {
//			if (myVars.currentNode.parent == null) {
//				throw new Exception("asked to go up at top trunk of tree");
////				HandleRequest.backToLogin(sVars);
////				return ret;
//			} else {
//				myVars.currentNode = myVars.currentNode.parent;
//				return ret;
//			}
//		}
//		for (String thisId : FormsArray.getIdsAndNoValues(sVars.getParameterMap(), NODE_GOTO)) {
//			int foundID = Integer.parseInt(thisId);
//			for (DispatchRunStuff thisRun : myVars.currentNode.children) {
//				if (thisRun.thisId == foundID) {
//					myVars.currentNode = thisRun;
//					sVars.buttonName = thisRun.buttonName;
//					if (thisRun.runName.isEmpty())
//						return ret;
//					else {
////						try {
//						try {
//							ret.nextFormToRun = new RunApplication().dispatchThis(thisRun.runName, sVars);
//						} catch (NoSuchMethodException e) {
//							ret.errorToUser(e);
//							return ret;
//						} catch (SecurityException e) {
//							ret.errorToUser(e);
//							return ret;
//						} catch (InstantiationException e) {
//							ret.errorToUser(e);
//							return ret;
//						} catch (IllegalAccessException e) {
//							ret.errorToUser(e);
//							return ret;
//						} catch (IllegalArgumentException e) {
//							ret.errorToUser(e);
//							return ret;
//						} catch (InvocationTargetException e) {
//							for (StackTraceElement ste : e.getStackTrace()) {
//								ret.rawText(ste.toString());
//								ret.newLine();
//							}
//							ret.errorToUser(e);
//							return ret;
//						}
////						} catch (Exception e) {
////							ret.errorToUser(e);
////							return ret;
////						}
//						if (ret.nextFormToRun.getClass().equals(Logout.class)) {
//							Login.logOff(sVars);
//							// prompt the user to log back in
//							ret.nextFormToRun = new Login(sVars);
//						}
//						return ret;
//					}
//				}
//			}
//			throw new Exception("couldn't find " + foundID + " amoung " + myVars.currentNode.children);
////			HandleRequest.backToLogin(sVars);
////			return ret;
//		}
		return ret;
//		throw new Exception("shouldn't get here");
	}

//	DispatchRunStuff currentNode = mainBranch;
	static DispatchRunStuff mainBranch = null;
	static {
		/**
		 * top of the menu
		 */
		mainBranch = null;
		try {
			mainBranch = new DispatchRunStuff("", null, "Main");
		} catch (ExceptionCoding e) {
			Internals.logStartupError(e);
		}
		try {
			mainBranch.addChildren(new DispatchRunStuff("com.forms.MainPartsForm", Permission.USER, "Parts"));
		} catch (ExceptionCoding e) {
			Internals.logStartupError(e);
		}
//		DispatchRunStuff testingBranch = new DispatchRunStuff("", null, "Testing");
//		mainBranch.addChildren(
//				new DispatchRunStuff("com.parts.forms.CSVForm", Permission.USER, "print inventory"));
		try {
			mainBranch
					.addChildren(new DispatchRunStuff("com.parts.security.FormLogout", Permission.MANAGER, "Log Off"));
		} catch (ExceptionCoding e) {
			Internals.logStartupError(e);
		}
//		testingBranch.addChildren(
//				new DispatchRunStuff("com.parts.forms.ImportCDM", Permission.ADMINISTRATOR, "import CDM database"));
//		mainBranch.addChildren(testingBranch);

		try {
			DispatchRunStuff.findDuplicateClassName(mainBranch, new HashSet<String>());
			DispatchRunStuff.findDuplicateButtonName(mainBranch, new HashSet<String>());
		} catch (Exception e) {
			com.errorLogging.Internals.dumpException(e);
		}

	}

	@Override
	public FormsArray processButtons(SessionVars sVars) throws Exception {
		FormsArray ret = new FormsArray();
		ret.addAll(extractParams(sVars));
		ret.addAll(getForm(sVars));
		return ret;
	}
}
