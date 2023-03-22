/*
Copyright 2023 DigiLens Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.digilens.voiceui_test;

import static com.digilens.digios_voiceui_api.utils.Constants.Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY;
import static com.digilens.digios_voiceui_api.utils.Constants.Voice_Command_CONFIG_TYPE_FEEDBACK_WITH_NUMBER_ONLY;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.digilens.digios_voiceui_api.VoiceUI_Interface;
import com.digilens.digios_voiceui_api.utils.VoiceUICallback;
import com.digilens.digios_voiceui_api.utils.VoiceUI_Listener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String LOG_TAG = "DigiOS-VoiceUI-Test";
    VoiceUI_Interface voiceUI_interface;
    boolean voiceUI_interface_active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // First, initialize the interface to the VoiceUI and acquire the instance of the interface to the voice UI
        initialize_voiceUI_interface();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (voiceUI_interface_active) {
            register_voice_commands();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Best practice : Unregister all registered voice commands from the voiceUI service when not in use
        if (voiceUI_interface_active) {
            unregister_all_voice_command();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Best practice : Destroy the interface when the voiceUI interface is no more needed
        if (voiceUI_interface_active) {
            voiceUI_interface.onDestroy();
        }
    }

    /*
        Use VoiceUI_Interface.createInstance(getApplicationContext()) call to initialize the interface to the VoiceUI and acquire the instance of the interface to the voice UI.

        Example :

        Code : voiceUI_interface = VoiceUI_Interface.createInstance(getApplicationContext());

        Note:
        1. VoiceUI_Interface.createInstance(...) returns null if the interface was unsuccessful in connecting to the DigiOS Voice UI Service
        */
    private void initialize_voiceUI_interface() {
        try {
            voiceUI_interface = VoiceUI_Interface.createInstance(getApplicationContext());
            voiceUI_interface_active = true;
        } catch (Exception e) {
            voiceUI_interface_active = false;
            Log.d(LOG_TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    /*
    Use voiceUI_interface.CHECK_VOICEUI_STATUS() call to check if the VoiceUI is initialized and ready to use

    Example :

    Code : boolean VOICEUI_STATUS = voiceUI_interface.CHECK_VOICEUI_STATUS();
    */
    private boolean check_voiceUI_status() {
        try {
            return voiceUI_interface.CHECK_VOICEUI_STATUS();
        } catch (Exception e) {
            Log.d(LOG_TAG,e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /*
    Create an instance of the VoiceUI_Listener with the voice command to be registered and voice command config type.

    Code :
    VoiceUI_Listener voiceUI_listener = new VoiceUI_Listener(String voice_command,int CONFIG_TYPE)

    Example with regular voice commands:
    VoiceUI_Listener voiceUI_listener = new VoiceUI_Listener("Take photo",Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY)
    User can say : Take photo
    Voice UI will callback voiceUI_listener with onReceive() when the <Registered voice command> is detected.

    Example with voice commands with number input:
    VoiceUI_Listener voiceUI_listener = new VoiceUI_Listener("set zoom to",Voice_Command_CONFIG_TYPE_FEEDBACK_WITH_NUMBER_ONLY)
    User can say : set zoom to 5 (<Registered voice command> <number input between 0 to 100>)
    Voice UI will callback voiceUI_listener with onReceive(int value) [onReceive(5)] when the <Registered voice command> is detected with a number (between 0 to 100).

    Alternatively, you can create a VoiceUI_Listener which uses VoiceUICallback interface (Java abstraction). This allows you to handle the callbacks within the interface.

    Code :
    VoiceUICallback voiceUICallback = new VoiceUICallback() {
                @Override
                public void onReceive(String voice_command) {
                    //TODO: Do your task

                }

                @Override
                public void onReceive(String voice_command, int number_input) {
                    //TODO: Do your task
                }
    };

    VoiceUI_Listener voiceUI_listener = new VoiceUI_Listener(VoiceUICallback voiceUICallback, String voice_command,int CONFIG_TYPE)

    Use voiceUI_interface.REGISTER_VOICE_COMMANDS(...) call to register a group of new voice commands in the voice UI.

    Code :
    voiceUI_interface.REGISTER_VOICE_COMMANDS(ArrayList<VoiceUI_Listener> voiceUI_listeners);

    Example:
    ArrayList<VoiceUI_Listener> voiceUI_listeners = new ArrayList<VoiceUI_Listener>();
    voiceUI_listeners.add(voiceUI_listener);
    ... (add more as needed)
    voiceUI_interface.REGISTER_VOICE_COMMANDS(voiceUI_listeners);

    Note:
    1. voiceUI_interface.REGISTER_VOICE_COMMANDS(...) is blocking call.
    2. The voice command should in alphanumeric, with no special characters.
    3. Maximum character limit on voice command : 32
    4. For voice commands with number input, the voice UI only accepts number input between 0 to 100.
    5. voiceUI_interface.REGISTER_VOICE_COMMANDS(...) call will erase all the previously registered voice commands associated with the parent voiceUI_interface and activate the new voice commands along with the new listeners.
    6. The voiceUI service can only accept limited unique voice commands throughout the system. If there's no available slots for the new commands, the voiceUI_interface.REGISTER_VOICE_COMMANDS(...) call will fail and throws exception with message "Unable to register voice command". This failure will still preserve the previously registered voice commands and listeners.
     */

    private void register_voice_commands() {
        ArrayList<VoiceUI_Listener> voiceUI_listeners = new ArrayList<VoiceUI_Listener>();

        try {
            voiceUI_listeners.add(new VoiceUI_Listener("open submenu",Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY) {
                @Override
                public void onReceive(){
                    Log.d(LOG_TAG,"Listener detected voice command : "+this.voice_command);
                    //TODO: Do your task
                    MAKE_A_TOAST(this.voice_command);
                }
            });

            voiceUI_listeners.add(new VoiceUI_Listener(new VoiceUICallback() {
                @Override
                public void onReceive(String s) {
                    Log.d(LOG_TAG,"Listener detected voice command : " + s);
                    //TODO: Do your task
                    MAKE_A_TOAST(s);
                }

                @Override
                public void onReceive(String s, int i) {

                }
            },"go back",Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY));

            voiceUI_listeners.add(new VoiceUI_Listener("select icon",Voice_Command_CONFIG_TYPE_FEEDBACK_WITH_NUMBER_ONLY) {
                @Override
                public void onReceive(int value){
                    Log.d(LOG_TAG,"Detected voice command : "+this.voice_command);
                    Log.d(LOG_TAG,"Listener detected number value : "+value);
                    //TODO: Do your task
                    MAKE_A_TOAST(this.voice_command + " " + String.valueOf(value));
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        long start = System.currentTimeMillis();
        try {
            voiceUI_interface.REGISTER_VOICE_COMMANDS(voiceUI_listeners);
        } catch (Exception exception) {
            Log.d(LOG_TAG,exception.getMessage());
            exception.printStackTrace();
        }
        long stop = System.currentTimeMillis() - start;
        Log.d(LOG_TAG,"Time taken to register the voice commmands : "+String.valueOf(stop)+" ms");
    }

    /*
    Use voiceUI_interface.UNREGISTER_ALL_VOICE_COMMAND() call to unregister all the voice commands associated with the voiceUI_interface.

    Note:
    1. voiceUI_listener.remove() is blocking call.
    2. Allow some time (about 1 secs) before calling voiceUI_interface.REGISTER_VOICE_COMMAND(...)/voiceUI_interface.REGISTER_VOICE_COMMAND_WITH_NUMBER(...)/voiceUI_interface.REGISTER_ALWAYS_ON_VOICE_COMMAND(...) after a voiceUI_interface.UNREGISTER_ALL_VOICE_COMMAND() call.
     */
    private void unregister_all_voice_command() {
        try {
            voiceUI_interface.UNREGISTER_ALL_VOICE_COMMAND();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void MAKE_A_TOAST(String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
