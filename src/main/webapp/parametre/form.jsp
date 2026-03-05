<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="itu.back.model.Parametre" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestion des Paramètres</title>
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
            --success: #059669;
            --danger: #dc2626;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #ffffff;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            padding: 30px 20px;
        }

        .page-wrapper {
            display: flex;
            flex-direction: column;
            gap: 30px;
            width: 100%;
            max-width: 900px;
        }

        .container {
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            padding: 40px;
            width: 100%;
            border: 1px solid var(--border-light);
        }

        h2 {
            color: var(--text-dark);
            margin-bottom: 20px;
            font-size: 1.3em;
            border-bottom: 2px solid var(--bg-secondary);
            padding-bottom: 10px;
        }

        h1 {
            color: var(--text-dark);
            margin-bottom: 30px;
            text-align: center;
            font-size: 2em;
            font-weight: 700;
        }

        .alert {
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: 500;
        }

        .alert-success {
            background-color: #ecfdf5;
            color: var(--success);
            border: 1px solid #a7f3d0;
        }

        .alert-error {
            background-color: #fef2f2;
            color: var(--danger);
            border: 1px solid #fecaca;
        }

        .form-group {
            margin-bottom: 25px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            color: var(--text-dark);
            font-weight: 600;
            font-size: 0.95em;
        }

        input[type="text"], textarea {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid var(--border-light);
            border-radius: 8px;
            font-size: 1em;
            transition: all 0.2s ease;
            background-color: #f9fafb;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        input[type="text"]:focus, textarea:focus {
            outline: none;
            border-color: var(--bg-secondary);
            background-color: white;
            box-shadow: 0 0 0 3px rgba(22, 27, 34, 0.1);
        }

        textarea {
            resize: vertical;
            min-height: 80px;
        }

        .btn-container {
            display: flex;
            gap: 15px;
            margin-top: 30px;
        }

        .btn {
            flex: 1;
            padding: 15px;
            border: none;
            border-radius: 8px;
            font-size: 1em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            text-decoration: none;
            text-align: center;
        }

        .btn-danger {
            background: var(--danger);
            color: white;
            padding: 6px 14px;
            font-size: 0.85em;
            border-radius: 6px;
            border: none;
            cursor: pointer;
            font-weight: 600;
            transition: all 0.2s ease;
        }

        .btn-danger:hover {
            background: #b91c1c;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }

        thead tr {
            background: var(--bg-secondary);
            color: var(--text-light);
        }

        th, td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid var(--border-light);
        }

        th {
            font-weight: 600;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        tbody tr:hover {
            background-color: #f9fafb;
        }

        .badge-code {
            background-color: var(--bg-secondary);
            color: var(--text-light);
            padding: 3px 10px;
            border-radius: 12px;
            font-family: monospace;
            font-size: 0.9em;
            font-weight: 600;
        }

        .no-data {
            text-align: center;
            color: var(--text-muted);
            padding: 30px;
            font-style: italic;
        }

        .btn-primary {
            background: var(--bg-secondary);
            color: var(--text-light);
        }

        .btn-primary:hover {
            background: var(--bg-tertiary);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(31, 41, 55, 0.3);
        }

        .btn-secondary {
            background: var(--bg-tertiary);
            color: var(--text-light);
        }

        .btn-secondary:hover {
            background: var(--accent-hover);
        }

        @media (max-width: 600px) {
            .container { padding: 30px 20px; }
            h1 { font-size: 1.5em; }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.html" %>
    <div class="content-with-sidebar">
    <div class="page-wrapper">

        <%
            List<Parametre> parametres = (List<Parametre>) request.getAttribute("parametres");
        %>

        <!-- ===== FORMULAIRE D'INSERTION ===== -->
        <div class="container">
            <h1>&#9881; Gestion des Paramètres</h1>

            <% if (request.getAttribute("success") != null) { %>
                <div class="alert alert-success"><%= request.getAttribute("success") %></div>
            <% } %>
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error"><%= request.getAttribute("error") %></div>
            <% } %>

            <h2>Ajouter un paramètre</h2>

            <form action="<%= request.getContextPath() %>/parametre/insert" method="post">

                <div class="form-group">
                    <label for="code">Code <span style="color:red">*</span></label>
                    <input type="text" id="code" name="code" required
                           placeholder="Ex: TVA_RATE, MAX_RESERVATION..."
                           maxlength="50">
                </div>

                <div class="form-group">
                    <label for="valeur">Valeur <span style="color:red">*</span></label>
                    <input type="text" id="valeur" name="valeur" required
                           placeholder="Ex: 20, true, http://..."
                           maxlength="100">
                </div>

                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description"
                              placeholder="Description optionnelle du paramètre..."
                              maxlength="255"></textarea>
                </div>

                <div class="btn-container">
                    <button type="reset" class="btn btn-secondary">Réinitialiser</button>
                    <button type="submit" class="btn btn-primary">&#43; Ajouter le paramètre</button>
                </div>
            </form>
        </div>

        <!-- ===== LISTE DES PARAMETRES EXISTANTS ===== -->
        <div class="container">
            <h2>Liste des paramètres</h2>

            <% if (parametres == null || parametres.isEmpty()) { %>
                <p class="no-data">Aucun paramètre enregistré.</p>
            <% } else { %>
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
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
                                      onsubmit="return confirm('Supprimer le paramètre &#34;<%= p.getCode() %>&#34; ?');">
                                    <input type="hidden" name="id" value="<%= p.getId() %>">
                                    <button type="submit" class="btn-danger">Supprimer</button>
                                </form>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
        </div>

    </div>
    </div>
</body>
</html>
