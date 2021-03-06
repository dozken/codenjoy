package com.codenjoy.dojo.battlecity.model;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.services.multiplayer.PlayerHero;

import java.util.LinkedList;
import java.util.List;

public class Pacman extends PlayerHero<Field> implements State<Elements, Player> {

    protected Dice dice;

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    private List<Food> foods;
    private boolean alive;
    private Gun gun;

    protected Direction direction;
    protected int speed;
    protected boolean moving;
    private boolean fire;
    private Point previousPosition;
    private boolean isGhost;

    public Pacman(int x, int y, Direction direction, Dice dice, int ticksPerBullets, boolean isGhost) {
        super(x, y);
        previousPosition = new PointImpl(-1, -1);
        this.direction = direction;
        this.dice = dice;
        this.isGhost = isGhost;
        gun = new Gun(ticksPerBullets);
        reset();
    }

    void turn(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void up() {
        if (alive) {
            direction = Direction.UP;
            moving = true;
        }
    }

    @Override
    public void down() {
        if (alive) {
            direction = Direction.DOWN;
            moving = true;
        }
    }

    @Override
    public void right() {
        if (alive) {
            direction = Direction.RIGHT;
            moving = true;
        }
    }

    @Override
    public void left() {
        if (alive) {
            direction = Direction.LEFT;
            moving = true;
        }
    }

    public Direction getDirection() {
        return direction;
    }

    // TODO подумать как устранить дублирование с MovingObject
    public void move() {
        for (int i = 0; i < speed; i++) {
            if (!moving) {
                return;
            }
            int newX = direction.changeX(x);
            int newY = direction.changeY(y);
            moving(newX, newY);
        }
    }

    public void moving(int newX, int newY) {
        if (field.isBarrier(newX, newY)) {
            // do nothing
        } else {
            previousPosition.setX(x);
            previousPosition.setY(y);
            move(newX, newY);
        }
        moving = false;
    }

    @Override
    public void act(int... p) {
        if (alive) {
            fire = true;
        }
    }

    public Iterable<Food> getFoods() {
        return new LinkedList<Food>(foods);
    }

    public void init(Field field) {
        super.init(field);

        int xx = x;
        int yy = y;
        while (field.isBarrier(xx, yy)) {
            xx = dice.next(field.size());
            yy = dice.next(field.size());
        }
        move(xx, yy);
        alive = true;
    }

    public void kill(Food food) {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public void removeBullets() {
        foods.clear();
    }

    @Override
    public void tick() {
        gun.tick();
    }

    @Override
    public Elements state(Player player, Object... alsoAtPoint) {
        if (isAlive()) {
            if (player.getHero() == this) {
                switch (direction) {
                    case LEFT:  return Elements.TANK_LEFT;
                    case RIGHT: return Elements.TANK_RIGHT;
                    case UP:    return Elements.TANK_UP;
                    case DOWN:  return Elements.TANK_DOWN;
                    default:    throw new RuntimeException("Неправильное состояние танка!");
                }
            } else {
                switch (direction) {
                    case LEFT:  return Elements.OTHER_TANK_LEFT;
                    case RIGHT: return Elements.OTHER_TANK_RIGHT;
                    case UP:    return Elements.OTHER_TANK_UP;
                    case DOWN:  return Elements.OTHER_TANK_DOWN;
                    default:    throw new RuntimeException("Неправильное состояние танка!");
                }
            }
        } else {
            return Elements.BANG;
        }
    }

    public void reset() {
        speed = 1;
        moving = false;
        fire = false;
        alive = true;
        gun.reset();
        foods = new LinkedList<>();
    }

    public void fire() {
        if (!fire) return;
        fire = false;

        if (!gun.tryToFire()) return;

        Food food = new Food(field, direction, copy(), this,
                b -> Pacman.this.foods.remove(b));

        if (!foods.contains(food)) {
            foods.add(food);
        }
    }

    public Point getPosition() {
        return new PointImpl(x, y);
    }

    public Point getPreviousPosition() {
        return previousPosition;
    }

    public boolean isGhost() {
        return isGhost;
    }
}
