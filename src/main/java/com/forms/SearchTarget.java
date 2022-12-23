package com.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.db.SessionVars;
import com.parts.forms.AttachToFormLocationLocation;
import com.parts.forms.PartLocationLocation;
import com.parts.inOut.Part;
import com.parts.location.Location;
import com.security.Anchor;
import com.security.MyLinkObject;
import com.security.MyObject;

/**
 * Presented to the user as a tab in a row of tabs. The tab represents a
 * MyObject as well as the MyObject's ancestors, descendants, inventory status,
 * etc.
 * 
 * @author joe
 *
 */
public class SearchTarget {

	/**
	 * A textual context (path) to the ancestors of myObject. For example, if
	 * myObject was a parts bin, the ancestors context would be
	 * shop->basement->laundry room->north wall->parts bin. Associated with
	 * ancestorTargets
	 */
	public String ancestorSource = null;
	/**
	 * A textual context (path) to the descendants of myObject. Associated with
	 * descendantTargets.
	 */
	public String descendantSource = null;
	public MyObject obj = null;
	public String NAMEBOX;
	public String SEARCHBOX;
	public static final int KEYWORDLENGTH = 50;
	public boolean objectSelectedLastTime = false;
	public int row = -1;
	public int column = -1;
	/**
	 * the select to use for this object
	 */
	public SmartForm selector = null;

	public enum EDITSELECTTYPE {
		NOTINITIALIZED, SELECT, EDITANDSELECT, NEITHER
	};

	public enum SEARCHTYPES {
		DESCENDANTS, INVENTORY, INVENTORYLINKS, ORPHANS, ANCESTORS, ALL
	}

	public static String getIdAndStringLabels(SEARCHTYPES types) {
		switch (types) {
		case ALL:
			return "All";
		case ANCESTORS:
			return "Ancestors";
		case DESCENDANTS:
			return "Descendants";
		case INVENTORY:
			return "Inventory";
		case INVENTORYLINKS:
			return "Inventory Links";
		case ORPHANS:
			return "Orphans";
		}
		return null;
	}

	public EDITSELECTTYPE editSelectType = EDITSELECTTYPE.NOTINITIALIZED;

//	public SearchTarget(FormsMatrixDynamic fmd) {
//		clear();
////		setFmd(fmd);
//	}

	public SearchTarget(MyObject obj, SessionVars sVars) {
		this.sVars = sVars;
		this.obj = obj;
		clear();
	}

	SessionVars sVars = null;
	public FormsMatrixDynamic fmd = null;

	HashMap<SEARCHTYPES, IdAndStrings> idAndStrings = new HashMap<SEARCHTYPES, IdAndStrings>();

	public IdAndStrings getIdAndStrings(SearchTarget.SEARCHTYPES type) throws Exception {
		if (!idAndStrings.containsKey(type))
			throw new Exception(type.toString() + " is not in " + idAndStrings.toString());
		return idAndStrings.get(type);
	}

	public void clear() {
		ancestorSource = descendantSource = "";
		for (SEARCHTYPES s : SEARCHTYPES.values()) {
			IdAndStrings tmp = new IdAndStrings(fmd, s, sVars);
			idAndStrings.put(s, tmp);
		}
	}

	SelectForm sf = null;
//	AttachToFormPair toTheLeftAndRight = null;
//	AttachToFormPair toTheRight = null;
//	AttachToFormPair toTheLeft = null;

	String CANCELOBJECT = Utils.getNextString();
	String CLEARSEARCH = Utils.getNextString();

