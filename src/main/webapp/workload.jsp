<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.base.Interval" %>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.ActionSF" %>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.Plan" %>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.SQL" %>
<%@page import="java.text.DateFormat" %>
<%@page import="java.text.DecimalFormat" %>
<%@page import="java.text.DecimalFormatSymbols" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Iterator" %>
<%@page import="java.util.Locale" %>

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
    <script type="text/javascript">
        google.charts.setOnLoadCallback(drawChart);

        function drawChart() {

            var data = google.visualization.arrayToDataTable([<%=request.getAttribute("workloadData")%>]);

            var view = new google.visualization.DataView(data);
            var options = {
                backgroundColor: '#F4F1EA',
                tooltip: {isHtml: true},
                vAxis: {title: "Duration (in seconds)"},
                width: 1024,
                height: 400,
                chartArea: {width: '75%', height: '80%'},
                legend: {position: 'right', maxLines: 3},
                bar: {groupWidth: '75%'},
                isStacked: true
            };
            var chart = new google.visualization.ColumnChart(document.getElementById("columnchart_values"));
            chart.draw(view, options);
            var selectHandler = function (e) {
                if (chart.getSelection()[0]['row'] !== null) {
                    window.location = '?cmd=workload&window=' + encodeURIComponent(data.getValue(chart.getSelection()[0]['row'], 0)) + '&sql=' + encodeURIComponent(data.getColumnLabel(view.getTableColumnIndex(chart.getSelection()[0]['column'])));
                } else {
                    window.location = '?cmd=workload&sql=' + encodeURIComponent(data.getColumnLabel(view.getTableColumnIndex(chart.getSelection()[0]['column'])));
                }
            };
            google.visualization.events.addListener(chart, 'select', selectHandler);
        }

        window.addEventListener('load', function () {
            var select = document.getElementById('windowSize');

            select.addEventListener('change', function () {
                window.location = '?cmd=workload&windowSize=' + this.value;
            }, false);
        }, false);

    </script>
