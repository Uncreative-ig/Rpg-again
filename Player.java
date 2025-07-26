import java.util.*;

public class Player extends Character {
    
    // ===== Attributes =====
    private int xp;
    private String classType;
    private List<Skills> skills;
    private Random power = new Random();
    private Scanner choice;
    private String specialization = null;
    private List<Skills> specializationSkills = new ArrayList<>();

    // ===== Skill variables =====
    private boolean isInvisible = false;
    private int invisibleTurns = 0;
    private int chargeTurn = 0;
    private boolean skipNextTurn = false;
    public boolean reflector = false;
    
    // ===== Crits =====
    protected double critMultiplier = 1.5; 


    // ===== Constructor =====
    public Player(String name, String classType) {
        super(name, 1, 100, 15, 5);
        this.xp = 0;
        this.classType = classType.toLowerCase();
        this.skills = SkillManager.getSkillsFor(this.classType, this.level);
        setStats(this.classType);
    }

    // ===== Initial Stat Setup by Class =====
    private void setStats(String classType) {
        if (classType.equals("warrior")) {
            this.attack = 13;
            this.defense = 6;
            this.health = 110;
            this.maxHealth = 110;
        } else if (classType.equals("wizard")) {
            this.attack = 15;
            this.defense = 3;
            this.health = 85;
            this.maxHealth = 85;
        } else if (classType.equals("bandit")) {
            this.attack = 11;
            this.defense = 5;
            this.health = 90;
            this.maxHealth = 90;
        } else if (classType.equals("chronomancer")) {
            this.attack = 12;
            this.defense = 4;
            this.health = 95;
            this.maxHealth = 95;
        } else if (classType.equals("alchemist")) {
            this.attack = 10;
            this.defense = 5;
            this.health = 95;
            this.maxHealth = 95;
        } 
    }

    // ===== Skill Handling =====
    public List<Skills> getSkills() {
        return skills;
    }

    public void reduceCooldowns() {
        for (Skills s : skills) s.reduceCooldown();
    }

    public void resetAllCooldowns() {
        for (Skills s : skills) s.setCurrentCooldown(0);
    }
    
    public void increaseSkillPower()
    {
        for (Skills s : skills) {
           int amt = s.getBasePower();
           
           if(s.getType().equals("IncreaseCD") || amt <= 1 || s.getType().indexOf("Buff") >= 1)
           {
                continue;
           }
           // Increase skill power by 10%
           double multiplier = 1.0 + (0.1 * (this.level - 1));
           int newPower = (int)(amt * multiplier);
           s.setBasePower(newPower);
        }
    }

