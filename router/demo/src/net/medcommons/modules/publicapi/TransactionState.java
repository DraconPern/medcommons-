package net.medcommons.modules.publicapi;

/**
 * States that the transaction can go
 * @author sdoyle
 *
 */
public enum TransactionState {
	UNINITALIZED, 
	NEW,
	DOWNLOADED,
	UPDATED,
	COMPLETE
}
