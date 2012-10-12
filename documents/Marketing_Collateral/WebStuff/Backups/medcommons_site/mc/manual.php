<html>
<head>
<meta http-equiv=Content-Type content="text/html; charset=windows-1252">
<link rel=Edit-Time-Data
href="User%20Guide%20in%20510(k)%20CB_files/editdata.mso">
<title>MedCommons OpenRadiology Gateway User Guide</title>
<style><!--
.Section1
	{page:Section1;}
.Section2
	{page:Section2;}
-->
</style>
</head>
<body bgcolor="#FFFFFF" link=blue vlink=purple class="Normal" lang=EN-US><div class=Section1> 
<h2>Product Identification</h2>
<p> <img width=367 height=73
src="images/image002.jpg" v:shapes="_x0000_i1028"> </p>
<h1>MedCommons  Open Radiology™ Gateway User Guide</h1>
<p>V 0.5</p>
<p>Part Number 01-0001</p>
<p><st1:date Month="5" Day="14" Year="2004" w:st="on">May 14, 2004</p>
<h2>Copyright Notice</h2>
<p>Copyright © 2004 MedCommons, Inc., <st1:address w:st="on"><st1:Street
 w:st="on">52 Marshall St. <st1:City w:st="on">Watertown, <st1:State w:st="on">MA 
  <st1:PostalCode w:st="on">02472<br>
  &nbsp;</p>
<p>All Rights Reserved. Portions of this document may be put into the public domain 
  under the Creative Commons guidelines.</p>
<p>This user guide has been produced to assist in providing instruction for the 
  MedCommons Open Radiology™ Gateway product.  Every effort has been made to make 
  the information in this guide as accurate as possible.  The authors of this 
  guide shall have neither liability nor responsibility to any person or entity 
  with respect to any loss or damages in connection with or arising from the information 
  contained in this guide.</p>
<h2>Trademarks</h2>
<p>MedCommons, Open Radiology, and MedCommons Gateway are trademarks of MedCommons, 
  Inc.  Any additional software products named in this document are claimed as 
  trademarks or registered trademarks of their respective companies.</p>
<h2>Contact Information</h2>
<p>MedCommons, Inc.</p>
<p><st1:Street w:st="on"><st1:address w:st="on">52 Marshall St.</p>
<p><st1:place w:st="on"><st1:City w:st="on">Watertown, <st1:State w:st="on">MA 
  <st1:PostalCode w:st="on">02472</p>
<p>Phone 617 395 6744</p>
<p>Fax 617 924 5329</p>
<p><a href="http://www.medcommons.net/">www.medcommons.net</a></p>
<p>mailto: <a href="mailto:support@medcommons.net">support@medcommons.net</a></p>
<h2><a name=UGCautions>Before you begin…</a></h2>
<p>This software is intended for use by licensed clinical practitioners who are 
  trained appropriately in the software’s functions and applications and are aware 
  of its limitations.</p>
<p>Poor ambient conditions (e.g.: excessive room light), improper display calibration 
  (e.g.: excessive contrast), careless application of image manipulation tools 
  (e.g.: window and level adjustment) and inadequate familiarity with the user 
  interface (e.g.: not reviewing all available information) can all result in 
  incorrect interpretations of an imaging study.</p>
<p>You should also be aware that the image information being processed by MedCommons 
  Gateway™ is potentially incomplete or does not adequately conform to the standards 
  as implemented by the device.  To mitigate the impact of these problems, you 
  should take into account the underlying imaging protocols and recognize the 
  potential absence or gross corruption of information. </p>
<p>This device is designed, manufactured and tested to reduce the risk of subtle 
  and serious corruption of labels and image contents.  Internal checks and redundant 
  external labels are used to reduce the likelihood of misinterpretation due to 
  internal software errors but these are not foolproof and cannot compensate for 
  bad information coming into the device. </p>
