package com.campusconnect.speech;

public interface SpeechActivator {
    /**
     * listen for speech activation, when heard, call a {@link SpeechActivationListener}
     * and stop listening
     */
    public void detectActivation();

    /**
     * stop waiting for activation.
     */
    public void stop();
}
