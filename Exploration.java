import java.util.Random;
import java.util.Scanner;

public class Exploration {
    private Player player;
    private Enemy[] enemies;
    private Random random;
    private Scanner scanner;

    public Exploration(Player player, Enemy[] enemies) {
        this.player = player;
        this.enemies = enemies;
        this.random = new Random();
        this.scanner = new Scanner(System.in);
    }

    public void startExplore() {
        System.out.println("\nA new adventure starts....");
        boolean exploring = true;

        while (exploring && player.isAlive()) {
            System.out.println("\nWhat do you want to do?");
            System.out.println("1. Explore");
            System.out.println("2. Check Stats");
            System.out.println("3. Quit Exploring");

            int choice = scanner.nextInt();

            if (choice == 1) {
                int eventChance = random.nextInt(126);

                if (eventChance <= 50) {
                    Enemy enemy = getRandomEnemy();
                    System.out.println("You encountered a " + enemy.getName() + "!");
                    Battle battle = new Battle(player, enemy);
                    battle.start();

                    if (!player.isAlive()) {
                        System.out.println("You have been defeated... (dumbass)");
                        exploring = false;
                    }
                } else {
                    if (eventChance <= 75) {
                        badEvent();
                    } 
                    else if (eventChance <= 100){
                        goodEvent();
                    }
                    else 
                    {
                        quickTime();
                    }
                }

            } else if (choice == 2) {
                player.displayStats();

            } else if (choice == 3) {
                System.out.println("You stop exploring... Which is the end of the game...");
                exploring = false;

            } else {
                System.out.println("Invalid input. Try again.");
            }
        }
    }

    private Enemy getRandomEnemy() {
        if (player.getLevel() == 2)
        {
            this.enemies = Enemy.getLvl2Enemies();
        }
        else if(player.getLevel() == 3)
        {
            this.enemies = Enemy.getModerateEnemies();
        }
        else if(player.getLevel() == 4)
        {
            this.enemies = Enemy.getLvl4Enemies();
        }
        else if(player.getLevel() >= 5)
        {
            this.enemies = Enemy.getMBossEnemies();
        }
        
        int index = random.nextInt(enemies.length);
        Enemy original = enemies[index];
        return new Enemy(original.getName(), original.getLevel(), original.getLevel() * 30 + 30,
                         original.getAttack(), original.getLevel() + 1, original.getXpReward(), SkillManager.getEnemySkillsFor(original.getName()));
    }

    private void goodEvent() {
        int event = random.nextInt(6);

        if (event == 0) {
            System.out.println("You found water... You drink it...");
            player.heal(25);
            System.out.println("It was clean water!");

        } else if (event == 1) {
            System.out.println("You found a chest! You open it and feel just a bit stronger!");
            player.gainXP(35);

        } else if (event == 2) {
            System.out.println("You set up camp and have a nice meal...");
            player.heal(10);
            player.attBuff(3, 2);

        } else if (event == 3) {
            System.out.println("You encounter a traveling merchant!");
            System.out.println("He offers you a choice:");
            System.out.println("1. Gain +3 attack for 2 turns");
            System.out.println("2. Gain +3 defense for 2 turns");

            int newChoice = scanner.nextInt();

            if (newChoice == 1) {
                player.attBuff(3, 2);
                System.out.println("You chose to become stronger!");
                System.out.println("You are now on steroids!");

            } else if (newChoice == 2) {
                player.defBuff(3, 2);
                System.out.println("You chose to become durable!");
                System.out.println("You now have a new pair of armor!");

            } else {
                System.out.println("You ignore the merchant... dumbass");
            }
        } else if (event == 4) {
            System.out.println("A glowing fountain restores your health and spirit.");
            player.heal(35);
            player.clearStatus();
        } else if (event == 5) {
            System.out.println("A ghostly sensei trains you silently in the dark...");
            player.attack += 1;
            System.out.println("Your attack permanently increased by 1!");
        }
    }

    private void badEvent() {
        int event = random.nextInt(6);

        if (event == 0) {
            System.out.println("You step on a pressure plate... Arrows come out of nowhere!");
            player.takeDamage(20);
 
        } else if (event == 1) {
            System.out.println("You touch a creepy idol... (what a dumbass)");
            player.attBuff(-3, 2);

        } else if (event == 2) {
            System.out.println("Suprise an EXTREME QTE!!!!!");
            boolean success = false;
            success = QTE.triggerQTE("Extreme");

            if (success) {
                System.out.println("You did it :D");
            } else {
                System.out.println("You were hit and lost some HP!");
                player.takeDamage(25);
            }
            
        } else if (event == 3) {
            System.out.println("You get ambushed by bees!");
            Skills beePoison = new Skills("Bee Sting", 12, "Status", 0, 3);
            player.applyStatus("Poison", 3, beePoison); 
        } else if (event == 4) {
            System.out.println("A cursed mirror shows your evil twin. You get punched.");
            player.takeDamage(10);
            System.out.println("You feel dizzy... You’ll miss your first turn in the next battle.");
            player.setSkipNextTurn(true);  
        }
        else if (event == 5 && player.getLevel() >= 6)
        {
            System.out.println("You feel an eerie presense while visting an abandoned castle...");
            System.out.println("As you visit the throne room...You feel something touch your shoulder... ");
            System.out.println("As you turn around...");
            System.out.println("==BOSS BATTLE==");
            Enemy king = new Enemy("Spirit King", 7, 300, 35, 10, 350, SkillManager.getEnemySkillsFor("Spirit King"));
            Battle battle = new Battle(player, king);
            battle.start();
        }
        else if (event == 5)
        {
            System.out.println("You drink some water");
            player.takeDamage(10);
            player.attBuff(-2, 3);
            player.defBuff(-2, 3);
            System.out.println("It was filled with bacteria");
        }
    }
    
    public void quickTime() {
        int obstecle = random.nextInt(4);
        int diff = random.nextInt(2);
        boolean success = false;
        if (obstecle == 0)
        {
            System.out.println("A log is hurling towards you!");
        }
        else if (obstecle == 1)
        {
            System.out.println("You TRIPPED!");
        }
        else if (obstecle == 2)
        {
            System.out.println("You set a trap and a bunch of arrows shoot at you!");
        }
        else
        {
            System.out.println("A falling rock is headed your way!");
        }
        
        if (diff == 0)
        {
            success = QTE.triggerQTE("easy");
        }
        else
        {
            success = QTE.triggerQTE("hard");
        }

        if (success) {
            System.out.println("You did it :D");
        } else {
            System.out.println("You were hit and lost some HP!");
            player.takeDamage(25);
        }
    }
}
