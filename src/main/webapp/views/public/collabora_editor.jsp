<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Dokumenten-Editor - TechnikTeam</title>
<style>
/* Basic styles for a full-page iframe experience */
html, body {
	margin: 0;
	padding: 0;
	height: 100%;
	overflow: hidden;
}

iframe {
	width: 100%;
	height: 100%;
	border: none;
}
</style>
</head>
<body>
	<form id="collabora-form" name="collabora-form" method="post"
		target="collabora_iframe" action="${collaboraUrl}">
		<input name="access_token" value="${accessToken}" type="hidden" />
	</form>

	<iframe id="collabora_iframe" name="collabora_iframe"></iframe>

	<script type="text/javascript">
		// Automatically submit the form to load the iframe with the required POST data.
		document.getElementById('collabora-form').submit();
	</script>
</body>
</html>