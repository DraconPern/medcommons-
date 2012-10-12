<?
/**
 * General DDL detection HTML.
 * <p>
 * This code works with dod.js to automatically detect and coach the user through
 * starting or updating their DDL.  Would be nice to rework it as pure javascript.
 */
?><div id='wholeStep1' <?if(req("step") && req("step")!="1"):?>class='hidden'<?endif;?> >
    <input type='hidden' id='startDDLUrl' value='<?=$startDDLUrl?>'/>
    <div id='step1boxes' class='section'>
        <p class='middled startsec' id='detecting'><img src='images/bigloading.gif'/> &nbsp; Please wait while 
           we detect your current system setup!</p>
        
        <p class='middled hidden startsec' id='waiting'><img src='images/bigloading.gif'/> &nbsp;
            Please approve security questions as they come up.  The startup may take a few minutes.</p>
            
        <span id='jre'></span>
           
        <div id='installJavaStep' class='hidden startsec'>
            <p id='installJavaMsg'>We were unable to find a sufficient version of Java installed on your computer.</p>
            <ul>
                <li>Please click below to install or update the version of Java on your computer. It will open a new window.</li>
                <li>When the installation is complete, reload this page or click the button that
                    will be shown to start the transfer utiltity.</li>
            
            <li style='list-style-type: none; margin-top: 1em;'>
               <button id='installJavaButton'>Install Java</button>
               <button id='startDDLAnyway'>I have Java - Try Starting the Transfer Utility</button>
               </li>
            </ul>
        </div>
        
        <div id='installingJavaStep' class='hidden startsec'>
            <p>When your installation of Java finishes, please click below to reload this page.</p>
            <p><b>Please note that some browsers may require to be restarted before the new version of Java can be used</b>.</p>
        
            <p><button id='javaInstallFinished'>Click Here When Java Install Finished</button></p>
        </div>
        
        <?/* This section is shown if we have evidence that the user has a DDL installed
             (eg: ddlid cookie) but we weren't able to probe for it (eg: because IE mixed content 
             security was blocked) */?>
        <div id='connectDDLStep' class='hidden startsec'>
            <p><button id='connectDDLButton'>Connect to Transfer Utility</button></p>
        </div>
        
        <p class='middled hidden startsec' id='connectingToDDL'><img src='images/bigloading.gif'/> &nbsp; Connecting to your DDL ...</p>
        
        <div id='installDDLStep' class='hidden startsec'>
            <h3>Start Transfer Utility</h3>
            <p id='unableToConnectMsg'>To upload imaging data we need to start a small transfer utility on your computer.</p>
            <p><button id='startDDLLink' class='mainlarge'>Start Transfer Utility</button></p>
        </div>
        
        <div id='foundDDL' class='hidden startsec'>
            <p class='middled'><img src='images/bigtick.png'/> &nbsp; A local DDL was found running on your computer!  
                You are ready to upload data.</p>
            <p><button id='continueFillOutFormButton'>Continue</button>
        </div>
        <div id='restartDDLStep' class='hidden startsec'>
            <p style='font-weight: bold;'>Update Required</p>
            <p>A transfer utility was detected on your computer, but it needs to be restarted to
               update to work with this page.</p>
                <p><a href='<?=$startDDLUrl?>' id='restartDDLLink'>Click Here to Update Your Transfer Utility.</a></p>
        </div>
        <div id='restartingDDL' class='hidden startsec'>
            <p><b>DDL Updating</b></p>
            <p>Please wait until your transfer utility has updated and restarted, then click below to refresh your page.</p>
                <p><a href='javascript:window.location.reload()'>Click Here when Transfer Utility has Updated</a></p>
        </div>
        
        <div id='startProblemHelp' class='hidden startsec'>
            <h4>Having Problems?</h4>
            <p>We haven't been able to connect to your local Transfer Utility.</p>
            <p>It might be that there was a problem downloading, installing or running it.</p>
            <ul>
                <li>If you can see that something is actively downloading or starting up, just wait 
                    a few minutes.  This message will automatically disappear when it is ready.</li>
                <li>If nothing at all seems to be happening, and this is the first time you saw 
                    this message, try starting it again by reloading the page.   
                    <br>
                    <a class='obviousLink' href='javascript:window.location.reload();'>Click Here to try Starting the Transfer Utility Again</a>
                </li>
                <li>If nothing seems to be working, let us know by sending a Problem Report. Include 
                    contact details if you would like us to contact you. 
                    <br>
                <a class='obviousLink' href='javascript:showProblemForm();'>Click Here to Send a Problem Report</a>.</li>
            </ul>
        </div>
    </div>
</div>
