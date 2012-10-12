Readme.txt for /php/p

This is the ============== Portal Replacement Package 5/8/10 =================================

 -- this version works with the existing MCX Group tables to add 'Portal' features for integrators and developers
 
 -- there is no special iPhad code here at all, perhaps web use from mobile safari should be discouraged
 
 
 
 Included here:
 
 
 fuf.php - a tool for building customized Upload Forms that a specific to particular business models
 
 uh.php - a standardized component that runs behind the scenes as an upload handler that reacts to the customized forms
 
 pl.php - a patient list navigator and display program that shows all patient lists 
 
 css/pstyle.css - some basic css for form and display presentation
 
 
Also, it is anticipated we will accumulate a small collection of different styles of upload forms, the inventory now is represented by:


 uploader0 - our standard up and out form

 uploader1 - jans form for vascular 


Although these are php files, they are really just cloneable html with a small php prefix, and can be extended by integrators


How to Deploy:

-- this is pretty fussy and needs to go in one level down from the top of the appliance eg http://x.y.z/p/...

-- everything should work immediately using the standard uploader0 form

-- set a bookmark at http://x.y.z/p/pl.php to enjoy fast navigation between patient lists

-- the view source output of fuf.php can be used by an integrator to build custom pages (see p.m.n/vascular, w.m.n/mets)

-- fuf.php can be deleted for safety after all interesting has been deployed, it can always be restored 

Notable Deficiencies:

-- the "custom labels"  feature for order verification of the previous portal code is not implemented, but could be reactivated

-- the naming and portage of all the fields from the custom form, to the standard order process, to the display, is hap-hazard and needs a formal review

-- the display is slapdash but effective, and relies upon the viewer itself to supply all features like sharing, etc. To fully implement communications via 

-- skype, sms, etc, will require a bit more discipline in the naming of fields and some additional code in the viewer 

-- there's really no field editing on these various fields beyond html5 tagging which DOESNT DO MUCH OF ANYTHING EXCEPT IN OPERA - javascript may need to be added, help!