	public FormsArray getForm(SessionVars sVars) throws Exception {
		FormsArray ret = new FormsArray();

		switch (sVars.fmd.direction) {
		case PARTNERBOTHSIDES:
			if (sVars.fmd.isSomethingToMyLeft() && sVars.fmd.isSomethingToMyRight())
				ret.addAll(new PartLocationLocation(sVars).getForm(sVars));
			break;
		case PARTNERTOTHELEFT:
			if (sVars.fmd.isSomethingToMyLeft())
				if (sVars.fmd.getToMyLeft().obj instanceof Part && sVars.fmd.getSearchTarget().obj instanceof Location)
					ret.addAll(new AttachToFormPartLocation(sVars).getForm(sVars));
				else if (sVars.fmd.getToMyLeft().obj instanceof Location
						&& sVars.fmd.getSearchTarget().obj instanceof Location)
					ret.addAll(new AttachToFormLocationLocation(sVars).getForm(sVars));
				else
					ret.addAll(new AttachToFormPair(sVars).getForm(sVars));
			break;
		case PARTNERTOTHERIGHT:
			if (sVars.fmd.isSomethingToMyRight())
				if (sVars.fmd.getSearchTarget().obj instanceof Part && sVars.fmd.getToMyRight().obj instanceof Location)
					ret.addAll(new AttachToFormPartLocation(sVars).getForm(sVars));
				else if (sVars.fmd.getSearchTarget().obj instanceof Location
						&& sVars.fmd.getToMyRight().obj instanceof Location)
					ret.addAll(new AttachToFormLocationLocation(sVars).getForm(sVars));
				else
					ret.addAll(new AttachToFormPair(sVars).getForm(sVars));
			break;
		case NOT_INITIALIZED:
			break;
		case NONE:
			// add a MyObject or change its name
			ret.addAll(editOrAddForm());

			// allow the user to search for a MyObject
			ret.addAll(obj.getSearchForm());

			// for each of the search type, such as ancestors, descendants, orphans, ...
			for (SEARCHTYPES s : SEARCHTYPES.values()) {
				IdAndStrings tmp = idAndStrings.get(s);
				ret.addAll(tmp.getForm(sVars));
			}

			if (!obj.searchString.isEmpty())
				ret.submitButton("Clear search field", CLEARSEARCH);
			ret.addAll(new SelectForm(sVars).getForm(sVars));
			break;
		default:
			break;

		}

//
//		
//		
//		ret.addAll(sf.getForm(sVars));
//		if (toTheLeft != null)
//			ret.addAll(toTheLeft.getForm(sVars));
//		if (toTheRight != null)
//			ret.addAll(toTheRight.getForm(sVars));
		return ret;
	}

//	protected AttachToFormPair getToTheLeft() throws Exception {
//		// override AttachToFormPair if we have a (part, location) or (location,
//		// location)
//		SearchTarget me = fmd.getSearchTarget();
//		if (me.row != fmd.row)
//			throw new Exception("rows are not the same");
//		if (me.column != fmd.column)
//			throw new Exception("columns not the same");
//
//		// if there's nothing to the left
//		if (fmd.column == 0)
//			return null;
//		SearchTarget toTheLeft = fmd.getToMyLeft();
//		if (me.obj instanceof Location) {
//			if (toTheLeft.obj instanceof Part)
//				return new AttachToFormPartLocation(fmd, sVars, toTheLeft, me);
//			if (toTheLeft.obj instanceof Location)
//				return new AttachToFormLocationLocation(fmd, sVars, toTheLeft, me);
//		}
//		return new AttachToFormPair(sVars, toTheLeft, me);
//	}

	/**
	 * return the search target to my left in the FormsMatrixDynaic row
	 * 
	 * @return
	 */
	SearchTarget getToMyLeft() {
		if (fmd == null)
			return null;
		if (column > 0)
			return fmd.get(row).get(column - 1);
		else
			return null;
	}

	/**
	 * return the search target to my right in the FormsMatrixDynamic row
	 * 
	 * @return
	 */
	SearchTarget getToMyRight() {
		if (fmd == null)
			return null;
		if (column + 1 < fmd.getRowSize())
			return fmd.get(row).get(column + 1);
		else
			return null;
	}

//	protected AttachToFormPair getToTheLeftAndRight() throws Exception {
//		/**
//		 * if there's no entry to the left
//		 */
//		if (fmd.column == 0)
//			throw new ExceptionCoding("no SearchTarget to the left.");
//		/**
//		 * looking for a special case of (part, location) or (location, location).
//		 * Otherwise, return the normal AttacheToFormPair
//		 */
//
//		SearchTarget me = fmd.get(fmd.row).get(fmd.column);
//		SearchTarget toTheLeft = fmd.get(fmd.row).get(fmd.column - 1);
//
//		// if the current SearchTarget is a location
//		if (me.obj instanceof com.parts.location.Location) {
//			// if the SearchTarget on the left is a part
//			if (toTheLeft.obj instanceof com.parts.inOut.Part)
//				return new AttachToFormPartLocation(fmd, sVars, toTheLeft, me);
//			else if (toTheLeft.obj instanceof com.parts.location.Location)
//				// if the SearchTarget on the left is a location
//				return new AttachToFormLocationLocation(fmd, sVars, toTheLeft, me);
//		}
//
//		return new AttachToFormPair(fmd, sVars);
//	}

//	protected AttachToFormPair getToTheRight() throws Exception {
//		/**
//		 * looking for a special case of (part, location) or (location, location).
//		 * Otherwise, return the normal AttacheToFormPair, or null if there's nothing to
//		 * the right
//		 */
//		// if there's not an entry to the right
//		if (fmd.column + 1 >= fmd.getRowSize())
//			return null;
//
//		SearchTarget me = fmd.getSearchTarget();
//		SearchTarget toTheRight = fmd.getToMyRight();
//
//		// if the SearchTarget on the right is a location
//		if (toTheRight.obj instanceof com.parts.location.Location) {
//			// if the current SearchTarget is a part
//			if (me.obj instanceof com.parts.inOut.Part)
//				return new AttachToFormPartLocation(fmd, sVars, me, toTheRight);
//			// if the current searchTarget is a location
//			if (me.obj instanceof com.parts.location.Location)
//				return new AttachToFormLocationLocation(fmd, sVars, me, toTheRight);
//		}
//		return new AttachToFormPair(fmd, sVars, me, toTheRight);
//
//	}

