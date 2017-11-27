import java.util.Random;

class Bag{
    //  容量
    //  重さ
    //  価値
    int capacity;
    int[] weights;
    int[] values;
    int itemsize = 50;

    Bag(){
        capacity = 40;
        weights  = new int[itemsize];
        values   = new int[itemsize];

        Random rnd = new Random();
        for(int i=0; i<itemsize; i++){
            weights[i] = rnd.nextInt(10) + 1;
            values[i] = rnd.nextInt(40) + 1;
        }
    }
}

class kpGene extends Gene{

    kpGene(int size){
        super(size);
        evaluate();
    }

    public void evaluate(){
        score = 0;
        int weight = 0;

        for(int i=0; i<gene.length; i++){
            if(gene[i] == 1){
                score += Knapsack.bag.values[i];
                weight += Knapsack.bag.values[i];
            }
        }

        if(weight > Knapsack.bag.capacity){
            Random rnd = new Random();
            int bit = rnd.nextInt(gene.length);
            while(gene[bit] == 0){
                bit = rnd.nextInt(gene.length);
            }
            gene[bit] = 0;
            evaluate();
        }
    }

}

class kpGA extends GA{
    kpGA(Gene[] g){
        super(g);
    }

    kpGene getElite(){
        sort();
        kpGene elite = new kpGene(genes[0].gene.length);
        elite.gene = genes[0].gene.clone();
        return elite;
    }
}

public class Knapsack {

    static Bag bag = new Bag();

    public static void main(String[] args){

        for(int i=0; i<bag.itemsize; i++){
            System.out.print(bag.values[i] + " ");
        }
        System.out.println();
        for(int i=0; i<bag.itemsize; i++){
            System.out.print(bag.weights[i] + " ");
        }


        int population = 50;
        int max_generation = 10000;

        Gene[] genes = new kpGene[population];
        for(int i = 0; i<genes.length; i++){
            genes[i] = new kpGene(bag.itemsize);
        }

        GA ga = new GA(genes);

        ga.sort();

        while(ga.generation < max_generation){
            System.out.println("generation : " + ga.generation);

            Gene elite = ga.getElite();

            ga.genes = ga.select();

            Random rnd = new Random();
            for(int i = 0; i<population/2; i++){
                Gene pair1 = ga.genes[rnd.nextInt(population)];
                Gene pair2 = ga.genes[rnd.nextInt(population)];
                ga.crossover(pair1,pair2);
            }

            ga.mutation();

            ga.genes[population - 1] = elite;

            for(Gene g : ga.genes){
                g.evaluate();
            }

            ga.sort();

            System.out.println(ga.genes[0].score);
            ga.generation++;
        }




    }

}
