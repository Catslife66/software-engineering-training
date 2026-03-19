package javaOop.exercises.game;

public class Player {
    private int health = 100;

    public void takeDamage(int damage){
        if(damage > 0){
            health -= damage;
        }
    }

    public int getHealth(){
        return health;
    }
}