	FormsArray editOrAddForm() throws Exception {
		FormsArray ret = new FormsArray();
		ret.newLine();
		ret.startTable();
//		MyObject obj = getObject();
		if (obj.isLoaded())
			ret.rawText("Edit the name of " + obj.getLogicalName());
		else
			ret.rawText("Add a new " + obj.getLogicalName());
		NAMEBOX = Utils.getNextString();
		ret.addAll(obj.getNameForm(NAMEBOX, obj.isLoaded()));
		ret.endTable();
		return ret;
	}

	public FormsArray extractParams(SessionVars sVars) throws Exception {
		FormsArray ret = new FormsArray();
		// add a MyObject or edit its name
		ret.addAll(extractEditAddFormParams(sVars));

		if (sVars.hasParameterKey(CANCELOBJECT)) {
			fmd.getObject().clear();
			fmd.resetAllIdAndStrings();
			throw new EndOfInputException(ret);
		}

		if (sVars.hasParameterKey(CLEARSEARCH)) {
			obj.searchString = "";
			throw new EndOfInputException(ret);
		}

		boolean searchChanged = false;
		try {
			ret.addAll(obj.extractSearchFormParams(sVars));
		} catch (EndOfInputException e) {
			searchChanged = true;
		}
		if (searchChanged) {
			for (SEARCHTYPES s : SEARCHTYPES.values())
				idAndStrings.get(s).clear();
			throw new EndOfInputException(ret);
		}
		for (SEARCHTYPES s : SEARCHTYPES.values())
			ret.addAll(idAndStrings.get(s).extractParams(sVars));

//		for (FormsMatrixDynamic.PARTNER direction : FormsMatrixDynamic.PARTNER.values()) {
		switch (sVars.fmd.direction) {
		case PARTNERBOTHSIDES:
			if (sVars.fmd.isSomethingToMyLeft() && sVars.fmd.isSomethingToMyRight())
				ret.addAll(new PartLocationLocation(sVars).extractParams(sVars));
			break;
		case PARTNERTOTHELEFT:
//				if (sVars.fmd.isSomethingToMyLeft())
//					ret.addAll(new AttachToFormPair(sVars).extractParams(sVars));

			if (sVars.fmd.isSomethingToMyLeft())
				if (sVars.fmd.getToMyLeft().obj instanceof Part && sVars.fmd.getSearchTarget().obj instanceof Location)
					ret.addAll(new AttachToFormPartLocation(sVars).extractParams(sVars));
				else if (sVars.fmd.getToMyLeft().obj instanceof Location
						&& sVars.fmd.getSearchTarget().obj instanceof Location)
					ret.addAll(new AttachToFormLocationLocation(sVars).extractParams(sVars));
				else
					ret.addAll(new AttachToFormPair(sVars).extractParams(sVars));
			break;
		case PARTNERTOTHERIGHT:
//				if (sVars.fmd.isSomethingToMyRight())
//					ret.addAll(new AttachToFormPair(sVars).extractParams(sVars));
//				break;

			if (sVars.fmd.isSomethingToMyRight())
				if (sVars.fmd.getSearchTarget().obj instanceof Part && sVars.fmd.getToMyRight().obj instanceof Location)
					ret.addAll(new AttachToFormPartLocation(sVars).extractParams(sVars));
				else if (sVars.fmd.getSearchTarget().obj instanceof Location
						&& sVars.fmd.getToMyRight().obj instanceof Location)
					ret.addAll(new AttachToFormLocationLocation(sVars).extractParams(sVars));
				else
					ret.addAll(new AttachToFormPair(sVars).extractParams(sVars));
			break;
		case NOT_INITIALIZED:
			break;
		case NONE:
			ret.addAll(new SelectForm(sVars).extractParams(sVars));
			break;
		default:
			break;

		}
//		}
		return ret;
	}

