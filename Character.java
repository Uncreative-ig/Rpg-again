public class Character {
    protected String name;
    protected static int level;
    protected int health;
    protected int maxHealth;
    protected int attack;
    protected int defense;

    // ====== Buffs & Debuffs ======
    protected int buffedAttack = 0;
    protected int buffedDefense = 0;
    protected int attackBuffTurns = 0;
    protected int defenseBuffTurns = 0;
    private boolean extraTurn = false;

    // ====== Status Effects ======
    public String statusEffect = "";
    protected int statusDuration = 0;
    protected Skills statusSkill;

    // ====== Regen & Healing ======
    protected int regenAmount = 0;
    protected int regenDuration = 0;

    // ====== Critical Chance ======
    protected double critChance = 0.1;
    protected double tempCritBoost = 0.0;
    protected int critBoostTurns = 0;

    // ====== Constructor ======
    public Character(String name, int level, int health, int attack, int defense) {
        this.name = name;
        this.level = level;
        this.health = this.maxHealth = health;
        this.attack = attack;
        this.defense = defense;
    }
    
    public String getName() {
        return name;
    }

    public static int getLevel() {
        return level;
    }

    public int getAttack() {
        return attack;
    }

    // ====== Combat Status ======
    public boolean isAlive() {
        return health > 0;
    }

    public void resetHealth() {
        health = maxHealth;
    }
    
    public boolean hasExtraTurn() {
        return extraTurn;
    }
    
    public void setExtraTurn (boolean t) {
        extraTurn = t;
    }

    public void takeDamage(int damage) {
        int totalDefense = defense + buffedDefense;
        int reduced = Math.max(0, damage - totalDefense);
        health -= reduced;
        if (health < 0) health = 0;
        System.out.println(name + " takes " + reduced + " damage. (HP: " + health + ")");
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
        System.out.println(name + " heals for " + amount + " (HP: " + health + ")");
    }

    // ====== Buffs ======
    public void attBuff(int amount, int duration) {
        buffedAttack += amount;
        attackBuffTurns = duration;
        System.out.println(amount > 0 ? "Attack Up!" : name + "'s attack is down!");
    }

    public void defBuff(int amount, int duration) {
        buffedDefense += amount;
        defenseBuffTurns = duration;
        System.out.println(amount > 0 ? "Defense Up!" : name + "'s defense is down!");
    }

    public void updateBuffs() {
        if (attackBuffTurns > 0 && --attackBuffTurns == 0) {
            buffedAttack = 0;
            System.out.println(name + "'s attack buff wore off!");
        }

        if (defenseBuffTurns > 0 && --defenseBuffTurns == 0) {
            buffedDefense = 0;
            System.out.println(name + "'s defense buff wore off!");
        }

        if (critBoostTurns > 0 && --critBoostTurns == 0) {
            tempCritBoost = 0.0;
            System.out.println(name + "'s critical buff has worn off");
        }
    }

    // ====== Status Effects ======
    public void applyStatus(String status, int duration, Skills skill) {
        this.statusEffect = status.toLowerCase();
        this.statusDuration = duration;
        this.statusSkill = skill;
        System.out.println(name + " is now afflicted with " + statusEffect);
    }

    public void dmgTick() {
        if (statusDuration > 0) {
            int damage = statusSkill.getBasePower();
            takeDamage(damage);
        }

        if (--statusDuration == 0 && !statusEffect.equals("")) {
            System.out.println(name + " is no longer affected by " + statusEffect + ".");
            statusEffect = "";
        }
    }

    // ====== Regen ======
    public void setRegen(int amount, int duration) {
        this.regenAmount = amount;
        this.regenDuration = duration;
    }

    public void applyRegen() {
        if (regenAmount > 0 && regenDuration > 0) {
            heal(regenAmount);
            regenDuration--;
            if (regenDuration == 0) {
                regenAmount = 0;
            }
        }
    }

    // ====== Crit Chance ======
    public void setCritChance(double amt, int duration) {
        this.tempCritBoost = amt;
        this.critBoostTurns = duration;
    }

    // ====== Display ======
    public void displayStats() {
        System.out.println(name + " (Level " + level + ") HP: " + health + "/" + maxHealth);
    }
}
