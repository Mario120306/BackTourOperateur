<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="itu.back.model.Reservation" %>
<%@ page import="itu.back.model.Client" %>
<%@ page import="itu.back.model.Hotel" %>
<%@ page import="itu.back.model.Vehicule" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="itu.back.util.SimulationService" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simulation par Date - Tour Operateur</title>
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
            --accent-hover: #2563eb;
            --success: #10b981;
            --success-light: #d1fae5;
            --warning: #f59e0b;
            --warning-light: #fef3c7;
            --danger: #ef4444;
            --text-primary: #0f172a;
            --text-secondary: #64748b;
            --text-light: #ffffff;
            --bg-primary: #f8fafc;
            --bg-card: #ffffff;
            --border: #e2e8f0;
            --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
            --shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
            --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
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
            display: flex;
            min-height: 100vh;
        }

        .main-content {
            flex: 1;
            padding: 32px 40px;
        }

        .content-wrapper {
            max-width: 100%;
            margin: 0 auto;
        }

        @media (max-width: 1024px) {
            .main-content {
                margin-left: 0;
                padding: 24px;
            }
        }

        /* Page Header */
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

        /* Search Form */
        .search-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 24px;
            margin-bottom: 24px;
            box-shadow: var(--shadow-sm);
        }

        .search-form {
            display: flex;
            gap: 16px;
            align-items: flex-end;
            flex-wrap: wrap;
        }

        .form-group {
            flex: 1;
            min-width: 200px;
            max-width: 300px;
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
            transition: all 0.2s;
            background: var(--bg-card);
        }

        .form-input:focus {
            outline: none;
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
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
            transition: all 0.2s;
        }

        .btn-primary {
            background: var(--primary);
            color: var(--text-light);
        }

        .btn-primary:hover {
            background: var(--primary-light);
        }

        .btn-success {
            background: var(--success);
            color: var(--text-light);
        }

        .btn-success:hover {
            background: #059669;
        }

        .btn-warning {
            background: var(--warning);
            color: var(--text-light);
        }

        .btn-warning:hover {
            background: #d97706;
        }

        .btn:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        /* Results Header */
        .results-header {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 20px 24px;
            margin-bottom: 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
            box-shadow: var(--shadow-sm);
        }

        .results-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-primary);
        }

        .stats-group {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
        }

        .stat-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 0.813rem;
            font-weight: 500;
        }

        .stat-badge.primary {
            background: var(--primary);
            color: var(--text-light);
        }

        .stat-badge.success {
            background: var(--success-light);
            color: #065f46;
        }

        .stat-badge.warning {
            background: var(--warning-light);
            color: #92400e;
        }

        /* Action Buttons */
        .action-bar {
            display: flex;
            gap: 12px;
            margin-bottom: 24px;
            flex-wrap: wrap;
        }

        /* Alert Box */
        .alert {
            border-radius: var(--radius-lg);
            padding: 20px 24px;
            margin-bottom: 24px;
            border-left: 4px solid;
        }

        .alert-warning {
            background: var(--warning-light);
            border-color: var(--warning);
        }

        .alert-title {
            font-size: 1rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .alert-text {
            font-size: 0.875rem;
            color: var(--text-secondary);
            margin-bottom: 16px;
        }

        .unassigned-list {
            list-style: none;
        }

        .unassigned-item {
            background: var(--bg-card);
            padding: 14px 16px;
            border-radius: var(--radius);
            margin-bottom: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border: 1px solid var(--border);
        }

        .unassigned-item:last-child {
            margin-bottom: 0;
        }

        .unassigned-info strong {
            display: block;
            font-size: 0.875rem;
            color: var(--text-primary);
            margin-bottom: 2px;
        }

        .unassigned-info span {
            font-size: 0.813rem;
            color: var(--text-secondary);
        }

        .passenger-badge {
            background: var(--warning-light);
            color: #92400e;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 0.813rem;
            font-weight: 500;
        }

        /* Vehicle Card */
        .vehicle-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            margin-bottom: 24px;
            overflow: hidden;
            box-shadow: var(--shadow-sm);
        }

        .vehicle-header {
            padding: 24px;
            border-bottom: 1px solid var(--border);
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 16px;
            flex-wrap: wrap;
        }

        .vehicle-title {
            font-size: 1.125rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .vehicle-ref {
            font-weight: 400;
            color: var(--text-secondary);
            font-size: 0.875rem;
        }

        .vehicle-specs {
            display: flex;
            gap: 20px;
            flex-wrap: wrap;
        }

        .spec-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 0.875rem;
            color: var(--text-secondary);
        }

        .spec-icon {
            width: 16px;
            height: 16px;
            opacity: 0.7;
        }

        .reservation-count {
            background: var(--bg-primary);
            padding: 8px 16px;
            border-radius: var(--radius);
            font-size: 0.875rem;
            font-weight: 500;
            color: var(--text-primary);
        }

        /* Timing Section */
        .timing-section {
            padding: 20px 24px;
            background: var(--bg-primary);
            display: flex;
            gap: 32px;
            flex-wrap: wrap;
            border-bottom: 1px solid var(--border);
        }

        .timing-item {
            text-align: center;
        }

        .timing-label {
            font-size: 0.75rem;
            font-weight: 500;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
        }

        .timing-value {
            font-size: 1.25rem;
            font-weight: 600;
        }

        .timing-value.depart {
            color: var(--primary);
        }

        .timing-value.retour {
            color: var(--success);
        }

        .timing-value.duree {
            color: var(--warning);
        }

        /* Route Details */
        .route-section {
            padding: 24px;
            border-bottom: 1px solid var(--border);
        }

        .route-title {
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 16px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .route-segments {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .route-segment {
            background: var(--bg-primary);
            padding: 16px;
            border-radius: var(--radius);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }

        .segment-route {
            display: flex;
            align-items: center;
            gap: 12px;
            flex: 1;
            min-width: 250px;
        }

        .segment-point {
            font-weight: 500;
            color: var(--text-primary);
            font-size: 0.875rem;
        }

        .segment-arrow {
            color: var(--accent);
            font-weight: 600;
        }

        .segment-metrics {
            display: flex;
            gap: 12px;
        }

        .metric-badge {
            padding: 4px 10px;
            border-radius: var(--radius);
            font-size: 0.813rem;
            font-weight: 500;
        }

        .metric-badge.distance {
            background: #dbeafe;
            color: #1e40af;
        }

        .metric-badge.duration {
            background: #fef3c7;
            color: #92400e;
        }

        /* Reservations Table */
        .table-section {
            padding: 0;
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

        .cell-primary {
            font-weight: 500;
            color: var(--text-primary);
        }

        .cell-secondary {
            font-size: 0.813rem;
            color: var(--text-secondary);
            margin-top: 2px;
        }

        .passengers-badge {
            display: inline-flex;
            align-items: center;
            background: var(--primary);
            color: var(--text-light);
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 0.813rem;
            font-weight: 500;
        }

        /* Empty State */
        .empty-state {
            text-align: center;
            padding: 60px 24px;
            color: var(--text-secondary);
        }

        .empty-state svg {
            width: 64px;
            height: 64px;
            margin-bottom: 16px;
            opacity: 0.5;
        }

        .empty-state h3 {
            font-size: 1.125rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .empty-state p {
            font-size: 0.875rem;
        }

        /* No Results Card */
        .no-results-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 80px 40px;
            text-align: center;
            box-shadow: var(--shadow-sm);
        }

        .no-results-card svg {
            width: 72px;
            height: 72px;
            color: var(--text-secondary);
            margin-bottom: 24px;
            opacity: 0.5;
        }

        .no-results-card h3 {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .no-results-card p {
            font-size: 0.875rem;
            color: var(--text-secondary);
        }

        /* Back Link */
        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            margin-top: 32px;
            color: var(--text-secondary);
            text-decoration: none;
            font-size: 0.875rem;
            font-weight: 500;
            transition: color 0.2s;
        }

        .back-link:hover {
            color: var(--text-primary);
        }

        /* Responsive */
        @media (max-width: 768px) {
            .results-header {
                flex-direction: column;
                align-items: flex-start;
            }

            .vehicle-header {
                flex-direction: column;
            }

            .timing-section {
                justify-content: space-between;
            }

            .route-segment {
                flex-direction: column;
                align-items: flex-start;
            }

            .data-table th,
            .data-table td {
                padding: 12px 16px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.jsp" %>
    
    <div class="content-with-sidebar">
        <div class="main-content">
            <div class="content-wrapper">
                <div class="page-header">
                    <h1 style="font-size: 100px;">003229 - 003208 - 003234</h1>
                    <h1 class="page-title">Simulation par Date</h1>
                    <p class="page-subtitle">Visualisez l'assignation des vehicules aux reservations pour une date donnee</p>
                </div>

                <div class="search-card">
                    <form class="search-form" method="get" action="<%= request.getContextPath() %>/reservation/par-date">
                        <div class="form-group">
                            <label class="form-label" for="date">Date de simulation</label>
                            <input type="date" id="date" name="date" class="form-input"
                                   value="<%= request.getAttribute("dateRecherche") != null ? request.getAttribute("dateRecherche") : "" %>" 
                                   required>
                        </div>
                        <button type="submit" class="btn btn-primary">Lancer la simulation</button>
                    </form>
                </div>

            <% 
            String dateRecherche = (String) request.getAttribute("dateRecherche");
            Map<Vehicule, List<Reservation>> vehiculesAvecReservations = (Map<Vehicule, List<Reservation>>) request.getAttribute("vehiculesAvecReservations");
            Map<Vehicule, List<SimulationService.InfosTrajet>> infosTrajetParVehicule = (Map<Vehicule, List<SimulationService.InfosTrajet>>) request.getAttribute("infosTrajetParVehicule");
            List<Reservation> reservationsNonAssignees = (List<Reservation>) request.getAttribute("reservationsNonAssignees");
            Map<Reservation, java.sql.Timestamp> heureDepartParReservation = (Map<Reservation, java.sql.Timestamp>) request.getAttribute("heureDepartParReservation");
            Integer nombreVehicules = (Integer) request.getAttribute("nombreVehicules");
            Integer nombreReservationsTotal = (Integer) request.getAttribute("nombreReservationsTotal");
            Integer nombreReservationsAssignees = (Integer) request.getAttribute("nombreReservationsAssignees");
            Integer nombreReservationsNonAssignees = (Integer) request.getAttribute("nombreReservationsNonAssignees");
            Integer tempsAttente = (Integer) request.getAttribute("tempsAttente");
            
            if (dateRecherche != null && !dateRecherche.isEmpty()) {
            %>
                <div class="results-header">
                    <h2 class="results-title">Simulation du <%= dateRecherche %></h2>
                    <div class="stats-group">
                        <span class="stat-badge primary"><%= nombreVehicules != null ? nombreVehicules : 0 %> vehicule(s)</span>
                        <span class="stat-badge success"><%= nombreReservationsAssignees != null ? nombreReservationsAssignees : 0 %> / <%= nombreReservationsTotal != null ? nombreReservationsTotal : 0 %> assignee(s)</span>
                        <% if (nombreReservationsNonAssignees != null && nombreReservationsNonAssignees > 0) { %>
                            <span class="stat-badge warning"><%= nombreReservationsNonAssignees %> non assignee(s)</span>
                        <% } %>
                        <% if (tempsAttente != null && tempsAttente > 0) { %>
                            <span class="stat-badge primary">Temps d'attente : <%= tempsAttente %> min</span>
                        <% } %>
                    </div>
                </div>

                <div class="action-bar">
                    <button type="button" class="btn btn-success" onclick="enregistrerSimulation('<%= dateRecherche %>')">
                        Enregistrer la simulation
                    </button>
                    <button type="button" class="btn btn-warning" onclick="reinitialiserSimulation('<%= dateRecherche %>')">
                        Reinitialiser les assignations
                    </button>
                </div>

                <% if (reservationsNonAssignees != null && !reservationsNonAssignees.isEmpty()) { %>
                    <div class="alert alert-warning">
                        <h3 class="alert-title">Reservations non assignees</h3>
                        <p class="alert-text"><strong><%= reservationsNonAssignees.size() %> reservation(s)</strong> n'ont pas pu etre assignees a un vehicule (capacite insuffisante).</p>
                        <ul class="unassigned-list">
                            <% 
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                            for (Reservation r : reservationsNonAssignees) { 
                                Client client = r.getClient();
                                Hotel hotel = r.getHotel();
                            %>
                                <li class="unassigned-item">
                                    <div class="unassigned-info">
                                        <strong><%= client != null ? client.getPrenom() + " " + client.getNom() : "Client inconnu" %></strong>
                                        <span><%= hotel != null ? hotel.getNom() + " (" + hotel.getVille() + ")" : "Hotel inconnu" %> - Arrivee: <%= r.getDateHeureArrive() != null ? timeFormat.format(r.getDateHeureArrive()) : "?" %></span>
                                    </div>
                                    <span class="passenger-badge"><%= r.getNombrePassage() %> pers.</span>
                                </li>
                            <% } %>
                        </ul>
                    </div>
                <% } %>

                <% 
                if (vehiculesAvecReservations != null && !vehiculesAvecReservations.isEmpty()) {
                    for (Map.Entry<Vehicule, List<Reservation>> entry : vehiculesAvecReservations.entrySet()) {
                        Vehicule vehicule = entry.getKey();
                        List<Reservation> reservations = entry.getValue();
                %>
                    <div class="vehicle-card">
                        <div class="vehicle-header">
                            <div>
                                <h3 class="vehicle-title">
                                    <%= vehicule.getMarque() %> <%= vehicule.getModele() %>
                                    <span class="vehicle-ref">(Ref: <%= vehicule.getReference() %>)</span>
                                </h3>
                                <div class="vehicle-specs">
                                    <span class="spec-item">
                                        <svg class="spec-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path></svg>
                                        <%= vehicule.getNombrePlaces() %> places
                                    </span>
                                    <% if (vehicule.getTypeCarburant() != null) { %>
                                        <span class="spec-item">
                                            <svg class="spec-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                                            <%= vehicule.getTypeCarburant().getNom() %>
                                        </span>
                                    <% } %>
                                    <span class="spec-item">
                                        <svg class="spec-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                                        <%= vehicule.getVitesseMoyenne() %> km/h
                                    </span>
                                </div>
                            </div>
                            <div class="reservation-count">
                                <%
                                    int totalPassagersVehicule = 0;
                                    for (Reservation r : reservations) {
                                        totalPassagersVehicule += r.getNombrePassage();
                                    }
                                %>
                                <%= reservations.size() %> reservation(s) · <%= totalPassagersVehicule %> passager(s)
                            </div>
                        </div>

                        <% 
                        List<SimulationService.InfosTrajet> listeTrajets = (infosTrajetParVehicule != null) ? infosTrajetParVehicule.get(vehicule) : null;
                        if (listeTrajets != null && !listeTrajets.isEmpty() && !reservations.isEmpty()) { 
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                            int numTrajet = 0;
                            for (SimulationService.InfosTrajet infosTrajet : listeTrajets) {
                                numTrajet++;
                        %>
                            <% if (listeTrajets.size() > 1) { %>
                                <h4 style="margin: 16px 24px 0; color: var(--text-secondary); font-size: 0.85rem;">Trajet #<%= numTrajet %></h4>
                            <% } %>
                            <div class="timing-section">
                                <div class="timing-item">
                                    <div class="timing-label">Depart Aeroport</div>
                                    <div class="timing-value depart"><%= timeFormat.format(infosTrajet.getHeureDepart()) %></div>
                                </div>
                                <div class="timing-item">
                                    <div class="timing-label">Retour Aeroport</div>
                                    <div class="timing-value retour"><%= timeFormat.format(infosTrajet.getHeureRetour()) %></div>
                                </div>
                                <div class="timing-item">
                                    <div class="timing-label">Duree Totale</div>
                                    <div class="timing-value duree"><% 
                                        int dureeMin = infosTrajet.getDureeTrajetMinutes();
                                        if (dureeMin < 60) {
                                            out.print(dureeMin + " min");
                                        } else {
                                            int heures = dureeMin / 60;
                                            int minutes = dureeMin % 60;
                                            if (minutes > 0) {
                                                out.print(heures + "h" + String.format("%02d", minutes));
                                            } else {
                                                out.print(heures + "h00");
                                            }
                                        }
                                    %></div>
                                </div>
                            </div>

                            <% if (infosTrajet.getSegments() != null && !infosTrajet.getSegments().isEmpty()) { %>
                                <div class="route-section">
                                    <h4 class="route-title">Details du trajet</h4>
                                    <div class="route-segments">
                                        <% for (int i = 0; i < infosTrajet.getSegments().size(); i++) {
                                            SimulationService.SegmentTrajet segment = infosTrajet.getSegments().get(i);
                                        %>
                                            <div class="route-segment">
                                                <div class="segment-route">
                                                    <span class="segment-point"><%= segment.getOrigine() %></span>
                                                    <span class="segment-arrow">→</span>
                                                    <span class="segment-point"><%= segment.getDestination() %></span>
                                                </div>
                                                <div class="segment-metrics">
                                                    <span class="metric-badge distance"><%= String.format("%.1f", segment.getDistanceKm()) %> km</span>
                                                    <span class="metric-badge duration"><%= segment.getDureeMinutes() %> min</span>
                                                </div>
                                            </div>
                                        <% } %>
                                    </div>
                                </div>
                            <% } %>
                        <% 
                            } // end for each trajet
                        } %>

                        <% if (!reservations.isEmpty()) { %>
                            <div class="table-section">
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>Client</th>
                                            <th>Hotel</th>
                                            <th>Ville</th>
                                            <th>Arrivee Vol</th>
                                            <th>Depart vehicule</th>
                                            <th>Passagers assignes</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% 
                                        SimpleDateFormat timeFormat2 = new SimpleDateFormat("HH:mm");
                                        for (Reservation r : reservations) { 
                                            Client client = r.getClient();
                                            Hotel hotel = r.getHotel();
                                        %>
                                        <tr>
                                            <td>
                                                <div class="cell-primary"><%= client != null ? client.getPrenom() + " " + client.getNom() : "-" %></div>
                                                <div class="cell-secondary"><%= client != null ? client.getEmail() : "" %></div>
                                            </td>
                                            <td>
                                                <div class="cell-primary"><%= hotel != null ? hotel.getNom() : "-" %></div>
                                            </td>
                                            <td>
                                                <div class="cell-secondary"><%= hotel != null ? hotel.getVille() : "-" %></div>
                                            </td>
                                            <td>
                                                <div class="cell-primary"><%= r.getDateHeureArrive() != null ? timeFormat2.format(r.getDateHeureArrive()) : "-" %></div>
                                            </td>
                                            <td>
                                                <%
                                                    java.sql.Timestamp hDepart = (heureDepartParReservation != null) ? heureDepartParReservation.get(r) : null;
                                                %>
                                                <div class="cell-primary"><%= hDepart != null ? timeFormat2.format(hDepart) : "-" %></div>
                                            </td>
                                            <td>
                                                <span class="passengers-badge"><%= r.getNombrePassage() %> pers.</span>
                                            </td>
                                        </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } else { %>
                            <div class="empty-state">
                                Aucune reservation assignee a ce vehicule
                            </div>
                        <% } %>
                    </div>
                <% 
                    } 
                } else { 
                %>
                    <div class="no-results-card">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <h3>Aucun vehicule assigne</h3>
                        <p>Aucun vehicule n'a ete assigne pour le <%= dateRecherche %></p>
                    </div>
                <% } %>
            <% } else { %>
                <div class="no-results-card">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <h3>Selectionnez une date</h3>
                    <p>Choisissez une date ci-dessus pour lancer la simulation d'assignation des vehicules</p>
                </div>
            <% } %>

                <a href="<%= request.getContextPath() %>/reservation/form" class="back-link">
                    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
                    Retour au formulaire de reservation
                </a>
            </div>
        </div>
    </div>

<script>
    function enregistrerSimulation(date) {
        if (!confirm('Voulez-vous vraiment enregistrer cette simulation ?\nCela assignera les vehicules aux reservations.')) {
            return;
        }
        
        var btn = event.target;
        btn.disabled = true;
        btn.innerHTML = 'Enregistrement...';
        
        fetch('<%= request.getContextPath() %>/simulation/enregistrer?date=' + date, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (data.success || data.data) {
                var result = data.data || data;
                alert('Succes: ' + result.message + '\n\n' + 
                      'Assignations enregistrees: ' + result.nbAssignations + '\n' +
                      'Non assignees: ' + result.nbNonAssignees);
                location.reload();
            } else {
                alert('Erreur: ' + (data.message || 'Erreur inconnue'));
                btn.disabled = false;
                btn.innerHTML = 'Enregistrer la simulation';
            }
        })
        .catch(function(error) {
            alert('Erreur de connexion: ' + error.message);
            btn.disabled = false;
            btn.innerHTML = 'Enregistrer la simulation';
        });
    }
    
    function reinitialiserSimulation(date) {
        if (!confirm('Voulez-vous vraiment reinitialiser les assignations pour cette date ?\nTous les vehicules seront desassignes.')) {
            return;
        }
        
        var btn = event.target;
        btn.disabled = true;
        btn.innerHTML = 'Reinitialisation...';
        
        fetch('<%= request.getContextPath() %>/simulation/reinitialiser?date=' + date, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (data.success || data.data) {
                var result = data.data || data;
                alert('Succes: ' + result.message + '\n\nReservations modifiees: ' + result.nbModifiees);
                location.reload();
            } else {
                alert('Erreur: ' + (data.message || 'Erreur inconnue'));
                btn.disabled = false;
                btn.innerHTML = 'Reinitialiser les assignations';
            }
        })
        .catch(function(error) {
            alert('Erreur de connexion: ' + error.message);
            btn.disabled = false;
            btn.innerHTML = 'Reinitialiser les assignations';
        });
    }
</script>
</body>
</html>
