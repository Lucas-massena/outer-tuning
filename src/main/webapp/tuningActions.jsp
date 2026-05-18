<%--
    Document   : tuningActions
    Created on : 19/05/2016, 15:59:52
    Author     : Rafael
--%>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration" %>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.ActionSF" %>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.Plan" %>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.base.Interval" %>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.SQL" %>
<%@page import="java.util.Locale" %>
<%@page import="java.text.DecimalFormatSymbols" %>
<%@page import="java.text.DecimalFormat" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="java.text.DateFormat" %>
<%@page import="java.util.Iterator" %>
<%@page import="java.util.concurrent.CopyOnWriteArrayList" %>
<%@page import="java.util.Date" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.math.RoundingMode" %>

<!-- === BEGIN HEADER === -->
<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8"> <![endif]-->
<!--[if IE 9]> <html lang="en" class="ie9"> <![endif]-->
<!--[if !IE]><!-->
<html lang="en"> <!--<![endif]-->
<head>
    <!-- Title -->
    <title>OT - Workload</title>
    <!-- Meta -->

    <jsp:include page="helpers/includesHeader.jsp"/>
    <% String actionsData = (String) request.getAttribute("actionsData");

    if (actionsData != null && !actionsData.isEmpty()) {%>
    <script type="text/javascript">
            function drawSeriesChart() {
                var data = google.visualization.arrayToDataTable([<%=actionsData%>]);
                var options = {
                    backgroundColor: '#F4F1EA',
                    width: 1100,
                    height: 500,
                    bar: {groupWidth: '75%'},
                    chartArea: {
                        left: 100,
                        right: 150,
                        top: 10,
                        bottom: 50,
                        width: "100%",
                        height: "100%"},
                    vAxis: {title: 'Creation Cost'},
                    hAxis: {title: 'Gain Expectancy'},
                    bubble: {
                        textStyle: {
                            auraColor: 'none',
                            color: 'none'
                        }
                    }
                };
                var chart = new google.visualization.BubbleChart(document.getElementById('series_chart_div'));
                chart.draw(data, options);
                var selectHandler = function (e) {
                    var value = data.getValue(chart.getSelection()[0]['row'], 0);
                    if (value) {
                        window.location = '?cmd=tuningActions&actionid=' + value;
                    }
                }
                google.visualization.events.addListener(chart, 'select', selectHandler);
            }
            google.charts.setOnLoadCallback(drawSeriesChart);



    </script>
    <%}%>
</head>
<body>
<jsp:include page="helpers/header.jsp"/>
<!-- === BEGIN CONTENT === -->
<div id="content" class="container">
    <div class="row" style="text-align: left; margin-top: 5px;">
        <form>
            <table style="width: 100%">
                <tr>
                    <td style="text-align: right;">
                        <a href="javascript:window.location.href=window.location.href"><span
                                class="glyphicon glyphicon-refresh"></span></a>
                    </td>
                </tr>
            </table>
        </form>
    </div>
    <!-- === BEGIN GRAPHIC === -->
    <div class="row" style="text-align: center; border: 1px; margin-top: 5px; margin-bottom: 50px;">
        <div id="series_chart_div"></div>
    </div>
    <!-- === END GRAPHIC === -->
</div>
<jsp:include page="detailsAction.jsp"/>
<!-- === END CONTENT === -->
<!-- === BEGIN FOOTER === -->
<jsp:include page="helpers/foot.jsp"/>
</body>
</html>
