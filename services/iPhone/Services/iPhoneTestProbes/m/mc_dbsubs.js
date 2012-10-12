//
// sqlite interface subs for mc iphone
//
// Function: initDB()
// Init and create the local database, if possible
//
//
// Function: showError(errorString)
// Show an error
//
// errorString: string to be displayed
//
function showError(errorString)
{
    var element = document.getElementById('message');
    element.value = errorString;
    element.setAttribute('style', 'font-family: Helvetica; font-weight: bold; color: rgb(178, 6, 40);');
}


function initDB()
{
    //console.log ("calling initDB");
    try {
        if (window.openDatabase) {
            database = openDatabase("Message", "1.0", "Message Database", 1000);
            if (database) {
                //console.log (" sqlite database is open");
                database.transaction(function(tx) {
                  tx.executeSql("SELECT COUNT(*) FROM " + DB_tableName, [],
                        function(tx, result) {
                        //console.log (" Reloading Table " + DB_tableName);
                         //   reloadDB();
                                },
                    function(tx, error) {
                        // Database doesn't exist. Let's create one.
                        ctx.email=''; ctx.password='';
                        mc.log (" Creating Table " + DB_tableName);
                        tx.executeSql("CREATE TABLE " + DB_tableName +
                        " (id INTEGER PRIMARY KEY," +
                        "  key TEXT," +
                        "  value TEXT)", [], function(tx, result) {
                            initMessage();
                           // reloadDB();
                        });
                    });
                });
            }
        }
    } catch(e) {
    
        mc.log (" initDB failure code " +e.value);
        database = null;
    }
}

function forceLogout()
{
buttonLogoutClicked();
}

//
// Function: cleanTable()
// Utility function to clean table. It can be used to test table re-creation
//
function cleanTable()
{
    try {
    console.log ("forcing complete sqlite reinit with DROP TABLE " + DB_tableName);
        if (window.openDatabase) {
            database = openDatabase("Message", "1.0", "Message Database");
            if (database) {
                database.transaction(function(tx) {
                    tx.executeSql("DROP TABLE " + DB_tableName, []);
                });
            }
        }
    } catch(e) { 
    }
    forceLogout();
}

//
// Function: clearSettings()
// Reset settings to default
//
function clearSettings(event)
{

    console.log ("cleaning up table with DELETE FROM " + DB_tableName);
    if (database) {
        database.transaction(function(tx) {
            tx.executeSql("DELETE FROM " + DB_tableName, [],
            function(tx, result) {
                initMessage();
                loadMessage();
            },
            function (tx, error) {
                console.log("Can't reset database");
            });
        });
    }
    else {
        initMessage();
        loadMessage();
    }
    forceLogout();
}



//
// Function: initMessage()
// Initialize the message string to defaults. If there is database, the initialization values will be saved as well.
//
function initMessage()
{
    var element = document.getElementById('message');
    if (!element) return;
    
    // Clean inline styles so that external styles can be applied during init
    element.style.fontFamily = '';
    element.style.fontSize = '';
    element.value = originalSettings.message;
    
    if (database) {
        database.transaction(function (tx) {
            console.log ("Inserting originalSettings into sqlite");
            tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [0, 'message', originalSettings.message]);
            tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [1, 'font-family', originalSettings.fontFamily]);
            tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [2, 'font-size', originalSettings.fontSize]);
            tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [3, 'color', originalSettings.color]);
             tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [4, 'email',originalSettings.email]);
              tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [5, 'password',originalSettings.password]);
              tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [6, 'accid',originalSettings.accid]);
              tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [7, 'pracitceid',originalSettings.pracitceid]);
              tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [8, 'providerid',originalSettings.providerid]);
              tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [9, 'providername',originalSettings.providername]);
    tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [10, 'patientid',originalSettings.patientid]);
    tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [11, 'patientname',originalSettings.patientname]);
    tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [12, 'patientappliance',originalSettings.patientappliance]);
    
    tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [13, 'auth',originalSettings.auth]);
    
    tx.executeSql("INSERT INTO " + DB_tableName + " (id, key, value) VALUES (?, ?, ?)", [14, 'auth',originalSettings.practicename]);
    
    
    
             });
    }
}

//
// Function: fontSizeChanged(event)
// Update the database when user changed the font size setting
//
//
function updateDB()
{
    if (database) { 
        database.transaction(function (tx) {
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'email', value = ? WHERE id = 4", [ctx.email]);            
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'password', value = ? WHERE id = 5", [ctx.password]);     
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'accid', value = ? WHERE id = 6", [ctx.accid]);     
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'pracitceid', value = ? WHERE id = 7", [ctx.pracitceid]);     
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'providerid', value = ? WHERE id = 8", [ctx.providerid]);     
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'providername', value = ? WHERE id = 9", [ctx.providername]);     
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'patientid', value = ? WHERE id = 10", [ctx.patientid]);     
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'patientname', value = ? WHERE id = 11", [ctx.patientname]);     
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'patientappliance', value = ? WHERE id = 12", [ctx.patientappliance]);    
            
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'auth', value = ? WHERE id = 13", [ctx.auth]);   
                tx.executeSql("UPDATE " + DB_tableName + " SET key = 'practicename', value = ? WHERE id = 14", [ctx.practicename]);  
        });
    }
}