<p>On occasion, interpolated (magnified) data may introduce image artifacts which 
  should not be interpreted as real pathology.  When artifacts are suspected, 
  you should be able to change the magnification ratio or window/level setting 
  to help differentiate artifacts from pathology.</p>
<h2><a name=UGProdSpecs>System Requirements</a></h2>
<p>Hardware: </p>
<p>Pentium III or compatible processor</p>
<p>512 MB RAM</p>
<p>100 MB Hard Drive Space</p>
<p>Minimum Display Resolution 1024x768 pixels</p>
<p>Color Table Resolution 24 or 32bits per pixel</p>
<p>Operating System</p>
<p>Windows XP</p>
<p>Microsoft IE 5 or higher</p>
<p>Please check the support and help files at <a
href="http://www.medcommons.net/">www.medcommons.net</a> for the most up-to-date 
  system requirements.</p>
<h2>Software License</h2>
<p>This License Agreement (“Agreement”) is a legal agreement between you (either 
  an individual or a legal entity) (“Licensee”, “you”, “your”, as context requires) 
  and MedCommons, Inc., a corporation having its principal place of business at 
  <st1:address w:st="on"><st1:Street w:st="on">52 Marshall St. <st1:City w:st="on">Watertown 
  <st1:State w:st="on">MA.  BY INSTALLING, COPYING, DOWNLOADING, ACCESSING, OR 
  OTHERWISE USING THE SOFTWARE, YOU AGREE TO BE BOUND BY THE FOLLOWING TERMS AND 
  CONDITIONS.  PLEASE READ THESE TERMS AND CONDITIONS CAREFULLY AS THEY GOVERN 
  YOUR USE OF THE SOFTWARE.  If you do not agree to the terms and conditions of 
  this Agreement, please do NOT install the Software and immediately destroy any 
  copies of the Software and Documentation in your possession.  </p>
<h2>Open Source Licenses</h2>
<p>This software is licensed under various Open Source licenses including The 
  GNU General Public License (GPL) Version 2 and GNU Lesser General Public License 
  Version 2.1 as published and Copyright Free Software Foundation,, Inc. 59 <st1:City w:st="on">Temple 
  Place, Suite 330, <st1:City
w:st="on">Boston, MA 02111-1307 <st1:place w:st="on"><st1:country-region
 w:st="on">USA</st1:country-region>.</p>
<p>Source code is available for download directly from the Support section at 
  <a href="http://www.medcommons.net/">www.medcommons.net</a>.  Individual Copyrights 
  and Software Licenses are determined by the specific contributors of each of 
  the components of this software distribution and can be reviewed as attached 
  to each source file.</p>
<h2>Non-Open Source Licenses</h2>
<p>Some components of this software distribution are not Open Source.  They are 
  licensed by MedCommons, Inc. and, where applicable, its suppliers and licensors 
  in a manner that is compatible with the Open Source components of this distribution.</p>
<h2>License Restrictions</h2>
<p>You agree that you shall only use the Software and Documentation in a manner 
  that complies with all applicable laws, regulations and the like in the jurisdictions 
  in which you use the Software and Documentation, including, but not limited 
  to, applicable restrictions concerning medical uses and copyright and other 
  intellectual property rights. </p>
<p>The Software or the use of the Software may be subject to legal or regulatory 
  provisions related to products used in the health care.  Prior to using the 
  Software, it is your responsibility to ensure that your use of the Software 
  will not violate any legal or regulatory provisions.  Please contact MedCommons, 
  Inc. to determine the most recent information regarding legal and regulatory 
  approvals.</p>
<h2>Updates and Bugs</h2>
<p>MedCommons reserves the right at any time to, but is not obligated to, correct 
  errors, alter features, specifications, capabilities, functions, release dates, 
  general availability or other characteristics of the Software (“Updates”).  You 
  agree that you may not have access to such Updates without separate payment 
  or through a separate support agreement.  You agree and understand that although 
  MedCommons takes steps to prevent errors, the Software may contain errors affecting 
  proper operation.</p>
