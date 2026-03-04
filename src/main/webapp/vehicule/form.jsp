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

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }

        .container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            padding: 40px;
            max-width: 600px;
            width: 100%;
        }

        h1 {
            color: #333;
            margin-bottom: 30px;
            text-align: center;
            font-size: 2em;
        }

        .alert {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: 500;
        }

        .alert-success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        .alert-error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .form-group {
            margin-bottom: 25px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            color: #555;
            font-weight: 600;
            font-size: 0.95em;
        }

        select, input {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 1em;
            transition: all 0.3s ease;
            background-color: #f8f9fa;
        }

        select:focus, input:focus {
            outline: none;
            border-color: #667eea;
            background-color: white;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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
            transition: all 0.3s ease;
            text-decoration: none;
            text-align: center;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }

        .btn-secondary {
            background: #6c757d;
            color: white;
        }

        .btn-secondary:hover {
            background: #5a6268;
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
</body>
</html>
