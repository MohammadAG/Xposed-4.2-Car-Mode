package com.mohammadag.EnableCarDock;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class EnableCarDock implements IXposedHookLoadPackage {
	
	private UiModeManager mUiModeManager = null;
	private Context mContext = null;
	Intent mCarDockIntent = null;

	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("android"))
			return;
		
		findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager",
				lpparam.classLoader, "startDockOrHome", new XC_MethodHook() {
			
			@Override
			protected void beforeHookedMethod(MethodHookParam param) {
				if (mContext == null)
					mContext = (Context) getObjectField(param.thisObject, "mContext");
				
				if (mUiModeManager == null)
					mUiModeManager = (UiModeManager) mContext.getSystemService(Context.UI_MODE_SERVICE); 
				
				if (mCarDockIntent == null)
					 mCarDockIntent = (Intent) getObjectField(param.thisObject, "mCarDockIntent");
				
				if (mUiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {					
					mContext.startActivity(mCarDockIntent);
					param.setResult(false);
				}
				
				/*
				Class<?> contextClass = Class.forName("android.content.Context");
				@SuppressWarnings("unchecked")
				Method method = contextClass.getMethod("startActivityAsUser");
				
				method.invoke(mContext, carDockIntent, new UserHa));*/
			}
		});
	}
}