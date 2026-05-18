
<%@page import="java.util.Locale"%>
<%@page import="java.text.DecimalFormatSymbols"%>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.base.Interval"%>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.SQL"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.sgbd.ActionSF"%>
<%
    boolean executed = request.getAttribute("executed") != null;
    ActionSF action = (ActionSF) request.getAttribute("actionData");
    if (action != null) {
        DecimalFormat df = new DecimalFormat("###,###.##", new DecimalFormatSymbols(new Locale("pt", "BR")));
%>
<script type="text/javascript">
    google.charts.setOnLoadCallback(drawChartAction);
    function drawChartAction() {
        var dataAction = google.visualization.arrayToDataTable(<%=action.getDataFromChartIDE()%>);
        var optionsAction = {
            backgroundColor: '#F4F1EA',
            chartArea: {
                left: 100,
                top: 20,
                right: 10,
                bottom: 20,
                width: "100%",
                height: "100%"},
            chart: {
                title: 'SQL Performance',
                subtitle: 'Simulation of the tuning action creation',
            },
            is3D: true,
            bars: 'horizontal',
            colors: ['#db4437', '#4285f4']
        };
        var chartAction = new google.charts.Bar(document.getElementById('details_action_sf'));
        chartAction.draw(dataAction, optionsAction);
    }
</script>


<div id="content_action" class="container">
    <div class="row" style="text-align: left; margin-top: 5px; border-top: 1px solid #c7c5c1;">
        <br/>
        <h3>ACTION <%=action.getName()%></h3>
        <br/>
        <table width="100%" border="0">
            <tr>
                <td colspan="2"><b>Heuristic:</b> <%=action.getHeuristic()%></td>
                <td><b>Type: </b><%=action.getType()%></td>
            </tr>
            <tr>
            </tr>
            <tr>
                <td><b>Creation Cost:</b> <%= df.format(action.getCreationCost())%></td>
                <td><b>Execution Cost:</b> <%=df.format(action.getCost())%></td>
                <td><b>Acumulated bonus:</b> <%=df.format(action.getBonus())%></td>
            </tr>
            <tr>
                    <td><b>Status: </b>
                        <%if (action.getStatus().equals("criada")) {%><span style="color: blue;">suggested</span><%}%><%else {%><span style="color: red;">collecting data</span><%}%>
                    </td>
                    <td colspan="2"><b>Action: </b>
                        <%if (executed) {%><span style="color: blue;">Executed</span><%}%><% else {%> <a href="ServletAgents?cmd=executeTuningAction&executeid=<%=action.getId()%>"><button>Execute Tuning Action</button></a><%}%>
                    </td>
            </tr>
            <tr>
                <td colspan="3">
                    <b>Command:</b>
                    <pre class="prettyprint lang-sql"><%=action.getCommandToHtml()%></pre>
                </td>
            </tr>
            <tr>
                <td colspan="3">
                    <b>Justify:</b>
                    <pre class="prettyprint lang-sql"><%=action.getJustify()%></pre>
                </td>
            </tr>
            <%if (action.getSql().size() > 0) {%>
            <tr>
                <td colspan = "3">
                    <b>SQL can benefit with this Action: </b>
                    <!-- Accordion -->
                    <div  id="accordion" class="panel-group">
                        <% ArrayList<SQL> sqlIn = (ArrayList<SQL>) action.getSql();
                            for (int i = 0; i < sqlIn.size(); i++) {%>
                        <!-- Item Accordion -->
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h4 class="panel-title">
                                    <a class="accordion-toggle collapsed" href="#collapse-<%=sqlIn.get(i).getId()%>" data-parent="#accordion" data-toggle="collapse">
                                        <table style="width: 100%">
                                            <tr>
                                                <td style="width: 15%;"><span class="fa-bookmark"></span> SQL #<%=sqlIn.get(i).getId()%></td>
                                                <td style="width: 30%;"><span class="fa-clock-o"></span>Time (avg): <%=df.format(sqlIn.get(i).getDurationAVG((Interval) request.getAttribute("intervalAsked")))%> seconds</td>
                                                <td style="width: 30%;"><span class="fa-dashboard"></span>Cost (avg): <%=df.format(sqlIn.get(i).getCostAVG((Interval) request.getAttribute("intervalAsked")))%></td>
                                                <% if (request.getParameter("window") != null) {%>
                                                <td style="width: 25%;"><span class="fa-bars"></span>Frequency in this window: <%=sqlIn.get(i).getExecutionsInWindow((Interval) request.getAttribute("intervalAsked")).size()%>x </td>
                                                <% } else {%>
                                                <td style="width: 25%;"><span class="fa-bars"></span>Total executions: <%=sqlIn.get(i).getCaptureCount()%>x</td>
                                                <% }%>
                                            </tr>
                                        </table>
                                    </a>
                                </h4>
                            </div>
                            <div id="collapse-<%=sqlIn.get(i).getId()%>" class="accordion-body collapse" style="height: 0px">
                                <div class="panel-body">
                                    <div class="modules_sql row">
                                        <pre class="prettyprint lang-sql"><%=sqlIn.get(i).getSqlHTML()%></pre>
                                        <a href="ServletAgents?cmd=workload&sql=SQL%20%23<%=sqlIn.get(i).getId()%>">More details...</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- End item Accordion -->
                        <%  }%>
                    </div>
                    <!-- Accordion -->
                </td>
            </tr>
            <% }%>
        </table>
        <br/>
    </div>
    <div class="row" style=" margin-bottom: 50px; width: 1036px; padding: 15px; background: #fff; height: <%=((action.getSql().size() * 70) + 100)%>px;">
        <div id="details_action_sf" style="width: 100%; height: 100%"></div>
        <br/>
    </div>
</div>
<% }%>