<?

require_once "DB.inc.php";

/**
 * Performs query on patient list for specified practice.
 * 
 * @param int $practiceId             practice id to query
 * @param int $limit                  max number of rows to return 
 * @param int  $start                 row to start at
 * @param String $whereclause         optional where clause to filter results
 * @param String $viewStatusClause    optional viewstatus clause to filter results
 */
function query_patient_list($practiceIds, $limit, $start=0, $whereclause="", $viewStatusClause=" AND e.ViewStatus = 'Visible' ", $customFields=false) {
    
    $db = DB::get();
    
    if(is_array($practiceIds)) {
        $practiceIds = " IN (".join(",", $practiceIds).")";
    }
    else
        $practiceIds = " = $practiceIds";
    
    $orderFields = "";
    if($customFields) {
        $orderFields .= ",do.custom_00, do.custom_01, do.custom_02, do.custom_03, do.custom_04, do.custom_05, do.custom_06, do.custom_07, do.custom_08, do.custom_09";
    }
    
    // IMPORTANT NOTE:  if you are updating this query in such a way that you might change
    // the number of rows that are returned, you MUST also update the "count" query
    // in rls.php
    $select = "SELECT e.*, wia.wi_id as wi_available_id, wid.wi_id as wi_downloaded_id, c.couponum,
               c.status as couponstatus, c.voucherid as voucherid, do.ddl_status as order_status, do.callers_order_reference as order_reference,
               do.ddl_status as dicom_order_status, do.patient_id, do.protocol_id, do.auth_token, do.auth_secret,
               pp.pp_name
               $orderFields
               FROM practice p, users u, practiceccrevents e
               LEFT JOIN practice p2 on p2.practiceid $practiceIds
               LEFT JOIN workflow_item wia ON e.PatientIdentifier = wia.wi_target_account_id 
                    AND wia.wi_type = 'Download Status' 
                    AND wia.wi_active_status = 'Active' and wia.wi_status = 'Available' 
                    AND wia.wi_source_account_id = p2.accid
               LEFT JOIN workflow_item wid ON e.PatientIdentifier = wid.wi_target_account_id 
                    AND wid.wi_type = 'Download Status' 
                    AND wid.wi_active_status = 'Active' and wid.wi_status = 'Downloaded' 
                    AND wid.wi_source_account_id = p2.accid
               LEFT JOIN modcoupons c on c.mcid = e.PatientIdentifier
               LEFT JOIN dicom_order do on do.mcid = e.PatientIdentifier
               LEFT JOIN practice_patient pp on (pp.pp_accid = e.PatientIdentifier and pp.pp_name = 'members')
               WHERE 
                   e.practiceid $practiceIds
               AND e.practiceid = p.practiceid 
               AND ((p.accid = wia.wi_source_account_id) OR (wia.wi_source_account_id is NULL))
               AND ((p.accid = wid.wi_source_account_id) OR (wid.wi_source_account_id is NULL))
               AND u.mcid = e.PatientIdentifier
               AND u.acctype <> 'EXPIRE_IMMEDIATE'
               AND (u.expiration_date is NULL OR u.expiration_date > NOW())
               $whereclause $viewStatusClause
               GROUP BY e.PatientGivenName, e.PatientFamilyName, e.Guid
               ORDER BY p.practiceid, e.CreationDateTime DESC LIMIT $start,$limit";
               
               
    // file_put_contents("c:\\cygwin\\tmp\\sql.txt", $select);               
    // echo $select;                
    return $db->query($select);
}

?>