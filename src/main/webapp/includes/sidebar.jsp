<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<style>
    .sidebar {
        position: fixed;
        left: 0;
        top: 0;
        width: 260px;
        height: 100vh;
        background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
        padding: 20px 0;
        box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        z-index: 1000;
    }

    .sidebar-header {
        padding: 0 20px 20px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        margin-bottom: 20px;
    }

    .sidebar-header h2 {
        color: #fff;
        font-size: 1.4em;
        margin-bottom: 5px;
    }

    .sidebar-header p {
        color: #bdc3c7;
        font-size: 0.85em;
    }

    .sidebar-menu {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .sidebar-menu li {
        margin-bottom: 5px;
    }

    .sidebar-menu a {
        display: flex;
        align-items: center;
        padding: 12px 20px;
        color: #ecf0f1;
        text-decoration: none;
        transition: all 0.3s ease;
        font-size: 0.95em;
        border-left: 3px solid transparent;
    }

    .sidebar-menu a:hover {
        background-color: rgba(255, 255, 255, 0.1);
        border-left-color: #3498db;
        padding-left: 25px;
    }

    .sidebar-menu a.active {
        background-color: rgba(52, 152, 219, 0.2);
        border-left-color: #3498db;
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
        color: #95a5a6;
        font-size: 0.75em;
        text-transform: uppercase;
        letter-spacing: 1px;
        font-weight: 600;
    }

    /* Adaptation pour les contenus avec sidebar */
    .content-with-sidebar {
        margin-left: 260px;
        min-height: 100vh;
        padding: 20px;
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
        <h2>&#128656; Tour Operator</h2>
        <p>Gestion &amp; Simulation</p>
    </div>

    <div class="menu-section">RESERVATIONS</div>
    <ul class="sidebar-menu">
        <li>
            <a href="<%= request.getContextPath() %>/reservation/form">
                <i>&#10133;</i> Nouvelle reservation
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/reservation/par-date/form">
                <i>&#128197;</i> Simulation par date
            </a>
        </li>
    </ul>

    <div class="menu-section">VEHICULES</div>
    <ul class="sidebar-menu">
        <li>
            <a href="<%= request.getContextPath() %>/vehicule/form">
                <i>&#128663;</i> Nouveau vehicule
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/vehicule/list">
                <i>&#128665;</i> Liste des vehicules
            </a>
        </li>
    </ul>

    <div class="menu-section">PARAMETRES</div>
    <ul class="sidebar-menu">
        <li>
            <a href="<%= request.getContextPath() %>/parametre/form">
                <i>&#9881;</i> Configuration
            </a>
        </li>
    </ul>

    <div class="menu-section">SIMULATION</div>
    <ul class="sidebar-menu">
        <li>
            <a href="<%= request.getContextPath() %>/simulation/assignement">
                <i>&#127919;</i> Simulation d'assignement
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

    document.addEventListener('DOMContentLoaded', function() {
        var currentPath = window.location.pathname;
        document.querySelectorAll('.sidebar-menu a').forEach(function(link) {
            var href = link.getAttribute('href');
            if (href && currentPath.indexOf(href) !== -1) {
                link.classList.add('active');
            }
        });
    });
</script>
