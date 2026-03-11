<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            --primary: #0f172a;
            --primary-light: #1e293b;
            --accent: #3b82f6;
            --accent-hover: #2563eb;
            --danger: #ef4444;
            --danger-bg: #fef2f2;
            --text-primary: #0f172a;
            --text-secondary: #64748b;
            --bg-primary: #f8fafc;
            --bg-card: #ffffff;
            --border: #e2e8f0;
            --radius: 8px;
            --radius-lg: 12px;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: var(--bg-primary);
            color: var(--text-primary);
            min-height: 100vh;
            line-height: 1.6;
        }

        .error-container {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: calc(100vh - 100px);
        }

        .card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 48px;
            max-width: 500px;
            width: 100%;
            text-align: center;
        }

        .error-code {
            font-size: 4rem;
            font-weight: 700;
            color: var(--danger);
            line-height: 1;
            margin-bottom: 16px;
        }

        h1 {
            font-size: 1.5rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 16px;
        }

        .error-message {
            background: var(--danger-bg);
            color: var(--danger);
            border: 1px solid #fecaca;
            padding: 16px 20px;
            border-radius: var(--radius);
            margin-bottom: 32px;
            font-size: 0.938rem;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 24px;
            font-size: 0.938rem;
            font-weight: 600;
            font-family: inherit;
            border: none;
            border-radius: var(--radius);
            cursor: pointer;
            transition: all 0.2s;
            text-decoration: none;
            background: var(--accent);
            color: white;
        }

        .btn:hover {
            background: var(--accent-hover);
        }

        @media (max-width: 768px) {
            .card {
                padding: 32px 24px;
                margin: 16px;
            }

            .error-code {
                font-size: 3rem;
            }
        }
    </style>
</head>
<body>
    <%@ include file="includes/sidebar.jsp" %>
    <div class="content-with-sidebar">
        <div class="error-container">
            <div class="card">
                <div class="error-code">Erreur</div>
                <h1>Une erreur s'est produite</h1>
                <div class="error-message">
                    <%= request.getAttribute("error") != null ? request.getAttribute("error") : "Une erreur inattendue s'est produite." %>
                </div>
                <a href="<%= request.getContextPath() %>/reservation/form" class="btn">Retour au formulaire</a>
            </div>
        </div>
    </div>
</body>
</html>
