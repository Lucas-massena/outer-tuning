<%--
    Document   : start
    Created on : Feb 23, 2016, 3:10:30 PM
    Author     : Rafael
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.ontology.Heuristic"%>
<%@page import="java.util.ArrayList"%>
<!-- === BEGIN HEADER === -->
<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8"> <![endif]-->
<!--[if IE 9]> <html lang="en" class="ie9"> <![endif]-->
<!--[if !IE]><!--> <html lang="en"> <!--<![endif]-->
    <head>
        <!--<meta http-equiv="refresh" content="0; url=http://localhost:8084/OuterTuning/ServletAgents?cmd=workload" />-->
        <!-- Title -->
        <title>OT - Workload</title>
        <jsp:include page="helpers/includesHeader.jsp"/>
    </head>
    <body>
        <div id="pre_header" class="visible-lg"></div>
        <div id="header" class="container">
            <div class="row">
                <!-- Logo -->
                <div class="logo">
                    <a href="index.jsp" title="">
                        <img src="assets/img/logo.png" width="250px" alt="Logo" />
                    </a>
                </div>
                <!-- End Logo -->
            </div>
        </div>
        <!-- === END HEADER === -->

        <!-- === BEGIN CONTENT === -->
        <div id="content" class="container">
            <div class="row margin-top-30">
                <div class="col-md-12 text-center">
                    <br>
                    <br>
                    <a class="btn btn-default" href="ServletAgents?cmd=selectHeuristics">START&nbsp;&nbsp;<i class="fa-chevron-right"></i></a>
                    <br>
                    <br>
                    <br>
                    <br>
                </div>
                <div class="col-md-12 text-left">
                </div>
            </div>
        </div>
        <!-- === END CONTENT === -->
        <jsp:include page="helpers/foot.jsp"/>
    </body>
</html>
<!-- === END FOOTER === -->