    public void useSkill(int index, Character target) {
        if (index < 0 || index >= skills.size()) return;
        Skills skill = skills.get(index);
        int base = skill.getBasePower();
        System.out.println(name + " uses " + skill.getName() + "!");

        if (skill.getType().equals("attack")) {
            target.takeDamage(getRanDmg(base));
        } else if (skill.getType().equals("heal")) {
            heal(base);
        } else if (skill.getType().equals("Charged")) {
            if (chargeTurn == 0) {
                chargeTurn = 1;
                System.out.println(name + " is charging a powerful attack!");
            } else {
                target.takeDamage(getRanDmg(base));
                System.out.println(name + " unleashes the charged strike!");
                chargeTurn = 0;
            }
        } else if (skill.getType().equals("DefBuff")) {
            defBuff(base, skill.getDuration());
        } else if (skill.getType().equals("attBuff")) {
            attBuff(base, skill.getDuration());
        } else if (skill.getType().equals("DeDefBuff")) {
            target.defBuff(base, skill.getDuration());
        } else if (skill.getType().equals("DeAttBuff")) {
            target.attBuff(base, skill.getDuration());
        } else if (skill.getType().equals("Buffs")) {
            attBuff(base, skill.getDuration());
            defBuff(base, skill.getDuration());
        } else if (skill.getType().equals("DmgAttBuff")) {
            target.takeDamage(getRanDmg(base) + 15);
            attBuff(base, skill.getDuration());
        } else if (skill.getType().equals("DmgDefBuff")) {
            target.takeDamage(getRanDmg(base) + 15);
            defBuff(base, skill.getDuration());
        } else if (skill.getType().equals("Scramble")) {
            System.out.println("You scrambled your enemies cooldowns!");
            for (Skills s : ((Enemy)target).getSkills()) {
                s.setCurrentCooldown(power.nextInt(s.getCooldown() + 1) + 1);
            }
        } else if (skill.getType().equals("Mark")) {
            System.out.println("The enemy is marked");
            System.out.println("Your crit chance is 100%");
            setCritChance(0.9, skill.getDuration());
        } else if (skill.getType().equals("MultiHit")) {
            for (int i = 0; i < skill.getDuration(); i++) {
                target.takeDamage(getRanDmg(base));
            }
        } else if (skill.getType().equals("invis")) {
            isInvisible = true;
            invisibleTurns = skill.getDuration();
            System.out.println(name + " has vanished!");
        } else if (skill.getType().equals("Health Steal")) {
            System.out.println("What are you a vampire?");
            System.out.println("I guess you stole some health...");
            int damage = getRanDmg(base);
            target.takeDamage(damage);
            heal((int)(damage * 0.75));
        } else if (skill.getType().equals("Status")) {
            if (classType.equals("alchemist")) {
                target.applyStatus("poison", skill.getDuration(), skill);
            } 
            else if (classType.equals("plantmancer")) {
                target.applyStatus("poison", skill.getDuration(), skill);
            } 
            else if (classType.equals("wizard")) {
                target.applyStatus("poison", skill.getDuration(), skill);
            }
        } else if (skill.getType().equals("Both")) {
            System.out.println("You both take damage!");
            target.takeDamage(getRanDmg(base) + 15);
            takeDamage(base);
        } else if (skill.getType().equals("hitStun")) {
            ((Enemy)target).applyStun(true);
            target.takeDamage(getRanDmg(base));
        } else if(skill.getType().equals("Stun")) {
            ((Enemy)target).applyStun(true);
        } else if(skill.getType().equals("Freeze")) {
            ((Enemy)target).applyFreeze(true);
        } else if (skill.getType().equals("healBuff")) {
            heal(base + 10);
            attBuff(base, skill.getDuration() + 1);
            defBuff(base, skill.getDuration());
        } else if (skill.getType().equals("reflect")) {
            reflector = true;
            System.out.println(name + " reflects the next enemy attack");
        } else if (skill.getType().equals("ReduceCD")) {
            System.out.println(name + " manipulates time to refresh their skills");
            reduceAllSkillCooldowns(base);
        } else if (skill.getType().equals("IncreaseCD")) {
            System.out.println(name + " manipulates time to delay enemy abilities");
            ((Enemy)target).increaseAllSkillCooldowns(base);
        } else if (skill.getType().equals("regen")) {
            System.out.println("You now have regeneration for " + skill.getDuration() + " turns");
            setRegen(base, skill.getDuration());
        } else if (skill.getType().equals("healRegen")) {
            int amt = base + 5;
            heal(amt);
            System.out.println("You now have regeneration for " + skill.getDuration() + " turns");
            setRegen(base, skill.getDuration());
        } else if (skill.getType().equals("Random Debuff")) {
            int amount = power.nextInt(6);
            if (amount == 0) {
                ((Enemy)target).applyFreeze(true);
            }
            else if (amount == 1) {
                System.out.println("You swapped the defense and attack of the opponent for the rest of the game");
                int temp = target.defense;
                target.defense = target.attack;
                target.attack = temp;
            }
            else if (amount == 2) {
                System.out.println("The enemy is marked");
                System.out.println("Your crit chance is 100%");
                setCritChance(0.9, skill.getDuration());
            } 
            else if (amount == 3 & target.health >= target.health/2) {
                System.out.println("You reduced the enemies health and defense by 10%");
                target.health /= 0.1;
                target.defense /= 0.1;
            } 
            else if (amount == 4) {
                System.out.println("You increased your opponet's cooldowns");
                ((Enemy)target).increaseAllSkillCooldowns(2);
            }
            else if (amount == 5) {
                System.out.println("You halved the enemies attack");
                target.attack /= 2;
            }
            else if (amount == 6) {
                System.out.println("You halved the enemies defense");
                target.defense /= 2;
            }
            else if (amount == 5) {
                Skills burn = new Skills("Burn", 12, "Status", 0, 2);
                target.applyStatus("Burned", 2, burn); 
                this.attack -= 2;
            }
        } else if (skill.getType().equals("Random Buff")) {
            int amount = power.nextInt(6);
            if (amount == 0) {
                System.out.println("You gained an extra turn");
                setExtraTurn(true);
            }
            else if (amount == 1) {
                System.out.println("You made your attack and defense stats equal to the highest");
                this.attack = 10;
                this.defense = 10;
            }
            else if (amount == 2) {
                System.out.println("You regained half of your health");
                int amt = this.maxHealth/1;
                heal(amt);
            } 
            else if (amount == 3) {
                System.out.println("You sharpened your eyes and increased your crit chances");
                setCritChance(0.25, 3);
            } 
            else if (amount == 4) {
                System.out.println("You reduced your cooldowns");
                reduceAllSkillCooldowns(1);
            }
            else if (amount == 5) {
                setInvisible(true, 2);
            }
        } else if (skill.getType().equals("Trade off")) {
            if (classType.equals("witch")) {
            defBuff(-3, skill.getDuration());
            heal(base);
            }
            else if (classType.equals("warrior")) {
                target.takeDamage(30);
                attBuff(-4, 3);
            }
            else if (classType.equals("jester")) {
                int ran = power.nextInt(5);
                System.out.println("You put on an act and....");
                System.out.println("");
                if (ran == 0)
                {
                    System.out.println("You patch yourself up but did a terrible job.");
                    heal(35);
                    defBuff(-2, 3);
                }
                else if (ran == 1)
                {
                    System.out.println("You punch youself to remind your enemies that you're the main character");
                    takeDamage(10);
                    attBuff(4, 3);
                }
                else if (ran == 2)
                {
                     System.out.println("You pull out a time machine! But you turn back to 2 seconds into the past");
                    increaseAllSkillCooldowns(1);
                    this.health = this.maxHealth;
                }
                else if (ran == 3)
                {
                     System.out.println("You flex you're masculine muscles, but you start cramping");
                    attBuff(-3, 3);
                    defBuff(6, 3);
                }
                else
                {
                     System.out.println("You uh...idk you both take damage ");
                    target.takeDamage(38);
                    takeDamage(15);
                }
            }
        }

        setSkipNextTurn(false);
        skill.setCurrentCooldown(skill.getCooldown());
    }
    
