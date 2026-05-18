<%@page import="br.com.pucrio.inf.biobd.outertuning.bib.configuration.Configuration"%>
<div id="base">
    <div class="container padding-vert-30">
        <%
            Configuration configuration = (Configuration) request.getAttribute("config");
            if (configuration != null) {
                String sgbd = configuration.getProperty("sgbd").toUpperCase();
                String databaseName = configuration.getProperty("databaseName").toUpperCase();
        %>
        <%=sgbd%> - <%=databaseName%>
        <% }%>
    </div>
</div>
<!-- Footer Menu -->
<div id="footer">
    <div class="container">
        <div class="row">
            <div id="copyright" class="col-md-4">
            </div>
            <div id="footermenu" class="col-md-8">
                <ul class="list-unstyled list-inline pull-right">
                    <li>
                        <a class="" href="http://www.biobd.inf.puc-rio.br">&copy; 2016 BioBD - PUC-Rio</a> -- <a class="" href="http://www.free-css.com/free-css-templates/page191/substance"> CSS by SUBSTANCE</a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>

