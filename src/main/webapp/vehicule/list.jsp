<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="itu.back.model.Vehicule" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Véhicules</title>
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
            --bg-light: #f9fafb;
            --accent-primary: #1f2937;
            --accent-hover: #374151;
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
            padding: 30px;
        }

        .container {
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            padding: 40px;
            max-width: 1200px;
            margin: 0 auto;
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

        .header-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
            flex-wrap: wrap;
            gap: 15px;
        }

        .stats {
            display: flex;
            gap: 20px;
        }

        .stat-item {
            background: var(--bg-secondary);
            color: var(--text-light);
            padding: 15px 25px;
            border-radius: 10px;
            text-align: center;
        }

        .stat-item .number {
            font-size: 1.8em;
            font-weight: 700;
        }

        .stat-item .label {
            font-size: 0.85em;
            color: #d1d5db;
        }

        .btn-add {
            display: inline-block;
            background: var(--bg-secondary);
            color: var(--text-light);
            padding: 14px 28px;
            text-decoration: none;
            border-radius: 8px;
            font-weight: 600;
            transition: all 0.2s ease;
        }

        .btn-add:hover {
            background: var(--bg-tertiary);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(31, 41, 55, 0.3);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        th, td {
            padding: 15px;
            text-align: left;
            border-bottom: 1px solid #e5e7eb;
        }

        th {
            background-color: var(--bg-secondary);
            color: var(--text-light);
            font-weight: 600;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        th:first-child {
            border-radius: 8px 0 0 0;
        }

        th:last-child {
            border-radius: 0 8px 0 0;
        }

        tr:hover {
            background-color: #f9fafb;
        }

        .actions {
            display: flex;
            gap: 10px;
        }

        .btn-edit, .btn-delete {
            padding: 8px 16px;
            border-radius: 6px;
            text-decoration: none;
            font-size: 0.9em;
            font-weight: 500;
            transition: all 0.2s ease;
        }

        .btn-edit {
            background-color: var(--bg-tertiary);
            color: var(--text-light);
        }

        .btn-edit:hover {
            background-color: var(--accent-hover);
            transform: translateY(-1px);
        }

        .btn-delete {
            background-color: var(--danger);
            color: white;
        }

        .btn-delete:hover {
            background-color: #b91c1c;
            transform: translateY(-1px);
        }

        .empty-message {
            text-align: center;
            padding: 60px 40px;
            color: var(--text-muted);
        }

        .empty-message .icon {
            font-size: 4em;
            margin-bottom: 20px;
        }

        .empty-message h3 {
            color: var(--text-dark);
            margin-bottom: 10px;
        }

        .badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 15px;
            font-size: 0.85em;
            font-weight: 500;
        }

        .badge-info {
            background-color: var(--bg-secondary);
            color: var(--text-light);
        }

        .badge-success {
            background-color: var(--bg-tertiary);
            color: var(--text-light);
        }

        @media (max-width: 768px) {
            .container {
                padding: 20px;
            }

            .header-actions {
                flex-direction: column;
                align-items: stretch;
            }

            .stats {
                justify-content: center;
            }

            table {
                font-size: 0.9em;
            }

            th, td {
                padding: 10px;
            }

            .actions {
                flex-direction: column;
                gap: 5px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.html" %>
    <div class="content-with-sidebar">
    <%
        List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
        int totalVehicules = vehicules != null ? vehicules.size() : 0;
        int totalPlaces = 0;
        if (vehicules != null) {
            for (Vehicule v : vehicules) {
                totalPlaces += v.getNombrePlaces();
            }
        }
    %>

    <div class="container">
        <h1>Liste des Véhicules</h1>

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

        <div class="header-actions">
            <div class="stats">
                <div class="stat-item">
                    <div class="number"><%= totalVehicules %></div>
                    <div class="label">Véhicules</div>
                </div>
                <div class="stat-item">
                    <div class="number"><%= totalPlaces %></div>
                    <div class="label">Places</div>
                </div>
            </div>
            <a href="<%= request.getContextPath() %>/vehicule/form" class="btn-add">+ Ajouter un véhicule</a>
        </div>

        <% if (vehicules != null && !vehicules.isEmpty()) { %>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Référence</th>
                        <th>Marque</th>
                        <th>Modèle</th>
                        <th>Places</th>
                        <th>Vitesse Moy.</th>
                        <th>Carburant</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Vehicule v : vehicules) { %>
                        <tr>
                            <td><%= v.getId() %></td>
                            <td><span class="badge badge-info"><%= v.getReference() %></span></td>
                            <td><%= v.getMarque() %></td>
                            <td><%= v.getModele() %></td>
                            <td><%= v.getNombrePlaces() %></td>
                            <td><%= v.getVitesseMoyenne() %> km/h</td>
                            <td><span class="badge badge-success"><%= v.getTypeCarburant() != null ? v.getTypeCarburant().getNom() : "-" %></span></td>
                            <td class="actions">
                                <a href="<%= request.getContextPath() %>/vehicule/edit?id=<%= v.getId() %>" class="btn-edit">Modifier</a>
                                <a href="<%= request.getContextPath() %>/vehicule/delete?id=<%= v.getId() %>" class="btn-delete" 
                                   onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce véhicule ?')">Supprimer</a>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else { %>
            <div class="empty-message">
                <div class="icon">🚗</div>
                <h3>Aucun véhicule enregistré</h3>
                <p>Commencez par ajouter votre premier véhicule</p>
            </div>
        <% } %>
    </div>
    </div>
</body>
</html>
