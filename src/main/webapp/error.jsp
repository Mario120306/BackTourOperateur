<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            --bg-primary: #ffffff;
            --bg-secondary: #1f2937;
            --bg-tertiary: #374151;
            --text-light: #ffffff;
            --text-muted: #6b7280;
            --text-dark: #1f2937;
            --border-light: #e5e7eb;
            --danger: #dc2626;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #ffffff;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 30px;
        }

        .container {
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            padding: 40px;
            max-width: 600px;
            width: 100%;
            text-align: center;
            border: 1px solid var(--border-light);
        }

        h1 {
            color: var(--danger);
            margin-bottom: 20px;
            font-size: 2em;
            font-weight: 700;
        }

        .error-icon {
            font-size: 4em;
            margin-bottom: 20px;
        }

        .error-message {
            background-color: #fef2f2;
            color: var(--danger);
            border: 1px solid #fecaca;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
        }

        .btn {
            display: inline-block;
            padding: 12px 24px;
            background: var(--bg-secondary);
            color: var(--text-light);
            text-decoration: none;
            border-radius: 8px;
            font-weight: 600;
            transition: all 0.2s ease;
        }

        .btn:hover {
            background: var(--bg-tertiary);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(31, 41, 55, 0.3);
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.html" %>
    <div class="content-with-sidebar">
    <div class="container">
        <div class="error-icon">⚠️</div>
        <h1>Une erreur s'est produite</h1>
        <div class="error-message">
            <%= request.getAttribute("error") != null ? request.getAttribute("error") : "Une erreur inattendue s'est produite." %>
        </div>
        <a href="<%= request.getContextPath() %>/reservation/form" class="btn">Retour au formulaire</a>
    </div>
    </div>
</body>
</html>
