<%@page import="br.com.pucrio.inf.biobd.outertuning.agents.CoordenatorAgent.AIComparisonRow"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@page import="java.text.DecimalFormatSymbols"%>
<%@page import="java.text.DecimalFormat"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>OT - AI Comparison</title>
    <jsp:include page="helpers/includesHeader.jsp"/>
    <%
        String chartData = (String) request.getAttribute("comparisonChartData");
        if (chartData != null && !chartData.isEmpty()) {
    %>
    <script type="text/javascript">
        google.charts.load('current', {packages: ['bar']});
        google.charts.setOnLoadCallback(drawComparisonChart);
        function drawComparisonChart() {
            var data = google.visualization.arrayToDataTable(<%=chartData%>);
            var options = {
                chart: { title: 'AI Tuning Comparison: Original vs Gemini vs GPT vs Claude' },
                backgroundColor: '#F4F1EA',
                bars: 'vertical',
                colors: ['#db4437', '#4285f4', '#0f9d58', '#e07b39'],
                chartArea: { left: 80, top: 60, right: 20, bottom: 60, width: '100%', height: '100%' },
                vAxis: { title: 'Query Cost (planner units)' },
                hAxis: { title: 'SQL Query' }
            };
            var chart = new google.charts.Bar(document.getElementById('comparison_chart_div'));
            chart.draw(data, google.charts.Bar.convertOptions(options));
        }
    </script>
    <% } %>
