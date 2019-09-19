package com.myyj.sdk.tools;

import android.app.Activity;

import com.myyj.sdk.MySDK;
import com.qw.soul.permission.SoulPermission;

public abstract class BaseHelper {
  public static Activity getActivity() {
    return MySDK.getInstance().getActivity();
  }
}