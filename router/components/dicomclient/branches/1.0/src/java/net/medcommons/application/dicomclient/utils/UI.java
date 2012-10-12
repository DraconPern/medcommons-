package net.medcommons.application.dicomclient.utils;

import static java.lang.String.format;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import net.medcommons.application.dicomclient.transactions.ContextState;

import org.apache.log4j.Logger;

/**
 * A default UI for DDL that assumes no JDK1.6 available
 * 
 * @author ssadedin
 */
public class UI {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(UI.class);

    
    /**
     * One and only UI instance
     */
    private static UI ui = new UI();

    public void verifyIdRequest() {
        String msg = 
            format("A web page you are viewing would like to connect to the MedCommons DDL installed on  your computer.\n\n"
                        + "Do you want to allow the web page to connect and send commands to your DDL?");
        showSecurityVerification(msg);
    }
    
    
    public void showSecurityVerification(final String msg) {
        
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        final Boolean [] result = {null};
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    final JFrame frame = new JFrame("DDL Security Confirmation") {{
                        setMinimumSize(new Dimension(350,140));
                        setBounds(new Rectangle(300,300,600,160));
                        setAlwaysOnTop(true);
                        setIconImage(StatusDisplayManager.activeImage);
                        setLayout(new GridBagLayout());
                        setVisible(true);
                        setAlwaysOnTop(false);
                    }};
                    
                    GridBagConstraints top = 
                        new GridBagConstraints(0,0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
                    
                    frame.add(new JLabel("<html><p>"+msg+"</p></html>", UIManager.getIcon("OptionPane.questionIcon"), SwingConstants.CENTER)
                        {{ setBounds(0, 0, 300, 90); }},top);
                    
                    GridBagConstraints bottom = 
                        new GridBagConstraints(0,1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);
                    
                    JPanel buttons = new JPanel(new FlowLayout());
                    buttons.add(new JButton("Yes") {{
                        addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
                            frame.setVisible(false);
                            frame.dispose();
                            result[0] = true;
                        }}); 
                    }});
                    buttons.add(new JButton("No") {{
                        addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
                            frame.setVisible(false);
                            frame.dispose();
                            result[0] = false;
                        }}); 
                    }});
                    frame.add(buttons,bottom);
                }
            });
            
            while(result[0]==null) {
                Thread.sleep(100);
            }
        }
        catch (Exception e) {
            log.error("Experienced error showing warning message: ",e);
            throw new RuntimeException(e);
        }
        
        if(!result[0]) 
            throw new IllegalStateException(
                            format("User refused security question: " + msg));
       
        /*
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        int result = JOptionPane.showConfirmDialog(null, msg, "DDL Security Confirmation", JOptionPane.YES_NO_OPTION);
        if(result != JOptionPane.YES_OPTION) 
            throw new IllegalStateException(
                            format("User refused permission to accept commands from web page"));
        */
    }
    
    public void verifySafeCommand(CommandBlock commandBlock) {
        String msg = 
            format("A web page you are viewing is sending a " + commandBlock.getCommand() + " command to the MedCommons DDL service on your computer.\n\n"
                        + "Do you want to allow the web page to connect and send this command to your DDL?");
        showSecurityVerification(msg);
    }
    
    public void verifyNewHost(final ContextState ctx) {
        
        String msg = 
            format("A web site is changing your MedCommons Appliance Context to a different group.\n\n"
                        + "Do you want to allow patient transfers to and from the %s group hosted at %s?", 
                           ctx.getGroupName(), ctx.getGatewayRoot());
                                
        try {
            showSecurityVerification(msg);
        }
        catch(IllegalStateException e) {
            throw new IllegalStateException(
                            format("User refused permission to change group context to group %s on host %s",
                                   ctx.getGroupName(), ctx.getGatewayRoot()));
        }
    }
    
    public static UI get() {
        return ui;
    }
}
