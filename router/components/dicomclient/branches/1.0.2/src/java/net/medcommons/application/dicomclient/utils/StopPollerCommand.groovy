package net.medcommons.application.dicomclient.utils;

import java.util.concurrent.Future;

import org.json.JSONObject;



public class StopPollerCommand implements Command {

    public Future<JSONObject> execute(CommandBlock params) {

       
       if(!PollGroupCommand.running)
           return [ isDone: { true },  get: { "No Poller" }] as Future

       PollGroupCommand.running.stopExistingPoller();

       if(params.properties.permanent == "true")
           PollGroupCommand.configFile.delete()

       return [ isDone: { true },  get: { "Stopped" }] as Future
    }
    
}
