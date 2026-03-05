<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<style>
    /* === THEME PROFESSIONNEL CLAIR === */
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
        --border-dark: #374151;
        --success: #059669;
        --danger: #dc2626;
    }

    .sidebar {
        position: fixed;
        left: 0;
        top: 0;
        width: 260px;
        height: 100vh;
        background: var(--bg-secondary);
        padding: 20px 0;
        box-shadow: 2px 0 15px rgba(0, 0, 0, 0.15);
        overflow-y: auto;
        z-index: 1000;
    }

    .sidebar-header {
        padding: 0 20px 20px;
        border-bottom: 1px solid var(--bg-tertiary);
        margin-bottom: 20px;
    }

    .sidebar-header h2 {
        color: var(--text-light);
        font-size: 1.4em;
        margin-bottom: 5px;
        font-weight: 700;
    }

    .sidebar-header p {
        color: #9ca3af;
        font-size: 0.85em;
    }

    .sidebar-menu {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .sidebar-menu li {
        margin-bottom: 2px;
    }

    .sidebar-menu a {
        display: flex;
        align-items: center;
        padding: 12px 20px;
        color: #d1d5db;
        text-decoration: none;
        transition: all 0.2s ease;
        font-size: 0.95em;
        border-left: 3px solid transparent;
    }

    .sidebar-menu a:hover {
        background-color: var(--bg-tertiary);
        color: var(--text-light);
        border-left-color: var(--text-light);
        padding-left: 25px;
    }

    .sidebar-menu a.active {
        background-color: var(--bg-tertiary);
        border-left-color: var(--text-light);
        color: var(--text-light);
        font-weight: 600;
    }

    .sidebar-menu a i {
        margin-right: 12px;
        font-size: 1.1em;
        width: 20px;
        text-align: center;
    }

    .menu-section {
        padding: 15px 20px 5px;
        color: #9ca3af;
        font-size: 0.75em;
        text-transform: uppercase;
        letter-spacing: 1px;
        font-weight: 600;
    }

    /* Adaptation pour les contenus avec sidebar */
    .content-with-sidebar {
        margin-left: 260px;
        min-height: 100vh;
        padding: 30px;
        background: #ffffff;
    }

    @media (max-width: 768px) {
        .sidebar {
            width: 0;
            overflow: hidden;
        }

        .sidebar.mobile-open {
            width: 260px;
        }

        .content-with-sidebar {
            margin-left: 0;
        }

        .mobile-toggle {
            display: block;
            position: fixed;
            top: 10px;
            left: 10px;
            z-index: 1001;
            background: #2c3e50;
            color: white;
            border: none;
            padding: 10px 15px;
            border-radius: 5px;
            cursor: pointer;
        }
    }

    .mobile-toggle {
        display: none;
    }
</style>

<button class="mobile-toggle" onclick="toggleSidebar()">&#9776; Menu</button>

<div class="sidebar" id="sidebar">
    <div class="sidebar-header">
        <h2>🚐 Tour Operator</h2>
        <p>Gestion & Simulation</p>
    </div>

    <div class="menu-section">Réservations</div>
    <ul class="sidebar-menu">
        <li>
            <a href="${pageContext.request.contextPath}/reservation/form">
                <i>➕</i> Nouvelle réservation
            </a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/reservation/par-date/form">
                <i>📅</i> Simulation par date
            </a>
        </li>
    </ul>

    <div class="menu-section">Véhicules</div>
    <ul class="sidebar-menu">
        <li>
            <a href="${pageContext.request.contextPath}/vehicule/form">
                <i>🚗</i> Nouveau véhicule
            </a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/vehicule/list">
                <i>🚙</i> Liste des véhicules
            </a>
        </li>
    </ul>

    <div class="menu-section">Paramètres</div>
    <ul class="sidebar-menu">
        <li>
            <a href="${pageContext.request.contextPath}/parametre/form">
                <i>⚙️</i> Configuration
            </a>
        </li>
    </ul>

    <div class="menu-section">SIMULATION</div>
    <ul class="sidebar-menu">
        <li>
            <a href="${pageContext.request.contextPath}/simulation/assignement">
                <i>🎯</i> Simulation d'assignement
            </a>
        </li>
    </ul>
</div>

<script>
    function toggleSidebar() {
        document.getElementById('sidebar').classList.toggle('mobile-open');
    }

    document.querySelectorAll('.sidebar-menu a').forEach(function(link) {
        link.addEventListener('click', function() {
            if (window.innerWidth <= 768) {
                document.getElementById('sidebar').classList.remove('mobile-open');
            }
        });
    });

    // Marquer l'élément actif
    document.addEventListener('DOMContentLoaded', () => {
        const currentPath = window.location.pathname;
        document.querySelectorAll('.sidebar-menu a').forEach(link => {
            if (link.getAttribute('href') && currentPath.includes(link.getAttribute('href'))) {
                link.classList.add('active');
            }
        });
    });
</script>
