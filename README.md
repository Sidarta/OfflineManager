# OfflineManager - Handle Offline Retrofit calls

This lib offers an easy way for developers to provide offline functionality and treatment in their app.
It handles Retrofit calls and offers methods to cater for when device has no connection, or for when server seems to be offline or not responding.

## Prerequisites

### API Support
This Lib is only supported on SDK >= 21. This is because services used to execute calls on the background are not supported and optmized for previous SDK versions.

### Retrofit2 is a prerequisite
```
compile 'com.squareup.retrofit2:retrofit:2.1.0'
```


## Configuring on the App

Configuring and using the API is very very simple.

### 1. Add permissions on App Manifest

```xml
 <!--Permissions required and used by Offline Manager-->
 <uses-permission android:name="android.permission.INTERNET" />
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 <!---->
```

### 2. Declare and initialize OfflineManager
```java
//OfflineManager
OfflineManager mManager;
...
 //initializing offlineManager with its Builder
 mManager = new OfflineManager.Builder(this, aView)
            .maxTimeoutSeconds(3)
            .build();
```
Like shown, You can configure custom timeout values in the builder, but it´s not required.

### 3. Use

To use you need to declare a retrofit call

```java
 //separating creation of a retrofit call
Call retrofitCall = mRetrofitApi.getOrPutSomething(headerParam1, headerParam2);
```

... Then instead of using *Enqueue*, use API method passing the call and configuration parameters

```java
mManager.treatedCall(
      retrofitCall, 
      OfflineManager.DeviceOfflineTreatment.Flexible, //Flexible mode - user interaction via pop up
      OfflineManager.ServerOfflineTreatment.Flexible, //Flexible mode - user interaction via pop up
      5,          //number of retries in case of server offline
      true,       //verbose - true: all interactions will give a feedback message to the user
      new OfflineManager.CustomCallbackSuccess() {        //implement response callback here
          @Override
          public void responseCallback(Call call, Response response) {
              //handles on response
          }
      },
      new OfflineManager.CustomCallbackFail() {           //implement failure callback here
          @Override
          public void failCallback(Call call, Throwable t) {
              //handles on failure
          }
      }
);
```

## Understanding Device/Server Treatments

### Boolean VERBOSE

This boolean is passed in the API call so programmer can specify if user will be given feedbacks on actions taken by the API, or if it´s going to be in a silent way.

* true = user will be given feedbacks via Snackbar
* false = silent way, only crucial API feedbacks will be given


### DeviceOfflineTreatment
1. Enforce: Connection is required. No treatment is done by API.
2. Flexible: If device is offline, user is asked whether he/she wants to do the action on background when connection is available.
3. Transparent: If device is offline, action will be stored and executed again silently when connection if available.

### ServerOfflineTreatment + Tries

For server offline treatments, wou will pass mode and an int number of tries. Tries just indicate how many times API will try to execute the call if server is offline.

1. NoAction: If server is offline, no action will be done by the API.
2. Flexible: If server is offline, user will be prompted (yes/no) to store the action and execute again in background after some time (specified in builder via *maxTimeoutSeconds*)
3. Transparent: If server is offline, action will be stored and executed again silently after some time (specified in builder via *maxTimeoutSeconds*)

*Ps.: In Flexible mode, prompt will show every time action is tries (if app is on foreground). This makes user have control to stop the chain if he/she wants*


## Using custon strings

The lib already implements default messages for all cases. Programer can override them to provide his own custom messages (and also implements internationalization)

Strings are:

```xml
<!--default pop up controls-->
<string name="pop_up_yes">Yes</string>
<string name="pop_up_no">No</string>

<!--enforce device mode-->
<string name="enforce_no_connection_feedback">Your device does not have connection. Please try again later.</string>

<!--flexible mode dialog texts (title and message) -->
<string name="flexible_dialog_connection_title">No connection</string>
<string name="flexible_dialog_connection_message">Your device does not have connection. Do you wish to automatically proceeds when internet becomes available?</string>
<string name="flexible_dialog_server_retry_title">Server appears to be offline...</string>
<string name="flexible_dialog_server_retry_message">Do you wish for it to be done again in a few minutes?</string>

<!--verbose mode yes feedback strings-->
<string name="flexible_yes_selected_device_offline">Your action was stored and will take effect as soon as the device gets an active connection.</string>
<string name="flexible_no_selected_device_offline">"Action not taken. You can try again later."</string>
<string name="last_retry_finished">last retry finished</string>
<string name="no_action">Your server is offline. Please try again in a later time.</string>
<string name="flexible_yes_selected_server_offline">Action stored, will be tried again in a bit</string>
<string name="flexible_no_selected_server_offline">Ok, maybe try again later for yourself</string>
<string name="service_onresponse">Action was able to complete now.</string>
```

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