<h2>Title</h2>
<p>MedCommons retains all right title and interest in and to the trademarks, trade 
  names, logos and icons (collectively “Marks”) used in or identifying the Software 
  or its features and you may not use such Marks without the prior written consent 
  of MedCommons.</p>
<h2>No Warranty</h2>
<p>DISCLAIMER OF WARRANTY &amp; LIMIT OF LIABILITY.  TO THE MAXIMUM EXTENT PERMITTED 
  BY APPLICABLE LAW, MEDCOMMONS EXPRESSLY DISCLAIMS ALL WARRANTIES, INCLUDING 
  WITHOUT LIMITATION ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
  PARTICULAR PURPOSE, TITLE OR NONINFRINGEMENT TO THE MAXIMUM EXTENT PERMITTED 
  BY APPLICABLE LAW.  IN NO EVENT SHALL MEDCOMMONS BE LIABLE FOR ANY CONSEQUENTIAL, 
  INCIDENTAL, INDIRECT, SPECIAL, PUNITIVE, OR OTHER DAMAGES WHATSOEVER (INCLUDING, 
  WITHOUT LIMITATION, DAMAGES FOR LOSS OF PROFITS, SERVICE INTERRUPTION, LOSS 
  OF INFORMATION, OR OTHER PECUNIARY LOSS) ARISING OUT OF THIS AGREEMENT OR THE 
  USE OF OR INABILITY TO USE THE SOFTWARE OR DOCUMENTATION, EVEN IF MEDCOMMONS 
  HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  MEDCOMMONS’ TOTAL LIABLITY 
  FOR ANY DIRECT DAMAGES SHALL NOT EXCEED FIVE DOLLARS ($5.00).  BECAUSE SOME 
  JURISDICTIONS DO NOT ALLOW THE EXCLUSION OR LIMITATION OF LIABILITY FOR CONSEQUENTIAL 
  OR INCIDENTAL DAMAGES, THE ABOVE LIMITATION(S) MAY NOT APPLY TO YOU.</p>
<h2>Indemnification</h2>
<p>The Software is not intended to replace the skill and judgment of a qualified 
  medical practitioner and should only be used by people that have been appropriately 
  trained in the Software’s functions, capabilities and limitations.  You agree 
  to hold harmless, indemnify and defend MedCommons, its suppliers, officers, 
  directors and employees, from and against any losses, damages, fines and expenses 
  (including attorneys’ fees and costs) arising out of or relating to your use 
  of the Software.  THE ENTIRE RISK ARISING OUT OF YOUR USE OF THE SOFTWARE AND 
  DOCUMENTATION REMAINS WITH YOU.</p>
<h2>Termination</h2>
<p>This License Agreement will automatically terminate if you fail to comply with 
  any term hereof.  No notice shall be required from MedCommons to effect such 
  termination.</p>
<h2>Export Restriction</h2>
<p>Parts of this distribution may be subject to the limitations on transfer imposed 
  by the <st1:place w:st="on"><st1:country-region w:st="on">United States</st1:country-region>’ 
  Export Administration Act of 1979 as amended.  You agree that you will not, 
  and will not assist or permit others under your control and direction to, export 
  the software or documentation or any part thereof, in contravention of these 
  laws or the related rules and regulations.</p>
<h2>Miscellaneous</h2>
<p>This Agreement shall constitute the complete and exclusive agreement between 
  the Parties, notwithstanding any variance with any other written instrument 
  submitted by you, whether formally rejected by MedCommons or not.  The terms 
  and conditions contained in this Agreement may not be modified except in a writing 
  duly signed by you and an authorized representative of MedCommons.  This Agreement 
  is personal to you, and may not be assigned without MedCommons’ express written 
  consent.  If any provision of this Agreement is held to be unenforceable for 
  any reason, such provision shall be reformed only to the extent necessary to 
  make it enforceable, and such decision shall not affect the enforceability of 
  such provision under other circumstances, or of the remaining provisions hereof 
  under all circumstances.  This Agreement shall be governed by the laws of the 
  <st1:PlaceType w:st="on">Commonwealth of <st1:PlaceName
