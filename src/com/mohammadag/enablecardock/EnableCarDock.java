package com.mohammadag.enablecardock;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class EnableCarDock implements IXposedHookLoadPackage, IXposedHookZygoteInit {
	private Context mContext = null;
	Intent mCarDockIntent = null;
	static Object mUserHandle = null;
	private boolean mCarMode = false;
	private XSharedPreferences mPreferences = null;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(EnableCarDock.class.getPackage().getName());
		mPreferences.makeWorldReadable();
	}

	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("android"))
			return;

		try {
			mUserHandle = XposedHelpers.getStaticObjectField(UserHandle.class, "CURRENT");
		} catch (Throwable t) { }

		Class<?> PhoneWindowManager = findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader);
		XposedBridge.hookAllMethods(PhoneWindowManager, "init", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (param.args.length == 0)
					return;

				Object contextMaybe = param.args[0];
				if (contextMaybe instanceof Context) {
					Context context = (Context) contextMaybe;
					IntentFilter iF = new IntentFilter();
					iF.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
					iF.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
					iF.addAction(Constants.INTENT_SETTINGS_UPDATED);
					context.registerReceiver(new BroadcastReceiver() {
						@Override
						public void onReceive(Context context, Intent intent) {
							if (UiModeManager.ACTION_ENTER_CAR_MODE.equals(intent.getAction())) {
								mCarMode = true;
								launchCarHomeIfUserWantsThat(context);
							} else if (UiModeManager.ACTION_EXIT_CAR_MODE.equals(intent.getAction())) {
								mCarMode = false;
							} else if (Constants.INTENT_SETTINGS_UPDATED.equals(intent.getAction())) {
								mPreferences.reload();
							}
						}
					}, iF);
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (mCarDockIntent == null)
					mCarDockIntent = (Intent) getObjectField(param.thisObject, "mCarDockIntent");
			}
		});

		findAndHookMethod(PhoneWindowManager, "startDockOrHome", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) {
				if (mContext == null)
					mContext = (Context) getObjectField(param.thisObject, "mContext");

				if (mCarDockIntent == null)
					mCarDockIntent = (Intent) getObjectField(param.thisObject, "mCarDockIntent");

				if (mCarMode) {	
					startActivitySafe(mContext, mCarDockIntent);
					param.setResult(false);
				}
			}
		});
	}

	private void launchCarHomeIfUserWantsThat(Context context) {
		if (context == null)
			return;

		boolean soDoesUserWantThat =
				mPreferences.getBoolean(Constants.SETTINGS_KEY_AUTO_LAUNCH_CAR_HOME, false);

		if (soDoesUserWantThat) {
			startActivitySafe(context, mCarDockIntent);
		}
	}

	private static void startActivitySafe(Context context, Intent intent) {
		if (mUserHandle == null) {
			context.startActivity(intent);
		} else {
			XposedHelpers.callMethod(context, "startActivityAsUser", intent, mUserHandle);
		}
	}
}