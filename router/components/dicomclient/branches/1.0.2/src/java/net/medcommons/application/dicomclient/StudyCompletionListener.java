package net.medcommons.application.dicomclient;

import java.util.List;

import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.utils.DicomTransaction;

public interface StudyCompletionListener {
	

    void studyComplete(ContextState contextState, List<DicomTransaction> seriesToUpload);

    void newTransaction(DicomTransaction tx);

}