w:st="on">Massachusetts, without regard to conflicts of law provisions, and you 
  hereby consent to the exclusive jurisdiction of the courts sitting in <st1:place w:st="on"><st1:State w:st="on">Massachusetts. 
   To the extent that you have breached or have indicated your intention to breach 
  this Agreement in any manner which violates or may violate MedCommons’ intellectual 
  property rights, or may cause continuing or irreparable harm to MedCommons (including, 
  but not limited to, any breach that may impact MedCommons’ intellectual property 
  rights), MedCommons may seek injunctive relief, or any other appropriate relief, 
  in any court of competent jurisdiction.</p>
<br clear=all style='page-break-before:always'>
<h1>MedCommons Open Radiology™ Gateway Overview</h1>
<p>The MedCommons™ Open Radiology™ Gateway is Open Source Software.</p>
<p>Medical images are increasing in resolution and accuracy and are playing an 
  ever more important role in medical care.  Improved resolution and accuracy 
  coupled with increasing computer processing capacity enables enhanced diagnosis, 
  treatment planning and evidence-based medicine. </p>
<p>MedCommons is founded on the belief that public peer review of software and 
  open access to sophisticated software algorithms are essential to managing the 
  next generation of health care software.</p>
<p>In the MedCommons Gateway™, the processing chain from DICOM modality to the 
  physician’s eye is based on Open Source Software and is intended for clinical 
  use.</p>
<p>High-resolution data sets, such as multi-detector CT and modern MRI, increasingly 
  benefit from sophisticated processing to display data quickly and efficiently. 
   Whether as simple as interpolation between pixels or as complex as tracking 
  the location of an aortic stent, image manipulation methods—from the acquisition 
  protocols to the physician’s user interface—are becoming too important to keep 
  secret.</p>
<p>Open Source Software architecture, particularly in the image and image interpretation 
  pipelines, enables peer review and incremental extension of functionality.  Because 
  Open Source methods allow for public visibility at all stages of the software 
  life cycle, all bugs are public and innovators are less prone to introducing 
  errors when they re-invent or reverse engineer the functionality of critical 
  subsystems.</p>
<p>Open Source Software also provides powerful components for data interchange 
  and archival storage.  Open Source Software protects the data by enabling interchange 
  between vendors even when standards compliance is imperfect, or when vendor-provided 
  support is unavailable.</p>
<p>As a complete image acquisition, management and display device in a format 
  accessible to clinicians, MedCommons provides an Open Source Software alternative 
  to physicians as they take increasing responsibility for understanding and using 
  ever more sophisticated software-based tools.  </p>
<br clear=all
style='page-break-before:always'>
<h2><a name=UGIndications>Indications for Use</a></h2>
<p>MedCommons Open Radiology™ Gateway is a software application for viewing medical 
  images.  Typical MedCommons Gateway™ users are healthcare professionals, such 
  as clinicians, radiologists, and technologists.</p>
<p>MedCommons Gateway™ receives, communicates, and displays digital images and 
  data from various types of imaging and image processing system, such as CT, 
  <st1:place w:st="on"><st1:City w:st="on">MR, <st1:country-region
 w:st="on">US</st1:country-region>, RF units, computed and direct radiographic 
  devices, scanners, imaging gateways and image processing sources).  MedCommons 
  Gateway™ can be integrated with an institution’s HIS or RIS, linking or transferring 
  images and data into electronic patient records.</p>
