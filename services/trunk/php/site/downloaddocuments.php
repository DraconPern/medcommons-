<?
  require_once "template.inc.php";
  template("base.tpl.php")->extend();
?>
<?section("head")?>
<style type="text/css"> 
      body {
        font-family: arial, sans, sans-serif;
        margin: 0;
      }
 
      iframe {
        border: 0;
        frameborder: 0;
        height: 100%;
        width: 100%;
      }
 
      #contents {
        margin: 6px;
      }

      p {
        margin: 1em 0;
      }
 
      .dash {
        padding: 0 6px;
      }
    </style>
    <?end_section("head")?>

<?section("content")?>
<body><div id="contents"><style
      type="text/css">ol{margin:0;padding:0}p{margin:0}.c1{line-height:1.15;text-indent:0pt;direction:ltr;margin-left:36.0pt}.c3{color:#000099;font-size:11pt;text-decoration:underline;font-family:Arial}.c2{line-height:1.15;margin-top:0.5em;text-indent:0pt;direction:ltr}.c0{color:#000000;font-size:11pt;font-family:Arial}.c5{font-weight:bold}.c4{background-color:#ffffff}</style><p
    class="c2"><span class="c0 c5">About Download All Documents</span></p><p
    class="c2"><span class="c0">Updated September 20, 2010</span></p><p
    class="c2"><span class="c0"> </span></p><p class="c2"><span
      class="c0">Download All Documents is the MedCommons implementation of the
      '</span><span class="c3"><a
        href="http://www.markle.org/downloadable_assets/20100831_bluebutton_pr.pdf">Blue
        Button' initiative</a></span><span class="c0">. </span></p><p
    class="c2"><span class="c0"> </span></p><p class="c2"><span
      class="c0">Download All Documents is a MedCommons feature that allows the
      patient or their designated agent to download a file with all of the
      medical information stored in the MedCommons account. The purpose of this
      function is to allow the patient or authorized user to back-up, store or
      share the information or to move the information to another networked
      health records service as they see fit. The Download All Documents
      feature protects the consumer from lock-in to the MedCommons service by
      allowing them to move their health data easily if they
      choose.</span></p><p class="c2"><span class="c0"> </span></p><p
    class="c2"><span class="c0">Download All Documents is delivered as a single
      compressed file containing all of the health records documents including
      radiology images unmodified in their original format. Download All
      Documents also includes at least one file in the federally-recognized
      Continuity of Care (CCR) record format that represents information used
      to organize the health information in the patient account.</span></p><p
    class="c2"><span class="c0"> </span></p><p class="c2"><span
      class="c0">Support, privacy and security questions should be sent to
      MedCommons at </span><span class="c3"><a
        href="mailto:cmo@medcommons.net">cmo@medcommons.net</a></span><span
      class="c0"> Please use this address if you believe that the downloaded
      information is different that what was uploaded but address errors in the
      uploaded information at their source. </span></p><p class="c2"><span
      class="c0"> </span></p><p class="c2"><span class="c0">MedCommons supports
      the Connecting for Health </span><span class="c3"><a
        href="http://www.connectingforhealth.org/phti/reports/overview.html">Common
        Framework for Networked Personal Information</a></span><span
      class="c0"> developed by the Markle Foundation. Here are the Common
      Framework principals:</span></p><p class="c2"><span class="c0">
  </span></p><p class="c2"><span class="c0">1. Openness and
  transparency</span></p><p class="c1"><span class="c0">MedCommons collects
  only the information you upload to your account for no purpose other than for
  you to save and share as you want. We do not mine or otherwise use your
  information for any secondary purpose. You can see all of the information we
  have at all times and can download all information at any time and then close
  your account.</span></p><p class="c1"><span class="c0"> </span></p><p
class="c2"><span class="c0">2. Purpose specification</span></p><p
class="c1"><span class="c0">MedCommons collects a minimum of information from
  you for the purpose of allowing you access to your account. This information
  is limited to your name and email address as well as an Inbox name you choose
  to label your account. None of this information is shared with third-parties
  or used for any purpose other than management of your account.</span></p><p
class="c1"><span class="c0"> </span></p><p class="c2"><span class="c0">3.
  Collection limitation and data minimization</span></p><p class="c1"><span
  class="c0">MedCommons allows you to upload health data for whatever purpose
  you have. Your data is not shared, aggregated or otherwise used by us for any
  purpose other than to enable you to store and share your data with others
  under your specific command. MedCommons does not use your data to search for
  additional information for or about you or to offer any additional services
  to you or others.</span></p><p class="c1"><span class="c0"> </span></p><p
class="c2"><span class="c0">4. Use limitation</span></p><p class="c1"><span
  class="c0">MedCommons does not use your data for any purpose other that those
  specified above and under your explicit control.</span></p><p
class="c1"><span class="c0"> </span></p><p class="c2"><span class="c0">5.
  Individual participation and control</span></p><p class="c1"><span
  class="c0">You can use the Share link in the viewer to control who else can
  access your information and review this list at any time on the Patient
  Details page. On this page you will see who has been granted access to your
  information and a log of who has accessed the information. People with access
  to your information will also be able to download it using the Download All
  Documents feature.</span></p><p class="c1"><span class="c0"> </span></p><p
class="c2"><span class="c0">6. Data quality and integrity</span></p><p
class="c1"><span class="c0">MedCommons calculates a digital signature for every
  document and image we manage and uses that digital signature to ensure that
  information was not altered by us during storage. MedCommons checks the
  integrity of data to the extent possible for standard formats such as
  Continuity of Care Record (ASTM-CCR) using published XML schema.
  </span></p><p class="c1"><span class="c0"> </span></p><p class="c2"><span
  class="c0">7. Security safeguards and controls</span></p><p class="c1"><span
  class="c0">MedCommons is designed to meet all of the ARRA-HITECH and HIPAA
  requirements including encryption during transmission and while information
  is stored, access logs as well as “break-the-glass” features to limit and log
  administrative access to accounts by MedCommons support
  personnel.</span></p><p class="c2"><span class="c0"> </span></p><p
class="c2"><span class="c0">8. Accountability and oversight</span></p><p
class="c1"><span class="c0">MedCommons is responsible for the security of your
  information as described in our </span><span class="c3"><a
    href="http://www.medcommons.net/privacy.html">Privacy
    Policy</a></span><span class="c0"> and </span><span class="c3"><a
    href="http://www.medcommons.net/terms.html">Terms
    of Use</a></span><span class="c0">. We will notify the account holder of
  breaches as required by law. Please contact MedCommons if your organization
  requires a signed HIPAA Business Associate agreement with
  MedCommons.</span></p><p class="c1"><span class="c0"> </span></p><p
class="c2"><span class="c0">9. Remedies</span></p><p class="c1"><span
  class="c0">MedCommons offers limited or no remedies to users as described in
  the </span><span class="c3"><a
    href="http://www.medcommons.net/terms.html">Terms
    of Use</a></span><span class="c0">.</span></p><p class="c1"><span
  class="c0"> </span></p></div>
<?end_section("content")?>
