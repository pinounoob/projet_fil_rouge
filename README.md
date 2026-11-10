# Réveil — Ne sois pas le dernier ⏰

Application Android de réveil collectif réalisée pour le cours **8INF865 – Informatique mobile**.

## Le concept

Se lever seul, c'est facile à repousser. Se lever contre ses amis, beaucoup moins.

1. On crée un **groupe** avec ses amis et on choisit une **heure de réveil commune**.
2. Avant le réveil, chaque membre **mise** une fois de l'argent virtuel dans la cagnotte du groupe. **Sans mise, on ne joue pas** : l'alarme sonne quand même, mais on ne gagne rien et on n'apparaît pas dans le classement des gains.
3. Le matin, l'alarme sonne en même temps chez tout le monde.
4. Pour l'arrêter, il ne suffit pas d'appuyer sur un bouton : il faut **secouer son téléphone**, donc vraiment se réveiller.
5. Dès que tous les joueurs sauf un sont levés, **toute la cagnotte est partagée** selon l'ordre des réveils :
   - **le premier** récupère sa mise **et celle du dernier** ;
   - **ceux du milieu** récupèrent leur mise ;
   - **le dernier** perd sa mise.

| Joueurs (10 € chacun) | 1er | 2e | 3e | 4e |
|---|---|---|---|---|
| 2 | 20 € | 0 € | | |
| 3 | 20 € | 10 € | 0 € | |
| 4 | 20 € | 10 € | 10 € | 0 € |

Après chaque réveil, la cagnotte repart à zéro et chacun peut remiser pour le suivant.

Le but : **ne pas être le dernier**.

## Fonctionnalités

- Comptes avec pseudo unique (Firebase Authentication)
- Groupes avec heure d'alarme commune, contacts et cagnotte
- Alarme en plein écran, sonnerie ou vibreur, arrêt en secouant le téléphone
- Classement du matin et gains pour les 3 premiers
- Paramètres : thème, sonnerie, solde, compte

## Technologies

Kotlin · Jetpack Compose · MVVM · Coroutines et Flow · Firebase (Auth, Firestore) · DataStore · AlarmManager · Notifications · Accéléromètre

## Lancer le projet

1. Cloner le dépôt et l'ouvrir dans Android Studio.
2. Lancer l'application sur un téléphone ou un émulateur.
3. Accepter les notifications, qui sont nécessaires pour que l'alarme sonne.

## Équipe

- Inès
- Aliona
- Kaouthar
