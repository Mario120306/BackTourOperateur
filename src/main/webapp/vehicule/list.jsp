<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="itu.back.model.Vehicule" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Vehicules</title>
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
            --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
            --shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
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
        }

        @media (max-width: 1024px) {
            .content-with-sidebar {
                margin-left: 0;
                padding: 24px;
            }
        }

        .content-wrapper {
            max-width: 1400px;
            margin: 0 auto;
        }

        .page-header {
            margin-bottom: 32px;
        }

        .page-title {
            font-size: 1.875rem;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .page-subtitle {
            font-size: 0.975rem;
            color: var(--text-secondary);
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

        .stats-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
            flex-wrap: wrap;
            gap: 16px;
        }

        .stats-group {
            display: flex;
            gap: 16px;
        }

        .stat-card {
            background: var(--primary);
            color: var(--text-light);
            padding: 16px 24px;
            border-radius: var(--radius);
            text-align: center;
            min-width: 100px;
        }

        .stat-number {
            font-size: 1.5rem;
            font-weight: 700;
            line-height: 1.2;
        }

        .stat-label {
            font-size: 0.75rem;
            color: #94a3b8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 20px;
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

        .btn-sm {
            padding: 6px 12px;
            font-size: 0.813rem;
        }

        .btn-edit {
            background: var(--primary-light);
            color: var(--text-light);
        }

        .btn-edit:hover {
            background: #334155;
        }

        .btn-danger {
            background: var(--danger);
            color: var(--text-light);
        }

        .btn-danger:hover {
            background: #dc2626;
        }

        .data-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            overflow: hidden;
            box-shadow: var(--shadow-sm);
        }

        .data-table {
            width: 100%;
            border-collapse: collapse;
        }

        .data-table thead {
            background: var(--primary);
        }

        .data-table th {
            padding: 14px 20px;
            text-align: left;
            font-size: 0.75rem;
            font-weight: 600;
            color: var(--text-light);
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .data-table td {
            padding: 16px 20px;
            border-bottom: 1px solid var(--border);
            font-size: 0.875rem;
        }

        .data-table tbody tr:last-child td {
            border-bottom: none;
        }

        .data-table tbody tr:hover {
            background: var(--bg-primary);
        }

        .badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 0.75rem;
            font-weight: 500;
        }

        .badge-primary {
            background: var(--primary);
            color: var(--text-light);
        }

        .badge-secondary {
            background: #e2e8f0;
            color: var(--text-primary);
        }

        .actions-cell {
            display: flex;
            gap: 8px;
        }

        .empty-state {
            text-align: center;
            padding: 80px 40px;
        }

        .empty-state h3 {
            font-size: 1.125rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .empty-state p {
            font-size: 0.875rem;
            color: var(--text-secondary);
        }

        @media (max-width: 768px) {
            .stats-bar {
                flex-direction: column;
                align-items: stretch;
            }

            .stats-group {
                justify-content: center;
            }

            .data-table th,
            .data-table td {
                padding: 12px 16px;
            }

            .actions-cell {
                flex-direction: column;
                gap: 4px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.jsp" %>
    <div class="content-with-sidebar">
        <div class="content-wrapper">
    <%
        List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
        SimpleDateFormat dispoFormat = new SimpleDateFormat("HH:mm");
        int totalVehicules = vehicules != null ? vehicules.size() : 0;
        int totalPlaces = 0;
        if (vehicules != null) {
            for (Vehicule v : vehicules) {
                totalPlaces += v.getNombrePlaces();
            }
        }
    %>

            <div class="page-header">
                <h1 class="page-title">Liste des Vehicules</h1>
                <p class="page-subtitle">Gestion de la flotte de vehicules</p>
            </div>

        <% if (request.getAttribute("success") != null) { %>
            <div class="alert alert-success"><%= request.getAttribute("success") %></div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

            <div class="stats-bar">
                <div class="stats-group">
                    <div class="stat-card">
                        <div class="stat-number"><%= totalVehicules %></div>
                        <div class="stat-label">Vehicules</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number"><%= totalPlaces %></div>
                        <div class="stat-label">Places</div>
                    </div>
                </div>
                <a href="<%= request.getContextPath() %>/vehicule/form" class="btn btn-primary">Ajouter un vehicule</a>
            </div>

        <% if (vehicules != null && !vehicules.isEmpty()) { %>
            <div class="data-card">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Reference</th>
                            <th>Marque</th>
                            <th>Modele</th>
                            <th>Places</th>
                            <th>Vitesse Moy.</th>
                            <th>Disponible a</th>
                            <th>Carburant</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Vehicule v : vehicules) { %>
                            <tr>
                                <td><%= v.getId() %></td>
                                <td><span class="badge badge-primary"><%= v.getReference() %></span></td>
                                <td><%= v.getMarque() %></td>
                                <td><%= v.getModele() %></td>
                                <td><%= v.getNombrePlaces() %></td>
                                <td><%= v.getVitesseMoyenne() %> km/h</td>
                                <td><%= v.getHeureDisponibilite() != null ? dispoFormat.format(v.getHeureDisponibilite()) : "-" %></td>
                                <td><span class="badge badge-secondary"><%= v.getTypeCarburant() != null ? v.getTypeCarburant().getNom() : "-" %></span></td>
                                <td class="actions-cell">
                                    <a href="<%= request.getContextPath() %>/vehicule/edit?id=<%= v.getId() %>" class="btn btn-sm btn-edit">Modifier</a>
                                    <a href="<%= request.getContextPath() %>/vehicule/delete?id=<%= v.getId() %>" class="btn btn-sm btn-danger" 
                                       onclick="return confirm('Etes-vous sur de vouloir supprimer ce vehicule ?')">Supprimer</a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="data-card">
                <div class="empty-state">
                    <h3>Aucun vehicule enregistre</h3>
                    <p>Commencez par ajouter votre premier vehicule</p>
                </div>
            </div>
        <% } %>
        </div>
    </div>
</body>
</html>