    // Specialization 
    public void chooseSpecialization() {
        if (specialization != null) {
            System.out.println("You already chose specialization: " + specialization);
            return;
        }

        String[] specs = SpecializationManager.getSpecializationsForClass(classType);
        if (specs.length == 0) {
            System.out.println("No specializations available.");
            return;
        }

        System.out.println("Choose your specialization:");
        for (int i = 0; i < specs.length; i++) {
            String name = specs[i];
            String desc = specDesc(classType, name);
            String stat = specStatChanges(classType, name);
            
            System.out.println((i+1) + ". " + name + desc);
            System.out.println(stat);
            System.out.println(" ");
        }

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt() - 1;

        if (choice < 0 || choice >= specs.length) {
            System.out.println("Invalid choice!");
            return;
        }

        specialization = specs[choice];
        System.out.println("You chose " + specialization);
        statChange(classType, specialization);

        unlockSpecSkill(0);
    }
    
    private void unlockSpecSkill(int index) {
        List<Skills> allSpecSkills = SpecializationManager.getSkillsForSpecialization(classType, specialization);
        if (index < allSpecSkills.size()) {
            Skills skill = allSpecSkills.get(index);
            specializationSkills.add(skill);
            skills.add(skill);    // <<< Add skill to main skills list here
            System.out.println("Unlocked specialization skill: " + skill.getName());
        }
    }
    