	public FormsArray extractEditAddFormParams(SessionVars sVars) throws Exception {
		FormsArray ret = new FormsArray();

		if (obj.nameChanged(sVars, NAMEBOX)) {
			obj.extractName(sVars, NAMEBOX);
			MyObject tmp = obj.getNew();
			boolean nameFound = false;
			try {
				tmp.find(obj.getInstanceName());
				nameFound = true;
			} catch (Exception e) {
			}
			if (nameFound) {
				ret.errorToUser(obj.getInstanceName() + " already exists.");
				// clear the name from the instance
				obj.clear();
				throw new EndOfInputException(ret);
			}
			ret.addAll(obj.doSanityUpdateAddTryAgain(fmd));
			fmd.resetAllIdAndStrings();
			throw new EndOfInputException(ret);
		}
		return ret;
	}

//	boolean atLeastOne(String query) {
//		return true;
//	}
//
//	FormsArray downButton(SEARCHTYPES s) {
////		FormsArray ret = new FormsArray();
//		return new FormsArray().submitButton(getIdAndStringLabels(s), getIdAndStringLabels(s) + "downButton");
////		return new FormsArray();
//	}
//
//	FormsArray upButton(SEARCHTYPES s) {
//		return new FormsArray().submitButton(getIdAndStringLabels(s), getIdAndStringLabels(s) + "upButton");
//	}
//
//	FormsArray nextBatch(SEARCHTYPES s) {
//		return new FormsArray();
//	}
//
//	/**
//	 * the user wants to choose an object from a list
//	 * 
//	 * @param s
//	 * @return
//	 */
//	FormsArray wantsToChooseButton(SEARCHTYPES s) {
//		return new FormsArray().submitButton(getIdAndStringLabels(s), getIdAndStringLabels(s) + "chooseButton");
//	}

	/**
	 * check from the top of the array up to but NOT including the caller if every
	 * instance is loaded and there's a link between each layer.
	 * 
	 * @param indexOfCaller
	 * @return
	 * @throws Exception
	 */
	public boolean linkedAndLoadedToTheTop(int indexOfCaller) throws Exception {
		// from the top of the array to the caller
		for (int index = 0; index < indexOfCaller; index++) {
			if (!fmd.get(fmd.row).get(index).obj.isLoaded())
				return false;
			// if there's an instance below index
			if (index < indexOfCaller) {
				// if a link does not exist
				if (!fmd.get(fmd.row).get(index).obj.linkToChildExists(fmd.get(fmd.row).get(index + 1).obj))
					return false;
			}
		}
		return true;
	}

	public Anchor getSearchTargetsAnchor(MyObject whosAsking) throws Exception {
		MyObject obj = fmd.get(fmd.row).get(fmd.column).obj;
		if (!obj.isLoaded())
			throw new Exception(obj.getMyFileName() + " is not loaded. " + new Exception().getStackTrace()[0]);
		return obj.getAnchor();
	}

	void setDescendants() throws Exception {
		// if the first entry (company) is not loaded, something is wrong
//		if (!objs.get(0).obj.isLoaded())
//			throw new Exception("first entry not loaded");

		String descendantSource = "";
		// for each column in the row
		for (int myIndex = 0; myIndex < fmd.get(fmd.row).size(); myIndex++) {

			fmd.get(fmd.row).get(myIndex).descendantSource = descendantSource;
			MyObject obj = fmd.get(fmd.row).get(myIndex).obj;
			if (obj.isLoaded()) {
				// update the descendantSource with the latest layer information
				descendantSource = obj.getAName() + ":" + obj.getInstanceName();
				// don't create descendants for a loaded layer
				continue;
			}
		}
	}

	public String lastJoin(int layer, String linkFileName, String thisLevelFileName) {
		String ret = "";
		if (layer + 1 == fmd.column)
			ret += " AND " + linkFileName + ".childId = " + thisLevelFileName + ".id";
		ret += ")";
		return ret;
	}

