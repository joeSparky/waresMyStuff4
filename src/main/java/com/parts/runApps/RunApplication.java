package com.parts.runApps;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.db.SessionVars;
import com.forms.SmartForm;

public class RunApplication extends com.runApps.RunApplication{
	public SmartForm dispatchThis(String className, SessionVars sVars)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException {

		Class<?> runClass = null;
		runClass = this.getClass().getClassLoader().loadClass(className);
		SmartForm ret = null;
		Constructor<?> constructor = runClass.getDeclaredConstructor(SessionVars.class);
		ret = (SmartForm) constructor.newInstance(sVars);
		return ret;
	}
}