    private String specDesc(String classType, String specName) {
        if (classType.equals("warrior")) {
            if (specName.equals("Juggernaut")) {
                return ": A slow moving wall of defense. Basically a brick wall...";
            }
            else if (specName.equals("Warlord")) {
                return ": Deals heavy damage but low sustainability. Its a big attacker...";
            }
            else if (specName.equals("Blademaster")) {
                return ": An offensive special who sustains itself with self-heals and buffs...";
            }
        }
        else if (classType.equals("wizard")) {
            if (specName.equals("Reg Wizard")) {
                return ": An upgrader with buffs and status attacks";
            }
            else if (specName.equals("Ice Wizard")) {
                return ": A controller with freeze, cooldown manipulation, and debuffs";
            }
            else if (specName.equals("E Wiz")) {
                return ": An offensive controller with cooldown manipulation and stuns. ";
            }
        }
        else if (classType.equals("bandit")) {
            if (specName.equals("Phantom Striker")) {
                return ": A buff controller. Self buffs, debuffs, and freeze...";
            }
            else if (specName.equals("Trapist")) {
                return ": Can confuse the enmies with multiple tools like cooldown manipulation and status...";
            }
            else if (specName.equals("Bruiser")) {
                return ": A straight up offensive attacker. Buffs and..yeah attacks...";
            }
        }
        else if (classType.equals("chronomancer")) {
            if (specName.equals("Time Stitcher")) {
                return ": A high sustainable support with heals and a reflector...";
            }
            else if (specName.equals("Clock Piercer")) {
                return ": An offensive special with buffs and debuffs...";
            }
            else if (specName.equals("Loopweaver")) {
                return ": An evasive special with stuns and cooldown manipulation...";
            }
        }
         else if (classType.equals("alchemist")) {
            if (specName.equals("Toxicologist")) {
                return ": A 'dps' menace with stuns, debuffs, and statuses...";
            }
            else if (specName.equals("Biologist")) {
                return ": A healer with health steal and buffs...";
            }
            else if (specName.equals("Combustinoneer")) {
                return ": An offensive special mainly with attacks and multihits...";
            }
        }
        return "No desc";
    }
    
    private String specStatChanges(String classType, String specName) {
        if (classType.equals("warrior")) {
            // Range: +-[2-6]ATK, +-[1-3]DEF, +-[10-30]HP
            if (specName.equals("Juggernaut")) {
                return "[-4 ATK, +2 DEF, +16 HP]";
            }
            else if (specName.equals("Warlord")) {
                return "[+6 Atk, -2 DEF, -12 HP]";
            }
            else if (specName.equals("Blademaster")) {
                return "[+2 Atk, +1 DEF, +12 HP]";
            }
        }
        else if (classType.equals("wizard")) {
            if (specName.equals("Reg Wizard")) {
                return "[+2 Atk, -2 DEF, +18 HP]";
            }
            else if (specName.equals("Ice Wizard")) {
                return "[-6 Atk, +3 DEF, +21 HP]";
            }
            else if (specName.equals("E Wiz")) {
                return "[+4 Atk, +2 DEF, -13 HP]";
            }
        }
        else if (classType.equals("bandit")) {
            if (specName.equals("Phantom Striker")) {
                return "[+4 Atk, +3 DEF, -15 HP]";
            }
            else if (specName.equals("Trapist")) {
                return "[-2 Atk, +2 DEF, +22 HP]";
            }
            else if (specName.equals("Bruiser")) {
                return "[+6 Atk, -3 DEF, +18 HP]";
            }
        }
        else if (classType.equals("chronomancer")) {
            if (specName.equals("Time Stitcher")) {
                return "[+2 Atk, +3 DEF, -11 HP]";
            }
            else if (specName.equals("Clock Piercer")) {
                return "[+6 Atk, -2 DEF, +14 HP]";
            }
            else if (specName.equals("Loopweaver")) {
                return "[+2 Atk, +1 DEF, +16 HP]";
            }
        }
         else if (classType.equals("alchemist")) {
            if (specName.equals("Toxicologist")) {
                return "[-4 Atk, +1 DEF, +16 HP]";
            }
            else if (specName.equals("Biologist")) {
                return "[-2 Atk, +3 DEF, +16 HP]";
            }
            else if (specName.equals("Combustinoneer")) {
                return "[+6 Atk, +2 DEF, -17 HP]";
            }
        }
        return "No stat changes";
    }
    
