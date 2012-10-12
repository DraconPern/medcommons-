<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java"%>
<%@ page contentType="text/xml"%>
<%@ page isELIgnored="false" %> 
<CXP>
	<OperationCode>QUERY</OperationCode>
  <Status>${status}</Status>
  <UID>${uid}</UID>
	<Files>
		<File>
			<FileName>CCR</FileName>
			<FileType>application/x-ccr+xml</FileType>
			<FileContents>${encodedCcr}</FileContents>
		</File>
	</Files>
	<QueryString>${queryString}</QueryString>
</CXP>


