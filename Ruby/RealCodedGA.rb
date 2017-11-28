# https://github.com/shikao-yuzu/RealCodedGA/blob/master/real-coded_GA.rb

class Gene

  attr_accessor :chromosome, :fitness, :rank, :ruled, :ruling

  def initialize(length, num_objective)
    @chromosome = []

    length.times do
      # 0以上1未満の実数
      @chromosome << rand
    end

    @fitness = Array.new(num_objective){0}

    @penalty_weight = 5

    @rank = 0
    @ruled = 0
    @ruling = 0
  end

  def calc_fitness(bags:Bags.new)

    weight_sum = Array.new(bags.size){0}
    value_sum = Array.new(bags.size){0}

    @chromosome.each_with_index do |e, i|
      bags.each_with_index do |bag, idx|
        weight_sum[idx] += bag.weights[i] * e
        value_sum[idx] += bag.values[i] * e
      end
    end


    bags.size.times do |i|
      @fitness[i] = value_sum[i]
      if weight_sum[i] > bags[i].capacity
        # ペナルティを各個体が持つのは変な気がする
        alpha = @penalty_weight * (weight_sum[i] - bags[i].capacity)
        @fitness[i] -= alpha
      end
    end

  end

end

class Bag
  attr_accessor :capacity, :weights, :values

  def initialize(item_amount:100)

    # 重さと価値はそれぞれ0～100の整数
    @weights = Array.new(item_amount){ rand(100) }
    @values = Array.new(item_amount){ rand(100) }

    # ナップサックの容量 : 0.5 x Σw_ij
    @capacity = 0.5 * @weights.inject(:+)
  end

end

class RealCodedGA
  # 初期個体生成
  # 各個体の評価
  # 遺伝的演算
  #   親個体選択
  #   交叉
  #   突然変異
  # 次世代の個体選択
  # 終了判定

  def initialize(population_size: 50, finish_population: 50, gene_size:100)
    @population_size = 150# * gene_size #population_size
    @finish_population = finish_population

    @m = gene_size

    @o = 2

    # 初期個体生成
    @population = Array.new(@population_size) { Gene.new(gene_size,@o) }

    # 問題設定？ 2=>目的数
    @bags = Array.new(@o){ Bag.new(item_amount:gene_size) }

    @mean_fitness = Array.new(@o){0}
    # 評価
    @population.each do |pop|
      pop.calc_fitness(bags:@bags)
      @mean_fitness.each_with_index do |m, i|
        m += pop.fitness[i]
      end
    end

    @mean_fitness.map!{|m| m /= population_size}

    fast_nondominated_sort(@population)

  end

  def run(generation)

    # NSGA-III
    # N=50
    # Q_t : ランダムに選択し，交叉を行った子個体
    # P_t : 親個体
    # R_t : Q_t∪P_t
    # →　R_tに対して，非優越ソート，reference lineを用いた選択により次世代の個体を選択する

    offsprings = []

#    while offsprings.size < @population_size
      # 親個体選択
      parents = select_parents(@population)

      # 交叉
#    offsprings << crossover(parents)
    offsprings = crossover(parents)
#    end

    # 世代交代
    evolve_dominated(parents,offsprings)

    puts "evolve-----"
    @population.each do |pop|
      p pop
    end

    # 適合度計算
    @population.each_with_index do |pop,i|
      pop.calc_fitness(bags:@bags)
    end

    # 世代の表示
    show(generation)

    return

  end


  # ランダムに選択
  def select_parents(population)
    population.shuffle!
    return population.slice!(0,@m+1)
  end

  def crossover(parents)
    child = Array.new
#    child = Gene.new(@m,@o)
    (10 * @m).times do
      child << simplex_crossover(parents)
    end
    return child
  end

  # シンプレックス交叉
  def simplex_crossover(parent)

    # 重心の計算
    g = Array.new(@m, 0.0)
    for i in 0..@m-1
      for k in 0..@m
        g[i] += parent[k].chromosome[i]
      end
    end
    g.map! { |x| x / (@m+1) }

    z = Array.new(@m+1) { Array.new(@m, 0.0) }
    for i in 0..@m-1
      for k in 0..@m
        z[k][i] = g[i] + Math.sqrt(@m + 2) * (parent[k].chromosome[i] - g[i])
      end
    end

    c = Array.new(@m+1) { Array.new(@m, 0.0) }
    for i in 0..@m-1
      for k in 1..@m
        c[k][i] = (z[k-1][i] - z[k][i] + c[k-1][i]) * rand(0.0..1.0) ** (1.0 / (1.0 + (k-1).to_f))
      end
    end