<h2><a name=UGOperatingInstructions>&nbsp;</a></h2>
<h2>Registration and Installation</h2>
<p> <img border=0 width=576 height=570
src="images/image003.jpg" v:shapes="_x0000_i1027"> </p>
<p> <img border=0 width=575 height=814
src="images/image004.jpg" v:shapes="_x0000_i1025"> </p>
<br clear=all
style='page-break-before:always'>
<h2>The WADO Viewer</h2>
<p> <img border=0 width=575 height=767
src="images/image006.jpg" v:shapes="_x0000_i1026"> </p>
<br clear=all
style='page-break-before:always'>
<h2>Using the Tools</h2>
<p>Image display is controlled primarily by gestures made with a mouse or equivalent 
  pointing tool such as the “pen” of a tablet PC.  Some actions have keyboard 
  alternatives.</p>
<p>The two principal gestures are click and drag.  A click is typically used to 
  select or activate.  Drag is the gesture that draws an imaginary line or rectangle 
  across the screen.  Drag gestures are used to indicate a region or to achieve 
  proportional control.</p>
<p>Gestures cause different actions depending on the region (e.g.: image display, 
  thumbnail display, tool palette, menu) of the screen where they are begun.  
  Most gestures provide the user with visual feedback as they are performed.</p>
<ul type=disc>
  <li><b>Display Menu</b></li>
</ul>
<p>A Display Menu at the bottom of the WADO Viewer lists all of the available 
  series for the current patient / study.  A check mark next to an item indicates 
  that the thumbnail for that series is currently displayed. </p>
<p>By default (un-clicked) the Display Menu label shows the total number of series 
  available for the current patient and the patient’s identifying information.  
  This feature allows the patient’s name to be displayed in two places in the 
  viewer for safety reasons.  Although the WADO Viewer is designed to operate 
  without scroll bars to ensure that critical information is always on-screen, 
  some browser / screen configurations can automatically introduce scroll bars.  
  Operation of the WADO Viewer on a screen with less than 1024 x 768 pixels is 
  not recommended.</p>
<p><a name=UserQA>Select <b>About…</b> </a>on the Display Menu to display the 
  portion of the SMPTE test pattern that will allow you to check the settings 
  of your monitor. </p>
<p> <img width=333 height=133
src="images/image008.jpg" align=left hspace=12
v:shapes="_x0000_s1027"> Clinical applications require a monitor capable of adequate 
  gray-scale range.  The test pattern contains 12 discernible squares including 
  two small squares of 95% and 5% density.  If, in your viewing environment, your 
  monitor is not adjusted to display all 12 squares, then it is inadequate for 
  clinical use.  Adjust the brightness and contrast controls per the monitor manufacturer 
  instructions until all 12 squares are visible at the same time.</p>
<ul type=disc>
  <li><b>Scroll Wheel</b></li>
</ul>
<p>The Scroll Wheel of the mouse controls the displayed image number in all modes. 
   Keyboard arrow keys can also be used to step through images and series.</p>
<ul type=disc>
  <li><b>Left Mouse Function</b></li>
</ul>
<p>The primary mouse button (typically the left button) toggles between two modes 
  of control for Window / Level or Zoom.  Clicking the tool icon toggles the mode 
  and changes the icon to match.  The Window / Level settings and Zoom / Pan settings 
  apply to the display of all images in a series.</p>
