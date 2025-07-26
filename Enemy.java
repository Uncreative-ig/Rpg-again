import java.util.*;

public class Enemy extends Character {
    private int xpReward;
    private List<Skills> skills;
    private Random random = new Random();

    private boolean isInvisible = false;
    private int invisibleTurns = 0;
    private int chargeTurn = 0;
    private boolean isStunned = false;
    private boolean isFrozen = false;

    private int attBuff = 0;
    private int attBuffDuration = 0;
    private int defBuff = 0;
    private int defBuffDuration = 0;
    
    protected double critChance = 0.05; 
    protected double critMultiplier = 1.5;

    public Enemy(String name, int level, int health, int attack, int defense, int xpReward, List<Skills> skills) {
        super(name, level, health, attack, defense);
        this.xpReward = xpReward;
        this.skills = skills;
    }

    public void useSkill(Character target) {
        if (skills == null || skills.isEmpty()) {
            target.takeDamage(this.attack);
            return;
        }

        List<Skills> availableSkills = new ArrayList<>();
        for (Skills s : skills) {
            if (s.isReady()) availableSkills.add(s);
        }

        if (availableSkills.isEmpty()) {
            System.out.println(name + " uses a basic attack!");
            target.takeDamage(getRanDmg(0));
            return;
        }

        Skills skill = availableSkills.get(random.nextInt(availableSkills.size()));
        int base = skill.getBasePower();
        System.out.println(name + " uses " + skill.getName() + "!");

        if (skill.getType().equals("attack")) {
            target.takeDamage(getRanDmg(base));

        } else if (skill.getType().equals("heal")) {
            heal(base);

        } else if (skill.getType().equals("Buff")) {
            attBuff(base, skill.getDuration());

        } else if (skill.getType().equals("DefBuff")) {
            defBuff(base, skill.getDuration());

        } else if (skill.getType().equals("Charged")) {
            if (chargeTurn == 0) {
                chargeTurn = 1;
                System.out.println(name + " is charging a powerful attack!");
            } else {
                System.out.println(name + " unleashes the charged strike!");
                target.takeDamage(getRanDmg(base));
                chargeTurn = 0;
            }

        } else if (skill.getType().equals("DeAttBuff")) {
            int amt = -base;
            System.out.println(target.name + "'s attack is lowered!");
            target.attBuff(amt, skill.getDuration());

        } else if (skill.getType().equals("DeDefBuff")) {
            int amt = -base;
            System.out.println(target.name + "'s defense is lowered!");
            target.defBuff(amt, skill.getDuration());

        } else if (skill.getType().equals("Health Steal")) {
            int damage = getRanDmg(base);
            target.takeDamage(damage);
            heal((int)(damage * 0.75));

        } else if (skill.getType().equals("Status")) {
            if (getName().equals("Dart Goblin")) {
                target.applyStatus("poison", skill.getDuration(), skill);
            }
            target.applyStatus("burn", skill.getDuration(), skill);

        } else if (skill.getType().equals("Stun")) {
            System.out.println("The player is now stunned");
            ((Player)target).setSkipNextTurn(true);

        } else if (skill.getType().equals("MultiHit")) {
            for (int i = 0; i < skill.getDuration(); i++) {
                target.takeDamage(getRanDmg(base));
            }

        } else if (skill.getType().equals("Invis")) {
            isInvisible = true;
            invisibleTurns = skill.getDuration();
            System.out.println(name + " has vanished!");

        } else if (skill.getType().equals("Both")) {
            int damage = getRanDmg(base);
            target.takeDamage(damage);
            takeDamage(base);
        }

        skill.setCurrentCooldown(skill.getCooldown());
    }

    public void reduceCooldowns() {
        for (Skills s : skills) {
            s.reduceCooldown();
        }
    }

    public void resetAllCooldowns() {
        for (Skills s : skills) {
            s.setCurrentCooldown(0);
        }
    }

    public void updateInvisibility() {
        if (isInvisible) {
            invisibleTurns--;
            if (invisibleTurns <= 0) {
                isInvisible = false;
                System.out.println(name + " is visible again!");
            }
        }
    }

