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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.digilens.digios_voiceui_api.VoiceUI_Interface;
import com.digilens.digios_voiceui_api.utils.VoiceUICallback;
import com.digilens.digios_voiceui_api.utils.VoiceUI_Listener;
import com.digilens.digios_voiceui_api.utils.VoiceUI_Model;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String LOG_TAG = "DigiOS-VoiceUI-Test";
    VoiceUI_Interface voiceUI_interface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // First, initialize the interface to the VoiceUI and acquire the instance of the interface to the voice UI
        initialize_voiceUI_interface();
    }

    /*
        In order to use the DigiOS Voice UI, you need to first create voice models for each language you wish to support voice interaction with the user.

        Code :
        VoiceUI_Model voiceUI_model = new VoiceUI_Model(LANGUAGE_CODE)

        LANGUAGE_CODE is ISO 639 alpha-2 or alpha-3 language code.

        Supported languages and corresponding LANGUAGE_CODES are :
        1. US-ENGLISH - "en"
        2. MEX-SPANISH - "es"

        Then, you need to create the VoiceUI_Listeners' for the voice commands that needs to be registered with the DigiOS VoiceUI.

        There are different configuration types for each voice command:
        Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY - Used for regular voice commands
        Voice_Command_CONFIG_TYPE_FEEDBACK_WITH_NUMBER_ONLY - Used for voice commands with variable number input

        Code :
        VoiceUI_Listener voiceUI_listener = new VoiceUI_Listener(String voice_command,int CONFIG_TYPE)

        Example with regular voice commands:
        VoiceUI_Listener voiceUI_listener = new VoiceUI_Listener("Take photo",Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY)
        User can say : Take photo
        Voice UI will callback voiceUI_listener.onReceive() when the <Registered voice command> is detected.

        Example with voice commands with number input:
        VoiceUI_Listener voiceUI_listener = new VoiceUI_Listener("set zoom to",Voice_Command_CONFIG_TYPE_FEEDBACK_WITH_NUMBER_ONLY)
        User can say : set zoom to 5 (<Registered voice command> <number input between 0 to 100>)
        Voice UI will callback voiceUI_listener.onReceive(int value) [onReceive(5)] when the <Registered voice command> is detected with a number (between 0 to 100).

        Alternatively, you can also create a VoiceUI_Listener using VoiceUICallback interface (which uses Java abstraction). This allows you to handle the callbacks from DigiOS Voice UI.

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

        Note:
        1. Maximum character limit on voice command : 32
        2. For voice commands with number input, the voice UI only accepts number input between 0 to 100.
        3. The voiceUI service can only accept limited unique voice commands throughout the system. If there's no available slots for the new commands, the voiceUI_interface will fail to your voice commands and throws exception with message "Unable to register voice command".

        Once a VoiceUI_Listener is created, you can add it to the appropriate language VoiceUI_Model. The VoiceUI_Model checks the voice command within the VoiceUI_Listener to ensure valid characters are present for the specified language VoiceUI_Model.

        Expected Characters for different languages:
        1. US-ENGLISH - https://unicode.org/charts/PDF/U0000.pdf
        2. MEX-SPANISH - https://unicode.org/charts/PDF/U0000.pdf , https://unicode.org/charts/PDF/U0080.pdf

        Code :
        voiceUI_model.addVoiceUI_Listener((VoiceUI_Listener) voiceUI_Interface);

        Note:
        1. Adding VoiceUI_Listener with same voice command as previously added VoiceUI_Listener to a given VoiceUI_Model will overwrite the previous VoiceUI_Listener.

        After creating the (VoiceUI_Model)s, you need to create a new instance of VoiceUI_Interface and start adding the (VoiceUI_Model)s to the VoiceUI_Interface.

        Code :
        VoiceUI_Interface voiceUI_interface = new VoiceUI_Interface();
        voiceUI_interface.add((VoiceUI_Model) voiceUI_model);

        Note:
        1. You can only add one voice model for a given language in the VoiceUI_Interface. Adding a new VoiceUI_Model to the VoiceUI_Interface for the same language will replace the previous added voice model for that LANGUAGE_CODE.

        Finally, use voiceUI_interface.start((Activity) this) call to initialize the interface and start communication with the DigiOS VoiceUI. This will automatically register the voice commands for a given language based on the current Locale defined by the system.

        Code :
        voiceUI_interface.start((Activity) <current_activity>)

        Note:
        1. voiceUI_interface.start((Activity) <current_activity>) will throw exception if the interface was unsuccessful in connecting with the DigiOS Voice UI Service
        */
    private void initialize_voiceUI_interface() {
        try {
            voiceUI_interface = new VoiceUI_Interface();

            VoiceUI_Model en_voiceUI_model = new VoiceUI_Model("en");

            en_voiceUI_model.addVoiceUI_Listener(new VoiceUI_Listener("take picture",Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY) {
                @Override
                public void onReceive(){
                    Log.d(LOG_TAG,"Listener detected voice command : "+this.voice_command);
                    //TODO: Do your task
                    MAKE_A_TOAST(this.voice_command);
                }
            });

            en_voiceUI_model.addVoiceUI_Listener(new VoiceUI_Listener(new VoiceUICallback() {
                @Override
                public void onReceive(String s) {
                    Log.d(LOG_TAG,"Listener detected voice command : " + s);
                    //TODO: Do your task
                    MAKE_A_TOAST(s);
                }

                @Override
                public void onReceive(String s, int i) {

                }
            },"toggle camera",Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY));

            en_voiceUI_model.addVoiceUI_Listener(new VoiceUI_Listener("set zoom to",Voice_Command_CONFIG_TYPE_FEEDBACK_WITH_NUMBER_ONLY) {
                @Override
                public void onReceive(int value){
                    Log.d(LOG_TAG,"Detected voice command : "+this.voice_command);
                    Log.d(LOG_TAG,"Listener detected number value : "+value);
                    //TODO: Do your task
                    MAKE_A_TOAST(this.voice_command + " " + String.valueOf(value));
                }
            });

            voiceUI_interface.add_model(en_voiceUI_model);

            VoiceUI_Model es_voiceUI_model = new VoiceUI_Model("es");

            es_voiceUI_model.addVoiceUI_Listener(new VoiceUI_Listener("tomar la foto",Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY) {
                @Override
                public void onReceive(){
                    Log.d(LOG_TAG,"Listener detected voice command : "+this.voice_command);
                    //TODO: Do your task
                    MAKE_A_TOAST(this.voice_command);
                }
            });

            es_voiceUI_model.addVoiceUI_Listener(new VoiceUI_Listener(new VoiceUICallback() {
                @Override
                public void onReceive(String s) {
                    Log.d(LOG_TAG,"Listener detected voice command : " + s);
                    //TODO: Do your task
                    MAKE_A_TOAST(s);
                }

                @Override
                public void onReceive(String s, int i) {

                }
            },"alternar cámara",Voice_Command_CONFIG_TYPE_FEEDBACK_ONLY));

            es_voiceUI_model.addVoiceUI_Listener(new VoiceUI_Listener("establece el zoom en",Voice_Command_CONFIG_TYPE_FEEDBACK_WITH_NUMBER_ONLY) {
                @Override
                public void onReceive(int value){
                    Log.d(LOG_TAG,"Detected voice command : "+this.voice_command);
                    Log.d(LOG_TAG,"Listener detected number value : "+value);
                    //TODO: Do your task
                    MAKE_A_TOAST(this.voice_command + " " + String.valueOf(value));
                }
            });

            voiceUI_interface.add_model(es_voiceUI_model);

            long start = System.currentTimeMillis();
            voiceUI_interface.start(this);
            long stop = System.currentTimeMillis() - start;
            Log.d(LOG_TAG,"Time taken to register the voice commmands : "+String.valueOf(stop)+" ms");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /*
    Use voiceUI_interface.stop() call to unregister all the voice commands associated with the voiceUI_interface and disconnects the interface from DigiOS Voice UI Service.

    Note:
    1. voiceUI_interface.stop() is blocking call.
     */
    private void unregister_all_voice_command() {
        try {
            voiceUI_interface.stop();
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