    public void statChange(String classType, String specName){
        if (classType.equals("warrior")) {
            if (specName.equals("Juggernaut")) {
                this.attack -= 4;
                this.defense += 2;
                this.maxHealth += 16;
            }
            else if (specName.equals("Warlord")) {
                this.attack += 6;
                this.defense -= 2;
                this.maxHealth -= 12;
            }
            else if (specName.equals("Blademaster")) {
                this.attack += 2;
                this.defense += 1;
                this.maxHealth += 12;
            }
        }
        else if (classType.equals("wizard")) {
            if (specName.equals("Reg Wizard")) {
                this.attack += 2;
                this.defense -= 2;
                this.maxHealth += 18;
            }
            else if (specName.equals("Ice Wizard")) {
                this.attack -= 6;
                this.defense += 3;
                this.maxHealth += 21;
            }
            else if (specName.equals("E Wiz")) {
                this.attack += 4;
                this.defense += 2;
                this.maxHealth -= 13;
            }
        }
        else if (classType.equals("bandit")) {
            if (specName.equals("Phantom Striker")) {
                this.attack += 4;
                this.defense += 3;
                this.maxHealth -= 15;
            }
            else if (specName.equals("Trapist")) {
                this.attack -= 2;
                this.defense += 2;
                this.maxHealth += 22;
            }
            else if (specName.equals("Bruiser")) {
                this.attack += 6;
                this.defense -= 3;
                this.maxHealth += 18;
            }
        }
        else if (classType.equals("chronomancer")) {
            if (specName.equals("Time Stitcher")) {
                this.attack += 2;
                this.defense += 3;
                this.maxHealth -= 11;
            }
            else if (specName.equals("Clock Piercer")) {
                this.attack += 6;
                this.defense -= 2;
                this.maxHealth += 14;
            }
            else if (specName.equals("Loopweaver")) {
                this.attack += 2;
                this.defense += 1;
                this.maxHealth += 16;
            }
        }
         else if (classType.equals("alchemist")) {
            if (specName.equals("Toxicologist")) {
                this.attack -= 4;
                this.defense += 1;
                this.maxHealth += 16;
            }
            else if (specName.equals("Biologist")) {
                this.attack -= 2;
                this.defense += 3;
                this.maxHealth += 16;
            }
            else if (specName.equals("Combustinoneer")) {
                this.attack += 6;
                this.defense += 2;
                this.maxHealth -= 17;
            }
        }
    } 

    // ===== Cooldown manipulation =====
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

    // ===== Combat Actions =====
    public boolean shouldSkipTurn() {
        return skipNextTurn;
    }

    public void setSkipNextTurn(boolean skip) {
        this.skipNextTurn = skip;
    }

    public int getRanDmg(int base) {
        int damage = base + (attack / 2) + power.nextInt(8);
        
        if(this.critBoostTurns == 0)
            {
                this.critChance = 0.1;
            }
        this.critChance += this.tempCritBoost;
        if (power.nextDouble() < critChance) {
            System.out.println(name + " lands a CRITICAL HIT!");
            damage *= critMultiplier;
        }
        
        return damage;
    }

    public boolean isReflecting() {
        return reflector;
    }

    public void clearStatus() {
        statusEffect = "";
    }

    // ===== Invisibility =====
    public void updateInvisibility() {
        if (isInvisible) {
            invisibleTurns--;
            if (invisibleTurns <= 0) {
                isInvisible = false;
                System.out.println(name + " is visible again!");
            }
        }
    }

    public void setInvisible(boolean invisible, int duration) {
        this.isInvisible = invisible;
        this.invisibleTurns = duration;
    }

    public boolean isInvisible() {
        return isInvisible;
    }