//
//
function reloadDBandFlip()
{
    var element = document.getElementById('message');
    if (database) {
    
        database.transaction(function(tx) {
            tx.executeSql("SELECT key, value FROM " + DB_tableName, [],
            function(tx, result) {
                for (var i = 0; i < result.rows.length; ++i) {
                    var row = result.rows.item(i);
                    var key = row['key'];
                    var value = row['value'];
                    mc.log ("     row " + i + "key "+key+"value "+value);

                    if (key == 'message') {
                        element.value = value;
                    }
                    else {
                        element.style[key] = value;
                        if (key == 'font-family') {
                            updateSelectValue(document.getElementById('fontFamily'), value);
                        }
                        else if (key == 'font-size') {
                            updateSelectValue(document.getElementById('fontSize'), value);
                        }
                        else if (key == 'color') {
                            updateColorChip(value);
                        } else
                            if (key == 'email') {
                            document.getElementById('email').value =  value;
                            ctx.email = value;
                             }
                        else if (key == 'password') {
                           document.getElementById('password').value = value;
                           ctx.password = value;
                        }
                        else if (key == 'accid') {
                          // document.getElementById('accid').value = value;
                           ctx.accid = value;
                           mc.log ("setting ctx.accid to " + ctx.accid);
                        }else if (key == 'pracitceid') {
                           //document.getElementById('pracitceid').value = value;
                           ctx.pracitceid = value;
                        }else if (key == 'providerid') {
                          // document.getElementById('providerid').value = value;
                           ctx.providerid = value;
                        }else if (key == 'providername') {
                          // document.getElementById('providername').value = value;
                           ctx.providername = value;
                        }else if (key == 'patientid') {
                          // document.getElementById('patientid').value = value;
                           ctx.patientid = value;
                        }else if (key == 'patientname') {
                          /// document.getElementById('patientname').value = value;
                           ctx.patientname = value;
                        }else if (key == 'patientappliance') {
                          // document.getElementById('patientappliance').value = value;
                           ctx.patientappliance = value;
                        }
                        else if (key == 'auth') {
                          // document.getElementById('patientappliance').value = value;
                           ctx.auth = value;
                        }
                        else if (key == 'practicename') {
                          // document.getElementById('patientappliance').value = value;
                           ctx.practicename = value;
                        }
                        
                    }
                }
                // when everything is done, call flipToFront
                
                 mc.log ("read saved values from sqlite");
                flipToFront(element);
            },
            function(tx, error) {
               mc.log('Failed to retrieve stored information from database - ' + error.message);
            });
        });
    }
    else {
        // Load defaults
        updateColorChip(originalSettings.color);
        updateSelectValue(document.getElementById('fontFamily'), originalSettings.fontFamily);
        updateSelectValue(document.getElementById('fontSize'), originalSettings.fontSize);
    }
}

//
// Function: messageChanged(event)
// Update the database when user changed the message
//
//
function messageChanged(event)
{
    if (database) {
        var element = document.getElementById('message');
        database.transaction(function (tx) {
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'message', value = ? WHERE id = 0", [element.value]);
        });
    }
}

//
// Function: fontFamilyChanged(event)
// Update the database when user changed the font family setting
//
//
function fontFamilyChanged(event)
{
    var value = document.getElementById('fontFamily').value;
    document.getElementById('message').style.fontFamily = value;
    
    if (database) {
        database.transaction(function (tx) {
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'font-family', value = ? WHERE id = 1", [value]);
        });
    }
}

//
// Function: fontSizeChanged(event)
// Update the database when user changed the font size setting
//
//
function fontSizeChanged(event)
{
    var value = document.getElementById('fontSize').value;
    document.getElementById('message').style.fontSize = value;
    
    if (database) {
        database.transaction(function (tx) {
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'font-size', value = ? WHERE id = 2", [value]);
        });
    }
}
//
// Function: fontSizeChanged(event)
// Update the database when user changed the font size setting
//
//


//
// Function: colorChanged(event)
// Update the database when user changed the color setting
//
//
function colorChanged(event)
{
    var element = event.target.parentNode;
    var index = element.id.indexOf('ColorChip');
    var color = element.id.substring(0, index);
    updateColorChip(color);
    
    if (database) {
        database.transaction(function (tx) {
            tx.executeSql("UPDATE " + DB_tableName + " SET key = 'color', value = ? WHERE id = 3", [color]);
        });
    }
}