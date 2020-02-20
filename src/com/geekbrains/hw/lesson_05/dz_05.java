package com.geekbrains.hw.lesson_05;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class dz_05 {
        public static final int CARS_COUNT = 4;


        public static void main(String[] args) {
            //CyclicBarrier isReady=new CyclicBarrier(CARS_COUNT);

            Race race = new Race(new Road(60), new Tunnel(), new Road(40));
            race.start();

        }
    }
    class Car implements Runnable {
        private static int CARS_COUNT;
        private Race race;
        private int speed;
        private String name;
        public String getName() {
            return name;
        }
        public int getSpeed() {
            return speed;
        }
        public Car(Race race, int speed) {
            this.race = race;
            this.speed = speed;
            CARS_COUNT++;
            this.name = "Участник #" + CARS_COUNT;
        }
        @Override
        public void run() {
            try {
                System.out.println(this.name + " готовится");
                Thread.sleep(500 + (int)(Math.random() * 800));
                System.out.println(this.name + " готов");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                race.isReady.countDown();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                race.isReady.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < race.getStages().size(); i++) {
                race.getStages().get(i).go(this);
            }
            if(race.finish.getCount()==dz_05.CARS_COUNT){
              System.out.println(this.name+" ---WIN!!!!");
            }
            //System.out.println(race.finish.getCount());
            race.finish.countDown();
            //
            //System.out.println(this.name + " завершил гонку");
        }
    }
    abstract class Stage {
        protected int length;
        protected String description;
        public String getDescription() {
            return description;
        }
        public abstract void go(Car c);
    }
    class Road extends Stage {
        public Road(int length) {
            this.length = length;
            this.description = "Дорога " + length + " метров";
        }
        @Override
        public void go(Car c) {
            try {
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
                System.out.println(c.getName() + " закончил этап: " + description);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    class Tunnel extends Stage {
        private Semaphore sema=new Semaphore(dz_05.CARS_COUNT/2);
        public Tunnel() {
            this.length = 80;
            this.description = "Тоннель " + length + " метров";

        }
        @Override
        public void go(Car c) {
            try {
                try {
                    System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                    sema.acquire();
                    System.out.println(c.getName() + " начал этап: " + description);
                    Thread.sleep(length / c.getSpeed() * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(c.getName() + " закончил этап: " + description);
                    sema.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class Race {
        static CountDownLatch isReady=new CountDownLatch(dz_05.CARS_COUNT);
        static CountDownLatch finish=new CountDownLatch(dz_05.CARS_COUNT);
        private ArrayList<Stage> stages;
        public ArrayList<Stage> getStages() { return stages; }
        public Race(Stage... stages) {
            this.stages = new ArrayList<>(Arrays.asList(stages));
        }
        public void start(){
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
            Car[] cars = new Car[dz_05.CARS_COUNT];
            for (int i = 0; i < cars.length; i++) {
                cars[i] = new Car(this, 20 + (int) (Math.random() * 10));
            }

            for (int i = 0; i < cars.length; i++) {
                new Thread(cars[i]).start();
            }
            try{
                isReady.await();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
            try {
                finish.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
        }



}
