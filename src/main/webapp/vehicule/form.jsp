<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="itu.back.model.Vehicule" %>
<%@ page import="itu.back.model.TypeCarburant" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("isEdit") != null && (Boolean)request.getAttribute("isEdit") ? "Modifier" : "Ajouter" %> un Véhicule</title>
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
            border: 1px solid #e5e7eb;
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

        select, input {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid var(--border-light);
            border-radius: 8px;
            font-size: 1em;
            transition: all 0.2s ease;
            background-color: #f9fafb;
        }

        select:focus, input:focus {
            outline: none;
            border-color: var(--bg-secondary);
            background-color: white;
            box-shadow: 0 0 0 3px rgba(22, 27, 34, 0.1);
        }

        select {
            cursor: pointer;
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

        .form-row {
            display: flex;
            gap: 20px;
        }

        .form-row .form-group {
            flex: 1;
        }

        @media (max-width: 600px) {
            .form-row {
                flex-direction: column;
                gap: 0;
            }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.html" %>
    <div class="content-with-sidebar">
    <div class="container">
        <%
            Boolean isEdit = (Boolean) request.getAttribute("isEdit");
            Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
            List<TypeCarburant> typesCarburant = (List<TypeCarburant>) request.getAttribute("typesCarburant");
        %>

        <h1>🚗 <%= isEdit != null && isEdit ? "Modifier" : "Ajouter" %> un Véhicule</h1>

        <% if (request.getAttribute("success") != null) { %>
            <div class="alert alert-success">
                <%= request.getAttribute("success") %>
            </div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <form action="<%= request.getContextPath() %><%= isEdit != null && isEdit ? "/vehicule/update" : "/vehicule/save" %>" method="post">
            <% if (isEdit != null && isEdit && vehicule != null) { %>
                <input type="hidden" name="id" value="<%= vehicule.getId() %>">
            <% } %>

            <div class="form-row">
                <div class="form-group">
                    <label for="marque">Marque *</label>
                    <input type="text" id="marque" name="marque" required
                           value="<%= vehicule != null && vehicule.getMarque() != null ? vehicule.getMarque() : "" %>"
                           placeholder="Ex: Toyota">
                </div>

                <div class="form-group">
                    <label for="modele">Modèle *</label>
                    <input type="text" id="modele" name="modele" required
                           value="<%= vehicule != null && vehicule.getModele() != null ? vehicule.getModele() : "" %>"
                           placeholder="Ex: Corolla">
                </div>
            </div>

            <div class="form-group">
                <label for="reference">Référence *</label>
                <input type="text" id="reference" name="reference" required
                       value="<%= vehicule != null && vehicule.getReference() != null ? vehicule.getReference() : "" %>"
                       placeholder="Ex: VEH-001">
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="nombrePlaces">Nombre de places *</label>
                    <input type="number" id="nombrePlaces" name="nombrePlaces" required min="1"
                           value="<%= vehicule != null && vehicule.getNombrePlaces() > 0 ? vehicule.getNombrePlaces() : "" %>"
                           placeholder="Ex: 5">
                </div>

                <div class="form-group">
                    <label for="vitesseMoyenne">Vitesse moyenne (km/h) *</label>
                    <input type="number" id="vitesseMoyenne" name="vitesseMoyenne" required min="1"
                           value="<%= vehicule != null && vehicule.getVitesseMoyenne() > 0 ? vehicule.getVitesseMoyenne() : "" %>"
                           placeholder="Ex: 80">
                </div>
            </div>

            <div class="form-group">
                <label for="typeCarburantId">Type de carburant *</label>
                <select id="typeCarburantId" name="typeCarburantId" required>
                    <option value="">-- Sélectionner un type --</option>
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

            <div class="btn-container">
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
