package net.medcommons.application.dicomclient.utils;

import java.util.concurrent.Future;

import org.json.JSONObject;

public interface Command {
    
    Future<JSONObject> execute(CommandBlock params);

}
