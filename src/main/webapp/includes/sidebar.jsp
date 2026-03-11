<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
<style>
    :root {
        --primary: #0f172a;
        --primary-light: #1e293b;
        --primary-hover: #334155;
        --accent: #3b82f6;
        --text-light: #ffffff;
        --text-muted: #94a3b8;
        --border-dark: #334155;
    }

    .sidebar {
        position: fixed;
        left: 0;
        top: 0;
        width: 280px;
        height: 100vh;
        background: var(--primary);
        padding: 0;
        overflow-y: auto;
        z-index: 1000;
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    .sidebar-header {
        padding: 24px 24px 20px;
        border-bottom: 1px solid var(--border-dark);
    }

    .sidebar-logo {
        font-size: 1.25rem;
        font-weight: 700;
        color: var(--text-light);
        letter-spacing: -0.5px;
    }

    .sidebar-subtitle {
        color: var(--text-muted);
        font-size: 0.813rem;
        margin-top: 4px;
    }

    .menu-section {
        padding: 20px 24px 8px;
        color: var(--text-muted);
        font-size: 0.688rem;
        text-transform: uppercase;
        letter-spacing: 1px;
        font-weight: 600;
    }

    .sidebar-menu {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .sidebar-menu li {
        margin: 2px 8px;
    }

    .sidebar-menu a {
        display: block;
        padding: 10px 16px;
        color: var(--text-muted);
        text-decoration: none;
        font-size: 0.875rem;
        font-weight: 500;
        border-radius: 6px;
        transition: all 0.15s ease;
    }

    .sidebar-menu a:hover {
        background: var(--primary-hover);
        color: var(--text-light);
    }

    .sidebar-menu a.active {
        background: var(--accent);
        color: var(--text-light);
    }

    .content-with-sidebar {
        margin-left: 280px;
        min-height: 100vh;
        background: #f8fafc;
        padding: 40px;
    }

    @media (max-width: 1024px) {
        .sidebar {
            transform: translateX(-100%);
            transition: transform 0.3s ease;
        }

        .sidebar.mobile-open {
            transform: translateX(0);
        }

        .content-with-sidebar {
            margin-left: 0;
            padding: 80px 20px 20px;
        }

        .mobile-toggle {
            display: flex;
            align-items: center;
            justify-content: center;
            position: fixed;
            top: 16px;
            left: 16px;
            z-index: 1001;
            background: var(--primary);
            color: var(--text-light);
            border: none;
            width: 40px;
            height: 40px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 1.25rem;
        }
    }

    .mobile-toggle {
        display: none;
    }
</style>

<button class="mobile-toggle" onclick="toggleSidebar()">&#9776;</button>

<div class="sidebar" id="sidebar">
    <div class="sidebar-header">
        <div class="sidebar-logo">Tour Operateur</div>
        <div class="sidebar-subtitle">Gestion des transferts</div>
    </div>

    <div class="menu-section">Reservations</div>
    <ul class="sidebar-menu">
        <li>
            <a href="${pageContext.request.contextPath}/reservation/form">Nouvelle reservation</a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/reservation/par-date/form">Simulation par date</a>
        </li>
    </ul>

    <div class="menu-section">Vehicules</div>
    <ul class="sidebar-menu">
        <li>
            <a href="${pageContext.request.contextPath}/vehicule/list">Liste des vehicules</a>
        </li>
    </ul>

    <div class="menu-section">Parametres</div>
    <ul class="sidebar-menu">
        <li>
            <a href="${pageContext.request.contextPath}/parametre/form">Configuration</a>
        </li>
    </ul>
</div>

<script>
    function toggleSidebar() {
        document.getElementById('sidebar').classList.toggle('mobile-open');
    }

    document.querySelectorAll('.sidebar-menu a').forEach(function(link) {
        link.addEventListener('click', function() {
            if (window.innerWidth <= 1024) {
                document.getElementById('sidebar').classList.remove('mobile-open');
            }
        });
    });

    document.addEventListener('DOMContentLoaded', function() {
        var currentPath = window.location.pathname;
        document.querySelectorAll('.sidebar-menu a').forEach(function(link) {
            var href = link.getAttribute('href');
            if (href && currentPath.includes(href)) {
                link.classList.add('active');
            }
        });
    });
</script>
