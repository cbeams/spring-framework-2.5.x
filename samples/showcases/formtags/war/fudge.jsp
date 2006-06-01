<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<% String endScript = "</script>"; %>
<html>
  <body>
    <script type="text/javascript">
alert("<spring:escapeBody javaScriptEscape="true"><%= endScript %></spring:escapeBody>");
alert("/" == "\/");
alert("</"+"script>" == "<spring:escapeBody javaScriptEscape="true"><%= endScript %></spring:escapeBody>");
    </script>
  </body>
</html>