</head>
<body>
<jsp:include page="helpers/header.jsp"/>
<div id="content" class="container">

    <div class="row" style="margin-top: 15px;">
        <div class="col-md-12">
            <h3>AI Tuning Comparison &mdash; Gemini vs GPT vs Claude</h3>
            <p style="color:#666;">Each agent analyzed the workload independently and suggested an index.
               The chart compares the planner cost before and after each suggestion (via HypoPG simulation).</p>
        </div>
    </div>

    <%
        List<AIComparisonRow> rows = (List<AIComparisonRow>) request.getAttribute("comparisonRows");
        DecimalFormat df = new DecimalFormat("###,###.##", new DecimalFormatSymbols(new Locale("pt","BR")));

        if (rows == null || rows.isEmpty()) {
    %>
    <div class="row" style="margin-top: 30px;">
        <div class="col-md-12 text-center">
            <div class="alert alert-info">
                No AI suggestions yet. Select <b>GeminiTuning</b>, <b>GPTTuning</b>, and/or <b>ClaudeTuning</b> heuristics
                and wait up to 60 seconds for the first analysis cycle.
            </div>
        </div>
    </div>
    <% } else { %>

    <!-- Bar chart -->
    <div class="row" style="margin-top: 10px; margin-bottom: 30px;">
        <div id="comparison_chart_div" style="width:100%; height:420px;"></div>
    </div>

    <!-- Legend -->
    <div class="row" style="margin-bottom: 20px; text-align:center;">
        <span style="margin-right:20px;">
            <span style="display:inline-block;width:14px;height:14px;background:#db4437;border-radius:2px;vertical-align:middle;"></span>
            &nbsp;Original Cost
        </span>
        <span style="margin-right:20px;">
            <span style="display:inline-block;width:14px;height:14px;background:#4285f4;border-radius:2px;vertical-align:middle;"></span>
            &nbsp;Gemini Suggestion
        </span>
        <span style="margin-right:20px;">
            <span style="display:inline-block;width:14px;height:14px;background:#0f9d58;border-radius:2px;vertical-align:middle;"></span>
            &nbsp;GPT Suggestion
        </span>
        <span>
            <span style="display:inline-block;width:14px;height:14px;background:#e07b39;border-radius:2px;vertical-align:middle;"></span>
            &nbsp;Claude Suggestion
        </span>
    </div>

    <!-- Per-SQL detail cards -->
    <% for (AIComparisonRow row : rows) { %>
    <div class="row" style="margin-bottom: 30px; border-top: 2px solid #c7c5c1; padding-top: 15px;">
        <div class="col-md-12">
            <h4>SQL #<%=row.sqlId%>
                <small style="color:#888; font-weight:normal;">
                    Original cost: <b><%=df.format(row.originalCost)%></b>
                </small>
            </h4>
            <!-- Original query (collapsible) -->
            <div class="panel panel-default">
                <div class="panel-heading" style="cursor:pointer;"
                     data-toggle="collapse" data-target="#sql-body-<%=row.sqlId%>">
                    <b>Original Query</b> <span class="glyphicon glyphicon-chevron-down pull-right"></span>
                </div>
                <div id="sql-body-<%=row.sqlId%>" class="collapse">
                    <div class="panel-body">
                        <pre class="prettyprint lang-sql" style="white-space:pre-wrap;"><%=row.sqlText != null ? row.sqlText.replace("<","&lt;").replace(">","&gt;") : ""%></pre>
                    </div>
                </div>
            </div>

            <div class="row">
                <!-- Gemini card -->
                <div class="col-md-4">
                    <div class="panel panel-primary">
                        <div class="panel-heading" style="background:#4285f4; border-color:#4285f4;">
                            <b>Gemini Suggestion</b>
                            <% if (row.geminiCommand != null && row.geminiCost > 0) {
                               long geminiPct = Math.round(Math.abs(1f - row.geminiCost / row.originalCost) * 100); %>
                            <span class="pull-right badge" style="background:#fff; color:#4285f4;">
                                Cost: <%=df.format(row.geminiCost)%>
                                <% if (row.geminiCost < row.originalCost) { %>&nbsp;(&darr;<%=geminiPct%>%)<% }
                                   else if (row.geminiCost > row.originalCost) { %>&nbsp;(&uarr;<%=geminiPct%>%)<% }
                                   else { %>&nbsp;(=)<% } %>
                            </span>
                            <% } else if (row.geminiCommand == null) { %>
                            <span class="pull-right" style="color:#cce;font-size:12px;">No suggestion yet</span>
                            <% } else { %>
                            <span class="pull-right" style="color:#fdd;font-size:12px;">No cost estimate</span>
                            <% } %>
                        </div>
                        <div class="panel-body" style="background:#f0f4ff;">
                            <% if (row.geminiCommand != null) { %>
                            <b>Suggested index:</b>
                            <pre class="prettyprint lang-sql" style="background:#fff; font-size:12px; white-space:pre-wrap;"><%=row.geminiCommand.replace("<","&lt;").replace(">","&gt;")%></pre>
                            <div style="font-size:12px; margin-bottom:6px;">
                                <div><b>Creation Cost:</b> <%=df.format(row.geminiCreationCost)%></div>
                                <div><b>Accumulated Bonus:</b> <%=df.format(row.geminiBonus)%></div>
                            </div>
                            <% if (row.geminiJustification != null && !row.geminiJustification.isEmpty()) { %>
                            <b>Justification:</b>
                            <p style="font-size:12px; color:#555;"><%=row.geminiJustification%></p>
                            <% } %>
                            <% } else { %>
                            <p style="color:#999;">Waiting for Gemini analysis...</p>
                            <% } %>
                        </div>
                    </div>
                </div>

                <!-- GPT card -->
                <div class="col-md-4">
                    <div class="panel panel-success">
                        <div class="panel-heading" style="background:#0f9d58; border-color:#0f9d58;">
                            <b>GPT Suggestion</b>
                            <% if (row.gptCommand != null && row.gptCost > 0) {
                               long gptPct = Math.round(Math.abs(1f - row.gptCost / row.originalCost) * 100); %>
                            <span class="pull-right badge" style="background:#fff; color:#0f9d58;">
                                Cost: <%=df.format(row.gptCost)%>
                                <% if (row.gptCost < row.originalCost) { %>&nbsp;(&darr;<%=gptPct%>%)<% }
                                   else if (row.gptCost > row.originalCost) { %>&nbsp;(&uarr;<%=gptPct%>%)<% }
                                   else { %>&nbsp;(=)<% } %>
                            </span>
                            <% } else if (row.gptCommand == null) { %>
                            <span class="pull-right" style="color:#cec;font-size:12px;">No suggestion yet</span>
                            <% } else { %>
                            <span class="pull-right" style="color:#cec;font-size:12px;">No cost estimate</span>
                            <% } %>
                        </div>
                        <div class="panel-body" style="background:#f0fff5;">
                            <% if (row.gptCommand != null) { %>
                            <b>Suggested index:</b>
                            <pre class="prettyprint lang-sql" style="background:#fff; font-size:12px; white-space:pre-wrap;"><%=row.gptCommand.replace("<","&lt;").replace(">","&gt;")%></pre>
                            <div style="font-size:12px; margin-bottom:6px;">
                                <div><b>Creation Cost:</b> <%=df.format(row.gptCreationCost)%></div>
                                <div><b>Accumulated Bonus:</b> <%=df.format(row.gptBonus)%></div>
                            </div>
                            <% if (row.gptJustification != null && !row.gptJustification.isEmpty()) { %>
                            <b>Justification:</b>
                            <p style="font-size:12px; color:#555;"><%=row.gptJustification%></p>
                            <% } %>
                            <% } else { %>
                            <p style="color:#999;">Waiting for GPT analysis...</p>
                            <% } %>
                        </div>
                    </div>
                </div>

                <!-- Claude card -->
                <div class="col-md-4">
                    <div class="panel panel-warning">
                        <div class="panel-heading" style="background:#e07b39; border-color:#e07b39; color:#fff;">
                            <b>Claude Suggestion</b>
                            <% if (row.claudeCommand != null && row.claudeCost > 0) {
                               long claudePct = Math.round(Math.abs(1f - row.claudeCost / row.originalCost) * 100); %>
                            <span class="pull-right badge" style="background:#fff; color:#e07b39;">
                                Cost: <%=df.format(row.claudeCost)%>
                                <% if (row.claudeCost < row.originalCost) { %>&nbsp;(&darr;<%=claudePct%>%)<% }
                                   else if (row.claudeCost > row.originalCost) { %>&nbsp;(&uarr;<%=claudePct%>%)<% }
                                   else { %>&nbsp;(=)<% } %>
                            </span>
                            <% } else if (row.claudeCommand == null) { %>
                            <span class="pull-right" style="color:#ffe;font-size:12px;">No suggestion yet</span>
                            <% } else { %>
                            <span class="pull-right" style="color:#ffe;font-size:12px;">No cost estimate</span>
                            <% } %>
                        </div>
                        <div class="panel-body" style="background:#fff7f0;">
                            <% if (row.claudeCommand != null) { %>
                            <b>Suggested index:</b>
                            <pre class="prettyprint lang-sql" style="background:#fff; font-size:12px; white-space:pre-wrap;"><%=row.claudeCommand.replace("<","&lt;").replace(">","&gt;")%></pre>
                            <div style="font-size:12px; margin-bottom:6px;">
                                <div><b>Creation Cost:</b> <%=df.format(row.claudeCreationCost)%></div>
                                <div><b>Accumulated Bonus:</b> <%=df.format(row.claudeBonus)%></div>
                            </div>
                            <% if (row.claudeJustification != null && !row.claudeJustification.isEmpty()) { %>
                            <b>Justification:</b>
                            <p style="font-size:12px; color:#555;"><%=row.claudeJustification%></p>
                            <% } %>
                            <% } else { %>
                            <p style="color:#999;">Waiting for Claude analysis...</p>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <% } %>
    <% } %>

</div>
<jsp:include page="helpers/foot.jsp"/>
</body>
</html>
