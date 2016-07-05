import java.util.Random;

/**
 * Created by 3bdi1230 on 2016/07/05.
 */

class Gene{
    int[] gene;
    int score;

    Gene(int size){
        gene = new int[size];

        Random rnd = new Random();
        for(int i =0; i<size; i++){
            gene[i] = rnd.nextInt(2);
        }

        evaluate();
    }

    public void evaluate(){
        score = 0;

        for(int i=0; i<gene.length; i++){
            score += gene[i];
        }
    }

    void mutation(double rate){
        Random rnd = new Random();
        if(rnd.nextDouble() < rate){
            int bit = rnd.nextInt(gene.length);
            gene[bit] = (gene[bit] == 0) ? 1 : 0;
        }
    }
}

class GA {
    public Gene[] genes;
    int generation;
    double mutation_rate;

    GA(Gene[] g){
        genes = g;

        mutation_rate = 1.0/ genes.length;
    }

    Gene[] select(){
        Gene[] parent = new Gene[genes.length];
        for(int i = 0; i < genes.length; i++){
            parent[i] = roulette_select();
        }
        return parent;
    }

    Gene roulette_select(){
        int total = 0;
        int roulette_value = 0;

        for(int i = 0; i < genes.length; i++){
            total += genes[i].score;
        }

        Random rnd = new Random();
        roulette_value = rnd.nextInt(total);

        int sum = 0;
        for(int i=0; i<genes.length; i++){
            sum += genes[i].score;
            if(sum > roulette_value){
                return genes[i];
            }
        }
        return genes[genes.length];
    }

    Gene getElite(){
        sort();
        Gene elite = new Gene(genes[0].gene.length);
        elite.gene = genes[0].gene.clone();
        return elite;
    }

    void crossover(Gene g1, Gene g2){
        int value;
        Random rn = new Random();
        value = rn.nextInt(g1.gene.length);

        for(int i=0; i<g1.gene.length; i++){
            if(i < value){
                g1.gene[i] = g2.gene[i];
            }else{
                g2.gene[i] = g1.gene[i];
            }
        }
    }

    void mutation(){
        //  突然変異
        for(Gene g : genes){
            g.mutation(mutation_rate);
        }

    }

    public void sort(){
        for(int i = 0; i < genes.length - 1; i++) {
            for (int j = 0; j < genes.length - 1; j++) {
                if (genes[j].score < genes[j + 1].score) {
                    Gene temp = genes[j];
                    genes[j] = genes[j + 1];
                    genes[j + 1] = temp;
                }
            }
        }
    }
}

public class Main{
    public static void main(String[] args) throws CloneNotSupportedException {
        int size = 10;
        int length = 100;
        Gene[] genes = new Gene[size];

        for(int i=0; i<size; i++){
            genes[i] = new Gene(length);
        }

        GA ga = new GA(genes);
        for(Gene g : ga.genes){
            System.out.println(g.score);
        }
        ga.sort();

        System.out.println("");

        for(Gene g : ga.genes){
            System.out.println(g.score);
        }

        while(ga.generation < 10000){
            Gene elite = ga.getElite();
            ga.genes = ga.select();

            Random rnd = new Random();
            for(int i=0; i<size/2; i++){
                Gene pair1 = ga.genes[rnd.nextInt(size)];
                Gene pair2 = ga.genes[rnd.nextInt(size)];
                ga.crossover(pair1, pair2);
            }

            ga.mutation();

            ga.genes[size - 1] = elite;

            for(Gene g : ga.genes){
                g.evaluate();
            }

            ga.sort();

            System.out.println(ga.genes[0].score);
            ga.generation ++;
        }
    }
}


