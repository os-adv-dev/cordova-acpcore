/*
 Copyright 2020 Adobe. All rights reserved.
 This file is licensed to you under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under
 the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 OF ANY KIND, either express or implied. See the License for the specific language
 governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile.cordova;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;

public class ACPCore_Cordova extends CordovaPlugin {

    final static String METHOD_CORE_EXTENSION_VERSION_CORE = "extensionVersion";
    final static String METHOD_CORE_GET_PRIVACY_STATUS = "getPrivacyStatus";
    final static String METHOD_CORE_GET_SDK_IDENTITIES = "getSdkIdentities";
    final static String METHOD_CORE_SET_ADVERTISING_IDENTIFIER = "setAdvertisingIdentifier";
    final static String METHOD_CORE_SET_LOG_LEVEL = "setLogLevel";
    final static String METHOD_CORE_SET_PRIVACY_STATUS = "setPrivacyStatus";
    final static String METHOD_CORE_TRACK_ACTION = "trackAction";
    final static String METHOD_CORE_TRACK_STATE = "trackState";
    final static String METHOD_CORE_UPDATE_CONFIGURATION = "updateConfiguration";

    // ===============================================================
    // all calls filter through this method
    // ===============================================================
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (METHOD_CORE_EXTENSION_VERSION_CORE.equals(action)) {
             this.extensionVersion(callbackContext);
             return true;
        } else if (METHOD_CORE_GET_PRIVACY_STATUS.equals(action)) {
             this.getPrivacyStatus(callbackContext);
             return true;
        } else if (METHOD_CORE_GET_SDK_IDENTITIES.equals(action)) {
             this.getSdkIdentities(callbackContext);
             return true;
        } else if (METHOD_CORE_SET_ADVERTISING_IDENTIFIER.equals(action)) {
             this.setAdvertisingIdentifier(args, callbackContext);
             return true;
        } else if (METHOD_CORE_SET_LOG_LEVEL.equals(action)) {
             this.setLogLevel(args, callbackContext);
             return true;
        } else if (METHOD_CORE_SET_PRIVACY_STATUS.equals(action)) {
             this.setPrivacyStatus(args, callbackContext);
             return true;
        } else if (METHOD_CORE_TRACK_ACTION.equals(action)) {
             this.trackAction(args, callbackContext);
             return true;
        } else if (METHOD_CORE_TRACK_STATE.equals(action)) {
             this.trackState(args, callbackContext);
             return true;
        } else if (METHOD_CORE_UPDATE_CONFIGURATION.equals(action)) {
             this.updateConfiguration(args, callbackContext);
             return true;
        }

        return false;
    }

    private void extensionVersion(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                final String version = MobileCore.extensionVersion();
                callbackContext.success(version);
            }
        });
    }

    private void getPrivacyStatus(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> MobileCore.getPrivacyStatus(mobilePrivacyStatus -> callbackContext.success(mobilePrivacyStatus.getValue())));
    }

    private void getSdkIdentities(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> MobileCore.getSdkIdentities(callbackContext::success));
    }

    private void setAdvertisingIdentifier(final JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                final String newAdId = args.getString(0);
                MobileCore.setAdvertisingIdentifier(newAdId);
                callbackContext.success();
            } catch (final Exception ex) {
                final String errorMessage = String.format("Exception in call to setAdvertisingIdentifier: %s", ex.getLocalizedMessage());
                MobileCore.setLogLevel(LoggingMode.WARNING);
                callbackContext.error(errorMessage);
            }
        });
    }

    private void setLogLevel(final JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LoggingMode newLogLevel;
                    switch (args.getInt(0)) {
                        case 0:
                        default:
                            newLogLevel = LoggingMode.ERROR;
                            break;
                        case 1:
                            newLogLevel = LoggingMode.WARNING;
                            break;
                        case 2:
                            newLogLevel = LoggingMode.DEBUG;
                            break;
                        case 3:
                            newLogLevel = LoggingMode.VERBOSE;
                            break;
                    }
                    MobileCore.setLogLevel(newLogLevel);
                    callbackContext.success();
                } catch (final Exception ex) {
                    final String errorMessage = String.format("Exception in call to setLogLevel: %s", ex.getLocalizedMessage());
                    MobileCore.setLogLevel(LoggingMode.WARNING);
                    callbackContext.error(errorMessage);
                }
            }
        });
    }

    private void setPrivacyStatus(final JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    MobilePrivacyStatus newPrivacyStatus;
                    switch (args.getInt(0)) {
                        case 0:
                            newPrivacyStatus = MobilePrivacyStatus.OPT_IN;
                            break;
                        case 1:
                            newPrivacyStatus = MobilePrivacyStatus.OPT_OUT;
                            break;
                        case 2:
                        default:
                            newPrivacyStatus = MobilePrivacyStatus.UNKNOWN;
                            break;
                    }
                    MobileCore.setPrivacyStatus(newPrivacyStatus);
                    callbackContext.success();
                } catch (final Exception ex) {
                    final String errorMessage = String.format("Exception in call to setPrivacyStatus: %s", ex.getLocalizedMessage());
                    MobileCore.setLogLevel(LoggingMode.WARNING);
                    callbackContext.error(errorMessage);
                }
            }
        });
    }

    private void trackAction(final JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                final String action = args.getString(0);

                if(action.isEmpty()) {
                    callbackContext.error("Action is required");
                    return;
                }

                final HashMap<String, String> contextData = getStringMapFromJSON(args.getString(1));
                MobileCore.trackAction(action, contextData);
                callbackContext.success();
            } catch (final Exception ex) {
                final String errorMessage = String.format("Exception in call to trackAction: %s", ex.getLocalizedMessage());
                MobileCore.setLogLevel(LoggingMode.WARNING);
                callbackContext.error(errorMessage);
            }
        });
    }

    private void trackState(final JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                final String state = args.getString(0);
                final HashMap<String, String> contextData = getStringMapFromJSON(args.getJSONObject(1));

                MobileCore.trackState(state, contextData);
                callbackContext.success();
            } catch (final Exception ex) {
                final String errorMessage = String.format("Exception in call to trackState: %s", ex.getLocalizedMessage());
                MobileCore.setLogLevel(LoggingMode.WARNING);
                callbackContext.error(errorMessage);
            }
        });
    }

    private void updateConfiguration(final JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                final HashMap<String, Object> newConfig = getObjectMapFromJSON(args.getJSONObject(0));

                MobileCore.updateConfiguration(newConfig);
                callbackContext.success();
            } catch (final Exception ex) {
                final String errorMessage = String.format("Exception in call to updateConfiguration: %s", ex.getLocalizedMessage());
                MobileCore.setLogLevel(LoggingMode.WARNING);
                callbackContext.error(errorMessage);
            }
        });
    }

    // ===============================================================
    // Helpers
    // ===============================================================
    private HashMap<String, String> getStringMapFromJSON(JSONObject data) {
        HashMap<String, String> map = new HashMap<String, String>();
        @SuppressWarnings("rawtypes")
        Iterator it = data.keys();
        while (it.hasNext()) {
            String n = (String) it.next();
            try {
                map.put(n, data.getString(n));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    private HashMap<String, String> getStringMapFromJSON(JSONArray jsonArray) throws Exception {
        HashMap<String, String> map = new HashMap<>();

        if (jsonArray.length() > 0) {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.getString(key);
                map.put(key, value);
            }
        }
        return map;
    }

    private HashMap<String, String> getStringMapFromJSON(String jsonString) throws Exception {
        HashMap<String, String> map = new HashMap<>();

        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String key = jsonObject.getString("Key");
            String value = jsonObject.getString("Value");

            map.put(key, value);
        }
        return map;
    }

    private HashMap<String, Object> getObjectMapFromJSON(JSONObject data) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        @SuppressWarnings("rawtypes")
        Iterator it = data.keys();
        while (it.hasNext()) {
            String n = (String) it.next();
            try {
                map.put(n, data.getString(n));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    private Event getEventFromMap(final HashMap<String, Object> event) throws Exception {
        return new Event.Builder(
                event.get("name").toString(),
                event.get("type").toString(),
                event.get("source").toString()
        ).setEventData(getObjectMapFromJSON(new JSONObject(event.get("data").toString()))).build();
    }

    private HashMap<String, Object> getMapFromEvent(final Event event) {
        final HashMap<String, Object> eventMap = new HashMap<>();
        eventMap.put("name", event.getName());
        eventMap.put("type", event.getType());
        eventMap.put("source", event.getSource());
        eventMap.put("data", event.getEventData());

        return eventMap;
    }

    // ===============================================================
    // Plugin lifecycle events
    // ===============================================================
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        MobileCore.setApplication(this.cordova.getActivity().getApplication());
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
    }
}
