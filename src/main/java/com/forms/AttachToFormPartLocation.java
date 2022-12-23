package com.forms;

import com.db.SessionVars;
import com.parts.inOut.Part;
import com.parts.location.Location;
import com.parts.security.PartLink;

public class AttachToFormPartLocation extends AttachToFormPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 366421999120725608L;
	static final String QUANTITY = AttachToFormPartLocation.class.getCanonicalName() + "a";
	static final String STOREBUTTON = AttachToFormPartLocation.class.getCanonicalName() + "b";
	static final String REMOVEBUTTON = AttachToFormPartLocation.class.getCanonicalName() + "c";
	static final String INVENTORYBUTTON = AttachToFormPartLocation.class.getCanonicalName() + "d";
//	static final String MOVETOSECONDLOCATION = AttachToFormPartLocation.class.getCanonicalName() + "e";

	public AttachToFormPartLocation(SessionVars sVars) throws Exception {
		super(sVars);
	}

	class MyVars extends com.forms.MyVars {
		
		int inOutQuantity = 1;
		boolean survived = true;
		
		protected MyVars(SessionVars sVars) throws Exception {
			super(sVars, AttachToFormPartLocation.class.getCanonicalName());
			if (get()==null) {
				survived = false;
				put();
			}
		}		
	}

	@Override
	// the link exists. get the PartLink
	public FormsArray removeLink(SessionVars sVars, LeftAndRight leftAndRight) throws Exception {
		FormsArray ret = new FormsArray();
		// get my variables from the session
		MyVars myVars = (MyVars) new MyVars(sVars).get();
		if (leftAndRight.left.obj instanceof Part && leftAndRight.right.obj instanceof Location) {

			ret.textBox(QUANTITY, 4, "Quantity", "" + myVars.inOutQuantity, false, false);
			ret.submitButton("Add '" + leftAndRight.left.obj.getInstanceName() + "' to '"
					+ leftAndRight.right.obj.getInstanceName() + "', current quantity:"
					+ new PartLink(leftAndRight.left.obj, leftAndRight.right.obj, sVars).find().getLinkQuantity(),
					STOREBUTTON);
			ret.submitButton("Remove '" + leftAndRight.left.obj.getInstanceName() + "' from '"
					+ leftAndRight.right.obj.getInstanceName() + "', current quantity:"
					+ new PartLink(leftAndRight.left.obj, leftAndRight.right.obj, sVars).find().getLinkQuantity(),
					REMOVEBUTTON);
			return ret;
		} else
			return super.removeLink(sVars, leftAndRight);
	}

	@Override
	public FormsArray addLink(SessionVars sVars, LeftAndRight leftAndRight) throws Exception {
		FormsArray ret = new FormsArray();
		MyVars myVars = (MyVars) new MyVars(sVars).get();
		if (forMe(leftAndRight)) {
			ret.textBox(QUANTITY, 4, "Quantity", "" + myVars.inOutQuantity, false, false);
			ret.submitButton("Store '" + leftAndRight.left.obj.getInstanceName() + "' at '"
					+ leftAndRight.right.obj.getInstanceName() + "'", STOREBUTTON);
			return ret;
		} else
			return super.addLink(sVars, leftAndRight);
	}

	@Override
	public FormsArray markAsInventoried(SessionVars sVars, LeftAndRight leftAndRight) throws Exception {
		FormsArray ret = new FormsArray();
//		MyVars myVars = (MyVars) new MyVars(sVars).get();
		if (forMe(leftAndRight)) {
			PartLink pl = new PartLink(leftAndRight.left.obj, leftAndRight.right.obj, sVars);
			pl.find();
			ret.rawText("Last inventoried on " + pl.getInventoryDate());
			ret.newLine();
			ret.submitButton("Mark '" + leftAndRight.right.obj.getInstanceName() + "' as containing "
					+ pl.getLinkQuantity() + " of '" + leftAndRight.left.obj.getInstanceName() + "'", INVENTORYBUTTON);
			return ret;
		} else {
			return (super.markAsInventoried(sVars, leftAndRight));
		}
	}

	boolean forMe(LeftAndRight leftAndRight) {
		return leftAndRight.left.obj instanceof Part && leftAndRight.right.obj instanceof Location;
	}

	@Override
	public FormsArray extractParams(SessionVars sVars) throws Exception {
//	super.extractParams(sVars);
		FormsArray ret = new FormsArray();
		MyVars myVars = (MyVars) new MyVars(sVars).get();
		LeftAndRight leftAndRight = new LeftAndRight(sVars);
		if (forMe(leftAndRight)) {

			if (sVars.hasParameterKey(QUANTITY)) {
				myVars.inOutQuantity = Integer.parseInt(sVars.getParameterValue(QUANTITY));
				if (myVars.inOutQuantity < 1) {
					ret.errorToUser("Quantity must be positive.");
					throw new EndOfInputException(ret);
				}
			}

			if (sVars.hasParameterKey(STOREBUTTON)) {
//			ret.addAll(form.tryAgain(sVars, location));
				if (leftAndRight.left.obj.childExists(leftAndRight.right.obj))
					new PartLink(leftAndRight.left.obj, leftAndRight.right.obj, sVars).find()
							.updateAddQuantity(myVars.inOutQuantity, false);

				myVars.inOutQuantity = 1;
				sVars.fmd.resetAllIdAndStrings();
				throw new EndOfInputException(ret);
			}
			if (sVars.hasParameterKey(REMOVEBUTTON)) {
				// update the quantity and throw an error if the resulting quantity is invalid
				PartLink pl = new PartLink(leftAndRight.left.obj, leftAndRight.right.obj, sVars).find();
				int newQuant = pl.getLinkQuantity() - myVars.inOutQuantity;
				if (newQuant < 0) {
					ret.errorToUser("The quantity to be removed is larger than the quantity in stock.");
					throw new EndOfInputException(ret);
				}
				if (newQuant == 0) {
					pl.deleteUnconditionally();
				} else {
					// update the quantity, keep the same time
					pl.updateSetQuantity(newQuant, false);
					myVars.inOutQuantity = 1;
				}
				sVars.fmd.resetAllIdAndStrings();
				throw new EndOfInputException(ret);
			}
			if (sVars.hasParameterKey(INVENTORYBUTTON)) {
//			ret.addAll(form.tryAgain(sVars, objectBelowMe));
				if (leftAndRight.left.obj.childExists(leftAndRight.right.obj)) {
					PartLink pl = new PartLink(leftAndRight.left.obj, leftAndRight.right.obj, sVars);
					pl.find();
					pl.setInventoried(true);
					pl.update();
				} else {
					throw new Exception("PartLink does not exist.");
				}
				throw new EndOfInputException(ret);
			}
//		if (sVars.hasParameterKey(MOVETOSECONDLOCATION)) {
////			if (part.childExists(secondLocation)) {
//			// remove the existing quantity from the old link
//			PartLink oldLink = new PartLink(partSearchTarget.obj, locationSearchTarget.obj, sVars).find();
//			int oldQuantity = oldLink.getLinkQuantity();
//			oldLink.deleteUnconditionally();
//
//			// create a link from the part to the second location
//			PartLink newLink = new PartLink(partSearchTarget.obj, secondLocationSearchTarget.obj, sVars);
//			if (newLink.linkExists())
//				newLink.updateAddQuantity(oldQuantity, false);
//			else {
//				// create the link to the second location
//				partSearchTarget.obj.addChild(secondLocationSearchTarget.obj);
//				newLink.updateAddQuantity(oldQuantity, false);
//			}
//			ret.rawText("All of Part:" + partSearchTarget.obj.getInstanceName() + " moved from Location:" + locationSearchTarget.obj.getInstanceName()
//					+ " to Location:" + secondLocationSearchTarget.obj.getInstanceName());
////			} else {
////				throw new Exception("PartLink does not exist.");
////			}
//			throw new EndOfInputException(ret);
		}
		return ret;
	}

	static final String MYNAME = AttachToFormPartLocation.class.getCanonicalName();
}
