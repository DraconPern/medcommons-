package net.medcommons.modules.publicapi;

/**
 * Need to think this thru.
 * The server needs to track which error messages have been sent to the client.
 * The error messages may have types (merge error -> ask user to log into web site),
 * patient name change error -> ???
 * Enums might be useful here for a state machine - but we may want to encode the 
 * 
 * @author sdoyle
 *
 */
public enum ClientErrorState {
	NONE,
	MERGE
}
