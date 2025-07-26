import java.util.*;

public class Battle {
    // ===== Attributes =====
    private Player player;
    private Enemy enemy;
    private Scanner scanner;
    private boolean passive = true;
    private int turn = 0;

    // ===== Constructor =====
    public Battle(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.scanner = new Scanner(System.in);
    }

    // ===== Main Battle Loop =====
    public void start() {
        System.out.println("\n--- A wild " + enemy.getName() + " appears! ---");
        
        player.resetAllCooldowns();
        enemy.resetAllCooldowns();
        player.resetHealth();
        if (passive) {
            passive = false;
        }

        while (player.isAlive() && enemy.isAlive()) {
            // Update buffs, invisibility, cooldowns, and damage/heal over time ticks
            player.updateBuffs();
            player.updateInvisibility();
            player.reduceCooldowns();
            player.dmgTick();
            if (!player.isAlive()) {
                break;
            }
            player.applyRegen();
            player.applyPassiveStart(enemy);

            enemy.updateBuffs();
            enemy.updateInvisibility();
            enemy.reduceCooldowns();
            enemy.dmgTick();
             if (!enemy.isAlive()) {
                break;
             }
             System.out.println(" ");
             
            // update turn order
            turn += 1;
            System.out.println("===== Turn " + turn + " =====");
            
            // Display stats
            player.displayStats();
            enemy.displayStats();

            // Player's turn
            if (player.shouldSkipTurn()) {
                handlePlayerSkipTurn();
            } else if (enemy.isInvisible()) {
                handleInvisibleEnemy();
            } else {
                handlePlayerAction();
            }
            
            if (player.hasExtraTurn()) {
                handlePlayerAction();
            }

            // Enemy's turn if still alive
            if (enemy.isAlive() && player.health != 0) {
                handleEnemyTurn();
            }
        }

        // End of battle result
        if (player.isAlive()) {
            System.out.println("You defeated the " + enemy.getName() + "!");
            player.gainXP(enemy.getXpReward());
        }
    }

    // ===== Player Turn Handling =====
    private void handlePlayerSkipTurn() {
        System.out.println(player.name + " got their turn skipped!");
        player.setSkipNextTurn(false);  // Only skip one turn
        System.out.println("");
        enemy.useSkill(player);
    }
    
    private void handleInvisibleEnemy() {
        System.out.println(enemy.name + " is invisible. " + player.name + " misses!");
         System.out.println("");
        enemy.useSkill(player);
    }

    private void handlePlayerAction() {
        System.out.println("\nChoose an action:");
        System.out.println("1. Normal Attack");
        System.out.println("2. Use Skill");

        int choice = scanner.nextInt();
        System.out.println("");

        if (choice == 1) {
            int dmg = player.attack;
            if (player.tempCritBoost > 0.0)
            {
                System.out.println("CRITICAL HIT");
                dmg *= 1.5;
                if (player.critBoostTurns == 0)
                {
                    dmg = player.attack;
                }
            }
            enemy.takeDamage(dmg);

        } else if (choice == 2) {
            List<Skills> skills = player.getSkills();
            for (int i = 0; i < skills.size(); i++) {
                Skills skill = skills.get(i);
                if (!skill.isReady()) {
                    System.out.println((i + 1) + ". " + skill.getName() + " (Cooldown: " + skill.currentCooldown() + ")");
                } else {
                    System.out.println((i + 1) + ". " + skill.getName());
                }
            }

            int skillChoice = scanner.nextInt() - 1;

            if (skillChoice >= 0 && skillChoice < skills.size()) {
                Skills selected = skills.get(skillChoice);
                if (!selected.isReady()) {
                    System.out.println("That skill is still on cooldown!");
                    System.out.println("");
                } else {
                    player.useSkill(skillChoice, enemy);
                    System.out.println("");
                }
            } else {
                System.out.println("Invalid skill choice");
            }
        }
    }

    // ===== Enemy Turn Handling =====
    private void handleEnemyTurn() {
        System.out.println(enemy.getName() + " attacks!");
        
        if (player.isInvisible()) {
            System.out.println(player.name + " is invisible! Enemy misses!");
            System.out.println("");
            player.applyPassiveEnd(enemy);

        } else if (enemy.checkIfStunned()) {
            System.out.println(enemy.getName() + " got their turn skippped");
            enemy.applyStun(false);
            System.out.println("");
            player.applyPassiveEnd(enemy);

        } else if (player.isReflecting()) {
            System.out.println(player.name + " reflects the attack");
            int amt = enemy.attack + 10;
            enemy.takeDamage(amt);
            player.reflector = false;
            System.out.println("");
            player.applyPassiveEnd(enemy);
        } else {
            enemy.useSkill(player);
            System.out.println("");
            player.applyPassiveEnd(enemy);
        }
        
        if (player.health < 0)
        {
            player.health = 0;
        }
    }
}
