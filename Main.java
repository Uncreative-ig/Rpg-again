import java.util.Scanner;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Welcome to the RPG Adventure ===");

        System.out.print("Enter your character name: ");
        String name = scanner.nextLine();

        System.out.println("Choose your class:");
        System.out.println("1. Warrior\n2. Wizard\n3. Bandit\n4. Chronomancer\n5. Alchemist");
        int classChoice = scanner.nextInt();
        scanner.nextLine(); // Consume leftover newline

        String classType = "Warrior";

        if (classChoice == 2) {
            classType = "Wizard";
        } else if (classChoice == 3) {
            classType = "Bandit";
        } else if (classChoice == 4) {
            classType = "Chronomancer";
        } else if (classChoice == 5) {
            classType = "Alchemist";
        }

        Player player = new Player(name, classType);

        // Assign skills using SkillManager
        List<Skills> playerSkills = SkillManager.getSkillsFor(classType, 1);

        // Get enemies from Enemy class
        Enemy[] enemies = Enemy.getDefaultEnemies();
        

        // Begin the Exploration system
        Exploration explore = new Exploration(player, enemies);
        explore.startExplore();

        System.out.println("\n=== Game Over ===");
        player.displayStats();

        scanner.close();
    }
}