	public String setDescendantsQuery(int firstRecordDisplayed) throws Exception {
		// tables that we've already included in the query
		Collection<String> tables = new ArrayList<String>();
		String query = "";
		// the top row can not be descendants (no parents above them)
		if (fmd.column == 0 ||
		// or the row is loaded
				obj.isLoaded()) {
			// return a query that wont find any records
			return "";
		}
		int lowestSelected = fmd.get(fmd.row).getLowestLoadedIndexAbove(fmd.column);
		if (lowestSelected == -1)
			return "";
//			throw new Exception("lowestSelected = -1");

		String thisLevelFileName = obj.getMyFileName();
		// build a list of link files between the layers
		ArrayList<String> layers = new ArrayList<String>();
		for (int layer = lowestSelected; layer < fmd.column; layer++) {
			layers.add(MyLinkObject.getFileName(fmd.get(fmd.row).get(layer).obj, fmd.get(fmd.row).get(layer + 1).obj,
					sVars));
		}

		query += "SELECT DISTINCT " + thisLevelFileName + "." + "id, ";
		query += thisLevelFileName + ".name";
		query += insertSearchMatch(obj);
		query += " from ";
		query += obj.getMyFileName() + " ";
		tables.add(obj.getMyFileName());
		// for the first layer, require the parentId of the link file to match the id of
		// the highest selected object
		query += "JOIN (" + layers.get(0) + ") ON (" + layers.get(0) + ".parentID="
				+ fmd.get(fmd.row).get(lowestSelected).obj.id;
		// if this is the last join of the query, add childId must equal id
		query += lastJoin(lowestSelected, layers.get(0), thisLevelFileName);
		// do the remainder of the joins matching the childId of the current level to
		// the parentId of the next lower level
		for (int layer = lowestSelected + 1; layer < fmd.column; layer++) {
			query += "JOIN (" + layers.get(layer) + ") ON (" + layers.get(layer) + ".parentId=" + layers.get(layer - 1)
					+ ".childId ";
			query += lastJoin(layer, layers.get(layer), thisLevelFileName);
		}
		query += insertScoreThreshold(obj);
		query += insertOrderByAndLimit(obj, IdAndStrings.DISPLAYSIZE, firstRecordDisplayed, SEARCHTYPES.DESCENDANTS);
//		}
		return query;
	}

	public String lastJoinAncestors(String linkFileName, String thisLevelFileName) {
		return " AND " + linkFileName + ".parentId = " + thisLevelFileName + ".id";
	}

	/**
	 * set the ancestors of the target object for my level
	 * 
	 * @param offset
	 * @param direction
	 * @return
	 * @throws Exception
	 */
	public String setAncestorsQuery(int offset) throws Exception {
//		MyObject obj = fmd.get(fmd.row).get(fmd.column).obj;

		// if the tab is the last in the row, it can not have any ancestors (no children
		// below it).
		if (fmd.column >= fmd.get(fmd.row).size() - 1 ||
		// or the row is loaded
				obj.isLoaded()) {
			// return a query that wont find any records
			return "";
		}
		int highestSelected = fmd.get(fmd.row).getHighestLoadedIndexBelow(fmd.column);
		if (highestSelected == -1)
			return "";

		String thisLevelFileName = obj.getMyFileName();
		// build a list of link files between the layers
		ArrayList<String> layers = new ArrayList<String>();
		for (int layer = highestSelected; layer > fmd.column; layer--) {
			layers.add(MyLinkObject.getFileName(fmd.get(fmd.row).get(layer - 1).obj, fmd.get(fmd.row).get(layer).obj,
					sVars));
		}
		String query = "";
		query += "SELECT DISTINCT " + thisLevelFileName + "." + "id, ";
		query += thisLevelFileName + ".name";
		query += insertSearchMatch(obj);
		query += " from ";
		query += obj.getMyFileName() + " ";
//		tables.add(obj.getMyFileName());
		// for the first layer, require the parentId of the link file to match the id of
		// the highest selected object
		query += "JOIN (" + layers.get(0) + ") ON (" + layers.get(0) + ".childID="
				+ fmd.get(fmd.row).get(highestSelected).obj.id;
		// if this is the last join of the query
		if (fmd.column + 1 == highestSelected)
			query += " AND " + layers.get(0) + ".parentId = " + thisLevelFileName + ".id)";
		else
			query += ")";
		// do the remainder of the joins matching the parentId of the current level to
		// the childId of the next higher level
		for (int layer = 1; layer < layers.size(); layer++) {
			query += "JOIN (" + layers.get(layer) + ") ON (" + layers.get(layer) + ".childId=" + layers.get(layer - 1)
					+ ".parentId ";
			if (layer == layers.size() - 1)
				query += " AND " + layers.get(layer) + ".parentId = " + thisLevelFileName + ".id)";
			else
				query += ")";
		}
		query += insertScoreThreshold(obj);
		query += insertOrderByAndLimit(obj, IdAndStrings.DISPLAYSIZE, offset, SEARCHTYPES.ANCESTORS);
//				fmd.getSearchTarget().getIdAndStrings(SEARCHTYPES.ANCESTORS).getFirstDisplayedRecord());
		return query;
	}

