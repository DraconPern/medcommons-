
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import javax.swing.JApplet;

import netscape.javascript.JSObject;

public class DDLChannelApplet extends JApplet {
    
    List<String[]> commands = new ArrayList<String[]>();
    
    
    List<String[]> results = new ArrayList<String[]>();
    
    
    Timer timer = new Timer(true);
    
    PrintStream out = System.out;
    
    JSObject js;
    
    public void init() {
        out.println("Starting DDL Channel Applet");
        
        // Launch thread to poll for tasks
        Thread t = new Thread("DDL Channel Connector") {
            @Override
            public void run() {
                pollTasks();
            }
        };
        t.setDaemon(true);
        t.start();
        
        js = JSObject.getWindow(this);
    }
    
    protected void pollTasks() {
        while(true) {
            try {
                String[] command = null;
                synchronized(commands) {
                    if(commands.isEmpty()) {
                        commands.wait();
                    }
                    command = commands.remove(0);
                }
                processCommand(command);
            }
            catch (Exception e) {
                out.println("Error in DDL communicator loop: " + e.toString());
                e.printStackTrace(out);
            }
        }
    }
    
    private void processCommand(String[] command) throws MalformedURLException, IOException {
        out.println("Processing command: " + join(command, ","));
        
        if(command.length == 0) {
            out.println("No command sent");
            return;
        }
        
        // the first argument is the command name, and the rest are to be url encoded as parameters
        String [] params = new String[ (command.length -1) / 2];
        String cmd = command[0];
        for(int i = 1; i < command.length; i++) {
            params[(i-1)/2] = URLEncoder.encode(command[i], "UTF-8") + "=" + URLEncoder.encode(command[++i],"UTF-8");
        }
        String url = "http://localhost:16092/CommandServlet?command="+URLEncoder.encode(cmd, "UTF-8") + "&"+join(params,"&");
        out.println("Sending command URL " + url);
        
        InputStream in = new URL(url).openStream();
        StringBuilder result = new StringBuilder();
        try {
            byte [] buffer = new byte[4096];
            int count = 0;
            while((count = in.read(buffer)) >= 0) {
                result.append(new String(buffer,0,count, "UTF-8"));
            }
        }
        finally {
            in.close();
        }
        String response = result.toString().trim();    
        out.println("Received response: " + response);
        js.eval(response);
    }

    
    
    public int sendCommand(String... args) {
        StringBuilder dump = join(args,",");
        out.println("Received command: " + dump);
        
        synchronized (commands) {
            commands.add(args);    
            commands.notifyAll();
        }
        
        return 0;
    }

    private StringBuilder join(String[]args, String sep) {
        StringBuilder dump = new StringBuilder();
        for(int i = 0; i < args.length; i++) {
            if(i>0)
                dump.append(sep);
            dump.append(args[i]);
        }
        return dump;
    }
    
    public String getTestData() {
        try {
            String url = "http://localhost/test.php";
            InputStream is = new URL(url).openStream();
            StringBuilder result = new StringBuilder();
            byte [] buffer = new byte[2048];
            int len = 0;
            while((len = is.read(buffer)) > 0) {
                result.append(new String(buffer, 0, len, "UTF-8"));
            }
            return url + ":" + result.toString();
        }
        catch (MalformedURLException e) {
            return "error: " + e;
        }
        catch (IOException e) {
            return "error: " + e;
        }
        
    }
    
    public String getVersion() {
        return System.getProperty("java.version");
    }
}
