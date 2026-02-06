<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Formulaire Multiple Etudiants</title>
    
    </style>
</head>
<body>
    <div class="container">
        <h2>Enregistrement Multiple d'Etudiants</h2>
        
        <form action="${pageContext.request.contextPath}/etudiant/saveMultiple" method="POST">
            <div class="etudiant-block">
                <h3>Etudiant 1</h3>
                <div class="form-group">
                    <label>ID:</label>
                    <input type="number" name="etudiants[0].id" value="1" required>
                </div>
                <div class="form-group">
                    <label>Nom:</label>
                    <input type="text" name="etudiants[0].nom" value="Dupont" required>
                </div>
                <div class="form-group">
                    <label>Prenom:</label>
                    <input type="text" name="etudiants[0].prenom" value="Jean" required>
                </div>
                <div class="form-group">
                    <label>Age:</label>
                    <input type="number" name="etudiants[0].age" value="20" required>
                </div>
            </div>
            
            <div class="etudiant-block">
                <h3>Etudiant 2</h3>
                <div class="form-group">
                    <label>ID:</label>
                    <input type="number" name="etudiants[1].id" value="2" required>
                </div>
                <div class="form-group">
                    <label>Nom:</label>
                    <input type="text" name="etudiants[1].nom" value="Martin" required>
                </div>
                <div class="form-group">
                    <label>Prenom:</label>
                    <input type="text" name="etudiants[1].prenom" value="Marie" required>
                </div>
                <div class="form-group">
                    <label>Age:</label>
                    <input type="number" name="etudiants[1].age" value="22" required>
                </div>
            </div>
            
            <div class="etudiant-block">
                <h3>Etudiant 3</h3>
                <div class="form-group">
                    <label>ID:</label>
                    <input type="number" name="etudiants[2].id" value="3" required>
                </div>
                <div class="form-group">
                    <label>Nom:</label>
                    <input type="text" name="etudiants[2].nom" value="Bernard" required>
                </div>
                <div class="form-group">
                    <label>Prenom:</label>
                    <input type="text" name="etudiants[2].prenom" value="Sophie" required>
                </div>
                <div class="form-group">
                    <label>Age:</label>
                    <input type="number" name="etudiants[2].age" value="21" required>
                </div>
            </div>
            
            <button type="submit">Enregistrer tous les etudiants</button>
        </form>
    </div>
</body>
</html>
