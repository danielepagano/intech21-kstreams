package com.nice.intech21.streams;


import com.nice.intech.AgentStateOuterClass;

public enum ActivityTimeClass {
    AVAILABLE, WORKING_CONTACTS, UNAVAILABLE, SYSTEM;

    public static ActivityTimeClass classify(AgentStateOuterClass.AgentState agentState) {
        switch (agentState != null ? agentState : AgentStateOuterClass.AgentState.UNRECOGNIZED) {
            case AVAILABLE:
                return AVAILABLE;
            case UNAVAILABLE:
                return UNAVAILABLE;
            case INBOUND_CONTACT:
            case OUTBOUND_CONTACT:
            case INBOUND_CONSULT:
            case OUTBOUND_CONSULT:
            case DIALER:
                return WORKING_CONTACTS;
            case LOGGED_IN:
            case LOGGED_OUT:
            case UNRECOGNIZED:
            default:
                return SYSTEM;
        }
    }
}
