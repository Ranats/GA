package com.company;
import java.io.IOException;
import java.util.*;

class Test{

    Gene gene;

    Test(int size){
        gene = new Gene(size);
    }

    boolean valid_mutation(){

        int[] beforeGene = Arrays.copyOf(gene.chromosome,gene.chromosome.length);

        System.out.println("Before : " + Arrays.toString(beforeGene));
        gene.mutate();
        System.out.println("After : " + Arrays.toString(gene.chromosome));

        System.out.println(Arrays.equals(beforeGene,gene.chromosome));
        if(Arrays.equals(beforeGene,gene.chromosome)){
            return false;
        }
        else{
           return true;
        }
    }
}

class Gene implements Comparable,Cloneable{

    int[] chromosome; //= [0,0,1,1,0,0,1,...]
    int score;
    Random rnd;

    double mutation_rate;

    Gene(int size){
        //  初期化
        rnd = new Random();
        chromosome = new int[size];
        for(int i=0; i<size; i++){
            chromosome[i] = rnd.nextInt(2);
        }

        mutation_rate = 1.0 / size;

    }

    //  評価
    void evaluete(){
        //  遺伝子にスコアをつける
        //  そのスコアが大きい順に並び替えする

        // One-max 問題
        score = 0;
        for(int bit : chromosome){
            score += bit;
        }

    }

    void mutate(){
        //  ビット反転
        //  ランダムな位置
        //  反応係数1/L (L:遺伝子長)

        double rate = rnd.nextDouble();
//        System.out.println("rate : " + rate);
//        System.out.println("mutation_rate : " + mutation_rate);
        if(rate < mutation_rate){
            int bit = rnd.nextInt(chromosome.length);
            chromosome[bit] = (chromosome[bit] == 1) ? 0 : 1;
        }
    }

    @Override
    public int compareTo(Object o) {
        Gene otherGene = (Gene) o;
        return -(this.score - otherGene.score);
    }

    @Override
    public Gene clone() throws CloneNotSupportedException {
        Gene res = (Gene)super.clone();

        res.chromosome = chromosome.clone();
        return res;
    }
}

class GA {
    List<Gene> genes = new ArrayList<>();
    Gene elite;

    Random rnd;

    GA(int population, int gene_size){
        for(int i=0; i<population; i++){
            genes.add(new Gene(gene_size));
        }
        evaluate();

        rnd = new Random();
    }

    Gene getElite() throws CloneNotSupportedException {
        //genes.sort[0]
        return(genes.get(0).clone());
    }

    List<Gene> roulette_select(){
        List<Gene> parent = new ArrayList<>();

        int total = 0;
        for(Gene gene: genes){
            total += gene.score;
        }

        for(int i=0; i<genes.size(); i++){
            double roulette_cf = rnd.nextInt(10000) / 10000.0 * total;
            int roulette_num = 0;
            double roulette_value = 0;
            for(int j =0; j<genes.size(); j++){
                roulette_value += genes.get(j).score;
                if(roulette_value > roulette_cf){
                    roulette_num = j;
                    break;
                }
            }
            parent.add(genes.get(roulette_num));
        }

        return parent;
    }

    List<Gene> select() throws CloneNotSupportedException {
        elite = getElite();
        List<Gene> parent = new ArrayList<>();
        parent = roulette_select();
        parent.set(parent.size()-1,elite);
        return parent;
    }

    void crossover(Gene g1, Gene g2){
        Random rnd = new Random();
        int start_bit = rnd.nextInt(100);
        int[] tmp1 = Arrays.copyOfRange(g2.chromosome,start_bit,100);
        int[] tmp2 = Arrays.copyOfRange(g1.chromosome,0,start_bit);
        for(int i=0; i<100-start_bit; i++){
            g1.chromosome[start_bit+i] = tmp1[i];
        }
        for(int i=0; i<start_bit; i++){
            g2.chromosome[i] = tmp2[i];
        }
    }

    void evaluate(){
        for(Gene gene : genes){
            gene.evaluete();
        }
    }

    void sort(){
        Gene tmp;
        Collections.sort(genes);
        /*
        for(Gene gene1 : genes){
            for(Gene gene2 : genes){
                if(gene1 == gene2){
                    continue;
                }

                System.out.println();
                show();

                if(Gene.compare(gene1,gene2) < 0){
                    System.out.println("gene2:"+Arrays.toString(gene2.chromosome));
                    tmp = gene1;
                    gene1 = gene2;
                    gene2 = tmp;

                    System.out.println("tmp:" + Arrays.toString(tmp.chromosome));
                    System.out.println("gene2:"+Arrays.toString(gene2.chromosome));

                }

                System.out.println("sorted1");
                show();
            }
        }*/
    }

    void show(){
        for(Gene gene : genes) {
            System.out.println(Arrays.toString(gene.chromosome) + " : " + gene.score);
        }
    }

    void show_onTop(){
        System.out.println(Arrays.toString(genes.get(0).chromosome) + " : " + genes.get(0).score);
    }

}

public class Main {

    // Geneクラスのコンストラクタって、Gene[] = new Gene[N]みたいに配列として初期化したときって呼ばれるの？
    public static void main(String[] args) throws IOException, CloneNotSupportedException {
	// write your code here

        GA ga = new GA(50,100);
        ga.show();

        ga.sort();

        System.out.println("sorted");

        ga.show();
/*
        Gene elite = ga.getElite();
        System.out.println("elite : " + Arrays.toString(elite.chromosome));
        System.out.println("gene[0] : " + Arrays.toString(ga.genes.get(0).chromosome));

        ga.genes.get(0).chromosome[0] += 1;

        System.out.println("elite : " + Arrays.toString(elite.chromosome));
        System.out.println("gene[0] : " + Arrays.toString(ga.genes.get(0).chromosome));

        System.in.read();
*/
        while(ga.genes.get(0).score < 100) {
            List<Gene> parent = ga.select();

            Random rnd1 = new Random();
            Random rnd2 = new Random();
            for (int i = 0; i < 25; i++) {
                Gene pair1 = ga.genes.get(rnd1.nextInt(50));
                Gene pair2 = ga.genes.get(rnd2.nextInt(50));
                ga.crossover(pair1, pair2);
            }

            for(Gene gene : ga.genes){
               gene.mutate();
            }

            ga.genes.set(ga.genes.size()-1,ga.elite);

         //   System.out.println("crossover\n");
            ga.evaluate();
            ga.sort();
        //    ga.show();

            ga.show_onTop();
        }

//        Gene g = new Gene(10);
//        System.out.println(Arrays.toString(g.chromosome));

//        Test test = new Test(10);

//        while(test.valid_mutation() != true){
//
//            System.out.println(test.valid_mutation());

//            System.in.read();
//        }

    }
}