<p> <img border=0 width=29 height=28
src="images/image010.jpg" v:shapes="_x0000_i1029">      <b>Window / Level</b></p>
<p>In Window / Level mode, a drag across the image defines a region. On mouse-up, 
  the image will be replaced by a matrix of 9 versions of the region.  Each version 
  of the region will be a different Window / Level option automatically selected 
  by the software.  For reference, the middle image of the matrix (cell # 5) will 
  be rendered with no change in the Window / Level.  The options presented in 
  the other 8 cells may depend on the modality of the originated the image and 
  other information derived from the DICOM image header.</p>
<p>A click on a cell of the Window / Level matrix redraws the entire previous 
  image using the selected Window / Level option.  A click on a thumbnail in Window 
  / Level mode selects the new series, replaces the image in the display frame, 
  and applies the current settings to the image series.</p>
<p>A drag on a cell of the Window / Level matrix causes the entire matrix to be 
  redrawn as variations from the selected cell.  The region itself is unchanged.  
  The extent of the drag gesture indicates how large a change is desired.</p>
<p style='&lt;/p&gt;

  &lt;p style='margin-left:1.0in'>A click on the image window toggles the image 
  between full display and magnified display.</p>
<p>A drag across a thumbnail showing the Zoom region frame pans the frame to adjust 
  the region on display. </p>
<p> <img border=0 width=29 height=28
src="images/image014.jpg" v:shapes="_x0000_i1031"> <b>     Reset</b></p>
<p>A click on the Reset tool restores the image display for the currently selected 
  series to the initial defaults.  This includes Window / Level and Zoom.</p>
<p>Two consecutive clicks on the Reset Tool restores the entire study (all series) 
  and other display modes (e.g.: Overlays) to the initial defaults.</p>
<p> <img border=0 width=29 height=28
src="images/image016.jpg" v:shapes="_x0000_i1032"> <b>     Rotate / Flip</b></p>
<p>Successive clicks on the Rotate / Flip Tool changes the displayed image in 
  sequence.  The sequence is 0<sup>&#9702;</sup>, 90<sup>&#9702;</sup>, 180<sup>&#9702;</sup>, 
  270<sup>&#9702;</sup>, Flip Horizontal, Flip Vertical and back to 0<sup>&#9702;</sup>. 
   Rotations apply to all images in a series.</p>
<p> <img border=0 width=29 height=28
src="images/image018.jpg" v:shapes="_x0000_i1033">      <b>Overlay Show / Hide</b></p>
<p>This tool toggles the display of information as an overlay on the image.  Patient 
  identifying information (Name, ID, Age, Sex) cannot be hidden.  The information 
  displayed depends on the modality, the information received and the current 
  display state. </p>
<p>Examples of display state information include: Image #, Window / Level, Lossy 
  Compression Ratio and Magnification Ratio.  Scale Bars, if displayed on the 
  Overlay, reflect the current Magnification Ratio.</p>
<p> <img border=0 width=29 height=28
src="images/image020.jpg" v:shapes="_x0000_i1034">      <b>Compare</b></p>
<p>This tool launches another instance of the WADO Viewer and automatically selects 
  the most likely series from the same patient and study for initial display. 
   Depending on screen size, screen configuration and user login preferences, 
  the new WADO Viewer instance can be running on the same computer or another 
  computer.</p>
<p>WADO Viewers launched using the Compare tool are automatically linked with 
  respect to the actions of the Mouse Scroll Wheel for image selection.</p>
<p> <img border=0 width=123 height=36
src="images/image022.jpg" v:shapes="_x0000_i1035"> <b>     Annotation Tools</b></p>
<p>The Annotation tool changes state in sequence to enable Text, Regions, Arrows 
  and Dimensions to add Annotation Objects to the Annotation Overlay.  The Annotation 
  Overlay is associated with a particular image and is not saved once the WADO 
  Viewer is closed or a new Patient is selected.</p>
<p>The behavior of each Annotation Object may depend on the particular browser 
  in use and some tools may not be available in your browser.</p>
<p><b>Caution:</b> The position of Annotation Objects is automatically adjusted 
  to reflect Zoom and Pan of an image.  This action can cause some annotations 
  to be obscured or to change position relative to each other.  If the image Magnification 
  and Pan states are changed, it is the user’s responsibility to verify that the 
  annotations still reflect the user’s clinical intent. </p>
<p> <img border=0 width=32 height=28
src="images/image024.jpg" v:shapes="_x0000_i1036"> <b>    Annotations Show / Hide</b></p>
<p>This tool toggles the visibility of all of the Annotation Overlays in a study.</p>
<p> <img border=0 width=29 height=28
src="images/image026.jpg" v:shapes="_x0000_i1037"> <b>     View HIPAA Log</b></p>
<p>This tool displays a page of information including security, transfer and warning 
  notices that apply to the current study.</p><p style='&lt;/p&gt;

  &lt;p&gt;&nbsp;&lt;/p&gt;

  &lt;h2&gt;User Feedback&lt;/h2&gt;

  &lt;p&gt;&nbsp;&lt;/p&gt;

  &lt;p&gt;The MedCommons Gateway™ is Open Source Software. Please
refer to the Support section of our Web site for an up to date listing of user
forums and directions on how to give feedback directly to MedCommons.&lt;/p&gt;

  &lt;p&gt;&nbsp;&lt;/p&gt;

  &lt;p&gt;MedCommons values your input.&lt;span>  Bug reports, feature requests, clinical notes and, yes, even software 
    are welcome submissions.<span>  </span>Under the terms of the Open Source 
    licenses associated with this distribution, source code is available at MedCommons.net 
    for most of the functional components.<span>  </span>Users with the skill 
    and support to modify and enhance the software are free to do so.<span>  </span>However, 
    modification of the Gateway software invalidates the performance, test and 
    labeling protections that apply to the software as distributed from MedCommons.<span>  
    </span>Modification may also impact your ability to get support from the community 
    of users.</p>
  <p>MedCommons Gateway™ software is designed, produced and tested in a formal 
    and documented manner consistent with the legal and regulatory requirements 
    that apply to medical devices in this product category.<span>  </span>Our 
    procedures include controlled documentation, risk analyses, traceability processes, 
    automated and manual tests and other controls that will not be available to 
    clinicians and engineers in the community.<span>  </span>Regulatory approval 
    of MedCommons Gateway™ software applies only to software issued to MedCommons, 
    Inc.<span>  </span>Redistribution of MedCommons software, whether commercial 
    or not, without regulatory approval is illegal in the <st1:country-region w:st="on">US</st1:country-region>, 
    <st1:place
w:st="on"><st1:country-region w:st="on">Canada</st1:country-region> and most other 
    countries.<span>  </span>Please contact MedCommons for additional information 
    on how to meet regulatory requirements or to discuss the inclusion of your 
    software in future versions of the MedCommons Open Radiology Gateway.</p>
</div>
<span style='font-size:12.0pt;font-family:"Times New Roman";"Times New Roman";'> 
<br clear=all style='page-break-before:always;
'></span>
<div class=Section2> 
  <h2><a name=Images>3. Images</a></h2>
  <p>The MedCommons Gateway™ software does not know the extent to which an image 
    has been processed before it is received.  On-image labeling about processing 
    may be misleading.  For example, MedCommons Gateway™ could indicate that an 
    image has not been processed, when in fact it had been processed extensively 
    prior to its receipt.  The following labeling is in the Users Guide: </p>
  <p><span style='font-size:10.0pt;
font-family:Arial'>The user should also be aware that the image information being 
    processed by the device is potentially incomplete on does not adequately conform 
    to the standards as implemented by the device. To mitigate the impact of these 
    problems, a qualified user is expected to take into account the underlying 
    imaging protocols and to recognize the absence or gross corruption of information. 
    </span></p>
  <p><span style='font-size:10.0pt;
font-family:Arial'>This device is designed, manufactured and tested to reduce 
    the risk of subtle and serious corruption of labels and image contents. Internal 
    checks and redundant external labels are used to reduce the likelihood of 
    misinterpretation due to internal software errors but these are not foolproof 
    and cannot compensate for bad information coming into the device. </span></p>
  <p><span style='font-size:10.0pt;
font-family:Arial'>On occasion, interpolated (magnified) data may introduce image 
    artifacts which should not be interpreted as real pathology. When artifacts 
    are suspected, the trained user may be able change the magnification ratio 
    or window/level setting to help differentiate artifacts from pathology.</span></p>
  <h1><span style='font-size:9.0pt;
font-family:"Times New Roman";font-weight:normal'>&nbsp;</span></h1>
  <h1>&nbsp;</h1>
</div>
</body>
</html>