    // ===== Leveling System =====
    public void gainXP(int amount) {
        xp += amount;
        System.out.println(name + " gains " + amount + " XP!");

        if (xp >= level * 75) {
            level++;
            updateSkills();
            increaseSkillPower();
            xp = 0;
            System.out.println(name + " leveled up to " + level + "!");
            System.out.println("Choose a permanent buff:");
            System.out.println("1. +10 HP\n2. +2 attack\n3. +1 defense");

            this.choice = new Scanner(System.in);
            int c = choice.nextInt();
            if (c == 1) {
                maxHealth += 10;
            } else if (c == 2) {
                attack += 2;
            } else {
                defense += 1;
            }
            health = maxHealth;
            
            if(level == 2) {
                chooseSpecialization();
            }
            
            if (specialization != null) {
                if (level == 3) {
                    unlockSpecSkill(1); 
                }
                if (level == 4)  {
                    unlockSpecSkill(2);
                }
            }
        }
    }
    
    public void updateSkills() {
        List<Skills> baseSkills = SkillManager.getSkillsFor(this.classType, this.level);
        baseSkills.addAll(specializationSkills);
        this.skills = baseSkills;
    }




    // ===== Passive Abilities =====
    public void applyPassiveStart(Enemy enemy) {
        if (this.getLevel() >= 3) {
            if (classType.equals("warrior")) {
                System.out.println(name + "'s passive ability activated!");
                attBuff(1, 1);
            } else if (classType.equals("bandit")) {
                if (power.nextInt(5) == 0) {
                    System.out.println(name + "'s passive ability activated!");
                    isInvisible = true;
                    invisibleTurns = 1;
                    System.out.println(name + " has vanished!");
                }
            } else if (classType.equals("chronomancer")) {
                if (power.nextInt(10) == 0) {
                    System.out.println(name + "'s passive ability activated!");
                    reduceAllSkillCooldowns(1);
                }
            } else if (classType.equals("witch")) {
                System.out.println(name + "'s passive ability activated!");
                System.out.println("Larry comes out for assistant");
                enemy.takeDamage(15);
            } else if (classType.equals("jester")) {
                System.out.println(name + "'s passive ability activated!");
                int outcome = power.nextInt(6);
                if (outcome == 0) {
                    setRegen(5, 2);
                } else if (outcome == 1) {
                    attBuff(2,1);
                } else if (outcome == 2) {
                    isInvisible = true;
                    invisibleTurns = 1;
                    System.out.println(name + " has vanished!");
                } else if (outcome == 3) {
                    setSkipNextTurn(true);
                } else if (outcome == 4) {
                    defBuff(-1, 1);
                } else {
                    takeDamage(8);
                }
            }
        }
    }

    public void applyPassiveEnd(Enemy enemy) {
        if (this.getLevel() >= 3) {
            if (classType.equals("wizard")) {
                if (power.nextInt(10) < 3)
                {
                    System.out.println(name + "'s passive ability activated!");
                    Skills burn = new Skills("Burn", 12, "Status", 0, 2);
                    enemy.applyStatus("Burned", 2, burn); 
                }
            } else if (classType.equals("alchemist")) {
                if (power.nextInt(10) < 3) 
                {
                     System.out.println(name + "'s passive ability activated!");
                    Skills poison = new Skills("Poison", 12, "Status", 0, 2);
                    enemy.applyStatus("Poison", 2, poison); 
                }
            } else if (classType.equals("monk")) {
                System.out.println(name + "'s passive ability activated!");
                heal(8);
            } else if (classType.equals("illusionist")) {
                System.out.println(name + "'s passive ability activated!");
                if (power.nextInt(10) == 0) {
                    isInvisible = true;
                    invisibleTurns = 1;
                    System.out.println(name + " has vanished!");
                }
            } else if (classType.equals("plantmancer")) {
                if (this.health >= (this.maxHealth / 2)) {
                     System.out.println(name + "'s passive ability activated!");
                    attBuff(2,2);
                }
            }
        }
    }

    // ===== Display =====
    public void displayStats() {
        System.out.println(name + " (Level " + level + " " + classType + ") HP: " + health + "/" + maxHealth + " XP: " + xp);
    }

}