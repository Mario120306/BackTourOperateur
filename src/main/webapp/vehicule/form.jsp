<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="itu.back.model.Vehicule" %>
<%@ page import="itu.back.model.TypeCarburant" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("isEdit") != null && (Boolean)request.getAttribute("isEdit") ? "Modifier" : "Ajouter" %> un Vehicule</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
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
            --success: #10b981;
            --success-light: #d1fae5;
            --danger: #ef4444;
            --danger-light: #fee2e2;
            --text-primary: #0f172a;
            --text-secondary: #64748b;
            --text-light: #ffffff;
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

        .content-with-sidebar {
            margin-left: 280px;
            min-height: 100vh;
            padding: 32px 40px;
            display: flex;
            justify-content: center;
        }

        @media (max-width: 1024px) {
            .content-with-sidebar {
                margin-left: 0;
                padding: 24px;
            }
        }

        .form-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 40px;
            max-width: 600px;
            width: 100%;
            box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
        }

        .page-title {
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 32px;
            text-align: center;
        }

        .alert {
            padding: 14px 20px;
            border-radius: var(--radius);
            margin-bottom: 24px;
            font-size: 0.875rem;
            font-weight: 500;
        }

        .alert-success {
            background: var(--success-light);
            color: #065f46;
            border: 1px solid #a7f3d0;
        }

        .alert-error {
            background: var(--danger-light);
            color: #991b1b;
            border: 1px solid #fecaca;
        }

        .form-group {
            margin-bottom: 24px;
        }

        .form-label {
            display: block;
            font-size: 0.875rem;
            font-weight: 500;
            color: var(--text-primary);
            margin-bottom: 6px;
        }

        .form-input {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid var(--border);
            border-radius: var(--radius);
            font-size: 0.875rem;
            font-family: inherit;
            transition: all 0.15s ease;
            background: var(--bg-card);
        }

        .form-input:focus {
            outline: none;
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .form-row {
            display: flex;
            gap: 16px;
        }

        .form-row .form-group {
            flex: 1;
        }

        .btn-group {
            display: flex;
            gap: 12px;
            margin-top: 32px;
        }

        .btn {
            flex: 1;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 20px;
            font-size: 0.875rem;
            font-weight: 500;
            font-family: inherit;
            border-radius: var(--radius);
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.15s ease;
        }

        .btn-primary {
            background: var(--primary);
            color: var(--text-light);
        }

        .btn-primary:hover {
            background: var(--primary-light);
        }

        .btn-secondary {
            background: var(--border);
            color: var(--text-primary);
        }

        .btn-secondary:hover {
            background: #cbd5e1;
        }

        @media (max-width: 600px) {
            .form-row {
                flex-direction: column;
                gap: 0;
            }

            .form-card {
                padding: 24px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.jsp" %>
    <div class="content-with-sidebar">
        <div class="form-card">
            <%
                Boolean isEdit = (Boolean) request.getAttribute("isEdit");
                Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
                List<TypeCarburant> typesCarburant = (List<TypeCarburant>) request.getAttribute("typesCarburant");
            %>

            <h1 class="page-title"><%= isEdit != null && isEdit ? "Modifier" : "Ajouter" %> un Vehicule</h1>

            <% if (request.getAttribute("success") != null) { %>
                <div class="alert alert-success"><%= request.getAttribute("success") %></div>
            <% } %>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error"><%= request.getAttribute("error") %></div>
            <% } %>

            <form action="<%= request.getContextPath() %><%= isEdit != null && isEdit ? "/vehicule/update" : "/vehicule/save" %>" method="post">
                <% if (isEdit != null && isEdit && vehicule != null) { %>
                    <input type="hidden" name="id" value="<%= vehicule.getId() %>">
                <% } %>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="marque">Marque</label>
                        <input type="text" id="marque" name="marque" class="form-input" required
                               value="<%= vehicule != null && vehicule.getMarque() != null ? vehicule.getMarque() : "" %>"
                               placeholder="Ex: Toyota">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="modele">Modele</label>
                        <input type="text" id="modele" name="modele" class="form-input" required
                               value="<%= vehicule != null && vehicule.getModele() != null ? vehicule.getModele() : "" %>"
                               placeholder="Ex: Corolla">
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="reference">Reference</label>
                    <input type="text" id="reference" name="reference" class="form-input" required
                           value="<%= vehicule != null && vehicule.getReference() != null ? vehicule.getReference() : "" %>"
                           placeholder="Ex: VEH-001">
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="nombrePlaces">Nombre de places</label>
                        <input type="number" id="nombrePlaces" name="nombrePlaces" class="form-input" required min="1"
                               value="<%= vehicule != null && vehicule.getNombrePlaces() > 0 ? vehicule.getNombrePlaces() : "" %>"
                               placeholder="Ex: 5">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="vitesseMoyenne">Vitesse moyenne (km/h)</label>
                        <input type="number" id="vitesseMoyenne" name="vitesseMoyenne" class="form-input" required min="1"
                               value="<%= vehicule != null && vehicule.getVitesseMoyenne() > 0 ? vehicule.getVitesseMoyenne() : "" %>"
                               placeholder="Ex: 80">
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="typeCarburantId">Type de carburant</label>
                    <select id="typeCarburantId" name="typeCarburantId" class="form-input" required>
                        <option value="">-- Selectionner un type --</option>
                        <% if (typesCarburant != null) {
                            for (TypeCarburant tc : typesCarburant) { %>
                                <option value="<%= tc.getId() %>"
                                    <%= vehicule != null && vehicule.getTypeCarburantId() == tc.getId() ? "selected" : "" %>>
                                    <%= tc.getNom() %> (<%= tc.getReference() %>)
                                </option>
                        <%  }
                        } %>
                    </select>
                </div>

                <div class="btn-group">
                    <a href="<%= request.getContextPath() %>/vehicule/list" class="btn btn-secondary">Annuler</a>
                    <button type="submit" class="btn btn-primary">
                        <%= isEdit != null && isEdit ? "Modifier" : "Ajouter" %>
                    </button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
