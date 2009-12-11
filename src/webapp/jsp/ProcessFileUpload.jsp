<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.fileupload.DiskFileUpload"%>
<%@ page import="org.apache.commons.fileupload.FileItem"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.io.File"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>process Hunglish file upload</title>
</head>
<%
        System.out.println("Content Type ="+request.getContentType());

        DiskFileUpload fu = new DiskFileUpload();
        // If file size exceeds, a FileUploadException will be thrown
        fu.setSizeMax(1000000);

        List fileItems = fu.parseRequest(request);
        Iterator itr = fileItems.iterator();

        while(itr.hasNext()) {
          FileItem fi = (FileItem)itr.next();

          //Check if not form field so as to only handle the file inputs
          //else condition handles the submit button input
          if(!fi.isFormField()) {
            System.out.println("\nNAME: "+fi.getName());
            System.out.println("SIZE: "+fi.getSize());
            //System.out.println(fi.getOutputStream().toString());
            File fNew= new File(application.getRealPath("/"), fi.getName());

            System.out.println(fNew.getAbsolutePath());
            fi.write(fNew);
          }
          else {
            System.out.println("Field ="+fi.getFieldName());
          }
        }
%>
<body>
Upload Successful!
</body>
</html>