package com.campusconnect.speech;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class WordActivator implements SpeechActivator, RecognitionListener {
	
	private String TAG = "WordActivator";
	private Context context;
    private SpeechRecognizer sr;
    private SpeechActivationListener resultListener;
    private String helpWord;
    
    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));
    protected AudioManager mAudioManager; 

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
	
    public WordActivator(Context context, SpeechActivationListener resultListener, String helpWord) {
    	this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE); 
        this.context = context;
        this.resultListener = resultListener;
        this.helpWord = helpWord;
    }
    
    @Override
    public void detectActivation() {
    	recognizeSpeechDirectly();
    }
    
    private void recognizeSpeechDirectly() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);        
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.campusconnect.speech");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        mSpeechRecognizerIntent = intent;
        getSpeechRecognizer().startListening(intent);
        Log.d(TAG,"Listening");
    }
    
    /**
     * lazy initialize the speech recognizer
     */
    private SpeechRecognizer getSpeechRecognizer() {
        if (sr == null) {
            sr = SpeechRecognizer.createSpeechRecognizer(context);
            sr.setRecognitionListener(this);
        }
        return sr;
    }
    
    public void stop() {
        if (sr != null) {
        	sr.stopListening();
        	sr.cancel();
        	sr.destroy();
            mNoSpeechCountDown.cancel();
        }
        sr = null;
    }
    
	protected static class IncomingHandler extends Handler {
		private WeakReference<WordActivator> mtarget;
	
		IncomingHandler(WordActivator target) {
			mtarget = new WeakReference<WordActivator>(target);
		}
	
		@Override
		public void handleMessage(Message msg) {
			final WordActivator target = mtarget.get();

			switch (msg.what) {
				case MSG_RECOGNIZER_START_LISTENING:
	
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						// turn off beep sound  
						target.mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
					}
					if (!target.mIsListening) {
						target.getSpeechRecognizer().startListening(target.mSpeechRecognizerIntent);
						target.mIsListening = true;
					}
					break;
	
				case MSG_RECOGNIZER_CANCEL:
					target.getSpeechRecognizer().cancel();
					target.mIsListening = false;
					break;
			}
		} 
	} 

    // Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {

		@Override
		public void onTick(long millisUntilFinished) {}

		@Override
		public void onFinish() {
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
			try {
				mServerMessenger.send(message);
				message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
				mServerMessenger.send(message);
			} catch (RemoteException e) {}
		}
    };
	
    public void onReadyForSpeech(Bundle params) {
    	Log.d(TAG, "onReadyForSpeech");
		mIsCountDownOn = true;
		mNoSpeechCountDown.start();
    }
    
    public void onBeginningOfSpeech() {
		// speech input will be processed, so there is no need for count down anymore
		if (mIsCountDownOn) {
		    mIsCountDownOn = false;
		    mNoSpeechCountDown.cancel();
		}
    }
    
    public void onRmsChanged(float rmsdB) {
    	//Log.d(TAG, "onRmsChanged");
    }
    
    public void onBufferReceived(byte[] buffer) {
    	Log.d(TAG, "onBufferReceived");
    }
    
    public void onEndOfSpeech() {
    	Log.d(TAG, "onEndofSpeech");
    }
    
    public void onError(int error) {
    	if ((error == SpeechRecognizer.ERROR_NO_MATCH) || (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
            Log.d(TAG, "didn't recognize anything");
            // keep going
            //recognizeSpeechDirectly();
        }
    	Log.d(TAG, "ERROR: " + error);
		if (mIsCountDownOn) {
			mIsCountDownOn = false;
			mNoSpeechCountDown.cancel();
		}
		mIsListening = false;
		Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
		try {
			mServerMessenger.send(message);
		} catch (RemoteException e) {}
    }
    
    public void onResults(Bundle results) {
    	boolean heardHelpWord = false;
    	Log.d(TAG, "onResults " + results);
    	ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    	for (int i = 0; i < data.size(); i++) {
    		Log.d(TAG, "result " + data.get(i));
    		if( data.get(i).toLowerCase().equals(helpWord) )  {
    			stop();
    			resultListener.activated(true);
    			heardHelpWord = true;
    			break;
    		}
    	}
    	if( !heardHelpWord )
    		recognizeSpeechDirectly();
    }
    
    public void onPartialResults(Bundle partialResults) {
    	Log.d(TAG, "onPartialResults");
    }
    
    public void onEvent(int eventType, Bundle params) {
    	Log.d(TAG, "onEvent " + eventType);
    }

}
