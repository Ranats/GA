class Bag
  attr_accessor :capacity, :weights, :values

  def initialize(item_amount:100)
    @weights = Array.new(item_amount){ rand(100) }
    @values = Array.new(item_amount){ rand(100) }
    @capacity = 0.5 * @weights.inject(:+)
  end
end

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

class RealCodedGA
  def initialize(population_size: 50, finish_population: 50, gene_size:100)
    @population_size = 3 * gene_size #population_size
    @finish_population = finish_population

    @m = gene_size
    @objective = 2

    # 初期個体生成
    @population = Array.new(@population_size) { Gene.new(gene_size, @objective) }

    # 問題設定？ 2=>目的数
    @bags = Array.new(@objective){ Bag.new(item_amount:gene_size) }

    # 評価
    @mean_fitness = Array.new(@objective){0}
    @population.each do |pop|
      pop.calc_fitness(bags:@bags)
      @mean_fitness.map!.with_index do |m, i|
        m += pop.fitness[i]
      end
    end
    @mean_fitness.map!{|m| m /= population_size}

    fast_nondominated_sort(@population)
  end

  def run(generation)
    elite = @population.select{|pop| pop.rank == 0}

    # 親個体選択
    parents = select_parents(@population)
    # 交叉
    offsprings = crossover(parents)

    # 世代交代
    evolve_dominated(parents,offsprings,elite)

    # 適合度計算
    @mean_fitness.map!{|m| m=0}
    @population.each_with_index do |pop,i|
      pop.calc_fitness(bags:@bags)
      @mean_fitness.map!.with_index do |m, i|
        m += pop.fitness[i]
      end
    end
    @mean_fitness.map!{|m| m /= @population_size}

    # 世代の表示
#   show_all(generation)
    show_overview(generation)
  end


  # ランダムに選択 (集団から非復元抽出)
  def select_parents(population)
    population.shuffle!
    return population.slice!(0,@m+1)
  end

  def crossover(parents)
    child = Array.new
    (2 * @m).times do
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
    gene = Gene.new(@m, @objective)
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

  
  def evolve_dominated(parent,child, elite)
    parent.shuffle!
    child += parent.slice!(0,2)
    child.each_with_index do |pop,i|
      pop.calc_fitness(bags:@bags)
    end
    fast_nondominated_sort(child)

    elite = child.select{|pop| pop.rank == 0}#.shuffle.slice(0,@m/10)

    i = 0
    population_p = []
    ranked_population = child.select { |pop| pop.rank == i }
    # while population_p.size + ranked_population.size < @limit_size
    while population_p.size < child.size
      population_p += ranked_population
      i += 1
      ranked_population = child.select { |pop| pop.rank == i }
    end

    # -> reference line による選択

    @population += population_p.slice(0,@m+1 - elite.size)
    @population += elite
  end

  def fast_nondominated_sort(arg_population)
    population = arg_population

    count = 0
    population.each do |pop|
      pop.ruled = population.reject { |item| item == pop }.count do |other|
        other.fitness.map.with_index do |other_f, idx|
          (other_f <=> pop.fitness[idx]) == 1
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

  def show_all(generation)
    print "\n\n---------- Population [generation:#{generation}] ----------\n"
    print "     i\t  f1\t  f2\n\n"

    @population.each_with_index do |pop, idx|
      printf("%5d\t|", idx)

      printf("%10.4f|",pop.fitness[0])
      printf("%10.4f|",pop.fitness[1])
      puts
    end

    @mean_fitness.each_with_index do |f,idx|
      printf("\n[mean_fitness#{idx}]\n%10.4f\n",f)
    end
  end

  def show_overview(generation)
    if generation == 0
      @population.each do |pop|
        printf("\n%10.4f|",pop.fitness[0])
        printf("%10.4f|",pop.fitness[1])
      end
      print "\n---------- Population [generation:#{generation}] ----------\n"
      print " f1_max\t  f2_max\t  f1_mean\t  f2_mean"
    end
    printf("\n%10.4f|",@population.max_by{|pop| pop.fitness[0]}.fitness[0])
    printf("%10.4f|",@population.max_by{|pop| pop.fitness[1]}.fitness[1])
    @mean_fitness.each do |f|
      printf("%10.4f|",f)
    end

    if generation == 99
      puts "=================================="
      @population.each do |pop|
        printf("\n%10.4f|",pop.fitness[0])
        printf("%10.4f|",pop.fitness[1])
      end
    end
  end

end