	/**
	 * insert match() against() as score
	 * 
	 * @return
	 * @throws Exception
	 */
	String insertSearchMatch(MyObject obj) throws Exception {
		String soFar = "";
		if (obj.searchString.isBlank())
			return soFar;
		soFar += ", match(";
		soFar += obj.getMyFileName();
		soFar += ".name) against ('";
		for (String subString : obj.searchString.split("\\W+")) {
			soFar += subString + "* ";
		}
		soFar += "' in BOOLEAN MODE) AS score ";
		return soFar;
	}

	/**
	 * insert HAVING score > 0
	 * 
	 * @param obj
	 * @return
	 */
	String insertScoreThreshold(MyObject obj) {
		String soFar = "";
		if (obj.searchString.isBlank())
			return soFar;
		soFar += " HAVING score > 0 ";
		return soFar;
	}

	/**
	 * insert ORDER BY
	 * 
	 * @param obj
	 * @param skip
	 * @param number
	 * @return
	 * @throws Exception
	 */
	String insertOrderByAndLimit(MyObject obj, int limit, int offset, SEARCHTYPES type) throws Exception {
		String soFar = "";
		if (!obj.searchString.isBlank())
			soFar += " ORDER BY score DESC, name";
		else
			soFar += " ORDER BY name";
		soFar += " LIMIT " + limit;
		soFar += " OFFSET " + offset;
		return soFar;
	}

	/**
	 * set the ancestorTargets for unloaded objects that have a loaded object below
	 * them
	 */
	void setAncestors() throws Exception {
//		MyObject obj = fmd.get(fmd.row).get(fmd.column).obj;
		String ancestorSource = "";
		// from the bottom up
		for (int index = fmd.getRowSize() - 1; index > 0; index--) {
			fmd.get(fmd.row).get(fmd.column).ancestorSource = ancestorSource;
			// if the current layer is loaded
			if (obj.isLoaded()) {
				ancestorSource = obj.getAName() + ":" + obj.getInstanceName();
				// don't bother with ancestors for a loaded object
				continue;
			}
		}
	}

