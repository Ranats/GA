import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Created by coda on 16/07/04.
 */

class Bag {
    int capacity;
    int[] weights;
    int[] values;

    Bag(int itemsize){
        capacity = (int)(itemsize * 1.5);
        weights = new int[itemsize];
        values = new int[itemsize];

        initialize();
    }

    void initialize(){
        Random rnd = new Random();
        int size = weights.length;

        for(int i = 0; i<size; i++){
            weights[i] =  rnd.nextInt(capacity/10) + 1;
            values[i] = rnd.nextInt(size/2);
        }
    }



}
class GeneK extends Gene implements Comparable, Cloneable{

    GeneK(int size) {
        super(size);
        evaluate();
    }

    //  オーバーライド
    public void evaluate(){
        score = 0;
        int weight = 0;

        for(int i=0; i<gene.length; i++){
            if(gene[i] == 1){
                score += knapsack.bag.values[i];
                weight += knapsack.bag.weights[i];
            }
        }

//        System.out.print(score + " ");
        if(weight > knapsack.bag.capacity){
            Random rnd = new Random();
            int bit = rnd.nextInt(gene.length);
            while(gene[bit] == 0){
                bit = rnd.nextInt(gene.length);
            }
            gene[bit] = 0;
            evaluate();
        }

//        System.out.println(" " + score);

    }

    @Override
    public int compareTo(Object o) {
        GeneK otherGene = (GeneK) o;
        return -(this.score - otherGene.score);
    }

    @Override
    public GeneK clone() throws CloneNotSupportedException{
        GeneK res = (GeneK)super.clone();

        res.gene = gene.clone();
        return res;
    }
}

class GAK extends GA{

    GeneK[] genes;

    GAK(int size, int length) {
        super(size,length);
        genes = new GeneK[size];

        for(int i = 0; i < size; i++){
            genes[i] = new GeneK(length);
        }

        mutation_rate = 1.0 / length;
    }

    GeneK[] select(){
        GeneK[] parent = new GeneK[genes.length];
        for(int i=0; i< genes.length; i++){
            parent[i] = roulette_select();
        }
        return parent;
    }

    GeneK roulette_select(){
                //  ルーレット選択
        int total = 0;
        int roulette_value = 0;

        for(int i = 0; i < genes.length; i++){
        //    System.out.println(genes[i].score);
            total += genes[i].score;
        }

        Random rnd = new Random();
        roulette_value = rnd.nextInt(total);

        int sum = 0;
        int i;
        for(i=0; i<genes.length; i++){
            sum += genes[i].score;
            if(sum > roulette_value){
                break;
            }
        }
        return genes[i];

    }

    void sort(){
                for(int i = 0; i < genes.length - 1; i++) {
            for (int j = genes.length-1; j > i; j--) {
                if (genes[j].score > genes[j - 1].score) {
                    GeneK temp = genes[j];
                    genes[j] = genes[j - 1];
                    genes[j - 1] = temp;
                }
            }
        }

    }

    GeneK getElite2() throws CloneNotSupportedException{
        return(genes[0].clone());
    }
}


public class knapsack {

    static Bag bag = new Bag(100);
    public static void main(String[] args) throws IOException, CloneNotSupportedException {

        GAK ga = new GAK(100,100);

        ga.sort();

        while(ga.generation < 1000){
            System.out.println("generation : " + ga.generation);
//            for(GeneK gene : ga.genes){
//                System.out.println(gene.score);
//            }
            GeneK elite = ga.getElite2();
//            System.out.println(elite);
            //GeneK[] parent = ga.select();
            ga.genes = ga.select();

/*            System.out.println("selected");
            for(GeneK gene : ga.genes){
                System.out.println(gene.score);
            }
*/            Random rnd = new Random();
            for(int i =0; i< ga.genes.length / 2; i++){
                Gene pair1 = ga.genes[rnd.nextInt(ga.genes.length)];
                Gene pair2 = ga.genes[rnd.nextInt(ga.genes.length)];
                ga.crossover(pair1,pair2);
            }

            ga.mutation();
/*
            System.out.println("mutations");

            for(GeneK gene : ga.genes){
                System.out.println(gene.score);
            }
*/            ga.genes[ga.genes.length - 1] = elite;

//            System.out.println(ga.genes[ga.genes.length - 1] == elite);

//            ga.genes[ga.genes.length - 1].evaluate();
//            System.out.println("elitescore: " + ga.genes[ga.genes.length - 1].score);

            for(GeneK g : ga.genes){
                g.evaluate();
            }
/*
            System.out.println("evaluated");
            for(GeneK gene : ga.genes){
                System.out.println(gene.score);
            }
*/            ga.sort();


/*            System.out.println(ga.genes[0] == elite);

            System.out.println("sorted");

            for(GeneK gene : ga.genes){
                System.out.println(gene.score);
            }

            System.in.read();
*/
            System.out.println(ga.genes[0].score);
            //System.out.println(Arrays.toString(ga.genes[0].gene));
            ga.generation += 1;
        }

    }


}
