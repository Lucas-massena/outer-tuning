<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.ontology.Heuristic"%>
<%@page import="java.util.ArrayList"%>
<!-- === BEGIN HEADER === -->
<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8"> <![endif]-->
<!--[if IE 9]> <html lang="en" class="ie9"> <![endif]-->
<!--[if !IE]><!--> <html lang="en"> <!--<![endif]-->
    <head>
        <title>OT - Select the tuning heuristics</title>
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
                <!-- Top Menu -->
                <div class="clear"></div>
                <!-- End Top Menu -->
            </div>
        </div>
        <!-- === END HEADER === -->

        <!-- === BEGIN CONTENT === -->
        <div id="content" class="container">
            <div class="row margin-top-30">
                <div class="col-md-12 text-center">
                    <h3 class"text-center">Select the tuning heuristics</h3>
                </div>
                <div class="col-md-12 text-left">
                    <form action="ServletAgents?cmd=workload" id="startTuningAgent" method="POST">
                        <input hidden="true" type="checkbox" name="mode_automatic" id="mode_automatic" value="mode_automatic">
                        <input hidden="true" type="checkbox" name="mode_semi_automatic" id="mode_semi_automatic" value="mode_semi_automatic">
                        <center>
                            <table >
                                <tr>
                                    <th style="width: 20px; text-align: center;"> </th>
                                    <th style="width: 250px; text-align: center;">Heuristic</th>
                                    <th style="width: 100px; text-align: center;">Version</th>
                                    <th style="width: 250px; text-align: center;">Strategy</th>
                                    <th style="width: 300px; text-align: center;">Author</th>
                                </tr>

                                <% ArrayList<Heuristic> heuristics = (ArrayList<Heuristic>) request.getAttribute("heuristicList");
                                    for (Heuristic heuristic : heuristics) {%>
                                <tr>
                                    <td> <input type = "checkbox" checked="true" name = "<%=heuristic.getName()%>" value = "true"> </td>
                                    <td><%=heuristic.getName()%> </td>
                                    <td style="text-align: center;"><%=heuristic.getVersion()%> </td>
                                    <td><%=heuristic.getStrategy()%> </td>
                                    <td><%=heuristic.getAuthor()%> </td>
                                </tr>
                                </p>
                                <%}%>
                            </table>
                        </center>
                        <br><br>
                        <div class="text-center">
                            <h3 class"text-center">Mode</h3>
                            <br>
                            <input type="radio" name="mode" checked="true" value="semi-automatic"> semi-automatic
                            &nbsp;&nbsp;&nbsp;
                            <input type="radio" name="mode" value="automatic"> automatic
                            <br><br><br>
                            <a href="javascript:{}" onclick="document.getElementById('startTuningAgent').submit(); return false;" class="btn btn-default">RUN&nbsp;&nbsp;<i class="fa-chevron-right"></i></a>
                            <br><br>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <!-- === END CONTENT === -->
        <jsp:include page="helpers/foot.jsp"/>

    </body>
</html>
<!-- === END FOOTER === -->