	/**
	 * get all objects at the "index" level
	 * 
	 * @param objs
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public String setAllQuery(int offset) throws Exception {
//		MyObject obj = fmd.get(fmd.row).get(fmd.column).obj;
		if (obj.isLoaded()) {
			return "";
		}
//		Anchor allAnchor = fmd.getAnchor();
		String thisLevelFileName = obj.getMyFileName();
		String query = "";
		query += "SELECT " + thisLevelFileName + "." + "id, ";
		query += thisLevelFileName + ".name";
		query += insertSearchMatch(obj);
		query += " from ";
		query += thisLevelFileName + " ";
//		query += "WHERE (";
//		query += thisLevelFileName + ".anchorId='" + allAnchor.id + "\')";

		query += insertScoreThreshold(obj);
		query += insertOrderByAndLimit(obj, IdAndStrings.DISPLAYSIZE, offset, SEARCHTYPES.ALL);
		return query;
	}

	public boolean hasGoodStuff(String lookIn, String lookFor) {

		for (String lookInWord : lookIn.toLowerCase().split(" ")) {
			for (String lookForWord : lookFor.split(" ")) {
				if (lookInWord.contains(lookForWord)) {
					return true;
				}
			}
		}
		return false;
	}

	// return the first selected layer above thisLayer or -1 if there is none
	public int firstSelectedLayerAbove(SearchTargets objs, int thisLayer) {
		for (int i = thisLayer - 1; i >= 0; i--) {
			if (objs.get(i).obj.isLoaded())
				return i;
		}
		return -1;
	}

	// return the first selected layer below thisLayer or -1 if there is none
	public int firstSelectedLayerBelow(SearchTargets objs, int thisLayer) {
		for (int i = thisLayer + 1; i < objs.size(); i++) {
			if (objs.get(i).obj.isLoaded())
				return i;
		}
		return -1;
	}

//	public void setAll(SearchTargets objs) throws Exception {
//		// if the first entry (company) is not loaded, something is wrong
//		if (!objs.get(0).obj.isLoaded())
//			throw new Exception("first entry not loaded");
//
//		Anchor lowestAnchor = objs.get(0).obj.getAnchor();
//		// if the level below is not loaded, the descendants of this level are added to
//		// descendantTargets of the level below
//		for (int myIndex = 0; myIndex < objs.size(); myIndex++) {
//			// if the object is loaded, update the anchor
//			if (objs.get(myIndex).obj.isLoaded()) {
//				// lowestAnchor = objs.get(myIndex).obj.getAnchor();
//				// if the object at this level is self-anchoring
//				try {
//					// update the lowestAnchor
//					Anchor tmpAnchor = new Anchor(sVars).find(objs.get(myIndex).obj);
//					lowestAnchor = tmpAnchor;
//				} catch (AnchorNotFoundException e) {
//					// otherwise, keep the lowestAnchor
//				}
//			}
////			objs.get(myIndex).allTargets.storeQuery(setAllQuery(objs, myIndex, lowestAnchor.id));
//		}
//	}

//	@Override
//	public void setOrphans(SearchTargets objs) throws Exception {
//		// if the first entry is not loaded, something is wrong
//		if (!objs.get(0).obj.isLoaded())
//			throw new Exception("first entry not loaded");
//
//		Anchor lowestAnchor = objs.get(0).obj.getAnchor();
//		// no orphans on the top layer (need a parent type to be an orphan)
//		for (int myIndex = 1; myIndex < objs.size(); myIndex++) {
//			// if the object is loaded, update the anchor
//			if (objs.get(myIndex).obj.isLoaded()) {
//				// lowestAnchor = objs.get(myIndex).obj.getAnchor();
//				// if the object at this level is self-anchoring
//				try {
//					// update the lowestAnchor
//					Anchor tmpAnchor = new Anchor(sVars).find(objs.get(myIndex).obj);
//					lowestAnchor = tmpAnchor;
//				} catch (AnchorNotFoundException e) {
//					// otherwise, keep the lowestAnchor
//				}
//			}
//
////			objs.get(myIndex).orphanTargets.storeQuery(setOrphanQuery(objs, myIndex, lowestAnchor.id));
//		}
//
//	}

	public String setOrphanQuery(int offset) throws Exception {

//		MyObject obj = fmd.get(fmd.row).get(fmd.column).obj;
		Anchor allAnchor = fmd.getAnchor();
		String thisLevelFileName = obj.getMyFileName();
		if (obj.isLoaded() || obj.isRecursive()) {
			// don't show any orphans if the current layer is loaded
			return "";
		}
		String linkFileName = new MyLinkObject(fmd.get(fmd.row).get(fmd.column - 1).obj, obj, sVars).getMyFileName();
		String query = "";
		query += "SELECT " + thisLevelFileName + "." + "id, ";
		query += thisLevelFileName + ".name";
		query += insertSearchMatch(obj);
		query += " from ";
		query += thisLevelFileName + " ";
		query += " LEFT JOIN ";
		query += linkFileName;
		query += " ON ";
		query += thisLevelFileName + ".id=";
		query += linkFileName + ".childid";

		query += " WHERE (";
		query += thisLevelFileName + ".anchorId='" + allAnchor.id + "\'";
		query += " AND ";
		query += "childid IS NULL";

		query += ")";
		query += insertScoreThreshold(obj);
		query += insertOrderByAndLimit(obj, IdAndStrings.DISPLAYSIZE, offset, SEARCHTYPES.ORPHANS);
//				fmd.get(fmd.row).get(fmd.column).getIdAndStrings(SEARCHTYPES.ORPHANS).getFirstDisplayedRecord());
//		query += " ORDER BY name;";
		return query;
	}

////	@Override
//	public void applySearch(SearchTargets objs) throws Exception {
//		Internals.logWithDate();
//		for (int objectIndex = 0; objectIndex < objs.size(); objectIndex++) {
//			MyObject obj = objs.get(objectIndex).obj;
//			if (obj.searchString != null && !obj.searchString.isEmpty()) {
//				for (SEARCHTYPES s : SEARCHTYPES.values())
//					objs.listInterface.setAll(objs);
//				objs.get(objectIndex).allTargets.sortByCounts(obj.searchString);
//				objs.get(objectIndex).ancestorTargets.sortByCounts(obj.searchString);
//				objs.get(objectIndex).descendantTargets.sortByCounts(obj.searchString);
//				objs.get(objectIndex).orphanTargets.sortByCounts(obj.searchString);
//				objs.get(objectIndex).inventoryTargets.sortByCounts(obj.searchString);
//				objs.get(objectIndex).inventoryLinkTargets.sortByCounts(obj.searchString);
//			}
//		}
//		Internals.logWithDate();
//	}

//	@Override
//	public void setOldestInventory(SearchTargets objs) throws Exception {
//		for (int myIndex = 0; myIndex < objs.size(); myIndex++) {
//			MyObject workingObject = objs.get(myIndex).obj;
////			if (workingObject.hasInventoryField())
////				objs.get(myIndex).inventoryTargets.storeQuery(setInventoryQuery(objs.get(myIndex)));
//		}
//	}

	// ret the least recently inventoried objects that have hasInventoryField set
	public String setInventoryQuery(int offset) throws Exception {
		String query = "";
		if (!obj.hasInventoryField())
			return query;
		query += "SELECT ";
		query += obj.getMyFileName() + "." + "id, ";
		query += obj.getMyFileName() + "." + MyObject.INVENTORYFIELDNAME + ", ";
		query += "concat(name, ' - ', " + MyObject.INVENTORYFIELDNAME + ") as name ";
//		query += insertSearchMatch(obj);
		query += "from ";
		query += obj.getMyFileName();
		query += " order by `" + MyObject.INVENTORYFIELDNAME + "`, `name` ";
		query += " LIMIT " + IdAndStrings.DISPLAYSIZE;
		query += " OFFSET " + offset;
//		query += insertOrderByAndLimit(obj, IdAndStrings.DISPLAYSIZE, offset, SEARCHTYPES.INVENTORY);
		return query;
	}

//	@Override
//	public void setOldestLinkInventory(SearchTargets objs) throws Exception {
//		for (int myIndex = 0; myIndex < objs.size(); myIndex++) {
//			MyObject workingObject = objs.get(myIndex).obj;
//			int belowMeIndex = myIndex + 1;
//			// if not at the bottom of objs
//			if (belowMeIndex < objs.size()) {
//				MyObject belowMeObject = objs.get(belowMeIndex).obj;
//				// and i'm not loaded
//				if (!workingObject.isLoaded()) {
//					// and we have an inventory link
////					if (workingObject.hasInventoryLinkWith(belowMeObject)) {
////						objs.get(myIndex).inventoryLinkTargets.storeQuery(setInventoryLinkQuery(workingObject, belowMeObject));
////					}
//				}
//			}
//		}
//	}

	/**
	 * 
	 * @param offset, the number of leading records to skip. present the records to
	 *                the user in blocks.
	 * @return
	 * @throws Exception
	 */
	public String setInventoryLinkQuery(int offset) throws Exception {
		/**
		 * if there's an entry to the right of this entry AND this entry is a part AND
		 * the next entry in the row is a location
		 */
		if (fmd.column >= fmd.get(fmd.row).size() - 1 && fmd.get(fmd.row).get(fmd.column).obj instanceof Part
				&& fmd.get(fmd.row).get(fmd.column + 1).obj instanceof Location) {

			MyObject parent = fmd.get(fmd.row).get(fmd.column).obj;
			MyObject child = fmd.get(fmd.row).get(fmd.column + 1).obj;
			MyLinkObject mlo = new MyLinkObject(parent, child, sVars);
			String linkFileName = mlo.getMyFileName();
			String parentFileName = parent.getMyFileName();
			String query = "";
			query += "SELECT ";
			query += parentFileName + "." + "id, ";
			query += "concat(" + parentFileName + ".name, \" - \", " + linkFileName + ".inventoryDate) as name ";
			query += "from ";
			query += linkFileName + " inner join " + parentFileName + " on " + parentFileName + ".id="
					+ mlo.getMyFileName() + ".parentId ";
			if (child.isLoaded())
				query += " and " + mlo.getMyFileName() + ".childId=" + child.id;
//		query += " order by `" + MyObject.INVENTORYFIELDNAME + "`, `name` ";
//		query += " OFFSET " + offset;
			query += insertOrderByAndLimit(obj, IdAndStrings.DISPLAYSIZE, offset, SEARCHTYPES.INVENTORYLINKS);
			return query;
		} else
			// no query string
			return "";
	}

	public String getQuery(SEARCHTYPES type, int offset) throws Exception {
		switch (type) {
		case ALL:
			return setAllQuery(offset);
		case ANCESTORS:
			return setAncestorsQuery(offset);
		case DESCENDANTS:
			return setDescendantsQuery(offset);
		case INVENTORY:
			return setInventoryQuery(offset);
		case INVENTORYLINKS:
			return setInventoryLinkQuery(offset);
		case ORPHANS:
			return setOrphanQuery(offset);
		}
		return "fer shure";
	}

	public boolean isTheSameTargetAs(SearchTarget larry) {
		return this.row == larry.row && this.column == larry.column;
	}
}
