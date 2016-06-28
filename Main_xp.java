import java.util.Random;

class Gene {
    int[] gene; //=> [0,0,1,1,0,1,....] 遺伝子
    int score;

    Gene(int size){
        //  newした時の初期化用
        gene = new int[size];

        //  乱数で0,1に各遺伝子座を初期化
        Random rnd = new Random();
        for(int i = 0; i<size; i++){
            gene[i] = rnd.nextInt(2);
        }

        //  初期集団の評価
        evaluate();
    }

    public void evaluate(){
        //  各個体の適応度を評価
        //  score = ...

        score = 0;
        for(int i=0;i<gene.length;i++) {
            score += gene[i];
        }
    }
}

class GA {
    //  個体集合genes
    Gene[] genes;
    int generation;

    //  個体数と遺伝子長を受け取って個体数分だけループで遺伝子を生成
    GA(int population, int size){
        //  初期化用の関数
        genes = new Gene[population];

        for(int i=0; i<population; i++){
            genes[i] = new Gene(size);
        }
    }

    Gene[] select(){
        //  選択
//        Gene elite = genes[0];
        Gene[] parent = new Gene[genes.length];
        for(int i=0; i < genes.length; i++){
            parent[i] = roulette_select();
        }

        return parent;
    }

    Gene getElite(){
        //  エリート解(並び替えたあとの最初の個体)を取得
        return genes[0];
    }

    Gene roulette_select(){
        //  ルーレット選択
        int total = 0;
        int roulette_value = 0;

        for(int i = 0; i < genes.length; i++){
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

    void crossover(Gene g1, Gene g2){
        //  交叉
        int value;
        Random rn = new Random();
        value = rn.nextInt(g1.gene.length);

    }

    void mutation(){
        //  突然変異

    }

    void sort(){
        //  並び替えるメソッド
        //  1.  すべての個体を評価
        //  2.  評価値をもとに並び替える
        for(int i = 1; i < genes.length; i++) {
            for (int j = 0; j < genes.length - i; j++) {
                if (genes[j].score < genes[j + 1].score) {
                    Gene temp = genes[j];
                    genes[j] = genes[j + 1];
                    genes[j + 1] = temp;
                }
            }
        }
    }

}

public class Main {

    public static void main(String[] args) {
        //  個体数10，遺伝子長100の個体群を生成
        GA ga = new GA(10,100);
        ga.sort();
        for(int i = 0; i < 10; i++)
        {
            System.out.println(ga.genes[i].score);
        }

        Gene[] parent = ga.select();
        for(int i = 0; i < 10; i++)
        {
            System.out.println(parent);
        }

    }
}
