<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration"%>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.ActionSF"%>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.Plan"%>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.base.Interval"%>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.SQL"%>
<%@page import="java.util.Locale"%>
<%@page import="java.text.DecimalFormatSymbols"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.concurrent.CopyOnWriteArrayList"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.math.RoundingMode"%>

<!-- === BEGIN HEADER === -->
<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8"> <![endif]-->
<!--[if IE 9]> <html lang="en" class="ie9"> <![endif]-->
<!--[if !IE]><!--> <html lang="en"> <!--<![endif]-->
    <head>
        <!-- Title -->
        <title>OT - Workload</title>
        <!-- Meta -->

        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <meta name="description" content="">
        <meta name="author" content="">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
        <link href="favicon.html" rel="shortcut icon">
        <link rel="stylesheet" href="assets/css/bootstrap.css" rel="stylesheet">
        <link rel="stylesheet" href="assets/css/animate.css" rel="stylesheet">
        <link rel="stylesheet" href="assets/css/font-awesome.css" rel="stylesheet">
        <link rel="stylesheet" href="assets/css/nexus.css" rel="stylesheet">
        <link rel="stylesheet" href="assets/css/responsive.css" rel="stylesheet">
        <link rel="stylesheet" href="assets/css/custom.css" rel="stylesheet">
        <!-- Google Fonts-->
        <link href="http://fonts.googleapis.com/css?family=Lato:400,300" rel="stylesheet" type="text/css">
        <link href="http://fonts.googleapis.com/css?family=Open+Sans:400,300" rel="stylesheet" type="text/css">

    </head>
    <body>
        <jsp:include page="helpers/header.jsp"/>
        <!-- === BEGIN CONTENT === -->
        <div id="content" class="container">
            <div class="row" style="text-align: left; margin-top: 5px;">
                <center><img src="assets/img/mog.gif" width="600px" alt="MOG" /></center>
            </div>
        </div>
        <!-- === END CONTENT === -->
        <!-- === BEGIN FOOTER === -->
        <jsp:include page="helpers/foot.jsp"/>
    </body>
</html>