    public boolean isInvisible() {
        return isInvisible;
    }

    public void setInvisible(boolean invisible, int duration) {
        this.isInvisible = invisible;
        this.invisibleTurns = duration;
    }

    public boolean checkIfStunned() {
        if (isStunned) {
            System.out.println(name + " is stunned and cannot move!");
            isStunned = false;
            return true;
        } 
        else if (isFrozen) {
            System.out.println(name + " is frozen and cannot move!");
            takeDamage(10);
            isFrozen = false;
            return true;
        }
        return false;
    }

    public void applyStun(boolean stun) {
        isStunned = stun;
    }
    
    public void applyFreeze(boolean frozen) {
        isFrozen = frozen;
    }

    public int getRanDmg(int base) {
        int damage = base + (attack / 2) + random.nextInt(8);
        if (random.nextDouble() < critChance) {
            System.out.println(name + " lands a CRITICAL HIT!");
            damage *= critMultiplier;
        }
        
        return damage;
    }
    
    public void reduceAllSkillCooldowns(int amount) {
        for (Skills s : skills) {
            s.setCurrentCooldown(Math.max(0, s.currentCooldown() - amount));
        }
    }

    public void increaseAllSkillCooldowns(int amount) {
        for (Skills s : skills) {
            s.setCurrentCooldown(s.currentCooldown() + amount);
        }
    }

    public void displayStats() {
        System.out.println("Enemy " + name + " (Level " + level + ") HP: " + health + "/" + maxHealth);
    }

    public List<Skills> getSkills() {
        return skills;
    }

    public int getXpReward() {
        return xpReward;
    }

    // --- Static enemy sets for spawning ---
    public static Enemy[] getDefaultEnemies() {
        return new Enemy[] {
            new Enemy("Goblin", 1, 50, 8, 2, 35, SkillManager.getEnemySkillsFor("Goblin")),
            new Enemy("Berserker", 1, 55, 8, 1, 35, SkillManager.getEnemySkillsFor("Berserker")),
            new Enemy("Skeleton", 1, 45, 9, 1, 35, SkillManager.getEnemySkillsFor("Skeleton"))
        };
    }

    public static Enemy[] getLvl2Enemies() {
        return new Enemy[] {
            new Enemy("Bats", 2, 70, 8, 2, 45, SkillManager.getEnemySkillsFor("Bats")),
            new Enemy("Firecraker", 2, 65, 11, 2, 45, SkillManager.getEnemySkillsFor("Firecraker")),
            new Enemy("Bomber", 2, 75, 9, 4, 45, SkillManager.getEnemySkillsFor("Bomber"))
        };
    }

    public static Enemy[] getModerateEnemies() {
        return new Enemy[] {
            new Enemy("Archers", 3, 80, 13, 4, 65, SkillManager.getEnemySkillsFor("Archers")),
            new Enemy("Mini Pekka", 3, 90, 16, 4, 65, SkillManager.getEnemySkillsFor("Mini Pekka")),
            new Enemy("Dart Goblin", 3, 80, 14, 3, 65, SkillManager.getEnemySkillsFor("Dart Goblin"))
        };
    }

    public static Enemy[] getLvl4Enemies() {
        return new Enemy[] {
            new Enemy("Goblin Machine", 4, 105, 18, 5, 90, SkillManager.getEnemySkillsFor("Goblin Machine")),
            new Enemy("Archer Queen", 4, 100, 16, 4, 90, SkillManager.getEnemySkillsFor("Archer Queen")),
            new Enemy("Dark Prince", 4, 110, 17, 6, 95, SkillManager.getEnemySkillsFor("Dark Prince"))
        };
    }

    public static Enemy[] getMBossEnemies() {
        return new Enemy[] {
            new Enemy("Mega Knight", 5, 200, 24, 9, 150, SkillManager.getEnemySkillsFor("Megaknight")),
            new Enemy("Boss Bandit", 5, 200, 23, 8, 175, SkillManager.getEnemySkillsFor("Boss Bandit")),
            new Enemy("Golem", 5, 190, 21, 12, 200, SkillManager.getEnemySkillsFor("Golem"))
        };
    }
}