</head>
<body>
<jsp:include page="helpers/header.jsp"/>
<!-- === BEGIN CONTENT === -->
<div id="content" class="container">
    <div class="row" style="text-align: left; margin-top: 5px;">
        <form>
            <table style="width: 100%">
                <tr>
                    <td>
                        <label for="windowSize">interval:</label><select id="windowSize">
                            <option value="1min" <% if (request.getAttribute("windowSize").equals("1min")) {%><%="selected=true"%><%}%>>
                                1 min
                            </option>
                            <option value="5min" <% if (request.getAttribute("windowSize").equals("5min")) {%><%="selected=true"%><%}%>>
                                5 min
                            </option>
                            <option value="10min"  <% if (request.getAttribute("windowSize").equals("10min")) {%><%="selected=true"%><%}%>>
                                10 min
                            </option>
                            <option value="30min" <% if (request.getAttribute("windowSize").equals("30min")) {%><%="selected=true"%><%}%>>
                                30 min
                            </option>
                            <option value="1h" <% if (request.getAttribute("windowSize").equals("1h")) {%><%="selected=true"%><%}%>>
                                1 hour
                            </option>
                            <option value="6h" <% if (request.getAttribute("windowSize").equals("6h")) {%><%="selected=true"%><%}%>>
                                6 hours
                            </option>
                            <option value="12h" <% if (request.getAttribute("windowSize").equals("12h")) {%><%="selected=true"%><%}%>>
                                12 hours
                            </option>
                            <option value="24h" <% if (request.getAttribute("windowSize").equals("24h")) {%><%="selected=true"%><%}%>>
                                24 hours
                            </option>
                        </select>
                    </td>
                    <td style="text-align: center;">
                        <a href="javascript:window.location.href=window.location.href"><span
                                class="glyphicon glyphicon-refresh"></span></a>
                    </td>
                </tr>
            </table>
        </form>
    </div>
    <!-- === BEGIN GRAPHIC === -->
    <div id="columnchart_values" style="width: 900px; height: 450px;"></div>
    <!-- === END GRAPHIC === -->

    <div class="row" style="border-top: 1px solid #c7c5c1;">
        <% if (request.getParameter("window") != null) {%>
        <h4><span class="fa-tasks"></span> Window <%=request.getParameter("window")%> selected</h4>
        <% }%>

        <% if (request.getAttribute("sqlAsked") != null) {
            DecimalFormat df = new DecimalFormat("###,###.##", new DecimalFormatSymbols(new Locale("pt", "BR")));
        %>
        <!-- Accordion -->
        <div id="accordion" class="panel-group">
            <% ArrayList<SQL> sqlIn = (ArrayList<SQL>) request.getAttribute("sqlAsked");
                for (SQL sql : sqlIn) {%>
            <!-- Item Accordion -->
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <% Integer sqlSelected = 0;
                            try {
                                sqlSelected = (Integer) request.getAttribute("sqlSelected");
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println(request.getAttribute("sqlSelected"));
                            }
                        %>
                        <a class="accordion-toggle <%if (sql.getId() != sqlSelected) {%><%="collapsed"%> <%}%>"
                           href="#collapse-<%=sql.getId()%>" data-parent="#accordion" data-toggle="collapse">
                            <table style="width: 100%">
                                <tr>
                                    <td style="width: 15%;"><span class="fa-bookmark"></span> SQL
                                        #<%=sql.getId()%>
                                    </td>
                                    <td style="width: 30%;"><span class="fa-clock-o"></span>Time
                                        (avg): <%=df.format(sql.getDurationAVG((Interval) request.getAttribute("intervalAsked")))%>
                                        seconds
                                    </td>
                                    <td style="width: 30%;"><span class="fa-dashboard"></span>Cost
                                        (avg): <%=df.format(sql.getCostAVG((Interval) request.getAttribute("intervalAsked")))%>
                                    </td>
                                    <% if (request.getParameter("window") != null) {%>
                                    <td style="width: 25%;"><span class="fa-bars"></span>Executions in this
                                        window: <%=sql.getExecutionsInWindow((Interval) request.getAttribute("intervalAsked")).size()%>
                                        x
                                    </td>
                                    <% } else {%>
                                    <td style="width: 25%;"><span class="fa-bars"></span>Total
                                        executions: <%=sql.getCaptureCount()%>x
                                    </td>
                                    <% }%>
                                </tr>
                            </table>
                        </a>
                    </h4>
                </div>
                <div id="collapse-<%=sql.getId()%>"
                     class="accordion-body collapse <% if (sql.getId()==sqlSelected) {%><%="in"%> <%}%>"
                     style="height: <% if (sql.getId() == sqlSelected) {%>
                         <%="auto"%> <%} else {%>
                         <%="0px"%> <%}%>;">
                    <div class="panel-body">

                        <div class="modules_sql row">
                            <h3>SQL: </h3>
                            <br/>
                            <pre class="prettyprint lang-sql"><%=sql.getSqlHTML()%></pre>
                        </div>

                        <div class="modules_sql row">
                            <h3>Execution(s):</h3>
                            <br/>
                            <!-- BEGIN item TAB -->
                            <div class="tabs">
                                <ul class="nav nav-tabs">
                                    <% Iterator<Plan> itr;
                                        if (request.getAttribute("intervalAsked") != null) {
                                            itr = sql.getExecutionsInWindow((Interval) request.getAttribute("intervalAsked")).iterator();
                                        } else {
                                            itr = sql.getExecutions().iterator();
                                        }
                                        int j = 1;
                                        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                        while (itr.hasNext()) {

                                            Plan execution = itr.next();

                                            String date = dateFormat.format(execution.getDateExecution());%>

                                    <li <% if (j == 1) {%><%="class='active'"%> <%}%>><a
                                            href="#execution-<%=sql.getId()%><%=j++%>"
                                            data-toggle="tab"><%=date%>
                                    </a></li>

                                    <%}%>
                                </ul>
                            </div>
                            <div class="col-sm-12">
                                <div class="tab-content">
                                    <%
                                        if (request.getAttribute("intervalAsked") != null) {
                                            itr = sql.getExecutionsInWindow((Interval) request.getAttribute("intervalAsked")).iterator();
                                        } else {
                                            itr = sql.getExecutions().iterator();
                                        }
                                        j = 1;
                                        while (itr.hasNext()) {
                                            Plan execution = itr.next();
                                    %>
                                    <div class="tab-pane fade<% if (j == 1) {%><%=" active in"%><%}%>"
                                         id="execution-<%=sql.getId()%><%=j++%>">
                                        <%=execution.getPlanToViewHtml()%>
                                    </div>
                                    <%}%>
                                </div>
                            </div>
                            <!-- End item TAB -->
                        </div>

                        <% if (sql.getActionsSF().size() > 0) {%>
                        <div class="modules_sql row">
                            <br>
                            <h3>Tuning actions:</h3>
                            <br>
                            <br>
                            <div class="row tabs">
                                <div class="col-sm-3">
                                    <ul class="nav nav-pills nav-stacked">
                                        <% int k = 0;
                                            for (ActionSF action : sql.getActionsSF()) {%>
                                        <li class="<% if (k++ == 0) {%><%="active"%><%}%>"><a
                                                href="#<%=sql.getId() + "_" + action.getName()%>"
                                                data-toggle="tab"><i
                                                class="fa fa-table"></i>&nbsp;&nbsp;&nbsp;<%=action.getName()%>
                                        </a></li>
                                        <%}%>
                                    </ul>
                                </div>
                                <div class="col-sm-9">
                                    <div class="tab-content">
                                        <% k = 0;
                                            for (ActionSF action : sql.getActionsSF()) {%>
                                        <div class="tab-pane fade in <% if (k++ == 0) {%><%="active"%><%}%>"
                                             id="<%=sql.getId() + "_" + action.getName()%>">
                                            <table style="width: 100%;border: 0" >
                                                <tr>
                                                    <td colspan="3"><b>Heuristic:</b> <%=action.getHeuristic()%>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td colspan="3"><b>Type: </b><%=action.getType()%>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td colspan="3">
                                                        <b>Status: </b
                                                        ><%if (action.getStatus().equals("criada")) {%>
                                                            <span style="color: blue;">suggested</span>
                                                        <%} else {%>
                                                            <span style="color: red;">collecting data</span>
                                                        <%}%>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><b>Creation
                                                        Cost:</b> <%= df.format(action.getCreationCost())%>
                                                    </td>
                                                    <td><b>Execution Cost:</b> <%=df.format(action.getCost())%>
                                                    </td>
                                                    <td><b>Acumulated
                                                        bonus:</b> <%=df.format(action.getBonus())%>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td colspan="3">
                                                        <b>Command:</b>
                                                        <pre class="prettyprint lang-sql"><%=action.getCommandToHtml()%></pre>
                                                    </td>
                                                </tr>
                                                <%if (action.getSql().size() > 1) {%>
                                                <tr>
                                                    <td colspan="3"><b>
                                                        Other(s) SQL this action can benefit: </b>
                                                        <% int m = 0;
                                                            for (SQL sqlAction : action.getSql()) {
                                                                if (sqlAction.getId() != sql.getId()) {
                                                        %>
                                                        <a href="ServletAgents?cmd=workload&sql=SQL%20%23<%=sqlAction.getId()%>">SQL
                                                            #<%=sqlAction.getId()%>
                                                        </a><% if (m++ < action.getSql().size() - 2) {%>, <% }
                                                        }
                                                        }%>
                                                    </td>
                                                </tr>
                                                <% }%>
                                                <tr>
                                                    <td colspan="3">
                                                        <div style="text-align: center;"><a
                                                                href="ServletAgents?cmd=tuningAction&actionid=<%=action.getId()%>">more
                                                            details...</a></div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                        <%}%>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <% }%>
                    </div>
                </div>
            </div>
            <!-- End item Accordion -->
            <% }%>
        </div>
        <!-- Accordion -->
        <% }%>
    </div>
</div>
<!-- === END CONTENT === -->
<!-- === BEGIN FOOTER === -->
<jsp:include page="helpers/foot.jsp"/>
</body>
</html>