#    gene = Array.new(@m, 0.0)
    gene = Gene.new(@m,@o)
    for i in 0..@m-1
      gene.chromosome[i] = z[@m][i] + c[@m][i]
    end

    return gene
  end



  # 世代交代を行う - (MGG)
  def evolve(eval, parent, child)
    # 「世代交代の候補」 = 「子個体」 + 「親個体からランダムに2個」
    parent.shuffle!
    candidate  = parent.slice!(0, 2)
    candidate += child

    # 世代交代の候補からエリートを選択する
    #   > 優越関係により選択
    elite = select_elite(eval, candidate)

    # 世代交代の候補(エリートは除く)からルーレット選択を行う
    #   > 優越関係，reference lineによる選択
    roulette = select_roulette(eval, candidate)

    # 親個体に戻す
    parent << elite
    parent << roulette

    # 次世代の集団を生成する
    @population.push(parent)
  end



  def evolve_dominated(parent,child)

    parent.shuffle!
    candidate = parent.slice!(0,2)
    candidate += child

    fast_nondominated_sort(candidate)

    i = 0
    population_p = []
    ranked_population = candidate.select { |pop| pop.rank == i }

    while population_p.size + ranked_population.size < candidate.size
      population_p += ranked_population
      i += 1
      ranked_population = candidate.select { |pop| pop.rank == i }
    end

#    @population.push(population_p)
    @population += population_p

    # 非優越ソートによる選択
    #  →　reference lineによる選択

    # 上位N個体をP_t+1とする．
    # Step 4 新たなアーカイブ母集団 Pt+1 = φ を生成．変数 i = 1 とする．
    # |Pt+1| + |Fi| > N を満たすまで，Pt+1 = Pt+1 ∪ Fi と i = i + 1 を実行．
#    i = 0
#    population_p = []
#    ranked_population = @population.select { |pop| pop.rank == i }
#
#    while population_p.size + ranked_population.size < @population.size
#      population_p += ranked_population
#      i += 1
#      ranked_population = @population.select { |pop| pop.rank == i }
#    end
#
#    candidate += population_p
#
#    @population.push()



  end


  def cxSimulatedBinary(ind1, ind2, eta)
    # param eta: Crowding degree of the crossover. A high eta will produce
    #            children resembling to their parents, while a small eta will
    #            produce solutions much more different.

  end

  def cxSimulatedBinaryBounded(ind1, ind2, eta, low, up)


  end

  def fast_nondominated_sort(arg_population)

    population = arg_population

    count = 0
    population.each do |pop|

      # (other_f <=> pop.f) ==  1 ... すべて他より大きい → 右上がランク0
      # (other_f <=> pop.f) == -1 ... すべて他より小さい → 左下がランク0
      # ruled→より優れている個体数(支配されている個体数)
      pop.ruled = population.reject { |item| item == pop }.count do |other|
        other.fitness.map.with_index do |other_f, idx|
          (other_f <=> pop.fitness[idx]) == 1 # fx^1[0]<=>fx^2[0] , fx^1[1]<=>fx^2[1] , ...
        end.all?
      end

      count += 1
    end

    rank = 0

    begin
      ranked_pop = population.select { |pop| pop.ruled==0 }

      ranked_pop.each do |pop|
        ruling_population =
            population.reject { |item| item == pop }.select { |other|
              other.fitness.map.with_index do |other_f, idx|
                (other_f <=> pop.fitness[idx]) == -1#(@min_or_max * -1)
              end.all?
            }

        ruling_population.each { |rpop| rpop.ruled -= 1 }
        pop.rank = rank
        pop.ruled -= 1
      end
      rank += 1
    end while ranked_pop.size > 0


  end

  def show(generation)

    puts "population"
    p @population

    print "\n\n---------- Population [generation:#{generation}] ----------\n"
    print "     i\t  f1\t  f2\tgene\n\n"

    @population.each_with_index do |pop, idx|
      printf("%5d\t|", idx)

      printf("%10.4f|",pop.fitness[0])
      printf("%10.4f|",pop.fitness[1])

      pop.chromosome.each do |g|
#        printf("%10.4f",g)
      end
      puts
    end

    @mean_fitness.each_with_index do |f,idx|
      printf("\n[mean_fitness#{idx}]\n%10.4f\n",f)
    end

  end

end