<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="itu.back.model.Parametre" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestion des Parametres</title>
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
            --success: #10b981;
            --success-bg: #ecfdf5;
            --danger: #ef4444;
            --danger-bg: #fef2f2;
            --warning: #f59e0b;
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

        .page-header {
            margin-bottom: 32px;
        }

        .page-header h1 {
            font-size: 1.75rem;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .page-header p {
            color: var(--text-secondary);
            font-size: 0.938rem;
        }

        .card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 32px;
            margin-bottom: 24px;
        }

        .card-title {
            font-size: 1.125rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 1px solid var(--border);
        }

        .alert {
            padding: 14px 18px;
            border-radius: var(--radius);
            margin-bottom: 24px;
            font-size: 0.875rem;
            font-weight: 500;
        }

        .alert-success {
            background: var(--success-bg);
            color: var(--success);
            border: 1px solid #a7f3d0;
        }

        .alert-error {
            background: var(--danger-bg);
            color: var(--danger);
            border: 1px solid #fecaca;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .required {
            color: var(--danger);
        }

        input[type="text"], textarea {
            width: 100%;
            padding: 12px 14px;
            border: 1px solid var(--border);
            border-radius: var(--radius);
            font-size: 0.938rem;
            font-family: inherit;
            background: var(--bg-card);
            color: var(--text-primary);
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        input[type="text"]:focus, textarea:focus {
            outline: none;
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        textarea {
            resize: vertical;
            min-height: 80px;
        }

        input::placeholder, textarea::placeholder {
            color: var(--text-secondary);
        }

        .btn-container {
            display: flex;
            gap: 12px;
            margin-top: 24px;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 24px;
            font-size: 0.875rem;
            font-weight: 600;
            font-family: inherit;
            border: none;
            border-radius: var(--radius);
            cursor: pointer;
            transition: all 0.2s;
            text-decoration: none;
        }

        .btn-primary {
            background: var(--accent);
            color: white;
        }

        .btn-primary:hover {
            background: var(--accent-hover);
        }

        .btn-secondary {
            background: var(--bg-primary);
            color: var(--text-secondary);
            border: 1px solid var(--border);
        }

        .btn-secondary:hover {
            background: var(--border);
            color: var(--text-primary);
        }

        .btn-danger {
            background: var(--danger-bg);
            color: var(--danger);
            padding: 8px 16px;
            font-size: 0.813rem;
        }

        .btn-danger:hover {
            background: var(--danger);
            color: white;
        }

        /* Table Styles */
        .table-container {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead {
            background: var(--bg-primary);
        }

        th {
            padding: 12px 16px;
            text-align: left;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--text-secondary);
            border-bottom: 1px solid var(--border);
        }

        td {
            padding: 16px;
            border-bottom: 1px solid var(--border);
            font-size: 0.875rem;
            color: var(--text-primary);
        }

        tbody tr:hover {
            background: var(--bg-primary);
        }

        .badge-code {
            display: inline-block;
            background: var(--primary);
            color: white;
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 0.75rem;
            font-weight: 600;
            font-family: 'Monaco', 'Consolas', monospace;
        }

        .no-data {
            text-align: center;
            color: var(--text-secondary);
            padding: 48px 16px;
        }

        @media (max-width: 768px) {
            .card {
                padding: 24px;
            }

            .page-header h1 {
                font-size: 1.5rem;
            }

            .btn-container {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.jsp" %>
    <div class="content-with-sidebar">
        <%
            List<Parametre> parametres = (List<Parametre>) request.getAttribute("parametres");
        %>

        <div class="page-header">
            <h1>Gestion des Parametres</h1>
            <p>Configurer les parametres systeme de l'application</p>
        </div>

        <% if (request.getAttribute("success") != null) { %>
            <div class="alert alert-success"><%= request.getAttribute("success") %></div>
        <% } %>
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <div class="card">
            <h2 class="card-title">Ajouter un parametre</h2>

            <form action="<%= request.getContextPath() %>/parametre/insert" method="post">
                <div class="form-group">
                    <label for="code">Code <span class="required">*</span></label>
                    <input type="text" id="code" name="code" required
                           placeholder="Ex: TVA_RATE, MAX_RESERVATION..."
                           maxlength="50">
                </div>

                <div class="form-group">
                    <label for="valeur">Valeur <span class="required">*</span></label>
                    <input type="text" id="valeur" name="valeur" required
                           placeholder="Ex: 20, true, http://..."
                           maxlength="100">
                </div>

                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description"
                              placeholder="Description optionnelle du parametre..."
                              maxlength="255"></textarea>
                </div>

                <div class="btn-container">
                    <button type="reset" class="btn btn-secondary">Reinitialiser</button>
                    <button type="submit" class="btn btn-primary">Ajouter le parametre</button>
                </div>
            </form>
        </div>

        <div class="card">
            <h2 class="card-title">Liste des parametres</h2>

            <% if (parametres == null || parametres.isEmpty()) { %>
                <p class="no-data">Aucun parametre enregistre.</p>
            <% } else { %>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Code</th>
                                <th>Valeur</th>
                                <th>Description</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Parametre p : parametres) { %>
                            <tr>
                                <td><%= p.getId() %></td>
                                <td><span class="badge-code"><%= p.getCode() %></span></td>
                                <td><%= p.getValeur() %></td>
                                <td><%= p.getDescription() != null ? p.getDescription() : "-" %></td>
                                <td>
                                    <form action="<%= request.getContextPath() %>/parametre/delete" method="post"
                                          onsubmit="return confirm('Supprimer le parametre?');">
                                        <input type="hidden" name="id" value="<%= p.getId() %>">
                                        <button type="submit" class="btn btn-danger">Supprimer</button>
                                    </form>
                                </td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>
