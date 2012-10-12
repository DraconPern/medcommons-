package net.medcommons.modules.filestore;

import net.medcommons.router.services.repository.RepositoryException;

public class TransactionException extends RepositoryException {
	public TransactionException(){
		super();
	}
	public TransactionException(String reason){
		super(reason);
	}
	public TransactionException(String reason, Throwable t){
		super(reason, t);
	}
}
